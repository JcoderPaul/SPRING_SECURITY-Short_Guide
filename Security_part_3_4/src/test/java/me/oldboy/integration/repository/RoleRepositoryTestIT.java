package me.oldboy.integration.repository;

import me.oldboy.integration.annotation.IT;
import me.oldboy.models.PrintAuthorities;
import me.oldboy.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@IT
class RoleRepositoryTestIT {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void shouldReturnListSizeFindAllTest() {
        List<PrintAuthorities> roleRepositoryList = roleRepository.findAll();
        assertThat(roleRepositoryList.size()).isEqualTo(4);
    }

    @Test
    void shouldReturnTrue_HR_ROLE_ContainsFindAllTest() {
        List<PrintAuthorities> roleRepositoryList = roleRepository.findAll();
        assertThat(roleRepositoryList.stream()
                .map(pa-> pa.getAuthority())
                .toList()
                .contains("ROLE_HR")).isTrue();
    }
}