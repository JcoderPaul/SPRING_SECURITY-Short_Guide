package me.oldboy.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.constants.SecurityConstants;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/* До UsernamePasswordAuthenticationFilter.class */
@Slf4j
public class JwtTokenValidatorAndBeforeFilter extends OncePerRequestFilter {

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		String jwtHeader = request.getHeader(SecurityConstants.JWT_HEADER);

		if(SecurityContextHolder.getContext().getAuthentication() == null) {
			if (jwtHeader != null) {
				String jwt = jwtHeader.substring("Bearer ".length());
				try {
					SecretKey key = Keys.hmacShaKeyFor(SecurityConstants.JWT_KEY.getBytes(StandardCharsets.UTF_8));

					Claims claims = Jwts.parser()
							.verifyWith(key)
							.build()
							.parseSignedClaims(jwt)
							.getPayload();

					String username = (String) claims.get("username");
					String authorities = (String) claims.get("authorities");

					Authentication auth = new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));
					SecurityContextHolder.getContext().setAuthentication(auth);
				} catch (Exception e) {
					throw new BadCredentialsException("Invalid Token received!");
				}
			}
		}
		chain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if(request.getServletPath().equals("/regClient") || request.getServletPath().equals("/loginClient")) {
			return true;
		} else {
			return false;
		}
	}
}