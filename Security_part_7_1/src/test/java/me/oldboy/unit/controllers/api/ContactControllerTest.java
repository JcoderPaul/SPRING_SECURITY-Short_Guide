package me.oldboy.unit.controllers.api;

import lombok.SneakyThrows;
import me.oldboy.controllers.api.ContactController;
import me.oldboy.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContactController.class)
class ContactControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;

    @Test
    @SneakyThrows
    void shouldReturnOkForEveryOneClientGetContactDetailsTest() {
       mockMvc.perform(get("/api/contact"))
                .andExpect(status().isOk())
                .andExpect(content().string("Get details from DB"));
    }
}