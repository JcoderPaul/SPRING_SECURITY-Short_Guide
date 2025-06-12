package me.oldboy.integration.controllers.webui;

import lombok.SneakyThrows;
import me.oldboy.config.auth_event_listener.AuthenticationEventListener;
import me.oldboy.config.securiry_details.SecurityClientDetails;
import me.oldboy.controllers.webui.LogoutController;
import me.oldboy.filters.utils.JwtSaver;
import me.oldboy.integration.IntegrationTestBase;
import me.oldboy.models.client.Client;
import me.oldboy.models.client.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static me.oldboy.constants.SecurityConstants.JWT_HEADER;
import static me.oldboy.test_constant.TestConstantFields.EXIST_EMAIL;
import static me.oldboy.test_constant.TestConstantFields.TEST_PASS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class LogoutControllerTestIT extends IntegrationTestBase {

    @Autowired
    private LogoutController logoutController;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JwtSaver jwtSaver;
    @Autowired
    private AuthenticationEventListener authenticationEventListener;
    private String testJwt;
    private Client testClient;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp(){
        testJwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkB0ZXN0LmNvbSIsImF1dGhvcml0aWVzIjoiTU" +
                "9SRSBCQUQgQUNUSU9OLEFETUlOIn0.LImgJxRtwjdDFPdy5od6W5AgeDV7o7t-gAq-vFwqGwY";

        jwtSaver.saveJwtToken(EXIST_EMAIL, testJwt);

        testClient = Client.builder()
                .id(1L)
                .email(EXIST_EMAIL)
                .pass(TEST_PASS)
                .role(Role.USER)
                .build();

        testUserDetails = new SecurityClientDetails(testClient);

        authenticationEventListener.setAuthenticationAfterFormLogin(new TestingAuthenticationToken(testClient, TEST_PASS, testUserDetails.getAuthorities()));
    }

    @Test
    @SneakyThrows
    @WithUserDetails(value = EXIST_EMAIL, userDetailsServiceBeanName = "clientDetailsService")
    void shouldRedirectToPostMethod_LogOut_Test() {
        mockMvc.perform(post("/webui/logout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/webui/bye"));
    }

    @Test
    @SneakyThrows
    void shouldReturn_2xx_AndClearAuth_Buy_Test() {
        mockMvc.perform(get("/webui/bye")
                        .header(JWT_HEADER, testJwt)
                        .sessionAttr("email", EXIST_EMAIL))
                .andExpect(status().is2xxSuccessful())
                .andExpect(view().name("/bye.html"))
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(content().string(containsString("<title>BYE</title>")));

        assertThat(jwtSaver.getSavedJwt(EXIST_EMAIL)).isNullOrEmpty();
        assertThat(authenticationEventListener.getAuthenticationAfterFormLogin()).isEqualTo(null);
    }
}