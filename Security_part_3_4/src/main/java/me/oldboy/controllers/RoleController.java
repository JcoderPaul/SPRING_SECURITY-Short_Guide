package me.oldboy.controllers;

import lombok.extern.slf4j.Slf4j;
import me.oldboy.models.PrintAuthorities;
import me.oldboy.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/roleList")
    public List<PrintAuthorities> getClientList(){
        return roleRepository.findAll();
    }
}
