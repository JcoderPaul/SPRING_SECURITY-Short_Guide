package me.oldboy.controller;

import me.oldboy.likebase.LikeBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ClientsController {

    @Autowired
    private LikeBase likeBase;

    @GetMapping("/clientList")
    public List<UserToPrint> getClientList(){
        return likeBase.getUserList().stream()
                                     .map(userDetails -> new UserToPrint(userDetails.getUsername(),
                                                                         userDetails.isEnabled()))
                                     .collect(Collectors.toList());
    }

    private class UserToPrint{

        public UserToPrint(String userName, Boolean isActive) {
            this.userName = userName;
            this.isActive = isActive;
        }

        private String userName;
        private Boolean isActive;

        public String getUserName() {
            return userName;
        }

        public Boolean getUserActive() {
            return isActive;
        }

        @Override
        public String toString() {
            return "UserToPrint {" +
                    "userName: '" + userName + '\'' +
                    ", userActive: '" + isActive + '\'' +
                    '}';
        }
    }
}
