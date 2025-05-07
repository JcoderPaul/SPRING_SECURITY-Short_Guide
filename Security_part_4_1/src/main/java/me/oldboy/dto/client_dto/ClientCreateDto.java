package me.oldboy.dto.client_dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.validation.annitation.CheckDetails;

public record ClientCreateDto(@Email(message = "Standard e-mail structure - email_name@email_domain.top_lavel_domain (for example: paul@tradsystem.ru)")
                              String email,
                              @NotBlank(message = "Field cannot be blank")
                              @Size(min = 2, max = 64, message = "Field size value can be between 2 and 64")
                              String pass,
                              @CheckDetails
                              @Valid
                              DetailsCreateDto details) {
}
