package me.oldboy.unit.securiry_details;

import me.oldboy.config.securiry_details.ClientDetailsService;
import me.oldboy.exception.EmailNotFoundException;
import me.oldboy.models.Client;
import me.oldboy.models.Details;
import me.oldboy.models.Role;
import me.oldboy.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class ClientDetailsServiceTest {

    @Mock
    private ClientRepository mockClientRepository;
    @InjectMocks
    private ClientDetailsService detailsService;

    private Client testClient;
    private Details testDetails;

    @BeforeEach
    void setUpTest(){
        MockitoAnnotations.openMocks(this);

        testDetails = Details.builder()
                .id(1L)
                .clientName(TEST_CLIENT_NAME)
                .clientSurName(TEST_CLIENT_SUR_NAME)
                .age(TEST_AGE)
                .client(testClient)
                .build();

        testClient = Client.builder()
                .id(1L)
                .email(EXIST_EMAIL)
                .pass(WRONG_PASS)
                .role(Role.USER)
                .details(testDetails)
                .build();
    }

    @Test
    void shouldReturnExistClient_loadUserByUsername_Test() {
        when(mockClientRepository.findByEmail(EXIST_EMAIL)).thenReturn(Optional.of(testClient));

        UserDetails userDetailsReturn = detailsService.loadUserByUsername(EXIST_EMAIL);

        assertThat(userDetailsReturn.getUsername()).isEqualTo(EXIST_EMAIL);
        assertThat(userDetailsReturn.getPassword()).isEqualTo(testClient.getPass());

        verify(mockClientRepository,times(1)).findByEmail(anyString());
    }

    @Test
    void expectedExceptionNoEmailFound_loadUserByUsername_Test() {
        when(mockClientRepository.findByEmail(EXIST_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> detailsService.loadUserByUsername(EXIST_EMAIL))
                .isInstanceOf(EmailNotFoundException.class)
                .hasMessageContaining("User email : " + EXIST_EMAIL + " not found!");

        verify(mockClientRepository,times(1)).findByEmail(anyString());
    }
}