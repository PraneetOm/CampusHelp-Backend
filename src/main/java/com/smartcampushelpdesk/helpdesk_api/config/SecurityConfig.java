
package com.smartcampushelpdesk.helpdesk_api.config;

import com.smartcampushelpdesk.helpdesk_api.entities.Role;
import com.smartcampushelpdesk.helpdesk_api.entities.User;
import com.smartcampushelpdesk.helpdesk_api.repositories.RoleRepository;
import com.smartcampushelpdesk.helpdesk_api.repositories.UserRepository;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.UUID;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final String ISSUER =
        "https://txwaugzwgdobuqmtzgug.supabase.co/auth/v1";

    private static final String JWKS_URL =
        "https://txwaugzwgdobuqmtzgug.supabase.co/auth/v1/.well-known/jwks.json";


    // 1. CORS CONFIGURATION

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
            List.of("http://localhost:5173")
        );

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
            List.of("Authorization", "Content-Type", "Accept")
        );

        // We use Bearer tokens, not cookie-based authentication.
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


    // 2. SUPABASE JWT DECODER

    @Bean
    public JwtDecoder jwtDecoder() {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withJwkSetUri(JWKS_URL)
            .jwsAlgorithm(SignatureAlgorithm.ES256)
            .build();

        decoder.setJwtValidator(
            JwtValidators.createDefaultWithIssuer(ISSUER)
        );

        return decoder;
    }


    // 3. DATABASE-BACKED ROLE RESOLUTION

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(
            UserRepository userRepository,
            RoleRepository roleRepository) {

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            UUID userId;

            try {
                userId = UUID.fromString(jwt.getSubject());
            } catch (Exception ex) {
                throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token"),
                    "Invalid user ID in Supabase token"
                );
            }

            // Find the campus user using the verified JWT subject.
            User user = userRepository.findById(userId)
                .orElseThrow(() ->
                    new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_token"),
                        "Campus user profile not found"
                    )
                );

            // Only active campus users can access the application.
            if (user.getStatus() == null ||
                !user.getStatus().equalsIgnoreCase("ACTIVE")) {

                throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token"),
                    "Campus account is inactive"
                );
            }

            if (user.getRoleId() == null) {
                throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token"),
                    "User role is missing"
                );
            }

            Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() ->
                    new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_token"),
                        "User role not found"
                    )
                );

            if (role.getName() == null ||
                role.getName().isBlank()) {

                throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_token"),
                    "Invalid user role"
                );
            }

            String authority =
                "ROLE_" + role.getName().trim().toUpperCase();

            return List.of(
                new SimpleGrantedAuthority(authority)
            );
        });

        return converter;
    }


    // 4. SECURITY FILTER CHAIN

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtDecoder jwtDecoder,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            CorsConfigurationSource corsConfigurationSource)
            throws Exception {

        http
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource)
            )

            .csrf(AbstractHttpConfigurer::disable)

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    org.springframework.http.HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()

                .requestMatchers("/api/public/**").permitAll()

                .anyRequest().authenticated()
            )

            // THIS IS THE IMPORTANT PART:
            // Process and verify Supabase Bearer tokens.
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(
                        jwtAuthenticationConverter
                    )
                )
            )

            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}