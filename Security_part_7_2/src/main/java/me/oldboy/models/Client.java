package me.oldboy.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "email")
    private String email;

    @Column(name = "pass")
    private String pass;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "details_id")
    private Details details;

    @ManyToMany(fetch = FetchType.EAGER,
                cascade = CascadeType.PERSIST)
    @JoinTable(name = "clients_authorities",
               joinColumns = @JoinColumn(name = "client_id"),
               inverseJoinColumns = @JoinColumn(name = "authority_id"))
    @Builder.Default
    private List<Auth> listOfAuth = new ArrayList<>();

    public void addAuthToList(Auth auth){
        listOfAuth.add(auth);
    }
}