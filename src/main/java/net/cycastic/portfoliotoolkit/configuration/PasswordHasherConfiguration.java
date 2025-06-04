package net.cycastic.portfoliotoolkit.configuration;

import net.cycastic.portfoliotoolkit.service.PasswordHasher;
import net.cycastic.portfoliotoolkit.service.Pbkdf2PasswordHasher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PasswordHasherConfiguration {
    @Bean
    public PasswordHasher passwordHasher(){
        return new Pbkdf2PasswordHasher();
    }
}
