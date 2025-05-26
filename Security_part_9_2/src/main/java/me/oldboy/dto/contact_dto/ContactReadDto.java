package me.oldboy.dto.contact_dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ContactReadDto {
    private String city;
    private Integer postalCode;
    private String address;
    private Integer building;
    private Integer apartment;
    private String homePhone;
    private String mobilePhone;
}
