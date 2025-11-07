# 📖 SWAGGER - GUIA PRÁTICO COMPLETO

## 🎯 O QUE É SWAGGER?

Swagger (OpenAPI) é uma ferramenta que gera **documentação interativa** da sua API automaticamente.

### Benefícios
- ✅ **Documentação automática** - Não precisa escrever manualmente
- ✅ **Interface de teste** - Testa endpoints direto no navegador
- ✅ **Exemplos de payload** - Mostra formato correto dos dados
- ✅ **Códigos de resposta** - Documenta todos os retornos possíveis
- ✅ **Autenticação JWT** - Testa endpoints protegidos

---

## 🚀 COMO ACESSAR

### URLs Importantes
```
Swagger UI: http://localhost:8080/swagger-ui.html
API Docs:   http://localhost:8080/v3/api-docs
```

### Primeira Vez
1. Inicie a aplicação: `./gradlew bootRun`
2. Abra: http://localhost:8080/swagger-ui.html
3. Veja todos os endpoints documentados automaticamente

---

## 🔐 USANDO JWT NO SWAGGER

### Passo a Passo
```
1. Clique em "Authorize" (cadeado no topo)
2. Digite: Bearer {seu_token}
3. Clique "Authorize"
4. Agora pode testar endpoints protegidos
```

### Obtendo Token
```http
POST /api/auth/login
{
  "username": "admin",
  "password": "123456"
}

# Resposta:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin"
}

# Use no Swagger:
Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 🧪 TESTANDO ENDPOINTS

### 1. Endpoints Públicos (sem autenticação)

#### Registrar Usuário
```
POST /api/auth/register
```
**Payload:**
```json
{
  "username": "teste",
  "password": "123456"
}
```

#### Fazer Login
```
POST /api/auth/login
```
**Payload:**
```json
{
  "username": "teste",
  "password": "123456"
}
```

### 2. Endpoints Protegidos (com JWT)

#### Listar Autores
```
GET /api/autores
Authorization: Bearer {token}
```

#### Criar Autor
```
POST /api/autores
Authorization: Bearer {token}
```
**Payload:**
```json
{
  "nome": "Machado de Assis",
  "email": "machado@literatura.com",
  "cep": "20040-020",
  "telefone": "(21) 99999-9999"
}
```

#### Criar Livro
```
POST /api/livros
Authorization: Bearer {token}
```
**Payload:**
```json
{
  "titulo": "Dom Casmurro",
  "isbn": "978-85-359-0277-5",
  "dataDePublicacao": "1899-12-01",
  "categoria": "LITERATURA",
  "autoresIds": [1]
}
```

---

## 📝 INTERPRETANDO RESPOSTAS

### Códigos de Status
- **🟢 200** - Sucesso (GET, PUT)
- **🟢 201** - Criado (POST)
- **🟡 400** - Dados inválidos
- **🟡 401** - Não autenticado
- **🟡 404** - Não encontrado
- **🔴 500** - Erro do servidor

### Exemplo de Resposta Sucesso
```json
{
  "idAutor": 1,
  "nome": "Machado de Assis",
  "email": "machado@literatura.com",
  "cep": "20040-020",
  "telefone": "(21) 99999-9999"
}
```

### Exemplo de Resposta Erro
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "message": "O email deve ter um formato válido"
}
```

---

## 🎨 PERSONALIZANDO DOCUMENTAÇÃO

### Adicionando Descrições aos Controllers
```java
@Tag(name = "Autores", description = "Gerenciamento de autores da biblioteca")
@RestController
@RequestMapping("/api/autores")
public class AutorController {
```

### Documentando Endpoints
```java
@Operation(
    summary = "Buscar autor por ID",
    description = "Retorna um autor específico baseado no ID fornecido"
)
@ApiResponse(responseCode = "200", description = "Autor encontrado com sucesso")
@ApiResponse(responseCode = "404", description = "Autor não encontrado")
@GetMapping("/{id}")
public AutorResponseDTO buscarPorId(@PathVariable Long id) {
```

### Documentando Parâmetros
```java
@GetMapping("/{id}")
public AutorResponseDTO buscarPorId(
    @Parameter(description = "ID único do autor", example = "1")
    @PathVariable Long id
) {
```

### Documentando Request Body
```java
@PostMapping
public AutorResponseDTO criar(
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Dados do autor a ser criado",
        required = true
    )
    @RequestBody @Valid AutorRequestDTO dto
) {
```

---

## 🔧 CONFIGURAÇÃO AVANÇADA

