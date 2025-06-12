package me.oldboy.dto.client_dto;

import lombok.*;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientReadDto {
    private String email;
    private String role;
    private String clientName;
    private String clientSurName;
    private Integer age;

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

    @Override
    public String toString() {
        return " Email: " + email +
               " Role: " + role +
               " Name: " + clientName +
               " Surname: " + clientSurName +
               " Age: " + age;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientReadDto that = (ClientReadDto) o;
        return Objects.equals(email, that.email) && Objects.equals(role, that.role) && Objects.equals(clientName, that.clientName) && Objects.equals(clientSurName, that.clientSurName) && Objects.equals(age, that.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, role, clientName, clientSurName, age);
    }
}
