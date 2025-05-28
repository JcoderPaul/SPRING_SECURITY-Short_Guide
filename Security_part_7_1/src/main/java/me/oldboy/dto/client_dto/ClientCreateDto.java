package me.oldboy.dto.client_dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import me.oldboy.dto.details_dto.DetailsCreateDto;
import me.oldboy.validation.annitation.CheckDetails;

import java.util.Objects;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientCreateDto {
        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Standard e-mail structure - email_name@email_domain.top_lavel_domain (for example: paul@tradsystem.ru)")
        private String email;
        @NotBlank(message = "Password cannot be blank")
        @Size(min = 2, max = 64, message = "Password size can be between 2 and 64")
        private String pass;

        /*
        Мы можем применить нашу аннотацию для валидации @CheckDetails, а можем классическую
        @Valid - результат валидации будет практически одинаковым. Но при тестировании
        приложения с использованием аннотации @Valid мы естественно, не протестируем наш
        самописный валидатор, что ухудшит показатели покрытия приложения тестами.
        */
        @Valid // Либо можем использовать @CheckDetails вместо текущей см. комментарии выше.
        private DetailsCreateDto details;

        public String getEmail() {
                return email;
        }

        public void setEmail(String email) {
                this.email = email;
        }

        public String getPass() {
                return pass;
        }

        public void setPass(String pass) {
                this.pass = pass;
        }

        public DetailsCreateDto getDetails() {
                return details;
        }

        public void setDetails(DetailsCreateDto details) {
                this.details = details;
        }

        @Override
        public String toString() {
                return "ClientCreateDto{" +
                        "email='" + email + '\'' +
                        ", pass='" + pass + '\'' +
                        ", details=" + details +
                        '}';
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                ClientCreateDto that = (ClientCreateDto) o;
                return Objects.equals(email, that.email) && Objects.equals(pass, that.pass) && Objects.equals(details, that.details);
        }

        @Override
        public int hashCode() {
                return Objects.hash(email, pass, details);
        }
}
