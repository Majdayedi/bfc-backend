package bfc.bfc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/team-members", "/api/team-members/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/articles", "/api/articles/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/projects", "/api/projects/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/services", "/api/services/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/history-events", "/api/history-events/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/representatives", "/api/representatives/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/courses", "/api/courses/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/partners", "/api/partners/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/clients", "/api/clients/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/enrollments/list").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/enrollments/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/enrollments/update/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/enrollments/delete/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/contact/list").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/contact/service-options", "/api/contact/service-options/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/contact/**").permitAll()
                .requestMatchers(HttpMethod.PUT, "/api/contact/update/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/contact/delete/**").permitAll()
                .requestMatchers("/uploads/**").permitAll()
                .requestMatchers("/error").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
