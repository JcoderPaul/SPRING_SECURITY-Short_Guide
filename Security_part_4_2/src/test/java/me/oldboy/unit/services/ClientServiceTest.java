package me.oldboy.unit.services;

import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.mapper.ClientMapper;
import me.oldboy.models.Client;
import me.oldboy.models.Details;
import me.oldboy.models.Role;
import me.oldboy.repository.ClientRepository;
import me.oldboy.services.ClientService;
import me.oldboy.test_config.TestConstantFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ClientRepository clientRepository;
    @InjectMocks
    private ClientService clientService;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;

    private ClientCreateDto testClientCreateDto;
    private DetailsCreateDto testDetailsCreateDto;
    private Client clientFromBase;
    private Details clientDetailsFromBase;
    private List<Client> testClientList;

    @BeforeEach
    void setTests(){
        testDetailsCreateDto = new DetailsCreateDto(TestConstantFields.TEST_CLIENT_NAME,
                                                    TestConstantFields.TEST_CLIENT_SUR_NAME,
                                                    TestConstantFields.TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TestConstantFields.TEST_EMAIL,
                                                  TestConstantFields.TEST_PASS,
                                                  testDetailsCreateDto);

        clientDetailsFromBase = new Details(1L,
                                            TestConstantFields.TEST_CLIENT_NAME,
                                            TestConstantFields.TEST_CLIENT_SUR_NAME,
                                            TestConstantFields.TEST_AGE,
                                            clientFromBase);
        clientFromBase = new Client(1L,
                                    TestConstantFields.TEST_EMAIL,
                                    TestConstantFields.TEST_PASS,
                                    Role.USER,
                                    clientDetailsFromBase);

        testClientList = new ArrayList<>(List.of(clientFromBase,
                                                 Client.builder().email("test_two@test.com").build(),
                                                 Client.builder().email("test_three@test.com").build()));
    }

    @Test
    void shouldReturnClientReadDtoSaveClientTest() {
        when(clientRepository.save(any(Client.class))).thenReturn(clientFromBase);

        ClientReadDto actualReadDto = clientService.saveClient(testClientCreateDto);

        assertThat(clientFromBase.getDetails().getClientName()).isEqualTo(actualReadDto.getClientName());
        verify(clientRepository,times(1)).save(any(Client.class));
    }

    /*
    Тут мы применили интересный рецепт - захват аргумента. В предыдущем тесте мы замокали метод *.save(),
    и в принципе такой вариант реализации теста нормален для подобного случая, когда внутри тестируемого
    метода, происходят некоторые преобразования с подаваемым на вход аргументом и, до "заглушенного" метода
    добирается нечто другое со входа тестируемого.

    Тут мы точно знаем как должна отработать логика и сравниваем состояние обоих объектов - того, что был
    подан на вход тестируемого метода *.saveClient() и тот, что добрался до входа mock-метода *.save().
    */
    @Test
    void checkReturnedClientReadDtoSaveClientTest() {
        clientService.saveClient(testClientCreateDto);

        verify(clientRepository).save(clientCaptor.capture());

        Client capturedClient = clientCaptor.getValue();

        ClientReadDto returnedClientDto = ClientMapper.INSTANCE.mapToClientReadDto(capturedClient);

        /* Мы в тестах нигде явно Role не задавали, это происходит в методе *.saveClient(), и тут мы это можем проверить */
        assertAll(
                () -> assertThat(testClientCreateDto.details().clientName()).isEqualTo(capturedClient.getDetails().getClientName()),
                () -> assertThat(testClientCreateDto.details().clientSurName()).isEqualTo(capturedClient.getDetails().getClientSurName()),
                () -> assertThat(testClientCreateDto.details().age()).isEqualTo(capturedClient.getDetails().getAge()),
                () -> assertThat(testClientCreateDto.email()).isEqualTo(capturedClient.getEmail()),
                () -> assertThat(Role.USER).isEqualTo(capturedClient.getRole())

        );

        assertAll(
                () -> assertThat(testClientCreateDto.details().clientName()).isEqualTo(returnedClientDto.getClientName()),
                () -> assertThat(testClientCreateDto.details().clientSurName()).isEqualTo(returnedClientDto.getClientSurName()),
                () -> assertThat(testClientCreateDto.details().age()).isEqualTo(returnedClientDto.getAge()),
                () -> assertThat(testClientCreateDto.email()).isEqualTo(returnedClientDto.getEmail()),
                () -> assertThat(Role.USER.name()).isEqualTo(returnedClientDto.getRole())
        );

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void checkReturnedSizeListOfClientReadDTOFindAllTest() {
        when(clientRepository.findAll()).thenReturn(testClientList);

        int originalListSize = testClientList.size();
        List<ClientReadDto> clientReadDtoList = clientService.findAll();

        assertThat(clientReadDtoList.size()).isEqualTo(originalListSize);

        verify(clientRepository,times(1)).findAll();
    }

    @Test
    void checkReturnedOptionalClientFindByEmailTest() {
        when(clientRepository.findByEmail(TestConstantFields.TEST_EMAIL)).thenReturn(Optional.of(clientFromBase));

        Optional<Client> mayBeClient = clientService.findByEmail(TestConstantFields.TEST_EMAIL);

        assertThat(mayBeClient.isPresent()).isTrue();
        assertThat(mayBeClient.get().getEmail()).isEqualTo(TestConstantFields.TEST_EMAIL);

        verify(clientRepository, times(1)).findByEmail(anyString());
    }
}