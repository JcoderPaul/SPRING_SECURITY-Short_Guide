package me.oldboy.integration.repository;

import lombok.RequiredArgsConstructor;
import me.oldboy.integration.IntegrationTestBaseConnection;
import me.oldboy.models.client.Client;
import me.oldboy.repository.ClientRepository;
import org.junit.jupiter.api.*;
import org.springframework.test.annotation.Rollback;

import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_EMAIL;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@RequiredArgsConstructor
class ClientRepositoryTestIT extends IntegrationTestBaseConnection {

    private final ClientRepository clientRepository;

    private String clientEmail;

    @Test
    @DisplayName("Test 11: Find client by email from DB")
    @Order(11)
    @Rollback(value = false)
    void shouldReturnTrue_OptionalClient_If_FindClientByEmail_Test() {
        clientEmail = EXIST_EMAIL;
        Optional<Client> mayBeClient = clientRepository.findByEmail(clientEmail);

        assertThat(mayBeClient.isPresent()).isTrue();
    }

    @Test
    @DisplayName("Test 12: Can not find client by not existing email")
    @Order(12)
    @Rollback(value = false)
    void shouldReturnOptionalEmpty_If_NotFindClientByEmail_Test() {
        clientEmail = TEST_EMAIL;
        Optional<Client> mayBeClient = clientRepository.findByEmail(clientEmail);

        assertThat(mayBeClient.isEmpty()).isTrue();
    }
}