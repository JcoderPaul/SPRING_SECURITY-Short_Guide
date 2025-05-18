package me.oldboy.unit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.controllers.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.exception.handlers.DtoValidationHandler;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import me.oldboy.test_config.TestConstantFields;
import me.oldboy.test_config.TestWebInitializer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestWebInitializer.class})
@WebAppConfiguration
class ClientControllerWebMockTest {

    /* Для тестирования метода getAdminName() */
    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private SecurityClientDetails mockClientDetails;

    /* Для тестирования методов getAllClient() и  registrationClient()*/
    @MockitoBean
    private ClientService clientService;
    @InjectMocks
    private ClientController clientController;

    @Autowired
    private WebApplicationContext webAppContext;
    @Autowired
    private DtoValidationHandler dtoValidationHandler;

    private MockMvc mockMvc;

    private List<ClientReadDto> testDtoList;
    private DetailsCreateDto testDetailsCreateDto;
    private ClientCreateDto testClientCreateDto, notValidCreateClientDto;
    private ClientReadDto testClientReadDto;
    private static ObjectMapper objectMapper;

    ClientControllerWebMockTest() {
    }

    @BeforeAll
    static void setStaticContent(){
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(clientController)
                                 .setControllerAdvice(dtoValidationHandler)
                                 .build();

        testClientReadDto = new ClientReadDto(TestConstantFields.TEST_EMAIL,
                                              Role.USER.name(),
                                              TestConstantFields.TEST_CLIENT_NAME,
                                              TestConstantFields.TEST_CLIENT_SUR_NAME,
                                              TestConstantFields.TEST_AGE);

        testDetailsCreateDto = new DetailsCreateDto(TestConstantFields.TEST_CLIENT_NAME,
                                                    TestConstantFields.TEST_CLIENT_SUR_NAME,
                                                    TestConstantFields.TEST_AGE);

        testClientCreateDto = new ClientCreateDto(TestConstantFields.TEST_EMAIL,
                                                  TestConstantFields.TEST_PASS,
                                                  testDetailsCreateDto);

        notValidCreateClientDto = new ClientCreateDto("sd","1", testDetailsCreateDto);

        testDtoList = new ArrayList<>(List.of(testClientReadDto,
                                              new ClientReadDto(),
                                              new ClientReadDto()));
    }


    @Test
    @SneakyThrows
    void shouldReturnWelcomeStringAdminWithAuthUserNameTest() {
        when(mockClientDetails.getClientName()).thenReturn(TestConstantFields.TEST_CLIENT_NAME);
        when(mockAuthentication.getPrincipal()).thenReturn(mockClientDetails);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);

        SecurityContextHolder.setContext(mockSecurityContext); // Задаем SecurityContext mock

        String expected = "This page for ADMIN only! \nHello: " + TestConstantFields.TEST_CLIENT_NAME;

        mockMvc.perform(get("/admin/helloAdmin"))
               .andExpect(MockMvcResultMatchers.status().isOk())
               .andExpect(MockMvcResultMatchers.content().string(expected));
    }

    @Test
    @SneakyThrows
    void shouldReturnOkAndTestedListGetAllClientTest(){
        when(clientService.findAll()).thenReturn(testDtoList);
        /* Выводить полный список смысла нет, возьмем только наглядную часть */
        mockMvc.perform(get("/admin/getAllClient"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content()
                .string(containsString("{\"email\":\"for_test@test.com\",\"role\":\"USER\"," +
                                                "\"clientName\":\"Testino\",\"clientSurName\":\"Testorelly\"," +
                                                "\"age\":32}")));
    }

    /*
    При тестировании метода *.registrationClient() нам нужно покрыть минимум 3-и сценария:
    - успешная регистрация;
    - дублирование e-mail при регистрации;
    - не валидный CreateClientDto;

    Первые два покрыты в ClientControllerTest.java

    А вот тут происходит еще более интересная кулинария! Теоретически и на первый взгляд все выглядит
    пристойно. Но по факту, если пытаться тестировать этот код без нужных зависимостей или "костылей",
    то валидация работать не будет. Мы получим полное прохождение теста без проверки входящих данных,
    а в логах интересное сообщение:

    "INFO [org.springframework.validation.beanvalidation.OptionalValidatorFactoryBean: 48]
    Failed to set up a Bean Validation provider: jakarta.validation.ValidationException:
    HV000183: Unable to initialize 'jakarta.el.ExpressionFactory'.

    Check that you have the EL dependencies on the classpath, or use ParameterMessageInterpolator instead"

    По этому нам понадобилась зависимость:

        implementation "org.glassfish:jakarta.el:${versions.glassfish}"

    После чего тесты стали отрабатывать все как и полагается. Еще раз, логику мы проверили ранее, защиту тоже,
    тут мы проверили работу валидации "условно изолированно" от остальной части когда.
    */

    @Test
    void shouldReturn_ValidationErrors_RegistrationClientTest() throws Exception {
        String notValidDto = objectMapper.writeValueAsString(notValidCreateClientDto);

        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(notValidDto))
                        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn_ValidationOk_RegistrationClientTest() throws Exception {
        String validDto = objectMapper.writeValueAsString(testClientCreateDto);
        String readDto = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(testClientReadDto);

        when(clientService.findByEmail(testClientCreateDto.email())).thenReturn(Optional.empty());
        when(clientService.saveClient(testClientCreateDto)).thenReturn(testClientReadDto);

        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDto))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string(readDto));

        verify(clientService, times(1)).findByEmail(anyString());
        verify(clientService, times(1)).saveClient(any(ClientCreateDto.class));
    }
}