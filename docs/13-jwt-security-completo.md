# JWT Security - Guia Completo e Detalhado

## 🎯 Visão Geral

JWT (JSON Web Token) é um padrão de autenticação stateless que permite autenticar usuários sem manter sessões no servidor. Este guia explica TUDO sobre a implementação JWT no projeto.

## 🏗️ Arquitetura JWT

### Como Funciona:
1. **Cliente** faz login com username/password
2. **Servidor** valida credenciais e gera JWT
3. **Cliente** recebe JWT e armazena (localStorage/cookie)
4. **Cliente** envia JWT no header Authorization em cada requisição
5. **Servidor** valida JWT e permite/nega acesso

### Vantagens:
- **Stateless**: Servidor não precisa armazenar sessões
- **Escalável**: Funciona em múltiplos servidores
- **Seguro**: Token assinado digitalmente
- **Flexível**: Pode carregar dados do usuário

## 📁 Estrutura de Arquivos

```
src/main/java/com/example/demo/
├── security/
│   ├── SecurityConfig.java      # Configuração principal
│   ├── JwtUtil.java            # Utilitários JWT
│   └── JwtAuthenticationFilter.java  # Filtro de validação
├── controller/
│   └── AuthController.java     # Endpoints de auth
├── model/
│   └── Usuario.java           # Entidade usuário
├── repository/
│   └── UsuarioRepository.java # Acesso a dados
└── service/
    └── AuthService.java       # Lógica de negócio
```

## 🔧 Implementação Passo a Passo

### 1. Dependências (build.gradle)

```gradle
dependencies {
    // Spring Security
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-impl:0.11.5'
    implementation 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    // Outras dependências...
}
```

### 2. Entidade Usuario

```java
@Entity
@Table(name = "usuario")
@Getter
@NoArgsConstructor
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false)
    private String password;  // Será criptografada com BCrypt
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;  // ADMIN, USER, etc.
    
    // Construtor para criação
    public Usuario(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }
}
```

### 3. JwtUtil - Coração do JWT

```java
@Component
public class JwtUtil {
    
    // CHAVE SECRETA - EM PRODUÇÃO, USE VARIÁVEL DE AMBIENTE
    private final String SECRET_KEY = "mySecretKey123456789012345678901234567890";
    
    // TEMPO DE EXPIRAÇÃO (24 horas)
    private final long JWT_EXPIRATION = 86400000;
    
    /**
     * GERAR TOKEN JWT
     * Chamado após login bem-sucedido
     */
    public String generateToken(String username) {
        return Jwts.builder()
            .setSubject(username)                    // Usuário
            .setIssuedAt(new Date())                // Data de criação
            .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))  // Expiração
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)  // Assinatura
            .compact();
    }
    
    /**
     * EXTRAIR USERNAME DO TOKEN
     * Usado pelo filtro para identificar o usuário
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * EXTRAIR DATA DE EXPIRAÇÃO
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * EXTRAIR QUALQUER CLAIM DO TOKEN
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * EXTRAIR TODOS OS CLAIMS
     * Decodifica o token JWT
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        } catch (Exception e) {
            throw new RuntimeException("Token JWT inválido", e);
        }
    }
    
    /**
     * VERIFICAR SE TOKEN EXPIROU
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    /**
     * VALIDAR TOKEN COMPLETO
     * Verifica se token é válido para o usuário
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = extractUsername(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }
}
```

### 4. JwtAuthenticationFilter - Interceptador

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, 
            HttpServletResponse response, 
            FilterChain filterChain) throws ServletException, IOException {
        
        // 1. EXTRAIR TOKEN DO HEADER
        String authorizationHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwt = null;
        
        // 2. VERIFICAR SE HEADER EXISTE E TEM FORMATO CORRETO
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);  // Remove "Bearer "
            
            try {
                // 3. EXTRAIR USERNAME DO TOKEN
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Token inválido - continua sem autenticar
                System.out.println("Erro ao extrair username do token: " + e.getMessage());
            }
        }
        
        // 4. VALIDAR TOKEN E AUTENTICAR USUÁRIO
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Validar token
            if (jwtUtil.validateToken(jwt, username)) {
                
                // 5. CRIAR AUTHENTICATION OBJECT
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        username, 
                        null, 
                        Collections.emptyList()  // Authorities/Roles
                    );
                
                // 6. DEFINIR DETALHES DA REQUISIÇÃO
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 7. AUTENTICAR NO SPRING SECURITY
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 8. CONTINUAR CADEIA DE FILTROS
        filterChain.doFilter(request, response);
    }
}
```

### 5. SecurityConfig - Configuração Principal

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // DESABILITAR CSRF (não precisamos com JWT)
            .csrf(csrf -> csrf.disable())
            
            // PERMITIR FRAMES (para H2 Console)
            .headers(headers -> headers.frameOptions().disable())
            
            // SESSÃO STATELESS (JWT não usa sessões)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // REGRAS DE AUTORIZAÇÃO
            .authorizeHttpRequests(auth -> auth
                // ENDPOINTS PÚBLICOS
                .requestMatchers(
                    "/api/auth/**",      // Login/Register
                    "/h2-console/**",    // Banco H2
                    "/swagger-ui/**",    // Swagger
                    "/v3/api-docs/**"    // API Docs
                ).permitAll()
                
                // TODOS OS OUTROS: AUTENTICAÇÃO OBRIGATÓRIA
                .anyRequest().authenticated()
            )
            
            // ADICIONAR FILTRO JWT ANTES DO PADRÃO
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 6. AuthController - Endpoints

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * REGISTRO DE USUÁRIO
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody UsuarioRequestDTO dto) {
        authService.register(dto);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Usuário registrado com sucesso");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * LOGIN - GERA TOKEN JWT
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequestDTO dto) {
        String token = authService.login(dto);
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        
        return ResponseEntity.ok(response);
    }
}
```

