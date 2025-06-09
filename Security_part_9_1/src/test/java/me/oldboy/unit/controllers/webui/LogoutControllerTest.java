package me.oldboy.unit.controllers.webui;

import lombok.SneakyThrows;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.config.securiry_details.ClientDetailsService;
import me.oldboy.controllers.webui.LogoutController;
import me.oldboy.filters.utils.JwtSaver;
import me.oldboy.repository.*;
import me.oldboy.services.ClientService;
import me.oldboy.test_constant.TestConstantFields;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static me.oldboy.constants.SecurityConstants.JWT_HEADER;
import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LogoutController.class)
class LogoutControllerTest {

    @MockBean
    private ClientService mockClientService;
    @MockBean
    private DataSource mockDataSource;
    @MockBean
    private ClientRepository mockClientRepository;
    @MockBean
    private ClientDetailsService mockClientDetailsService;
    @MockBean
    private AuthenticationEventListener mockAuthenticationEventListener;
    @MockBean
    private JwtSaver jwtSaver;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ContactRepository contactRepository;
    @MockBean
    private BalanceRepository balanceRepository;
    @MockBean
    private CardRepository cardRepository;
    @MockBean
    private LoanRepository loanRepository;

    private String testJwt;

    @BeforeEach
    void setTestData(){
        testJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImF1dGhvcml0aWVzIjoiTU" +
                "9SRSBCQUQgQUNUSU9OLEFETUlOIn0.LImgJxRtwjdDFPdy5od6W5AgeDV7o7t-gAq-vFwqGwY";
    }

    @Test
    @SneakyThrows
    void shouldRedirect_ByePage_PostLogOut_Test() {
        mockMvc.perform(post("/webui/logout")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/bye"));
    }

    @Test
    @SneakyThrows
    void buy() {
        mockMvc.perform(get("/webui/bye")
                        .header(JWT_HEADER, testJwt))
                .andExpect(status().isOk())
                .andExpect(view().name("/bye.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>BYE</title>")));
    }
}