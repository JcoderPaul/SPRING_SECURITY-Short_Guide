package me.oldboy.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.dto.client_dto.ClientCreateDto;
import me.oldboy.dto.client_dto.ClientReadDto;
import me.oldboy.mapper.ClientMapper;
import me.oldboy.models.Client;
import me.oldboy.models.Role;
import me.oldboy.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
@AllArgsConstructor
@Transactional(readOnly = true)
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional
    public ClientReadDto saveClient(ClientCreateDto clientCreateDto){
        String encodedPass = passwordEncoder.encode(clientCreateDto.pass());

        Client toCreateClient = ClientMapper.INSTANCE.mapToClient(clientCreateDto);
        toCreateClient.setRole(Role.USER);
        toCreateClient.setPass(encodedPass);

        Client createdClient = clientRepository.save(toCreateClient);
        ClientReadDto toPrintClient = ClientMapper.INSTANCE.mapToClientReadDto(createdClient);

        return toPrintClient;
    }

    public List<ClientReadDto> findAll(){
        return clientRepository.findAll().stream()
                                         .map(client -> ClientMapper.INSTANCE.mapToClientReadDto(client))
                                         .collect(Collectors.toList());
    }

    public Optional<Client> findByEmail(String email){
        return clientRepository.findByEmail(email);
    }
 }
