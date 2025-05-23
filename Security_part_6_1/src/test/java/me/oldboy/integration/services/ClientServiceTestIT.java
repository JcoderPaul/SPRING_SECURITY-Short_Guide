package me.oldboy.integration.services;

import lombok.RequiredArgsConstructor;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.TestContainerInit;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@RequiredArgsConstructor
class ClientServiceTestIT extends TestContainerInit {

    private final ClientService clientService;
    private ClientCreateDto testClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto;

    @BeforeEach
    void setUp(){
        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, testDetailsCreateDto);
    }

    @Test
    void shouldReturnClientReadDtoSaveClientTest() {
        ClientReadDto savedClient = clientService.saveClient(testClientCreateDto);

        assertAll(
                () -> assertThat(savedClient.getClientName()).isEqualTo(testClientCreateDto.details().clientName()),
                () -> assertThat(savedClient.getClientSurName()).isEqualTo(testClientCreateDto.details().clientSurName()),
                () -> assertThat(savedClient.getAge()).isEqualTo(testClientCreateDto.details().age()),
                () -> assertThat(savedClient.getEmail()).isEqualTo(testClientCreateDto.email()),
                () -> assertThat(savedClient.getRole()).isEqualTo(Role.USER.name())
        );
    }

    @Test
    void checkCollectionSizeFindAllTest() {
        List<ClientReadDto> currentList = clientService.findAll();
        assertThat(currentList.size() >= 5).isTrue();
    }

    @Test
    void checkCollectionSizeAfterClientAddFindAllTest() {
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
        assertThat(clientService.findByEmail(NON_EXIST_EMAIL).isPresent()).isFalse();
    }
}