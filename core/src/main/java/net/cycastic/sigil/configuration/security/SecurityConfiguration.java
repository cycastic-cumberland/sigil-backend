package net.cycastic.sigil.configuration.security;

import lombok.RequiredArgsConstructor;
import net.cycastic.sigil.configuration.application.CrossOriginConfiguration;
import net.cycastic.sigil.controller.filter.JwtAuthenticationFilter;
import net.cycastic.sigil.controller.filter.PerfFilter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.security.Security;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {
    private static final Logger logger;

    static {
        logger = LoggerFactory.getLogger(SecurityConfiguration.class);
        Security.addProvider(new BouncyCastleProvider());
    }

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final PerfFilter perfFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        // any AccessDeniedException (normally 403) → log and return 500
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            logger.error("Access denied: {}", accessDeniedException.getMessage(), accessDeniedException);
                            response.sendError(HttpStatus.FORBIDDEN.value());
                        })
                        // any AuthenticationException (normally 401) → log and return 500
                        .authenticationEntryPoint((request, response, authException) -> {
                            logger.error("Authentication failed: {}", authException.getMessage(), authException);
                            response.sendError(HttpStatus.UNAUTHORIZED.value());
                        })
                )
                .cors().and().csrf().disable()
                .authorizeHttpRequests()
                .requestMatchers(new RequestMatcher[]{
                        new AntPathRequestMatcher("/api/auth/**"),
                        new AntPathRequestMatcher("/api/public/**"),
                        new AntPathRequestMatcher("/api/storage/**"),
                        new AntPathRequestMatcher("/.well-known/**"),
                        new AntPathRequestMatcher("/v2/api-docs"),
                        new AntPathRequestMatcher("/v3/api-docs"),
                        new AntPathRequestMatcher("/v3/api-docs/**"),
                        new AntPathRequestMatcher("/configuration/ui"),
                        new AntPathRequestMatcher("/swagger-resources/**"),
                        new AntPathRequestMatcher("/configuration/security"),
                        new AntPathRequestMatcher("/swagger-ui/**"),
                        new AntPathRequestMatcher("/webjars/**"),
                        new AntPathRequestMatcher("/actuator/**"),
                })
                .permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(perfFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthFilter, PerfFilter.class);
        return http.build();
    }

    private static CorsConfiguration getDefaultCorsConfiguration(){
        var config = new CorsConfiguration();
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        return config;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CrossOriginConfiguration crossOriginConfiguration) {
        var allowedOrigins = crossOriginConfiguration.getAllowOrigins();
        return request -> {
            var config = getDefaultCorsConfiguration();
            if (allowedOrigins != null){
                for (var origin : allowedOrigins) {
                    config.addAllowedOrigin(origin);
                }
            }
            return config;
        };
    }
}
