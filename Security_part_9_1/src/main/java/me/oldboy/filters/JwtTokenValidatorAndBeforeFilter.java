package me.oldboy.filters;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.oldboy.constants.SecurityConstants;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;

/* До UsernamePasswordAuthenticationFilter.class */
@Slf4j
@RequiredArgsConstructor
public class JwtTokenValidatorAndBeforeFilter extends OncePerRequestFilter {

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		log.info("-- 2 - Start JwtTokenValidator or BeforeFilter");

		String jwt = request.getHeader(SecurityConstants.JWT_HEADER);

		if (jwt != null) {
			try {
				SecretKey key = Keys.hmacShaKeyFor(SecurityConstants.JWT_KEY.getBytes(StandardCharsets.UTF_8));

				Claims claims = Jwts.parser()
						.verifyWith(key)
						.build()
						.parseSignedClaims(jwt)
						.getPayload();

				String principal = claims.getSubject();
				String authorities = (String) claims.get("authorities");

				Authentication auth = new UsernamePasswordAuthenticationToken(principal,null,
						AuthorityUtils.commaSeparatedStringToAuthorityList(authorities));

				SecurityContextHolder.getContext().setAuthentication(auth);

			}catch (Exception e) {
				throw new BadCredentialsException("Invalid Token received!");
			}
		}
		chain.doFilter(request, response);
		log.info("-- 2 - Finish JwtTokenValidator or BeforeFilter");
	}
}