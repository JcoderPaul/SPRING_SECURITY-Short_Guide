package me.oldboy.likebase;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.core.userdetails.User.withUsername;

@Slf4j
@Component
@AllArgsConstructor
@NoArgsConstructor
public class LikeBase {

    @Value("${admin.name}")
    private String adminName;

    @Value("${admin.pass}")
    private String adminPass;

    @Value("${admin.role}")
    private String adminRole;

    @Value("${simple.user.name}")
    private String userName;

    @Value("${simple.user.pass}")
    private String userPass;

    @Value("${simple.user.role}")
    private String userRole;
    private List<UserDetails> userList = new ArrayList<>();

    public String getAdminName() {
        return adminName;
    }

    public String getAdminPass() {
        return adminPass;
    }

    public String getAdminRole() {
        return adminRole;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPass() {
        return userPass;
    }

    public String getUserRole() {
        return userRole;
    }
    public List<UserDetails> getUserList() {
        return userList;
    }

    public void loadUserList(){
        userList.add(withUsername(adminName).password(adminPass).authorities(adminRole).build());
        userList.add(withUsername(userName).password(userPass).authorities(userRole).build());
    }
}
