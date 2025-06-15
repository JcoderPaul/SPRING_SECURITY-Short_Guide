package me.oldboy.dto.auth_dto;

import lombok.*;

import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientAuthResponse {
    private Long id;
    private String clientLogin;
    private String accessToken;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientLogin() {
        return clientLogin;
    }

    public void setClientLogin(String clientLogin) {
        this.clientLogin = clientLogin;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public String toString() {
        return "ClientAuthResponse{" +
                "id=" + id +
                ", clientLogin='" + clientLogin + '\'' +
                ", accessToken='" + accessToken + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientAuthResponse that = (ClientAuthResponse) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(clientLogin, that.clientLogin) &&
                Objects.equals(accessToken, that.accessToken);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, clientLogin, accessToken);
    }
}