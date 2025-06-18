package me.oldboy.unit.controllers.api;

import lombok.SneakyThrows;
import me.oldboy.controllers.api.AdminController;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.models.client.Role;
import me.oldboy.services.ClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static me.oldboy.test_constant.TestConstantFields.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {AdminController.class})
class AdminControllerTest {

    @MockitoBean
    private ClientService clientService;
    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private List<ClientReadDto> testList;

    @BeforeEach
    public void setUp(){
        MockitoAnnotations.openMocks(this);

        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        testList = List.of(new ClientReadDto(1L, TEST_EMAIL, Role.USER.name(), TEST_CLIENT_NAME, TEST_CLIENT_SUR_NAME, TEST_AGE));
    }

    @Test
    @SneakyThrows
    void shouldReturnOk_AndSingletonList_GetAllClient_Test() {
        when(clientService.findAll()).thenReturn(testList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/getAllClient"))
                .andExpect(status().isOk())
                .andExpect(content().string(new ObjectMapper().writeValueAsString(testList)));
    }
}