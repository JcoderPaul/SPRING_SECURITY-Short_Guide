package me.oldboy.unit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.controllers.ClientController;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import me.oldboy.test_content.TestConstantFields;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = TestConstantFields.EXIST_EMAIL,
              password = TestConstantFields.TEST_PASS,
              authorities = {TestConstantFields.TEST_STR_ROLE_ADMIN})
class ClientControllerWebMockTest {

    /* Для тестирования метода getAdminName() */
    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private SecurityClientDetails mockClientDetails;
    @InjectMocks
    private ClientController clientController;

    @MockBean
    private ClientService mockClientService;
    @Autowired
    private MockMvc mockMvc;

    private List<ClientReadDto> testDtoList;
    private DetailsCreateDto testDetailsCreateDto;
    private ClientCreateDto testClientCreateDto, notValidCreateClientDto;
    private ClientReadDto testClientReadDto;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setStaticContent(){
        objectMapper = new ObjectMapper();
    }

    @BeforeEach
    void setUp(){
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
        when(mockClientDetails.getUsername()).thenReturn(TestConstantFields.EXIST_EMAIL);
        when(mockAuthentication.getPrincipal()).thenReturn(mockClientDetails);
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);

        String expected = "This page for ADMIN only! \nHello: " + TestConstantFields.EXIST_EMAIL;

        mockMvc.perform(get("/admin/helloAdmin"))
                .andExpect(status().isOk())
                .andExpect(content().string(expected));
    }

    @Test
    @SneakyThrows
    void shouldReturnOkAndTestedListGetAllClientTest(){
        when(mockClientService.findAll()).thenReturn(testDtoList);
        /* Выводить полный список смысла нет, возьмем только наглядную часть */
        mockMvc.perform(get("/admin/getAllClient"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string(containsString("{\"email\":\"for_test@test.com\",\"role\":\"USER\"," +
                                "\"clientName\":\"Testino\",\"clientSurName\":\"Testorelly\"," +
                                "\"age\":32}")));
    }

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

        when(mockClientService.findByEmail(testClientCreateDto.email())).thenReturn(Optional.empty());
        when(mockClientService.saveClient(testClientCreateDto)).thenReturn(testClientReadDto);

        mockMvc.perform(post("/admin/regClient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDto))
                .andExpect(status().isOk())
                .andExpect(content().string(readDto));

        verify(mockClientService, times(1)).findByEmail(anyString());
        verify(mockClientService, times(1)).saveClient(any(ClientCreateDto.class));
    }
}
