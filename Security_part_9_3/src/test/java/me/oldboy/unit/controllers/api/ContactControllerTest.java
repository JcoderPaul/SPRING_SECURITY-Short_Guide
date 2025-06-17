package me.oldboy.unit.controllers.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import me.oldboy.config.security_details.SecurityClientDetails;
import me.oldboy.controllers.api.ContactController;
import me.oldboy.controllers.util.UserDetailsDetector;
import me.oldboy.dto.contact_dto.ContactReadDto;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.services.ClientService;
import me.oldboy.services.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {ContactController.class})
class ContactControllerTest {

    @MockitoBean
    private ClientService clientService;
    @MockitoBean
    private ContactService contactService;
    @MockitoBean
    private UserDetailsDetector userDetailsDetector;

    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private SecurityClientDetails mockClientDetails;

    @InjectMocks
    private ContactController contactController;

    private MockMvc mockMvc;

    private Client testClient;
    private ContactReadDto testContactReadDto;
    private Long testId;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders.standaloneSetup(contactController).build();

        testId = 1L;

        testClient = Client.builder()
                .id(testId)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .build();

        testContactReadDto = ContactReadDto.builder()
                .mobilePhone("+7-555-545-657")
                .homePhone("+75 6768 83-43-65")
                .apartment(153)
                .building(32)
                .address("Green Point avn.")
                .postalCode(4432-4534-2123)
                .city("Atlantis")
                .build();

        mockClientDetails = new SecurityClientDetails(testClient);
        mockAuthentication = new TestingAuthenticationToken(mockClientDetails, TEST_PASS, mockClientDetails.getAuthorities());
    }

    @Test
    @SneakyThrows
    void shouldReturnOk_AndEqObject_GetContactDetails_Test() {
        /* "Заглушаем" систему безопасности */
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        /* "Заглушаем" ключевую логику метода */
        when(userDetailsDetector.isUserDetailsNotNull(any(ClientService.class), any(Authentication.class))).thenReturn(true);
        when(userDetailsDetector.getClientId()).thenReturn(testId);
        when(contactService.readContact(testId)).thenReturn(Optional.of(testContactReadDto));

        /* Имитируем запрос и ожидаемый ответ */
        MvcResult mvcResult = mockMvc.perform(get("/api/myContact"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        /* Получаем результат запроса -> ответ в виде строки */
        String strResult = mvcResult.getResponse().getContentAsString();

        /* Парсим результат response, ранее полученный как строка, в массив ContactReadDto объект */
        ContactReadDto contactReadDtoFromResponse = objectMapper.readValue(strResult, ContactReadDto.class);

        /* Сличаем исходный ContactReadDto и возвращенный из response после "прогона" тестируемого метода */
        assertAll(
                () -> assertThat(testContactReadDto.getCity()).isEqualTo(contactReadDtoFromResponse.getCity()),
                () -> assertThat(testContactReadDto.getAddress()).isEqualTo(contactReadDtoFromResponse.getAddress()),
                () -> assertThat(testContactReadDto.getApartment()).isEqualTo(contactReadDtoFromResponse.getApartment()),
                () -> assertThat(testContactReadDto.getBuilding()).isEqualTo(contactReadDtoFromResponse.getBuilding()),
                () -> assertThat(testContactReadDto.getHomePhone()).isEqualTo(contactReadDtoFromResponse.getHomePhone()),
                () -> assertThat(testContactReadDto.getMobilePhone()).isEqualTo(contactReadDtoFromResponse.getMobilePhone()),
                () -> assertThat(testContactReadDto.getPostalCode()).isEqualTo(contactReadDtoFromResponse.getPostalCode())
        );
    }

    @Test
    @SneakyThrows
    void shouldReturn_204_Status_AndEmptyPage_GetContactDetails_Test() {
        /* "Заглушаем" систему безопасности */
        when(mockSecurityContext.getAuthentication()).thenReturn(mockAuthentication);
        SecurityContextHolder.setContext(mockSecurityContext);

        /* "Заглушаем" ключевую логику метода - */
        when(userDetailsDetector.isUserDetailsNotNull(any(ClientService.class), any(Authentication.class))).thenReturn(false);

        /* Имитируем запрос и ожидаемый ответ */
        mockMvc.perform(get("/api/myContact"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().string(""));
    }
}