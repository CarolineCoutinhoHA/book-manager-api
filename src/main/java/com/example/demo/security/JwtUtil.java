package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * UTILITÁRIO JWT - GERAÇÃO E VALIDAÇÃO DE TOKENS
 * 
 * Esta classe é responsável por:
 * 1. GERAR tokens JWT quando o usuário faz login
 * 2. VALIDAR tokens JWT em cada requisição
 * 3. EXTRAIR informações do token (como username)
 * 
 * JWT (JSON Web Token) é um padrão para transmitir informações de forma segura.
 * Estrutura: HEADER.PAYLOAD.SIGNATURE
 * - HEADER: Tipo do token e algoritmo de assinatura
 * - PAYLOAD: Dados do usuário (claims)
 * - SIGNATURE: Assinatura para verificar integridade
 */
@Component // Marca como componente do Spring (pode ser injetado)
public class JwtUtil {

    // CHAVE SECRETA: Usada para assinar e verificar tokens
    // Keys.secretKeyFor() gera uma chave segura automaticamente
    // ⚠️ EM PRODUÇÃO: Use uma chave fixa do application.properties
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // TEMPO DE EXPIRAÇÃO: 24 horas em milissegundos
    // 86400000 ms = 24 * 60 * 60 * 1000 = 24 horas
    private final int expiration = 86400000;

    /**
     * GERAR TOKEN JWT
     * 
     * Chamado quando o usuário faz login com sucesso.
     * Cria um token contendo:
     * - Subject (sub): Username do usuário
     * - Issued At (iat): Quando o token foi criado
     * - Expiration (exp): Quando o token expira
     * - Signature: Assinatura para verificar integridade
     * 
     * @param username Nome do usuário logado
     * @return Token JWT como String
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)                                           // Define o "dono" do token
                .setIssuedAt(new Date())                                       // Data de criação
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // Data de expiração
                .signWith(key)                                                 // Assina com nossa chave secreta
                .compact();                                                    // Converte para String
    }

    /**
     * EXTRAIR USERNAME DO TOKEN
     * 
     * Decodifica o token e extrai o username (subject).
     * Usado para identificar qual usuário está fazendo a requisição.
     * 
     * @param token Token JWT recebido na requisição
     * @return Username do usuário
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)        // Usa nossa chave para verificar a assinatura
                .build()
                .parseClaimsJws(token)     // Decodifica e valida o token
                .getBody()                 // Pega o payload (claims)
                .getSubject();             // Extrai o subject (username)
    }

    /**
     * VALIDAR TOKEN JWT
     * 
     * Verifica se o token é válido:
     * - Assinatura correta (não foi alterado)
     * - Não expirou
     * - Formato correto
     * 
     * @param token Token JWT a ser validado
     * @return true se válido, false se inválido
     */
    public boolean isTokenValid(String token) {
        try {
            // Tenta decodificar o token
            // Se conseguir, o token é válido
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            // Qualquer erro = token inválido
            // Possíveis erros:
            // - Token expirado
            // - Assinatura inválida
            // - Formato incorreto
            return false;
        }
    }
}