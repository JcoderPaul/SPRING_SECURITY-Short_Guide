package me.oldboy.integration.services;

import lombok.RequiredArgsConstructor;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.client.Role;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@RequiredArgsConstructor
class ClientServiceTestIT extends IntegrationTestBase {

    private final ClientService clientService;
    private ClientCreateDto testClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto;

    @BeforeEach
    void setUp(){
        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, testDetailsCreateDto);
    }

    @Test
    void shouldReturnClientReadDto_SaveClient_Test() {
        ClientReadDto savedClient = clientService.saveClient(testClientCreateDto);

        assertAll(
                () -> assertThat(savedClient.getClientName()).isEqualTo(testClientCreateDto.getDetails().clientName()),
                () -> assertThat(savedClient.getClientSurName()).isEqualTo(testClientCreateDto.getDetails().clientSurName()),
                () -> assertThat(savedClient.getAge()).isEqualTo(testClientCreateDto.getDetails().age()),
                () -> assertThat(savedClient.getEmail()).isEqualTo(testClientCreateDto.getEmail()),
                () -> assertThat(savedClient.getRole()).isEqualTo(Role.USER.name())
        );
    }

    @Test
    void checkCollectionSize_FindAll_Test() {
        List<ClientReadDto> currentList = clientService.findAll();
        assertThat(currentList.size() > 0).isTrue();
    }

    @Test
    void checkCollectionSizeAfterClientAdd_FindAll_Test() {
        List<ClientReadDto> currentList = clientService.findAll();
        int listSize = currentList.size();

        clientService.saveClient(testClientCreateDto);

        List<ClientReadDto> listAfterSaving = clientService.findAll();
        int sizeListAfterSaving = listAfterSaving.size();

        assertThat(listSize < sizeListAfterSaving).isTrue();
    }

    @Test
    void checkExistEmail_ReturnTrue_FindByEmail_Test() {
        assertThat(clientService.findByEmail(EXIST_EMAIL).isPresent()).isTrue();
    }

    @Test
    void checkNotExistEmail_ReturnFalse_FindByEmail_Test() {
        assertThat(clientService.findByEmail(NON_EXIST_EMAIL).isPresent()).isFalse();
    }

    @Test
    void checkExistId_ReturnTrue_FindById_Test() {
        assertThat(clientService.findById(1L).isPresent()).isTrue();
    }

    @Test
    void checkNotExistId_ReturnFalse_FindById_Test() {
        assertThat(clientService.findById(100L).isEmpty()).isTrue();
    }
}