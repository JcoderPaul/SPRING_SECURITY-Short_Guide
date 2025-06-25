package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.auth_dto.ClientAuthRequest;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.exception.ClientServiceException;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.client.Client;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@IT
class ClientServiceIT extends TestContainerInit {

    @Autowired
    private ClientService clientService;
    private ClientCreateDto testClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto;
    private Long testExistId, testNotExistId;
    private ClientAuthRequest testAuthRequestWithCorrectData, testAuthRequestWithWrongPass, testAuthRequestWithWrongEmail;

    @BeforeEach
    void setUp(){
        testDetailsCreateDto = DetailsCreateDto.builder()
                .clientName(TEST_CLIENT_NAME)
                .clientSurName(TEST_CLIENT_SUR_NAME)
                .age(43)
                .build();
        testClientCreateDto = ClientCreateDto.builder()
                .email(TEST_EMAIL)
                .password(TEST_PASS)
                .details(testDetailsCreateDto)
                .build();

        testExistId = 1L;
        testNotExistId = 100L;

        testAuthRequestWithCorrectData = ClientAuthRequest.builder()
                .username(EXIST_EMAIL)
                .password(TEST_PASS)
                .build();
        testAuthRequestWithWrongPass = ClientAuthRequest.builder()
                .username(EXIST_EMAIL)
                .password(WRONG_PASS)
                .build();
        testAuthRequestWithWrongEmail = ClientAuthRequest.builder()
                .username(TEST_EMAIL)
                .password(TEST_PASS)
                .build();
    }

    @Test
    void saveClient_ShouldReturnSavedDto_Test() {
        ClientReadDto clientDtoFromBase = clientService.saveClient(testClientCreateDto);

        assertThat(clientDtoFromBase).isNotNull();

        assertAll(
                () -> assertThat(clientDtoFromBase.getClientName()).isEqualTo(testDetailsCreateDto.clientName()),
                () -> assertThat(clientDtoFromBase.getClientSurName()).isEqualTo(testDetailsCreateDto.clientSurName()),
                () -> assertThat(clientDtoFromBase.getAge()).isEqualTo(testDetailsCreateDto.age()),
                () -> assertThat(clientDtoFromBase.getEmail()).isEqualTo(testClientCreateDto.getEmail())
        );
    }

    @Test
    void findAll_ShouldReturnListSize_BeforeAndAfterSaveClient_Test() {
        List<ClientReadDto> fromBaseListBeforeSaveClient = clientService.findAll();
        assertThat(fromBaseListBeforeSaveClient.size()).isGreaterThan(1);

        clientService.saveClient(testClientCreateDto);

        List<ClientReadDto> fromBaseListAfterSaveClient = clientService.findAll();
        assertThat(fromBaseListAfterSaveClient.size()).isGreaterThan(1);

        assertThat(fromBaseListAfterSaveClient.size()).isGreaterThan(fromBaseListBeforeSaveClient.size());
    }

    @Test
    void findByEmail_ShouldReturnExistClient_Test() {
        Optional<Client> mayBeClient = clientService.findByEmail(EXIST_EMAIL);
        if(mayBeClient.isPresent()){
            assertThat(mayBeClient.get().getEmail()).isEqualTo(EXIST_EMAIL);
        }
    }

    @Test
    void findByEmail_ShouldReturnEmptyOptional_HaveNoCurrentEmailInBase_Test() {
        Optional<Client> mayBeClient = clientService.findByEmail(TEST_EMAIL);
        assertThat(mayBeClient.isEmpty()).isTrue();
    }

    @Test
    void findById_ShouldReturnExistClient_Test() {
        Optional<Client> mayBeClient = clientService.findById(testExistId);
        if(mayBeClient.isPresent()){
            assertThat(mayBeClient.get().getId()).isEqualTo(testExistId);
        }
    }

    @Test
    void findById_ShouldReturnEmptyOptional_HaveNoCurrentId_Test() {
        Optional<Client> mayBeClient = clientService.findById(testNotExistId);
        assertThat(mayBeClient.isEmpty()).isTrue();
    }

    @Test
    void getClientIfAuthDataCorrect_ShouldReturnOptionalClient_Test() {
        Optional<Client> mayBeClient = clientService.getClientIfAuthDataCorrect(testAuthRequestWithCorrectData);
        if(mayBeClient.isPresent()){
            assertThat(mayBeClient.get().getEmail()).isEqualTo(EXIST_EMAIL);
        }
    }

    @Test
    void getClientIfAuthDataCorrect_ShouldReturnExceptionWrongPass_Test() {
        assertThatThrownBy(() -> clientService.getClientIfAuthDataCorrect(testAuthRequestWithWrongPass))
                .isInstanceOf(ClientServiceException.class)
                .hasMessageContaining("Password not correct!");
    }

    @Test
    void getClientIfAuthDataCorrect_ShouldReturnExceptionWrongEmail_Test() {
        assertThatThrownBy(() -> clientService.getClientIfAuthDataCorrect(testAuthRequestWithWrongEmail))
                .isInstanceOf(ClientServiceException.class)
                .hasMessageContaining("User name (email) not correct!");
    }
}