package me.oldboy.unit.services;

import me.oldboy.dto.auth_dto.ClientAuthRequest;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.exception.ClientServiceException;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.models.client_info.Details;
import me.oldboy.repository.ClientRepository;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository mockClientRepository;
    @Mock
    private PasswordEncoder mockPasswordEncoder;
    @InjectMocks
    private ClientService clientService;

    private ClientReadDto testClientReadDto;
    private ClientCreateDto testClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto;
    private ClientAuthRequest testClientAuthRequest;
    private Client testClient;
    private Details testDetails;
    private List<ClientReadDto> testDtoList;
    private List<Client> testClientList;
    private Long testId;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);

        testId = 1L;

        testDetailsCreateDto = testDetailsCreateDto.builder()
                .clientName(TEST_CLIENT_NAME)
                .clientSurName(TEST_CLIENT_SUR_NAME)
                .age(TEST_AGE)
                .build();
        testClientCreateDto = ClientCreateDto.builder()
                .email(EXIST_EMAIL)
                .password(TEST_PASS)
                .details(testDetailsCreateDto)
                .build();
        testClientReadDto = ClientReadDto.builder()
                .email(EXIST_EMAIL)
                .clientName(TEST_CLIENT_NAME)
                .clientSurName(TEST_CLIENT_SUR_NAME)
                .age(TEST_AGE)
                .role(Role.USER.name())
                .build();
        testDetails = Details.builder()
                .client(testClient)
                .clientName(TEST_CLIENT_NAME)
                .clientSurName(TEST_CLIENT_SUR_NAME)
                .age(TEST_AGE)
                .build();
        testClient = Client.builder()
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .details(testDetails)
                .build();

        testClientAuthRequest = ClientAuthRequest.builder()
                .username(EXIST_EMAIL)
                .password(TEST_PASS)
                .build();

        testDtoList = List.of(testClientReadDto);
        testClientList = List.of(testClient);
    }

    @Test
    void shouldReturn_ExpectedDto_SaveClient_Test() {
        when(mockClientRepository.save(any(Client.class))).thenReturn(testClient);

        ClientReadDto testResult = clientService.saveClient(testClientCreateDto);

        assertAll(
                () -> assertThat(testResult.getClientName()).isEqualTo(testClientReadDto.getClientName()),
                () -> assertThat(testResult.getClientSurName()).isEqualTo(testClientReadDto.getClientSurName()),
                () -> assertThat(testResult.getEmail()).isEqualTo(testClientReadDto.getEmail()),
                () -> assertThat(testResult.getAge()).isEqualTo(testClientReadDto.getAge())
        );

        verify(mockClientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void shouldReturn_ExpectedFieldsListsSelectedElement_FindAll_Test() {
        when(mockClientRepository.findAll()).thenReturn(testClientList);

        List<ClientReadDto> testResultList = clientService.findAll();

        assertAll(
                () -> assertThat(testResultList.get(0).getClientName()).isEqualTo(testDtoList.get(0).getClientName()),
                () -> assertThat(testResultList.get(0).getClientSurName()).isEqualTo(testDtoList.get(0).getClientSurName()),
                () -> assertThat(testResultList.get(0).getEmail()).isEqualTo(testDtoList.get(0).getEmail()),
                () -> assertThat(testResultList.get(0).getAge()).isEqualTo(testDtoList.get(0).getAge()),
                () -> assertThat(testResultList.get(0).getRole()).isEqualTo(testDtoList.get(0).getRole())
        );

        verify(mockClientRepository, times(1)).findAll();
    }

    @Test
    void shouldReturn_OptionalExpectedEntity_FindByEmail_Test() {
        when(mockClientRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testClient));

        Optional<Client> mayBeClient = clientService.findByEmail(TEST_EMAIL);
        if(mayBeClient.isPresent()){
            assertThat(mayBeClient.get()).isEqualTo(testClient);
        }

        verify(mockClientRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    void shouldReturn_OptionalExpectedEntity_FindById_Test() {
        when(mockClientRepository.findById(testId)).thenReturn(Optional.of(testClient));

        Optional<Client> mayBeClient = clientService.findById(testId);
        if(mayBeClient.isPresent()){
            assertThat(mayBeClient.get()).isEqualTo(testClient);
        }

        verify(mockClientRepository, times(1)).findById(testId);
    }

    @Test
    void shouldReturn_OptionalClient_GetClientIfAuthDataCorrect_Test() {
        when(mockClientRepository.findByEmail(testClientAuthRequest.getUsername())).thenReturn(Optional.of(testClient));
        when(mockPasswordEncoder.matches(anyString(), anyString())).thenReturn(true);

        Optional<Client> mayBeClient = clientService.getClientIfAuthDataCorrect(testClientAuthRequest);
        if(mayBeClient.isPresent()){
            assertThat(mayBeClient.get()).isEqualTo(testClient);
        }

        verify(mockClientRepository, times(1)).findByEmail(anyString());
        verify(mockPasswordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    void shouldReturn_NotCorrectPasswordException_GetClientIfAuthDataCorrect_Test() {
        when(mockClientRepository.findByEmail(testClientAuthRequest.getUsername())).thenReturn(Optional.of(testClient));
        when(mockPasswordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> clientService.getClientIfAuthDataCorrect(testClientAuthRequest))
                .isInstanceOf(ClientServiceException.class)
                .hasMessageContaining("Password not correct!");

        verify(mockClientRepository, times(1)).findByEmail(anyString());
        verify(mockPasswordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    void shouldReturn_NotCorrectUsernameException_GetClientIfAuthDataCorrect_Test() {
        when(mockClientRepository.findByEmail(testClientAuthRequest.getUsername())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.getClientIfAuthDataCorrect(testClientAuthRequest))
                .isInstanceOf(ClientServiceException.class)
                .hasMessageContaining("User name (email) not correct!");

        verify(mockClientRepository, times(1)).findByEmail(anyString());
    }
}