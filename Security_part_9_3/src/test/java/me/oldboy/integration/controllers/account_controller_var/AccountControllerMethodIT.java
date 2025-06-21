package me.oldboy.integration.controllers.account_controller_var;

import lombok.SneakyThrows;
import me.oldboy.config.test_data_source.TestContainerInit;
import me.oldboy.controllers.api.AccountController;
import me.oldboy.dto.account_dto.AccountReadDto;
import me.oldboy.integration.annotation.IT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IT
class AccountControllerMethodIT extends TestContainerInit {

    @Autowired
    private AccountController accountController;

    @Test
    @SneakyThrows
    @WithMockUser(username = EXIST_EMAIL, password = TEST_PASS)
    void getAccountDetails_ShouldReturnOk_AndAccountRecord_ForCurrentClient_Test() {
          ResponseEntity<AccountReadDto> testReadDto = accountController.getAccountDetails();

          assertThat(testReadDto.getStatusCode().is2xxSuccessful()).isTrue();
          assertThat(testReadDto.getBody()).isNotNull();
    }

    @Test
    @SneakyThrows
    @WithMockUser(username = TEST_EMAIL, password = TEST_PASS)
    void getAccountDetails_ShouldReturn_204_NoContent_ForNotExistingClient_Test() {
        ResponseEntity<AccountReadDto> testReadDto = accountController.getAccountDetails();

        assertThat(testReadDto.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(testReadDto.getBody()).isEqualTo(null);
    }
}