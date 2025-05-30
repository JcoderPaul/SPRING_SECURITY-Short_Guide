package me.oldboy.dto.auth_dto;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ClientAuthResponse {
    private Long id;
    private String clientLogin;
    private String accessToken;
}
