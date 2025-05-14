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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/* Тестируем при помощи Mock технологии - "тестовые заглушки" */
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
    private String testEmail;
    private String testPassword;
    private String testClientName;
    private String testClientSurName;
    private Integer testClientAge;
    private Client clientFromBase;
    private Details clientDetailsFromBase;
    private List<Client> testClientList;

    @BeforeEach
    void setTests(){
        testEmail = "test@test.com";
        testPassword = "1234";
        testClientName = "John";
        testClientSurName = "Doe";
        testClientAge = 34;

        testDetailsCreateDto = new DetailsCreateDto(testClientName, testClientSurName, testClientAge);
        testClientCreateDto = new ClientCreateDto(testEmail, testPassword, testDetailsCreateDto);

        clientDetailsFromBase = new Details(1L, testClientName, testClientSurName, testClientAge, clientFromBase);
        clientFromBase = new Client(1L, testEmail, testPassword, Role.USER, clientDetailsFromBase);

        testClientList = new ArrayList<>(List.of(clientFromBase,
                                                 Client.builder().email("test_two@test.com").build(),
                                                 Client.builder().email("test_three@test.com").build()));
    }


    @Test
    void shouldReturnClientReadDtoSaveClientTest() {

        /*
        Есть странное расхожее выражение - "Шибче вдаришь, глубжее войдет", есть более простое и понятное - "Повторение - мать учения"
        и вот с этим многие согласны. И так, что мы тут имеем - конструкцию: when().thenReturn(). Вроде выглядит просто, и даже читается
        логично: "когда (произойдет обращение к методу, с некими параметрами).тогда вернуть (нечто заранее известное)". Все бы ничего,
        да вот только я бы усилил и уточнил одновременно это "разъяснение", а еще и к месту привязал. Уверен, где-то есть и верный
        перевод, и понятная интерпретация официальной документации по Mockito.

        Зачем в данном тесте мы делаем "заглушку" на метод *.save() класса репозитория? Потому что в нашем сервисном классе есть его
        использование, но связи с БД у нас тут нет, а данный метод должен что-то возвращать, чтобы тест прошел нормально, вот мы и
        имитируем некое возвращаемое значение.

        В идеале, было бы неплохо, если бы аргумент прилетевший в метод *.saveClient() передавался без преобразований в "заглушаемый"
        метод, но у нас есть Mapper, который преобразует один объект в другой: DTO -> Entity. В итоге, в коде метода *.saveClient()
        нашего сервиса, в метод *.save() нашего же репозитория прилетает некий объект прошедший через преобразующую логику и его мы
        не можем повторить, ну или скажем так - тест для проверки метода может стать много больше самого тестируемого метода.

        К чему я все это написал - трудности перевода или понимая конструкции!

        Конструкцию, применяемую в данном тесте - when().thenReturn(), нужно читать так - когда (when) в тестируемом методе *.saveClient()
        в метод clientRepository.save(поступит "конкретный аргумент"), тогда вернуть (thenReturn("конкретное возвращаемое значение")).
        Если этого не произойдет - Mockito будет выброшена ошибка, что мы и получили, когда пытались в метод *.save(подставить наше
        "тестовое значение"). Но при запуске теста оно - "тестовое значение" не прилетало в реальный clientRepository.save() тестируемого
        метода *.saveClient() и мы видели, в числе прочих, такую строку:

                - stubbed method is intentionally invoked with different arguments by code under test

        Мы же сами написали условия для "заглушки", когда в реальный метод прилетит "конкретное значение" верни мне "такое-то значение".
        А этого не произошло. Как бы мы не мучились в тесте, пытаясь достичь "конкретики" для *.save(), строка в реальном методе
        *.saveClient():

                Client toCreateClient = ClientMapper.INSTANCE.mapToClient(clientCreateDto);

        в ходе теста всегда будет генерировать новый экземпляр класса, а дальнейший код передавать его ссылку в аргумент метода *.save(),
        который в тесте мы пытаемся mock-ать своим значением. Другими словами, наше "тестовое значение", даже если оно экземпляр класса
        Client переданное в when(clientRepository.save("testClient"), будет отличаться от значения сгенерированного Mapper-ом в тестируемом
        классе в ходе теста.

        Нам нужно вспомнить, что мы тут тестируем - не метод сохранения, БД не подключена! Мы хотим просто протестировать и убедиться, что
        метод возвращает экземпляр ClientReadDto, а для этого нам не нужна "конкретика", т.е. нас вполне устроит конструкция вида:

                when(clientRepository.save(any(Client.class))).thenReturn(clientFromBase);

        Т.е. при написании заглушки нужно учитывать все преобразования происходящие в коде тестируемого класса.
        */

        when(clientRepository.save(any(Client.class))).thenReturn(clientFromBase);

        ClientReadDto actualReadDto = clientService.saveClient(testClientCreateDto);

        assertThat(clientFromBase.getDetails().getClientName()).isEqualTo(actualReadDto.getClientName());
    }

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
        when(clientRepository.findByEmail(testEmail)).thenReturn(Optional.of(clientFromBase));

        Optional<Client> mayBeClient = clientService.findByEmail(testEmail);

        assertThat(mayBeClient.isPresent()).isTrue();
        assertThat(mayBeClient.get().getEmail()).isEqualTo(testEmail);
    }
}