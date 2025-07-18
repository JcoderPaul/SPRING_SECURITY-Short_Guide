package me.oldboy.unit.services;

import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.mapper.ClientMapper;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import me.oldboy.models.client_info.Details;
import me.oldboy.repository.ClientRepository;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    private Long testId;

    @BeforeEach
    void setTests(){
        MockitoAnnotations.openMocks(this);

        testId = 1L;

        testDetailsCreateDto = new DetailsCreateDto(TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE);
        testClientCreateDto = new ClientCreateDto(TEST_EMAIL, TEST_PASS, testDetailsCreateDto);

        clientDetailsFromBase = new Details(testId, TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE, clientFromBase);
        clientFromBase = Client.builder()
                .id(testId)
                .email(TEST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .details(clientDetailsFromBase)
                .build();

        testClientList = new ArrayList<>(List.of(clientFromBase,
                Client.builder().email("test_two@test.com").build(),
                Client.builder().email("test_three@test.com").build()));
    }

    @Test
    void shouldReturnClientReadDto_SaveClientTest() {
        when(clientRepository.save(any(Client.class))).thenReturn(clientFromBase);

        ClientReadDto actualReadDto = clientService.saveClient(testClientCreateDto);

        assertThat(clientFromBase.getDetails().getClientName()).isEqualTo(actualReadDto.getClientName());

        verify(clientRepository, times(1)).save(any(Client.class));
    }

    @Test
    void checkReturnedClientReadDto_SaveClientTest() {
        clientService.saveClient(testClientCreateDto);

        verify(clientRepository).save(clientCaptor.capture());

        Client capturedClient = clientCaptor.getValue();

        ClientReadDto returnedClientDto = ClientMapper.INSTANCE.mapToClientReadDto(capturedClient);

        /* Мы в тестах нигде явно Role не задавали, это происходит в методе *.saveClient(), и тут мы это можем проверить */
        assertAll(
                () -> assertThat(testClientCreateDto.getDetails().clientName()).isEqualTo(capturedClient.getDetails().getClientName()),
                () -> assertThat(testClientCreateDto.getDetails().clientSurName()).isEqualTo(capturedClient.getDetails().getClientSurName()),
                () -> assertThat(testClientCreateDto.getDetails().age()).isEqualTo(capturedClient.getDetails().getAge()),
                () -> assertThat(testClientCreateDto.getEmail()).isEqualTo(capturedClient.getEmail()),
                () -> assertThat(Role.USER).isEqualTo(capturedClient.getRole())

        );

        assertAll(
                () -> assertThat(testClientCreateDto.getDetails().clientName()).isEqualTo(returnedClientDto.getClientName()),
                () -> assertThat(testClientCreateDto.getDetails().clientSurName()).isEqualTo(returnedClientDto.getClientSurName()),
                () -> assertThat(testClientCreateDto.getDetails().age()).isEqualTo(returnedClientDto.getAge()),
                () -> assertThat(testClientCreateDto.getEmail()).isEqualTo(returnedClientDto.getEmail()),
                () -> assertThat(Role.USER.name()).isEqualTo(returnedClientDto.getRole())
        );
    }

    @Test
    void checkReturnedSizeListOfClientReadDTO_FindAllTest() {
        when(clientRepository.findAll()).thenReturn(testClientList);

        int originalListSize = testClientList.size();
        List<ClientReadDto> clientReadDtoList = clientService.findAll();

        assertThat(clientReadDtoList.size()).isEqualTo(originalListSize);

        verify(clientRepository,times(1)).findAll();
    }

    @Test
    void checkReturnedOptionalClient_FindByEmailTest() {
        when(clientRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(clientFromBase));

        Optional<Client> mayBeClient = clientService.findByEmail(TEST_EMAIL);

        assertThat(mayBeClient.isPresent()).isTrue();
        assertThat(mayBeClient.get().getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    void checkReturnedOptionalEmptyClient_FindByEmailTest() {
        when(clientRepository.findByEmail(NON_EXIST_EMAIL)).thenReturn(Optional.empty());

        Optional<Client> mayBeClient = clientService.findByEmail(NON_EXIST_EMAIL);

        assertThat(mayBeClient.isEmpty()).isTrue();
        verify(clientRepository, times(1)).findByEmail(NON_EXIST_EMAIL);
    }

    @Test
    void checkReturnedOptionalClient_FindById_Test() {
        when(clientRepository.findById(testId)).thenReturn(Optional.of(clientFromBase));

        Optional<Client> mayBeClient = clientService.findById(testId);

        assertThat(mayBeClient.isPresent()).isTrue();
        assertThat(mayBeClient.get().getEmail()).isEqualTo(TEST_EMAIL);
    }

    @Test
    void checkReturnedOptionalEmptyClient_FindById_Test() {
        when(clientRepository.findById(anyLong())).thenReturn(Optional.empty());

        Optional<Client> mayBeClient = clientService.findById(anyLong());

        assertThat(mayBeClient.isEmpty()).isTrue();
        verify(clientRepository, times(1)).findById(anyLong());
    }
}