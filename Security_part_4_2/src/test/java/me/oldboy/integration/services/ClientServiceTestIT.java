package me.oldboy.integration.services;

import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.integration.annotation.IT;
import me.oldboy.models.Role;
import me.oldboy.services.ClientService;
import me.oldboy.test_config.TestConstantFields;
import me.oldboy.test_config.TestDataSourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@IT
@ContextConfiguration(classes = {TestDataSourceConfig.class})
@ExtendWith(SpringExtension.class)
class ClientServiceTestIT {

    @Autowired
    private ClientService clientService;

    private ClientCreateDto testClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto;

    @BeforeEach
    void setUp(){
        testDetailsCreateDto = new DetailsCreateDto(TestConstantFields.TEST_CLIENT_NAME, TestConstantFields.TEST_CLIENT_SUR_NAME, TestConstantFields.TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TestConstantFields.TEST_EMAIL, TestConstantFields.TEST_PASS, testDetailsCreateDto);
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
        assertThat(clientService.findByEmail(TestConstantFields.EXIST_EMAIL).isPresent()).isTrue();
    }

    @Test
    void checkNotExistEmail_ReturnFalse_FindByEmail() {
        assertThat(clientService.findByEmail(TestConstantFields.TEST_EMAIL).isPresent()).isFalse();
    }
}