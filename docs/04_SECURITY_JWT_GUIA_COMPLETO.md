# 🔐 SECURITY & JWT - GUIA COMPLETO HACKATHON

## 🎯 O QUE É JWT?

**JWT (JSON Web Token)** é um padrão para transmitir informações de forma segura entre cliente e servidor.

### Estrutura do JWT
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTY0MjQyNDQwMCwiZXhwIjoxNjQyNTEwODAwfQ.signature

HEADER.PAYLOAD.SIGNATURE
```

### Vantagens
- ✅ **Stateless** - Servidor não precisa armazenar sessões
- ✅ **Seguro** - Assinado digitalmente
- ✅ **Portável** - Funciona entre diferentes domínios
- ✅ **Escalável** - Não sobrecarrega banco de dados

---

## 🏗️ ARQUITETURA DE SEGURANÇA

### Fluxo Completo
```
1. Cliente faz login → POST /api/auth/login
2. Servidor valida credenciais
3. Servidor gera JWT e retorna
4. Cliente armazena JWT (localStorage/sessionStorage)
5. Cliente envia JWT em cada requisição → Header: Authorization: Bearer {token}
6. Servidor valida JWT em cada requisição
7. Se válido, processa requisição
8. Se inválido, retorna 401 Unauthorized
```

### Componentes da Segurança
```
SecurityConfig → Configuração geral de segurança
JwtUtil → Geração e validação de tokens
JwtAuthenticationFilter → Intercepta todas as requisições
AuthController → Endpoints de login/registro
```

---

## 🔧 CONFIGURAÇÃO SPRING SECURITY

### SecurityConfig Explicado
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 1. DESABILITA CSRF (não precisamos com JWT)
            .csrf(csrf -> csrf.disable())
            
            // 2. SESSÃO STATELESS (não mantém estado no servidor)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 3. DEFINE ENDPOINTS PÚBLICOS E PROTEGIDOS
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()  // Público
                .anyRequest().authenticated()                 // Protegido
            )
            
            // 4. ADICIONA FILTRO JWT
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### Endpoints Públicos vs Protegidos
```java
// PÚBLICOS (não precisam de JWT)
/api/auth/register  → Criar conta
/api/auth/login     → Fazer login
/h2-console/**      → Console do banco
/swagger-ui/**      → Documentação

// PROTEGIDOS (precisam de JWT)
/api/autores/**     → CRUD de autores
/api/livros/**      → CRUD de livros
/api/auth/me        → Verificar usuário atual
```

---

## 🎫 GERANDO E VALIDANDO JWT

### JwtUtil - Geração de Token
```java
public String generateToken(String username) {
    return Jwts.builder()
        .setSubject(username)                    // Quem é o usuário
        .setIssuedAt(new Date())                // Quando foi criado
        .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
        .signWith(secretKey)                    // Assina com chave secreta
        .compact();                             // Converte para string
}
```

### JwtUtil - Validação de Token
```java
public boolean isTokenValid(String token) {
    try {
        Jwts.parserBuilder()
            .setSigningKey(secretKey)           // Usa mesma chave para validar
            .build()
            .parseClaimsJws(token);             // Decodifica e valida
        return true;
    } catch (JwtException e) {
        return false;                           // Token inválido/expirado
    }
}
```

### Extraindo Informações do Token
```java
public String extractUsername(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(secretKey)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();                          // Retorna o username
}
```

---

## 🛡️ FILTRO DE AUTENTICAÇÃO

### JwtAuthenticationFilter Explicado
```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                               HttpServletResponse response, 
                               FilterChain filterChain) {
    
    // 1. PEGA O HEADER AUTHORIZATION
    String authHeader = request.getHeader("Authorization");
    
    // 2. VERIFICA SE TEM "Bearer " NO INÍCIO
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        
        // 3. EXTRAI O TOKEN (remove "Bearer ")
        String token = authHeader.substring(7);
        
        // 4. VALIDA O TOKEN
        if (jwtUtil.isTokenValid(token)) {
            
            // 5. EXTRAI USERNAME DO TOKEN
            String username = jwtUtil.extractUsername(token);
            
            // 6. CRIA AUTENTICAÇÃO PARA O SPRING SECURITY
            UsernamePasswordAuthenticationToken auth = 
                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());
            
            // 7. INFORMA AO SPRING QUE USUÁRIO ESTÁ AUTENTICADO
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
    }
    
    // 8. CONTINUA A CADEIA DE FILTROS
    filterChain.doFilter(request, response);
}
```

---

## 🔑 ENDPOINTS DE AUTENTICAÇÃO

### Registro de Usuário
```java
@PostMapping("/register")
public ResponseEntity<AuthResponse> register(@RequestBody LoginRequest request) {
    // 1. CRIPTOGRAFA A SENHA
    String hashedPassword = passwordEncoder.encode(request.password());
    
    // 2. CRIA USUÁRIO NO BANCO
    Usuario usuario = new Usuario(request.username(), hashedPassword);
    usuarioRepository.save(usuario);
    
    // 3. GERA TOKEN JWT
    String token = jwtUtil.generateToken(usuario.getUsername());
    
    // 4. RETORNA TOKEN
    return ResponseEntity.ok(new AuthResponse(token, usuario.getUsername()));
}
```

### Login de Usuário
```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
    // 1. BUSCA USUÁRIO NO BANCO
    Usuario usuario = usuarioRepository.findByUsername(request.username())
        .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    
    // 2. VERIFICA SENHA (NUNCA COMPARE SENHAS COM EQUALS!)
    if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
        throw new RuntimeException("Senha inválida");
    }
    
    // 3. GERA TOKEN JWT
    String token = jwtUtil.generateToken(usuario.getUsername());
    
    // 4. RETORNA TOKEN
    return ResponseEntity.ok(new AuthResponse(token, usuario.getUsername()));
}
```

---

## 🔐 CRIPTOGRAFIA DE SENHAS

### BCrypt - Por que usar?
```java
// ❌ NUNCA FAÇA ISSO (senha em texto plano)
usuario.setPassword("123456");

// ✅ SEMPRE FAÇA ISSO (senha criptografada)
String hashedPassword = passwordEncoder.encode("123456");
usuario.setPassword(hashedPassword);
```

### Como BCrypt Funciona
```
Senha: "123456"
Salt: "$2a$10$N9qo8uLOickgx2ZMRZoMye"
Hash: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIX0T7EmcuqDADfVF9wtcllAjuVqZzaW"

Características:
- Salt automático (previne rainbow tables)
- Configurável (pode ajustar complexidade)
- Sempre gera hash diferente para mesma senha
- Verificação com matches() em vez de equals()
```

---

## 🌐 TESTANDO AUTENTICAÇÃO

### 1. Criar Conta
```http
POST http://localhost:8080/api/auth/register
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}

# Resposta:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin"
}
```

### 2. Fazer Login
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

### 3. Usar Token em Requisições
```http
GET http://localhost:8080/api/autores
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 4. Verificar Token Atual
```http
GET http://localhost:8080/api/auth/me
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# Resposta:
"Usuário logado: admin"
```

---

## 🚨 ERROS COMUNS E SOLUÇÕES

### 401 Unauthorized
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "message": "Full authentication is required"
}
```

**Causas:**
- Token ausente no header
- Token malformado
- Token expirado
- Chave de assinatura incorreta

**Soluções:**
```http
# ❌ Sem Authorization header
GET /api/autores

# ✅ Com Authorization header correto
GET /api/autores
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# ❌ Formato incorreto
Authorization: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# ✅ Formato correto (com "Bearer ")
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 403 Forbidden
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 403,
  "message": "Access Denied"
}
```

**Causas:**
- Token válido mas sem permissão para o recurso
- CORS não configurado
- Endpoint não liberado no SecurityConfig

---

## 🔧 CONFIGURAÇÕES AVANÇADAS

### Múltiplas Roles
```java
@Entity
public class Usuario {
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;
}

public enum Role {
    USER, ADMIN, MODERATOR
}
```

### Autorização por Role
```java
@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/autores/{id}")
public void deletar(@PathVariable Long id) {
    // Só admins podem deletar
}

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@GetMapping("/autores")
public List<AutorResponseDTO> listar() {
    // Users e admins podem listar
}
```

### JWT com Claims Customizados
```java
public String generateToken(Usuario usuario) {
    return Jwts.builder()
        .setSubject(usuario.getUsername())
        .claim("role", usuario.getRole().name())        // Role do usuário
        .claim("userId", usuario.getId())               // ID do usuário
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(key)
        .compact();
}
```

---

## 🌍 CORS - CONECTANDO COM FRONTEND

### Configuração CORS
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")    // React/Vue/Angular
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*")                        // Inclui Authorization
            .allowCredentials(true);                    // Permite cookies
    }
}
```

### Frontend - Armazenando Token
```javascript
// Login e armazenar token
const login = async (username, password) => {
    const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ username, password })
    });
    
    const data = await response.json();
    localStorage.setItem('token', data.token);  // Armazena token
};

// Usar token em requisições
const fetchAutores = async () => {
    const token = localStorage.getItem('token');
    
    const response = await fetch('/api/autores', {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
        }
    });
    
    return response.json();
};
```

---

## 💡 BOAS PRÁTICAS HACKATHON

### 1. Segurança Básica
```java
// ✅ Sempre criptografar senhas
passwordEncoder.encode(password)

// ✅ Validar entrada do usuário
@Valid @RequestBody LoginRequest request

// ✅ Usar HTTPS em produção (não no hackathon)
// ✅ Configurar CORS corretamente
```

### 2. Gerenciamento de Token
```javascript
// ✅ Verificar se token existe antes de usar
const token = localStorage.getItem('token');
if (!token) {
    // Redirecionar para login
}

// ✅ Tratar erro 401 (token expirado)
if (response.status === 401) {
    localStorage.removeItem('token');
    // Redirecionar para login
}
```

### 3. UX Amigável
```javascript
// ✅ Feedback visual de loading
setLoading(true);
await login(username, password);
setLoading(false);

// ✅ Mensagens de erro claras
if (error.status === 401) {
    setError('Usuário ou senha incorretos');
}
```

---

## 🎯 CHECKLIST SEGURANÇA

### Implementação
- [ ] SecurityConfig configurado
- [ ] JwtUtil implementado
- [ ] JwtAuthenticationFilter funcionando
- [ ] Endpoints públicos/protegidos definidos
- [ ] Senhas criptografadas com BCrypt
- [ ] CORS configurado para frontend

### Testes
- [ ] Login funciona
- [ ] Registro funciona
- [ ] Token é gerado corretamente
- [ ] Endpoints protegidos exigem token
- [ ] Token expirado retorna 401
- [ ] CORS permite requisições do frontend

### Frontend Integration
- [ ] Token armazenado no localStorage
- [ ] Authorization header enviado
- [ ] Erro 401 tratado (logout automático)
- [ ] Loading states implementados
- [ ] Mensagens de erro amigáveis