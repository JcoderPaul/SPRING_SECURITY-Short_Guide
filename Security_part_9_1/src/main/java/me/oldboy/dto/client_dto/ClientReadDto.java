package me.oldboy.dto.client_dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ClientReadDto {
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
}
