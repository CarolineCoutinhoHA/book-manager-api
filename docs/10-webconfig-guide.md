# WebConfig - Configuração CORS e Web

## Visão Geral

WebConfig é a classe de configuração que define políticas de CORS (Cross-Origin Resource Sharing) e outras configurações web do Spring Boot.

## Problema que Resolve

### Same-Origin Policy
Navegadores implementam uma política de segurança que bloqueia requisições entre diferentes origens:
- **Origem**: Combinação de protocolo + domínio + porta
- **Exemplo**: `http://localhost:3000` ≠ `http://localhost:8080`

### Cenário Típico
```
Frontend (React/Vue/Angular): http://localhost:3000
Backend (Spring Boot):        http://localhost:8080
Resultado: ❌ Requisições bloqueadas pelo navegador
```

## Configuração CORS

### Implementação Atual
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/api/**")                    // Aplica a todos endpoints da API
            .allowedOrigins("http://localhost:3000")  // Frontend permitido
            .allowedMethods("GET", "POST", "PUT", "DELETE")  // Métodos HTTP
            .allowedHeaders("*");                     // Todos os headers
    }
}
```

### Parâmetros Explicados

#### addMapping("/api/**")
- **Função**: Define quais endpoints terão CORS habilitado
- **Padrão**: `/api/**` = todos os endpoints que começam com `/api/`
- **Exemplos**: `/api/autores`, `/api/livros`, `/api/auth/login`

#### allowedOrigins()
- **Função**: Lista de origens permitidas para fazer requisições
- **Desenvolvimento**: `"http://localhost:3000"` (React/Vue padrão)
- **Produção**: `"https://meudominio.com"`
- **Múltiplas**: `"http://localhost:3000", "https://app.exemplo.com"`

#### allowedMethods()
- **Função**: Métodos HTTP permitidos
- **CRUD Completo**: `"GET", "POST", "PUT", "DELETE"`
- **Somente Leitura**: `"GET"`
- **Todos**: `"*"` (não recomendado)

#### allowedHeaders()
- **Função**: Headers permitidos nas requisições
- **Todos**: `"*"` (inclui Authorization para JWT)
- **Específicos**: `"Content-Type", "Authorization"`

## Configurações Avançadas

### CORS Completo
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins("http://localhost:3000", "https://app.exemplo.com")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .allowCredentials(true)                    // Permite cookies/auth
        .maxAge(3600);                            // Cache preflight (1 hora)
}
```

### Múltiplos Mapeamentos
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    // API pública - CORS liberal
    registry
        .addMapping("/api/public/**")
        .allowedOrigins("*")
        .allowedMethods("GET")
        .allowedHeaders("Content-Type");
    
    // API privada - CORS restrito
    registry
        .addMapping("/api/private/**")
        .allowedOrigins("https://app.seguro.com")
        .allowedMethods("GET", "POST", "PUT", "DELETE")
        .allowedHeaders("*")
        .allowCredentials(true);
}
```

## Configurações por Ambiente

### application.yml
```yaml
# application-dev.yml
cors:
  allowed-origins: "http://localhost:3000,http://localhost:3001"
  
# application-prod.yml  
cors:
  allowed-origins: "https://app.exemplo.com"
```

### WebConfig Dinâmico
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry
            .addMapping("/api/**")
            .allowedOrigins(allowedOrigins.split(","))
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("*");
    }
}
```

## Alternativas de Configuração

### 1. Anotação @CrossOrigin
```java
@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AutorController {
    // Aplica CORS apenas a este controller
}

@GetMapping("/autores")
@CrossOrigin(origins = "http://localhost:3000")
public List<AutorResponseDTO> listar() {
    // Aplica CORS apenas a este endpoint
}
```

### 2. Filtro CORS Manual
```java
@Component
public class CorsFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
        HttpServletResponse response = (HttpServletResponse) res;
        HttpServletRequest request = (HttpServletRequest) req;
        
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE");
        response.setHeader("Access-Control-Allow-Headers", "*");
        
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            chain.doFilter(req, res);
        }
    }
}
```

### 3. Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

## Debugging CORS

### Headers de Resposta
```
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: *
```

### Preflight Request
Para requisições complexas, o navegador faz uma requisição OPTIONS primeiro:
```
OPTIONS /api/autores HTTP/1.1
Origin: http://localhost:3000
Access-Control-Request-Method: POST
Access-Control-Request-Headers: content-type,authorization
```

### Logs de Debug
```yaml
logging:
  level:
    org.springframework.web.cors: DEBUG
```

## Problemas Comuns

### 1. CORS ainda bloqueado
**Causa**: Configuração não aplicada
**Solução**: Verificar se `@Configuration` está presente

### 2. Preflight falha
**Causa**: Método OPTIONS não permitido
**Solução**: Adicionar "OPTIONS" aos métodos permitidos

### 3. Credentials rejeitados
**Causa**: `allowCredentials(true)` não configurado
**Solução**: Habilitar credentials quando usar cookies/auth

### 4. Wildcard com credentials
**Erro**: Não é possível usar `allowedOrigins("*")` com `allowCredentials(true)`
**Solução**: Especificar origens exatas

## Segurança

### Boas Práticas
1. **Nunca usar `*` em produção** para origins
2. **Especificar métodos** necessários apenas
3. **Limitar headers** quando possível
4. **Usar HTTPS** em produção
5. **Validar origins** dinamicamente se necessário

### Configuração Segura para Produção
```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/api/**")
        .allowedOrigins("https://app.exemplo.com")  // Domínio específico
        .allowedMethods("GET", "POST", "PUT", "DELETE")  // Métodos necessários
        .allowedHeaders("Content-Type", "Authorization")  // Headers específicos
        .allowCredentials(true)  // Para autenticação
        .maxAge(3600);  // Cache preflight
}
```

## Testes

### Teste Manual com cURL
```bash
# Preflight request
curl -X OPTIONS \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -H "Access-Control-Request-Headers: content-type" \
  http://localhost:8080/api/autores

# Requisição real
curl -X POST \
  -H "Origin: http://localhost:3000" \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","email":"teste@email.com"}' \
  http://localhost:8080/api/autores
```

### Teste Automatizado
```java
@SpringBootTest
@AutoConfigureTestDatabase
class CorsConfigTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void corsConfiguration_DevePermitirOrigemEspecifica() throws Exception {
        mockMvc.perform(options("/api/autores")
                .header("Origin", "http://localhost:3000")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }
}
```

## Integração com Frontend

### JavaScript/Fetch
```javascript
// Requisição que será permitida pelo CORS
fetch('http://localhost:8080/api/autores', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
    },
    body: JSON.stringify({
        nome: 'João Silva',
        email: 'joao@email.com'
    })
})
.then(response => response.json())
.then(data => console.log(data));
```

### Axios
```javascript
// Configuração global do Axios
axios.defaults.baseURL = 'http://localhost:8080/api';
axios.defaults.headers.common['Authorization'] = 'Bearer ' + token;

// Requisição
axios.post('/autores', {
    nome: 'João Silva',
    email: 'joao@email.com'
});
```