### Informações da API
```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("API Biblioteca Hackathon")
            .version("2.0")
            .description("API completa para gerenciamento de biblioteca")
            .contact(new Contact()
                .name("Equipe Hackathon")
                .email("equipe@hackathon.com")
                .url("https://github.com/equipe/biblioteca"))
            .license(new License()
                .name("MIT License")
                .url("https://opensource.org/licenses/MIT")));
}
```

### Múltiplos Esquemas de Segurança
```java
.addSecurityItem(new SecurityRequirement()
    .addList("Bearer Authentication")
    .addList("API Key"))
.components(new Components()
    .addSecuritySchemes("Bearer Authentication", 
        new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT"))
    .addSecuritySchemes("API Key",
        new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .in(SecurityScheme.In.HEADER)
            .name("X-API-Key")));
```

---

## 📱 TESTANDO FLUXOS COMPLETOS

### Fluxo 1: Criar Conta e Autor
```
1. POST /api/auth/register
   Body: {"username": "dev", "password": "123456"}

2. Copiar token da resposta

3. Authorize no Swagger com: Bearer {token}

4. POST /api/autores
   Body: {
     "nome": "J.K. Rowling",
     "email": "jk@hogwarts.com",
     "cep": "12345-678",
     "telefone": "(11) 99999-9999"
   }

5. GET /api/autores (verificar se foi criado)
```

### Fluxo 2: Criar Livro com Autor
```
1. GET /api/autores (pegar ID do autor)

2. POST /api/livros
   Body: {
     "titulo": "Harry Potter",
     "isbn": "978-0439708180",
     "dataDePublicacao": "1997-06-26",
     "categoria": "FANTASIA",
     "autoresIds": [1]
   }

3. GET /api/livros (verificar se foi criado)
```

---

## 🐛 TROUBLESHOOTING SWAGGER

### Problema: Swagger não carrega
```
Solução:
1. Verificar se aplicação está rodando
2. Acessar: http://localhost:8080/swagger-ui.html
3. Verificar logs por erros de configuração
```

### Problema: Endpoints não aparecem
```
Solução:
1. Verificar se controller tem @RestController
2. Verificar se está no package correto
3. Recompilar: ./gradlew clean bootRun
```

### Problema: JWT não funciona
```
Solução:
1. Fazer login primeiro
2. Copiar token COMPLETO
3. Usar formato: Bearer {token}
4. Verificar se token não expirou (24h)
```

### Problema: 401 Unauthorized
```
Solução:
1. Verificar se clicou "Authorize"
2. Verificar formato do token
3. Fazer novo login se necessário
```

---

## 💡 DICAS PRO HACKATHON

### 1. Use Swagger como Documentação Viva
- Sempre teste no Swagger antes de integrar com frontend
- Use exemplos do Swagger para validar payloads
- Compartilhe URL do Swagger com equipe frontend

### 2. Organize por Tags
```java
@Tag(name = "1. Autenticação", description = "Login e registro")
@Tag(name = "2. Autores", description = "Gerenciamento de autores")  
@Tag(name = "3. Livros", description = "Gerenciamento de livros")
```

### 3. Documente Erros Comuns
```java
@ApiResponse(responseCode = "400", 
    description = "Dados inválidos - verificar formato de email, CEP, telefone")
@ApiResponse(responseCode = "401", 
    description = "Token JWT inválido ou expirado - fazer login novamente")
@ApiResponse(responseCode = "409", 
    description = "Email ou ISBN já cadastrado - usar valores únicos")
```

### 4. Exemplos Realistas
```java
@Schema(example = "machado@literatura.com")
private String email;

@Schema(example = "20040-020")
private String cep;

@Schema(example = "978-85-359-0277-5")
private String isbn;
```

### 5. Atalhos Úteis
- **Ctrl+F** - Buscar endpoint específico
- **Try it out** - Testar endpoint
- **Execute** - Executar requisição
- **Clear** - Limpar dados do formulário

---

## 🎯 CHECKLIST SWAGGER

### Antes de Apresentar no Hackathon
- [ ] Swagger carrega sem erros
- [ ] Todos os endpoints estão documentados
- [ ] JWT funciona corretamente
- [ ] Exemplos de payload estão corretos
- [ ] Códigos de resposta estão documentados
- [ ] Tags organizam endpoints logicamente
- [ ] Descrições são claras e úteis
- [ ] Fluxos principais foram testados

### Para Demonstração
- [ ] Preparar conta de teste
- [ ] Ter payloads prontos para copiar
- [ ] Conhecer fluxos principais
- [ ] Saber resolver erros comuns
- [ ] Ter backup de tokens válidos