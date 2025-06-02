package me.oldboy.integration.services;

import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@IT
class ClientServiceTestIT extends TestContainerInit {

    @Autowired
    private ClientService clientService;
    private ClientCreateDto testClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto;

    @BeforeEach
    void setUp(){
        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, testDetailsCreateDto);
    }

    @Test
    void shouldReturn_ClientReadDto_SaveClientTest() {
        ClientReadDto savedClient = clientService.saveClient(testClientCreateDto);

        assertAll(
                () -> assertThat(savedClient.getClientName()).isEqualTo(testClientCreateDto.getDetails().clientName()),
                () -> assertThat(savedClient.getClientSurName()).isEqualTo(testClientCreateDto.getDetails().clientSurName()),
                () -> assertThat(savedClient.getAge()).isEqualTo(testClientCreateDto.getDetails().age()),
                () -> assertThat(savedClient.getEmail()).isEqualTo(testClientCreateDto.getEmail()),
                () -> assertThat(savedClient.getRole()).isEqualTo(Role.ROLE_USER.name())
        );
    }

    @Test
    void checkCollectionSize_FindAllTest() {
        List<ClientReadDto> currentList = clientService.findAll();
        assertThat(currentList.size() >= 5).isTrue();
    }

    @Test
    void checkCollectionSize_AfterClientAdd_FindAllTest() {
        List<ClientReadDto> currentList = clientService.findAll();
        int listSize = currentList.size();

        clientService.saveClient(testClientCreateDto);

        List<ClientReadDto> listAfterSaving = clientService.findAll();
        int sizeListAfterSaving = listAfterSaving.size();

        assertThat(listSize < sizeListAfterSaving).isTrue();
    }

    @Test
    void checkExistEmail_ReturnTrue_FindByEmail() {
        assertThat(clientService.findByEmail(EXIST_EMAIL).isPresent()).isTrue();
    }

    @Test
    void checkNotExistEmail_ReturnFalse_FindByEmail() {
        assertThat(clientService.findByEmail(TEST_EMAIL).isPresent()).isFalse();
    }
}