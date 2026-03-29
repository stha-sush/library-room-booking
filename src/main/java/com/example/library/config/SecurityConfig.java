package com.example.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.library.security.JwtAuthFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthFilter jwtAuthFilter;

	public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
		this.jwtAuthFilter = jwtAuthFilter;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http.csrf(csrf -> csrf.disable())
				.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.authorizeHttpRequests(auth -> auth
						// Public pages
						.requestMatchers("/", "/login", "/error", "/favicon.ico").permitAll()

						// Static
						.requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

						// Public APIs
						.requestMatchers("/api/auth/**").permitAll().requestMatchers("/api/availability/**").permitAll()
						.requestMatchers("/api/buildings").permitAll()

						// Role pages
						.requestMatchers("/admin/**").hasRole("ADMIN").requestMatchers("/student/**").hasRole("STUDENT")

						// Role APIs
						.requestMatchers("/api/admin/**").hasRole("ADMIN").requestMatchers("/api/bookings/**")
						.hasRole("STUDENT")

						.anyRequest().authenticated())

				// disable browser Basic Auth popup
				.httpBasic(basic -> basic.disable())

				// disable default Spring login form
				.formLogin(form -> form.disable());

		// JWT filter
		http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

		// if H2 console is used 
		http.headers(h -> h.frameOptions(f -> f.sameOrigin()));

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
		return cfg.getAuthenticationManager();
	}
}