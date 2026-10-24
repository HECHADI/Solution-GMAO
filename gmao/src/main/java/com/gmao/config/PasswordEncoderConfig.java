package com.gmao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * ⚠️ Nécessite la dépendance {@code spring-security-crypto} (légère, n'implique
 * pas d'activer Spring Security complet). Si ton pom.xml/build.gradle n'a pas
 * déjà {@code spring-boot-starter-security} ou {@code spring-security-crypto},
 * ajoute-le.
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
