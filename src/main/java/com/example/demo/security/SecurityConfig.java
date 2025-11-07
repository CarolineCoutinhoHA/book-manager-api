package com.example.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * CONFIGURAÇÃO DE SEGURANÇA - SPRING SECURITY + JWT
 * 
 * Esta classe é o CORAÇÃO da segurança da aplicação.
 * Ela define:
 * 1. Quais endpoints são públicos (não precisam de autenticação)
 * 2. Quais endpoints são protegidos (precisam de JWT válido)
 * 3. Como as senhas são criptografadas
 * 4. Como o JWT é validado em cada requisição
 */
@Configuration // Marca como classe de configuração do Spring
@EnableWebSecurity // Habilita a segurança web do Spring Security
@RequiredArgsConstructor // Lombok: gera construtor com campos final
public class SecurityConfig {

    // Injeta o filtro JWT que criamos (será executado antes de cada requisição)
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * CONFIGURAÇÃO PRINCIPAL DE SEGURANÇA
     * 
     * Este método define TODAS as regras de segurança da aplicação:
     * - Endpoints públicos vs protegidos
     * - Tipo de autenticação (JWT stateless)
     * - Filtros personalizados
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. DESABILITA CSRF: Não precisamos pois usamos JWT (stateless)
            .csrf(csrf -> csrf.disable())
            
            // 2. PERMITE FRAMES: Necessário para o H2 Console funcionar
            .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
            
            // 3. SESSÃO STATELESS: Não mantém sessão no servidor (JWT é stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 4. REGRAS DE AUTORIZAÇÃO: Define quais endpoints são públicos/protegidos
            .authorizeHttpRequests(auth -> auth
                // ENDPOINTS PÚBLICOS (não precisam de autenticação):
                .requestMatchers(
                    "/api/auth/**",      // Login e registro
                    "/h2-console/**",    // Console do banco H2
                    "/swagger-ui/**",    // Interface do Swagger
                    "/swagger-ui.html",  // Página principal do Swagger
                    "/v3/api-docs/**",   // Documentação da API
                    "/v3/api-docs",      // Endpoint base da documentação
                    "/v3/api-docs-manual", // Documentação manual
                    "/swagger-resources/**", // Recursos do Swagger
                    "/webjars/**"        // Recursos estáticos do Swagger
                ).permitAll()
                
                // TODOS OS OUTROS ENDPOINTS: Precisam de autenticação JWT
                .anyRequest().authenticated()
            )
            
            // 5. ADICIONA NOSSO FILTRO JWT: Executa ANTES do filtro padrão do Spring
            // Isso garante que o JWT seja validado antes de qualquer outra verificação
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * ENCODER DE SENHAS - BCRYPT
     * 
     * BCrypt é um algoritmo de hash seguro para senhas.
     * Características:
     * - Adiciona "salt" automático (previne rainbow tables)
     * - Configurável (pode ajustar a complexidade)
     * - Padrão da indústria para hash de senhas
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}