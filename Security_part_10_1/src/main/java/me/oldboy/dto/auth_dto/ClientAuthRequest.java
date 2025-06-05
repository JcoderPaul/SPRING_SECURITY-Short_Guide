package me.oldboy.dto.auth_dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ClientAuthRequest{
    @Email(message = "Standard e-mail structure - email_name@email_domain.top_lavel_domain (for example: paul@tradsystem.ru)")
    @Size(min = 5, max = 64, message = "Email size can be between 5 and 64")
    String username;
    @NotBlank(message = "Password cannot be blank")
    @Size(min = 2, max = 64, message = "Password size can be between 2 and 64")
    String password;
}
