package me.oldboy.config.securiry_details;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import me.oldboy.models.Client;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
public class SecurityClientDetails implements UserDetails {

	private Client client;

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		List<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority(client.getRole().name()));
		return authorities;
	}
    /* Два следующих метода объясняют системе безопасности откуда брать имя/пароль для аутентификации */
	@Override
	public String getUsername() {
		return client.getEmail();
	}

	@Override
	public String getPassword() {
		return client.getPass();
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public Client getClient(){
		return this.client;
	}
}