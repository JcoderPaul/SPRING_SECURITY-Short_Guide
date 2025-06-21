package me.oldboy.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.dto.auth_dto.ClientAuthRequest;
import me.oldboy.exception.ClientServiceException;
import me.oldboy.filters.request_wrapper.CachedBodyHttpServletRequest;
import me.oldboy.models.client.Client;
import me.oldboy.services.ClientService;
import me.oldboy.validation.ValidatorFilterDto;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

/* До UsernamePasswordAuthenticationFilter.class */
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class UserPassValidatorAndAfterLogoutFilter extends OncePerRequestFilter {

	private UserDetailsService userDetailsService;
	private ClientService clientService;

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		CachedBodyHttpServletRequest cachedBodyHttpServletRequest = new CachedBodyHttpServletRequest(request);

		if(cachedBodyHttpServletRequest.getServletPath().matches("/api/loginClient")){

			if (cachedBodyHttpServletRequest.getInputStream().readAllBytes().length == 0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().write("Incorrect auth request - empty or null body!");
				return;
			}

			ClientAuthRequest clientAuthRequest =
					new ObjectMapper().readValue(cachedBodyHttpServletRequest.getInputStream(), ClientAuthRequest.class);

			if(clientAuthRequest != null){

				try {
					ValidatorFilterDto.getInstance().isValidData(clientAuthRequest);
				} catch (ConstraintViolationException exception) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().write(exception.getMessage());
					return;
				}

				try {
					Optional<Client> mayByClient = clientService.getClientIfAuthDataCorrect(clientAuthRequest);

					if(mayByClient.isPresent()) {
						UserDetails client = userDetailsService.loadUserByUsername(mayByClient.get().getEmail());
						String username = client.getUsername();
						Collection<? extends GrantedAuthority> authorities = client.getAuthorities();

						Authentication auth = new UsernamePasswordAuthenticationToken(username, null, authorities);
						SecurityContextHolder.getContext().setAuthentication(auth);
					}
				} catch (ClientServiceException exception){
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					response.getWriter().write(exception.getMessage());
					return;
				}
			}
		}
		chain.doFilter(cachedBodyHttpServletRequest, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return request.getServletPath().equals("/api/regClient");
	}
}