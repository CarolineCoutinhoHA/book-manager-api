# Guia Prático do Postman - Testando a API

## 1. Preparação

### Iniciar o Projeto
```bash
# No terminal, dentro da pasta do projeto:
mvn spring-boot:run
```
**Aguarde até ver**: `Started DemoApplication in X.XXX seconds`

### Instalar Postman
- Download: https://www.postman.com/downloads/
- Ou usar versão web: https://web.postman.com/

## 2. Configuração Básica

### Criar Nova Collection
1. Abra o Postman
2. Clique em "New" → "Collection"
3. Nome: "Biblioteca API"
4. Salve

### URL Base
Todas as requisições usarão: `http://localhost:8080/api`

## 3. Testando Endpoints de Autor

### 3.1 Criar Autor (POST)
```
Método: POST
URL: http://localhost:8080/api/autores
```

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
    "nome": "João Silva",
    "email": "joao@email.com"
}
```

**Resultado Esperado:** Status 201 Created
```json
{
    "uuid": "123e4567-e89b-12d3-a456-426614174000",
    "nome": "João Silva",
    "email": "joao@email.com"
}
```

### 3.2 Listar Autores (GET)
```
Método: GET
URL: http://localhost:8080/api/autores
```

**Resultado Esperado:** Status 200 OK
```json
{
    "content": [
        {
            "nome": "João Silva"
        }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
}
```

### 3.3 Buscar Autor por ID (GET)
```
Método: GET
URL: http://localhost:8080/api/autores/1
```

**Resultado Esperado:** Status 200 OK
```json
{
    "uuid": "123e4567-e89b-12d3-a456-426614174000",
    "nome": "João Silva",
    "email": "joao@email.com"
}
```

### 3.4 Buscar por Email (GET)
```
Método: GET
URL: http://localhost:8080/api/autores/busca?email=joao@email.com
```

### 3.5 Atualizar Autor (PUT)
```
Método: PUT
URL: http://localhost:8080/api/autores/1
```

**Body (raw JSON):**
```json
{
    "nome": "João Santos",
    "email": "joao.santos@email.com"
}
```

### 3.6 Deletar Autor (DELETE)
```
Método: DELETE
URL: http://localhost:8080/api/autores/1
```

**Resultado Esperado:** Status 204 No Content

## 4. Testando Endpoints de Livro

### 4.1 Criar Livro (POST)
```
Método: POST
URL: http://localhost:8080/api/livros
```

**Body (raw JSON):**
```json
{
    "titulo": "Clean Code",
    "isbn": "978-0132350884",
    "dataDePublicacao": "2008-08-01",
    "categoria": "TECNOLOGIA",
    "autoresIds": [1]
}
```

### 4.2 Listar Livros (GET)
```
Método: GET
URL: http://localhost:8080/api/livros
```

### 4.3 Buscar Livro por ID (GET)
```
Método: GET
URL: http://localhost:8080/api/livros/1
```

## 5. Testando Autenticação JWT

### 5.1 Registrar Usuário (POST)
```
Método: POST
URL: http://localhost:8080/api/auth/register
```

**Body (raw JSON):**
```json
{
    "username": "admin",
    "password": "123456"
}
```

### 5.2 Login (POST)
```
Método: POST
URL: http://localhost:8080/api/auth/login
```

**Body (raw JSON):**
```json
{
    "username": "admin",
    "password": "123456"
}
```

**Resultado:** Copie o token da resposta
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 5.3 Usar Token em Requisições Protegidas
Para endpoints protegidos, adicione no **Header**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 6. Casos de Teste Importantes

### 6.1 Validação de Email
**Teste:** Email inválido
```json
{
    "nome": "Teste",
    "email": "email-invalido"
}
```
**Esperado:** Status 400 Bad Request

### 6.2 Email Duplicado
**Teste:** Criar autor com email já existente
**Esperado:** Status 409 Conflict

### 6.3 Autor Não Encontrado
**Teste:** GET /api/autores/999
**Esperado:** Status 404 Not Found

### 6.4 ISBN Duplicado
**Teste:** Criar livro com ISBN já existente
**Esperado:** Status 409 Conflict

## 7. Dicas do Postman

### Salvar Requisições
1. Após configurar uma requisição, clique "Save"
2. Escolha a collection "Biblioteca API"
3. Dê um nome descritivo: "Criar Autor"

### Usar Variáveis
1. Na collection, vá em "Variables"
2. Adicione: `baseUrl` = `http://localhost:8080/api`
3. Use nas URLs: `{{baseUrl}}/autores`

### Ambiente de Desenvolvimento
1. Clique no ícone de engrenagem (Settings)
2. "Manage Environments" → "Add"
3. Nome: "Desenvolvimento"
4. Variáveis:
   - `baseUrl`: `http://localhost:8080/api`
   - `token`: (cole o JWT aqui)

### Testes Automáticos
Na aba "Tests" de cada requisição:
```javascript
// Verificar status
pm.test("Status é 201", function () {
    pm.response.to.have.status(201);
});

// Verificar JSON
pm.test("Resposta tem nome", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.nome).to.eql("João Silva");
});

// Salvar token automaticamente
if (pm.response.json().token) {
    pm.environment.set("token", pm.response.json().token);
}
```

## 8. Sequência de Testes Recomendada

### Primeira Vez:
1. **POST** /api/auth/register (criar usuário)
2. **POST** /api/auth/login (obter token)
3. **POST** /api/autores (criar autor)
4. **GET** /api/autores (listar)
5. **POST** /api/livros (criar livro)
6. **GET** /api/livros (listar)

### Testes de Validação:
1. Criar autor com email inválido
2. Criar autor com email duplicado
3. Buscar autor inexistente
4. Criar livro com ISBN duplicado

## 9. Troubleshooting

### Erro "Connection refused"
- ✅ Verificar se o Spring Boot está rodando
- ✅ Confirmar porta 8080 no console

### Erro 401 Unauthorized
- ✅ Verificar se o token JWT está no header
- ✅ Token pode ter expirado (fazer login novamente)

### Erro 400 Bad Request
- ✅ Verificar JSON no body
- ✅ Verificar Content-Type: application/json
- ✅ Verificar validações (@NotBlank, @Email)

### Erro 404 Not Found
- ✅ Verificar URL (http://localhost:8080/api/...)
- ✅ Verificar se o recurso existe

### Erro 500 Internal Server Error
- ✅ Verificar logs do Spring Boot no console
- ✅ Verificar se o banco H2 está funcionando

## 10. Exportar/Importar Collection

### Exportar:
1. Clique nos "..." da collection
2. "Export" → "Collection v2.1"
3. Salve o arquivo JSON

### Importar:
1. "Import" → "Upload Files"
2. Selecione o arquivo JSON
3. Collection será criada automaticamente