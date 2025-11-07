package com.example.demo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * FILTRO DE AUTENTICAÇÃO JWT
 * 
 * Este filtro é executado em TODA requisição HTTP antes de chegar no controller.
 * Sua função é:
 * 1. Verificar se existe um token JWT no header Authorization
 * 2. Validar o token se existir
 * 3. Extrair o usuário do token
 * 4. Configurar o Spring Security para reconhecer o usuário como autenticado
 * 
 * FLUXO DE EXECUÇÃO:
 * Requisição HTTP → JwtAuthenticationFilter → Controller
 * 
 * OncePerRequestFilter garante que o filtro execute apenas uma vez por requisição.
 */
@Component // Marca como componente do Spring
@RequiredArgsConstructor // Lombok: gera construtor com campos final
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Injeta o utilitário JWT para validar tokens
    private final JwtUtil jwtUtil;

    /**
     * MÉTODO PRINCIPAL DO FILTRO
     * 
     * Executado automaticamente pelo Spring em cada requisição HTTP.
     * 
     * @param request Requisição HTTP recebida
     * @param response Resposta HTTP (não usamos aqui)
     * @param filterChain Cadeia de filtros (para continuar o processamento)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 1. EXTRAIR O HEADER AUTHORIZATION
        // Formato esperado: "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        String authHeader = request.getHeader("Authorization");
        
        // 2. VERIFICAR SE O HEADER EXISTE E TEM O FORMATO CORRETO
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            
            // 3. EXTRAIR O TOKEN (remove "Bearer " do início)
            // "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." → "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
            String token = authHeader.substring(7);
            
            // 4. VALIDAR O TOKEN
            if (jwtUtil.isTokenValid(token)) {
                
                // 5. EXTRAIR O USERNAME DO TOKEN
                String username = jwtUtil.extractUsername(token);
                
                // 6. CRIAR OBJETO DE AUTENTICAÇÃO DO SPRING SECURITY
                // UsernamePasswordAuthenticationToken representa um usuário autenticado
                // Parâmetros:
                // - principal: username (quem é o usuário)
                // - credentials: null (não precisamos da senha aqui)
                // - authorities: new ArrayList<>() (permissões vazias por enquanto)
                UsernamePasswordAuthenticationToken auth = 
                    new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
                
                // 7. INFORMAR AO SPRING SECURITY QUE O USUÁRIO ESTÁ AUTENTICADO
                // SecurityContextHolder é onde o Spring Security armazena informações de autenticação
                // A partir daqui, o Spring Security reconhece que há um usuário logado
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            // Se o token for inválido, não fazemos nada (usuário permanece não autenticado)
        }
        // Se não houver header Authorization, não fazemos nada (requisição anônima)
        
        // 8. CONTINUAR A CADEIA DE FILTROS
        // Passa a requisição para o próximo filtro ou controller
        // IMPORTANTE: Sempre chamar filterChain.doFilter() para não quebrar o fluxo
        filterChain.doFilter(request, response);
    }
}