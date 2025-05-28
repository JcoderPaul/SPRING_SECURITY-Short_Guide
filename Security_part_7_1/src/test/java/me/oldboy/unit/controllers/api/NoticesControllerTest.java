package me.oldboy.unit.controllers.api;

import me.oldboy.controllers.api.NoticesController;
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

@WebMvcTest(NoticesController.class)
class NoticesControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private DataSource dataSource;
    @MockBean
    private ClientRepository clientRepository;

    @Test
    void shouldReturn_isOk_WithAnyUser_GetNoticesTest() throws Exception {
        mockMvc.perform(get("/api/notices"))
                .andExpect(status().isOk())
                .andExpect(content().string("[\"Here are the notices details from the DB\"]"));
    }
}