### 7. AuthService - Lógica de Negócio

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * REGISTRAR NOVO USUÁRIO
     */
    public void register(UsuarioRequestDTO dto) {
        // Verificar se username já existe
        if (usuarioRepository.findByUsername(dto.username()).isPresent()) {
            throw new BusinessException("Username já existe");
        }
        
        // Criptografar senha
        String encodedPassword = passwordEncoder.encode(dto.password());
        
        // Criar e salvar usuário
        Usuario usuario = new Usuario(dto.username(), encodedPassword, Role.USER);
        usuarioRepository.save(usuario);
    }
    
    /**
     * LOGIN - VALIDAR CREDENCIAIS E GERAR TOKEN
     */
    public String login(LoginRequestDTO dto) {
        // Buscar usuário
        Usuario usuario = usuarioRepository.findByUsername(dto.username())
            .orElseThrow(() -> new BusinessException("Credenciais inválidas"));
        
        // Verificar senha
        if (!passwordEncoder.matches(dto.password(), usuario.getPassword())) {
            throw new BusinessException("Credenciais inválidas");
        }
        
        // Gerar e retornar token
        return jwtUtil.generateToken(usuario.getUsername());
    }
}
```

## 🔒 Configuração de Segurança

### application.properties
```properties
# JWT Configuration
jwt.secret=mySecretKey123456789012345678901234567890
jwt.expiration=86400000

# Security
spring.security.user.name=admin
spring.security.user.password=admin123
```

## 🚨 Pontos Críticos de Segurança

### 1. Chave Secreta
```java
// ❌ NUNCA FAÇA ISSO EM PRODUÇÃO
private final String SECRET_KEY = "mySecretKey123";

// ✅ USE VARIÁVEL DE AMBIENTE
@Value("${jwt.secret}")
private String secretKey;
```

### 2. Tempo de Expiração
```java
// ✅ Configure adequadamente
private final long JWT_EXPIRATION = 86400000; // 24 horas
```

### 3. Validação de Token
```java
// ✅ SEMPRE valide token completamente
public Boolean validateToken(String token, String username) {
    final String tokenUsername = extractUsername(token);
    return (tokenUsername.equals(username) && !isTokenExpired(token));
}
```

## 🧪 Testando JWT

### 1. Registro
```bash
POST /api/auth/register
{
    "username": "admin",
    "password": "123456"
}
```

### 2. Login
```bash
POST /api/auth/login
{
    "username": "admin", 
    "password": "123456"
}

# Resposta:
{
    "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 3. Usar Token
```bash
GET /api/autores
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## 🐛 Debugging JWT

### Logs Úteis
```java
// No JwtAuthenticationFilter
System.out.println("Token recebido: " + jwt);
System.out.println("Username extraído: " + username);
System.out.println("Token válido: " + jwtUtil.validateToken(jwt, username));
```

### Decodificar Token Online
- Site: https://jwt.io/
- Cole seu token para ver o conteúdo

## ⚠️ Erros Comuns e Soluções

### 1. "Token JWT inválido"
**Causa**: Token malformado ou chave secreta errada
**Solução**: Verificar formato do token e chave secreta

### 2. "403 Forbidden"
**Causa**: Token ausente ou inválido
**Solução**: Fazer login novamente e usar token correto

### 3. "Token expirado"
**Causa**: Token passou do tempo de vida
**Solução**: Fazer login novamente

### 4. "Bearer não encontrado"
**Causa**: Header Authorization mal formatado
**Solução**: Usar formato "Bearer TOKEN"

## 🚀 Melhorias Avançadas

### 1. Refresh Token
```java
// Implementar token de renovação
public String refreshToken(String expiredToken) {
    String username = extractUsername(expiredToken);
    return generateToken(username);
}
```

### 2. Roles/Authorities
```java
// Adicionar roles no token
public String generateToken(String username, List<String> roles) {
    return Jwts.builder()
        .setSubject(username)
        .claim("roles", roles)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + JWT_EXPIRATION))
        .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
        .compact();
}
```

### 3. Blacklist de Tokens
```java
// Invalidar tokens específicos
@Service
public class TokenBlacklistService {
    private Set<String> blacklistedTokens = new HashSet<>();
    
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }
    
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }
}
```

## 📋 Checklist de Implementação

- [ ] Dependências JWT adicionadas
- [ ] Entidade Usuario criada
- [ ] JwtUtil implementado
- [ ] JwtAuthenticationFilter criado
- [ ] SecurityConfig configurado
- [ ] AuthController implementado
- [ ] AuthService implementado
- [ ] Endpoints de auth testados
- [ ] Endpoints protegidos testados
- [ ] Tratamento de erros implementado
- [ ] Logs de debug adicionados

## 🎯 Resumo Final

JWT é um sistema de autenticação poderoso que:
1. **Gera tokens** após login bem-sucedido
2. **Valida tokens** em cada requisição
3. **Protege endpoints** automaticamente
4. **Não mantém estado** no servidor
5. **Escala facilmente** para múltiplos servidores

A implementação correta garante segurança e performance para sua API!