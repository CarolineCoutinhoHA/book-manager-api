package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CONFIGURAÇÃO WEB - CORS (Cross-Origin Resource Sharing)
 * 
 * CORS é uma política de segurança dos navegadores que bloqueia requisições
 * entre diferentes origens (domínios, portas, protocolos).
 * 
 * PROBLEMA SEM CORS:
 * - Frontend em localhost:3000 (React/Vue/Angular)
 * - Backend em localhost:8080 (Spring Boot)
 * - Navegador bloqueia as requisições por serem "cross-origin"
 * 
 * SOLUÇÃO COM CORS:
 * - Configuramos o backend para aceitar requisições do frontend
 * - Especificamos quais origens, métodos e headers são permitidos
 * 
 * CONFIGURAÇÃO ATUAL:
 * - Permite requisições de localhost:3000 (frontend típico)
 * - Permite métodos GET, POST, PUT, DELETE
 * - Permite todos os headers
 */
@Configuration // Marca como classe de configuração do Spring
public class WebConfig implements WebMvcConfigurer {

    /**
     * CONFIGURAÇÃO DE CORS
     * 
     * Define as regras de Cross-Origin Resource Sharing.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            // APLICAR CORS PARA TODOS OS ENDPOINTS DA API
            .addMapping("/api/**")
            
            // ORIGENS PERMITIDAS
            // localhost:3000 é a porta padrão do React/Vue em desenvolvimento
            // ⚠️ EM PRODUÇÃO: Substitua por seu domínio real
            .allowedOrigins("http://localhost:3000")
            
            // MÉTODOS HTTP PERMITIDOS
            // Cobre todas as operações CRUD básicas
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            
            // HEADERS PERMITIDOS
            // "*" permite todos os headers (incluindo Authorization para JWT)
            .allowedHeaders("*");
    }
}