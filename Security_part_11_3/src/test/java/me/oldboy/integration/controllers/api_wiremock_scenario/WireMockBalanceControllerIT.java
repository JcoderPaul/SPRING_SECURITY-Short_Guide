package me.oldboy.integration.controllers.api_wiremock_scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.transaction_dto.TransactionReadDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.jwt_test_utils.JwtTestUtils;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL_WITH_READ_AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
/*
В нашем случае тесты проходя поодиночке или даже в комплексе под одним классом при параллельном
запуске всех разом приводят к падению части из них. Spring Test кэширует контекст, что может
привести к проблемам. Добавим аннотацию @DirtiesContext на уровне классов, чтобы принудительно
пересоздавать контекст - это очень сильно замедлит выполнение тестов, но обеспечит изоляцию и
прохождения всех тестов "разом", при одновременном запуске.
*/
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WireMockBalanceControllerIT extends TestContainerInit {

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    private WireMockServer wireMockServer;
    private RsaJsonWebKey rsaJsonWebKey;
    private Map<String, Object> realmAccessClaimsAdmin;
    private Map<String, Object> realmAccessClaimsUser;

    @BeforeEach
    void setUp() throws JoseException {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        /* Инициализация WireMock сервера */
        wireMockServer = new WireMockServer(8089); // Если установить 0, то порт будет генерироваться случайный
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());

        /* Генерация RSA ключа для JWT */
        if (rsaJsonWebKey == null) {
            rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
            rsaJsonWebKey.setKeyId("k1");
            rsaJsonWebKey.setAlgorithm(AlgorithmIdentifiers.RSA_USING_SHA256);
            rsaJsonWebKey.setUse("sig");
        }

        /* Настройка WireMock заглушки JWKS endpoint-a */
        stubFor(WireMock.get(urlEqualTo("/auth/realms/test-realm/protocol/openid-connect/certs"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(new JsonWebKeySet(rsaJsonWebKey).toJson())));

        String openidConfig = "{ " +
                "\"issuer\": \"http://localhost:" + wireMockServer.port() + "/auth/realms/test-realm\", " +
                "\"jwks_uri\": \"http://localhost:" + wireMockServer.port() + "/auth/realms/test-realm/protocol/openid-connect/certs\" }";

        /* Настройка WireMock заглушки для .well-known/openid-configuration */
        stubFor(WireMock.get(urlEqualTo("/auth/realms/test-realm/.well-known/openid-configuration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(openidConfig)));

        realmAccessClaimsAdmin = new HashMap<>();
        realmAccessClaimsAdmin.put("roles", List.of("ROLE_READ", "ROLE_ADMIN"));

        realmAccessClaimsUser = new HashMap<>();
        realmAccessClaimsUser.put("roles", List.of("ROLE_USER"));
    }

    @AfterEach
    public void tearDown() {
        if (wireMockServer != null) {
            wireMockServer.resetAll();
            wireMockServer.stop();
            wireMockServer = null;
        }
    }

    @Test
    @SneakyThrows
    void getBalanceDetails_ShouldReturnOk_AndDtoList_Test() {
        /* Генерация JWT токена */
        String jwt = JwtTestUtils.generateJWT(EXIST_EMAIL, rsaJsonWebKey, wireMockServer, realmAccessClaimsAdmin);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/api/myBalance")
                        .header("Authorization", "Bearer " + jwt)) // У клиента с EXIST_EMAIL есть записи в БД
                .andExpect(status().isOk())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();
        List<TransactionReadDto> listFromResponse = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(strResult, new TypeReference<List<TransactionReadDto>>() {});

        assertThat(listFromResponse.size()).isGreaterThan(1);
    }

    @Test
    @SneakyThrows
    void getBalanceDetails_ShouldReturn_2xx_NoContent_ClientHasNoTransaction_Test() {
        /* Генерация JWT токена */
        String jwt = JwtTestUtils.generateJWT(EXIST_EMAIL_WITH_READ_AUTH, rsaJsonWebKey, wireMockServer, realmAccessClaimsUser);

        MvcResult result = mockMvc.perform(get("/api/myBalance")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

        String strResult = result.getResponse().getContentAsString();

        assertThat(strResult.isEmpty()).isTrue();
    }

    @Test
    @SneakyThrows
    void getBalanceDetails_ShouldReturnUnauthorized_Test() {
        String jwt = "Bad-token";

        mockMvc.perform(get("/api/myBalance")
                .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void getBalanceDetails_ShouldReturn_4xx_EmptyToken_Test() {
        mockMvc.perform(get("/api/myBalance"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }
}