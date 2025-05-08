package me.oldboy.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "client_details")
public class Details {

    public Details(String clientName, String clientSurName, Integer age) {
        this.clientName = clientName;
        this.clientSurName = clientSurName;
        this.age = age;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "client_surname")
    private String clientSurName;

    @Column(name = "client_age")
    private Integer age;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "clients_id")
    private Client client;
}
