package me.oldboy.models.client;

import jakarta.persistence.*;
import lombok.*;
import me.oldboy.models.client.Client;

import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "authorities")
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long Id;

    @Column(name = "authority_name")
    private String authName;

    @ManyToMany(fetch = FetchType.LAZY,
                cascade = CascadeType.PERSIST)
    @JoinTable(name = "clients_authorities",
               joinColumns = @JoinColumn(name = "authority_id"),
               inverseJoinColumns = @JoinColumn(name = "client_id"))
    @Builder.Default
    private List<Client> listOfClients = new ArrayList<>();

    public void addClientToList(Client client){
        listOfClients.add(client);
    }
}
