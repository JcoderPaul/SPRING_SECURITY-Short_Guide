package me.oldboy.dto.client_dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientReadDto {
    private Long id;
    private String email;
    private String role;
    private String clientName;
    private String clientSurName;
    private Integer age;

    @Override
    public String toString() {
        return " Email: " + email +
               " Role: " + role +
               " Name: " + clientName +
               " Surname: " + clientSurName +
               " Age: " + age;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientSurName() {
        return clientSurName;
    }

    public void setClientSurName(String clientSurName) {
        this.clientSurName = clientSurName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
