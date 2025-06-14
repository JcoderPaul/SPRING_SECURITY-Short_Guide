package me.oldboy.dto.details_dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DetailsCreateDto {
    @NotBlank
    @Size(min = 2, max = 64, message = "Name field size can be between 2 and 64")
    private String clientName;

    @NotBlank
    @Size(min = 2, max = 64, message = "Surname field size can be between 2 and 64")
    private String clientSurName;

    @Positive(message = "Age can't be lass then 0, unless you come from a counter-directional universe!")
    private Integer age;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DetailsCreateDto that = (DetailsCreateDto) o;
        return Objects.equals(clientName, that.clientName) &&
               Objects.equals(clientSurName, that.clientSurName) &&
               Objects.equals(age, that.age);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientName, clientSurName, age);
    }

    @Override
    public String toString() {
        return "DetailsCreateDto{" +
                "clientName='" + clientName + '\'' +
                ", clientSurName='" + clientSurName + '\'' +
                ", age=" + age +
                '}';
    }
}
