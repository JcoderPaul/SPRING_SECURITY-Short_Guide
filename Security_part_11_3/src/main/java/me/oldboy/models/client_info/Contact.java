package me.oldboy.models.client_info;

import jakarta.persistence.*;
import lombok.*;
import me.oldboy.models.client.Client;

import java.util.Objects;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "client_contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "client_id")
    private Client client;
    private String city;

    @Column(name = "postal_code")
    private Integer postalCode;
    private String address;
    private Integer building;
    private Integer apartment;

    @Column(name = "home_phone")
    private String homePhone;

    @Column(name = "mobile_phone")
    private String mobilePhone;

    @Override
    public String toString() {
        return " City: " + city +
               " Postal code: " + postalCode +
               " Address: " + address +
               " Building: " + building +
               " Apartment: " + apartment +
               " Home phone: " + homePhone +
               " Mobile phone: " + mobilePhone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return Objects.equals(id, contact.id) &&
                Objects.equals(city, contact.city) &&
                Objects.equals(postalCode, contact.postalCode) &&
                Objects.equals(address, contact.address) &&
                Objects.equals(building, contact.building) &&
                Objects.equals(apartment, contact.apartment) &&
                Objects.equals(homePhone, contact.homePhone) &&
                Objects.equals(mobilePhone, contact.mobilePhone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, city, postalCode, address, building, apartment, homePhone, mobilePhone);
    }
}
