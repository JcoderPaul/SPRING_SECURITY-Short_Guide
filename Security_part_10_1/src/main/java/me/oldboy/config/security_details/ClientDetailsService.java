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
/*
Уже в который раз происходит интересный момент с этой аннотацией при переходе со Spring Boot, на nonBoot (plain) Spring
приложение - данная аннотация, не нужна в этом классе если у нас Spring Boot и, обязательна если у нас обычное Spring
приложение иначе мы поймаем исключение вида:
"Error creating bean with name 'clientDetailsService': Unsatisfied dependency expressed through field 'clientRepository':
 No qualifying bean of type 'me.oldboy.repository.ClientRepository' available: expected at least 1 bean which qualifies as
 autowire candidate. Dependency annotations: {@org.springframework.beans.factory.annotation.Autowired(required=true)}"

Хотя вроде бы все необходимые аннотации над нужными классами стоят и мы ожидаем успешного запуска приложения.
*/
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
