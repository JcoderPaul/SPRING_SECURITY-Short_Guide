package me.oldboy.integration.services;

import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.client.Role;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

class ClientServiceIT extends IntegrationTestBase {

    @Autowired
    private ClientService clientService;
    private ClientCreateDto testClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto;
    private Long existId, notExistId;

    @BeforeEach
    void setUp(){
        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, testDetailsCreateDto);
        existId = 1L;
        notExistId = 100L;
    }

    @Test
    void saveClient_ShouldReturnClientReadDto_Test() {
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
    void findAll_CheckCollectionSize_Test() {
        List<ClientReadDto> currentList = clientService.findAll();
        assertThat(currentList.size() > 0).isTrue();
    }

    @Test
    void findAll_CheckCollectionSizeAfterClientAdd_Test() {
        List<ClientReadDto> currentList = clientService.findAll();
        int listSize = currentList.size();

        clientService.saveClient(testClientCreateDto);

        List<ClientReadDto> listAfterSaving = clientService.findAll();
        int sizeListAfterSaving = listAfterSaving.size();

        assertThat(listSize < sizeListAfterSaving).isTrue();
    }

    @Test
    void findByEmail_CheckExistEmail_ReturnTrue_Test() {
        assertThat(clientService.findByEmail(EXIST_EMAIL).isPresent()).isTrue();
    }

    @Test
    void findByEmail_CheckNotExistEmail_ReturnFalse_Test() {
        assertThat(clientService.findByEmail(NON_EXIST_EMAIL).isPresent()).isFalse();
    }

    @Test
    void findById_CheckExistId_ReturnTrue_Test() {
        assertThat(clientService.findById(existId).isPresent()).isTrue();
    }

    @Test
    void findById_CheckNotExistId_ReturnFalse_Test() {
        assertThat(clientService.findById(notExistId).isEmpty()).isTrue();
    }
}