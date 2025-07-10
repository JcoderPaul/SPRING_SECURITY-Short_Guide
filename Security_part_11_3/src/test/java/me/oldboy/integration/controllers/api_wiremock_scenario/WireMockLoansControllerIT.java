package me.oldboy.integration.controllers.api_wiremock_scenario;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import lombok.SneakyThrows;
import me.oldboy.config.security_details.ClientDetailsService;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.loan_dto.LoanCreateDto;
import me.oldboy.dto.loan_dto.LoanReadDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.jwt_test_utils.JwtTestUtils;
import me.oldboy.services.LoanService;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL_WITH_READ_AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class WireMockLoansControllerIT extends TestContainerInit {

    @Autowired
    private LoanService loanService;
    @Autowired
    private ClientDetailsService clientDetailsService;

    @Autowired
    private WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;
    private WireMockServer wireMockServer;
    private RsaJsonWebKey rsaJsonWebKey;
    private Map<String, Object> realmAccessClaimsAdmin;
    private Map<String, Object> realmAccessClaimsUser;

    private Long testId, anotherId;
    private LoanCreateDto testLoanCreateDtoForOwner, testLoanCreateDtoToAnotherClient;
    private List<LoanCreateDto> testList;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws JoseException {
        testId = 1L;
        anotherId = 3L;

        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        testLoanCreateDtoForOwner = LoanCreateDto.builder()
                .clientId(testId)
                .startDate(LocalDate.of(2021, 04,05))
                .loanType("Plane")
                .totalLoan(350000)
                .amountPaid(65000)
                .outstandingAmount(210000)
                .createDate(LocalDate.of(2021, 03,05))
                .build();
        testLoanCreateDtoToAnotherClient = LoanCreateDto.builder()
                .clientId(anotherId)
                .startDate(LocalDate.of(2022, 11,12))
                .loanType("Castle")
                .totalLoan(1350000)
                .amountPaid(115000)
                .outstandingAmount(600000)
                .createDate(LocalDate.of(2022, 10,03))
                .build();

        testList = List.of(testLoanCreateDtoForOwner, testLoanCreateDtoToAnotherClient);

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
    void getLoanDetails_ShouldReturnDtoList_ForClientWithLoans_Test() {
        String jwt = JwtTestUtils.generateJWT(EXIST_EMAIL, rsaJsonWebKey, wireMockServer, realmAccessClaimsAdmin);

        MvcResult result = mockMvc.perform(get("/api/myLoans")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn();

        String strRes = result.getResponse().getContentAsString();
        List<LoanReadDto> loansListFromBase = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .readValue(strRes, new TypeReference<List<LoanReadDto>>() {});

        assertThat(loansListFromBase.size()).isGreaterThan(0);
    }

    @Test
    @SneakyThrows
    void getLoanDetails_ShouldReturnEmptyBody_IfClientHasNoLoans_Test() {
        String jwt = JwtTestUtils.generateJWT(EXIST_EMAIL_WITH_READ_AUTH, rsaJsonWebKey, wireMockServer, realmAccessClaimsUser);

        mockMvc.perform(get("/api/myLoans")
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void getLoanDetails_ShouldReturnNotAuth_4xx_WithoutAuth_Test() {
        mockMvc.perform(get("/api/myLoans"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void createLoan_ShouldReturnOk_AndRecordIdFromBase_Test() {
        String jwt = JwtTestUtils.generateJWT(EXIST_EMAIL, rsaJsonWebKey, wireMockServer, realmAccessClaimsAdmin);
        /* Подготовим данные для сохранения в БД*/
        String strLoanCreateDto = objectMapper.writeValueAsString(testLoanCreateDtoForOwner);

        /* Делаем запрос на сохранение */
        MvcResult result = mockMvc.perform(post("/api/createLoan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strLoanCreateDto)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn();

        /* "Парсим" ответ */
        String strRes = result.getResponse().getContentAsString();
        Long afterCreateLoanId = objectMapper.readValue(strRes, Long.class);

        /* Сравниваем ожидание с результатом - ID последней записи будет больше последнего известного из БД - 8 */
        assertThat(afterCreateLoanId).isGreaterThan(8);
    }

    /* Id аутентифицированного клиента и Id того на кого оформлен кредит не совпадает - сохранить нельзя */

    @Test
    @SneakyThrows
    void createLoan_ShouldReturnBadRequest_TryToSaveNotYoursLoan_Test() {
        String jwt = JwtTestUtils.generateJWT(EXIST_EMAIL, rsaJsonWebKey, wireMockServer, realmAccessClaimsAdmin);
        /* Подготовим данные для сохранения в БД*/
        String strLoanCreateDto = objectMapper.writeValueAsString(testLoanCreateDtoToAnotherClient);

        /* Делаем запрос на сохранение */
        mockMvc.perform(post("/api/createLoan")
                        .header("Authorization", "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strLoanCreateDto))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    @SneakyThrows
    void saveAllMyRequestLoan_ShouldReturnOk_AndSaveOnlyAuthOwnerLoans_Test() {
        String jwt = JwtTestUtils.generateJWT(EXIST_EMAIL, rsaJsonWebKey, wireMockServer, realmAccessClaimsAdmin);
        /* Получим количество записей в БД */
        List<LoanReadDto> loansList = loanService.findAll();
        Integer listSizeBefore = loansList.size();

        /* Подготовим данные для сохранения в БД */
        String strList = objectMapper.writeValueAsString(testList);

        /* Делаем запрос на сохранение */
        mockMvc.perform(post("/api/save-all-loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strList)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().string("Saved all loans!"));

        /* Проверяем количество записей в БД после сохранения списка кредитов - больше на одну */
        Integer listSizeAfter = loanService.findAll().size();
        assertThat(listSizeAfter).isEqualTo(listSizeBefore + 1);
    }

    @Test
    @SneakyThrows
    void saveAllMyRequestLoan_ShouldReturnOk_ButOperationFailed_Test() {
        String jwt = JwtTestUtils.generateJWT("user3@test.com", rsaJsonWebKey, wireMockServer, realmAccessClaimsUser);
        /* Получим количество записей в БД */
        List<LoanReadDto> loansList = loanService.findAll();
        Integer listSizeBefore = loansList.size();

        /* Подготовим данные для сохранения в БД */
        String strList = objectMapper.writeValueAsString(testList);

        /* Делаем запрос на сохранение */
        mockMvc.perform(post("/api/save-all-loans")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(strList)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andExpect(content().string("Operation is failed!"));

        /* Проверяем количество записей в БД после сохранения списка кредитов - неизменен */
        Integer listSizeAfter = loanService.findAll().size();
        assertThat(listSizeAfter).isEqualTo(listSizeBefore);
    }

    @Test
    @SneakyThrows
    void getAllLoanByType_ShouldReturnOkForRoleAdmin_AndListOfLoansByType_Test() {
        String jwt = JwtTestUtils.generateJWT(EXIST_EMAIL, rsaJsonWebKey, wireMockServer, realmAccessClaimsAdmin);
        String loanType = "Home";

        MvcResult result = mockMvc.perform(get("/api/get-all-loans-by-type/" + loanType)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isOk())
                .andReturn();

        String strRes = result.getResponse().getContentAsString();

        List<LoanReadDto> respList = objectMapper.readValue(strRes, new TypeReference<List<LoanReadDto>>() {});

        assertThat(respList.size()).isGreaterThan(1);
    }

    @Test
    @SneakyThrows
    void getAllLoanByType_ShouldReturnForbidden_NotAdminAuth_Test() {
        String jwt = JwtTestUtils.generateJWT("user3@test.com", rsaJsonWebKey, wireMockServer, realmAccessClaimsUser);
        String loanType = "Home";

        mockMvc.perform(get("/api/get-all-loans-by-type/" + loanType)
                        .header("Authorization", "Bearer " + jwt))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("{\"exceptionMsg\":\"Access Denied\"}"));
    }
}