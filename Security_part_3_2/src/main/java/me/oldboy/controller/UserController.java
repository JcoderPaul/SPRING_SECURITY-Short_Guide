package me.oldboy.controller;

import me.oldboy.model.User;
import me.oldboy.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/userList")
    public List<User> getUserList(){
        return (List<User>) userRepository.findAll();
    }
}
