package me.oldboy.dto.details_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record DetailsCreateDto(@NotBlank
                               @Size(min = 2, max = 64, message = "Name field size can be between 2 and 64")
                               String clientName,
                               @NotBlank
                               @Size(min = 2, max = 64, message = "Surname field size can be between 2 and 64")
                               String clientSurName,
                               @Positive(message = "Age can't be lass then 0, unless you come from a counter-directional universe!")
                               Integer age) {
}
