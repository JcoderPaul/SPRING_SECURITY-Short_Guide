package me.oldboy.config.security_details;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.exception.EmailNotFoundException;
import me.oldboy.models.client.Client;
import me.oldboy.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@NoArgsConstructor
@EnableJpaRepositories(basePackages = "me.oldboy.repository")
public class ClientDetailsService implements UserDetailsService {

	@Autowired
	private ClientRepository clientRepository;

	/* Извлекаем из БД клиентов (user-ов) по имени */
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Optional<Client> mayBeClient = clientRepository.findByEmail(username);
		if (mayBeClient.isEmpty()) {
			throw new EmailNotFoundException("User email : " + username + " not found!");
		}
		return new SecurityClientDetails(mayBeClient.get());
	}
}
