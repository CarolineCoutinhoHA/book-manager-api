# 📋 CONTRATO DE API - DOCUMENTAÇÃO COMPLETA

## 🎯 VISÃO GERAL DA API

### Informações Básicas
- **Base URL**: `http://localhost:8080/api`
- **Formato**: JSON
- **Autenticação**: JWT Bearer Token
- **Versionamento**: v1 (implícito)
- **Charset**: UTF-8

### Recursos Disponíveis
- **Autenticação** (`/auth`) - Login, registro, verificação
- **Autores** (`/autores`) - CRUD completo de autores
- **Livros** (`/livros`) - CRUD completo de livros

---

## 🔐 AUTENTICAÇÃO

### Endpoints Públicos (sem token)
```http
POST /api/auth/register
POST /api/auth/login
```

### Endpoints Protegidos (com token)
```http
Authorization: Bearer {jwt_token}
```

---

## 👤 RECURSO: AUTENTICAÇÃO

### 1. Registrar Usuário
```http
POST /api/auth/register
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin"
}
```

**Response 400 Bad Request:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "message": "Username já existe"
}
```

### 2. Fazer Login
```http
POST /api/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "username": "admin",
  "password": "123456"
}
```

**Response 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin"
}
```

**Response 401 Unauthorized:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "message": "Usuário ou senha incorretos"
}
```

### 3. Verificar Token Atual
```http
GET /api/auth/me
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
"Usuário logado: admin"
```

---

## 📚 RECURSO: AUTORES

### 1. Criar Autor
```http
POST /api/autores
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "nome": "Machado de Assis",
  "email": "machado@literatura.com",
  "cep": "20040-020",
  "telefone": "(21) 99999-9999"
}
```

**Response 201 Created:**
```json
{
  "idAutor": 1,
  "nome": "Machado de Assis",
  "email": "machado@literatura.com",
  "cep": "20040-020",
  "telefone": "(21) 99999-9999"
}
```

**Response 400 Bad Request:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "message": "Dados inválidos. Verifique os campos.",
  "fieldErrors": {
    "nome": "O nome do autor é obrigatório!",
    "email": "O email deve ter um formato válido!",
    "cep": "O CEP deve estar no formato XXXXX-XXX"
  }
}
```

### 2. Listar Autores (Paginado)
```http
GET /api/autores?page=0&size=10
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
  "content": [
    {
      "nome": "Machado de Assis"
    },
    {
      "nome": "Clarice Lispector"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "ascending": true
    }
  },
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false,
  "numberOfElements": 10
}
```

### 3. Buscar Autor por ID
```http
GET /api/autores/{id}
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
  "idAutor": 1,
  "nome": "Machado de Assis",
  "email": "machado@literatura.com",
  "cep": "20040-020",
  "telefone": "(21) 99999-9999"
}
```

**Response 404 Not Found:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "message": "O autor de ID: 999 não foi encontrado!"
}
```

### 4. Buscar Autor por Email
```http
GET /api/autores/busca?email=machado@literatura.com
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
  "idAutor": 1,
  "nome": "Machado de Assis",
  "email": "machado@literatura.com",
  "cep": "20040-020",
  "telefone": "(21) 99999-9999"
}
```

### 5. Atualizar Autor
```http
PUT /api/autores/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "nome": "Machado de Assis Atualizado",
  "email": "novo@email.com",
  "cep": "21000-000",
  "telefone": "(21) 88888-8888"
}
```

**Response 200 OK:**
```json
{
  "idAutor": 1,
  "nome": "Machado de Assis Atualizado",
  "email": "novo@email.com",
  "cep": "21000-000",
  "telefone": "(21) 88888-8888"
}
```

### 6. Deletar Autor (Soft Delete)
```http
DELETE /api/autores/{id}
Authorization: Bearer {token}
```

**Response 204 No Content:**
```
(sem conteúdo)
```

---

## 📖 RECURSO: LIVROS

### 1. Criar Livro
```http
POST /api/livros
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "titulo": "Dom Casmurro",
  "isbn": "978-85-359-0277-5",
  "dataDePublicacao": "1899-12-01",
  "categoria": "LITERATURA",
  "autoresIds": [1, 2]
}
```

**Response 201 Created:**
```json
{
  "idLivro": 1,
  "titulo": "Dom Casmurro",
  "isbn": "978-85-359-0277-5",
  "dataDePublicacao": "1899-12-01",
  "categoriaNome": "Literatura",
  "autoresNome": ["Machado de Assis"]
}
```

### 2. Listar Livros
```http
GET /api/livros
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
[
  {
    "idLivro": 1,
    "titulo": "Dom Casmurro",
    "isbn": "978-85-359-0277-5",
    "dataDePublicacao": "1899-12-01",
    "categoriaNome": "Literatura",
    "autoresNome": ["Machado de Assis"]
  }
]
```

### 3. Buscar Livro por ID
```http
GET /api/livros/{id}
Authorization: Bearer {token}
```

**Response 200 OK:**
```json
{
  "idLivro": 1,
  "titulo": "Dom Casmurro",
  "isbn": "978-85-359-0277-5",
  "dataDePublicacao": "1899-12-01",
  "categoriaNome": "Literatura",
  "autoresNome": ["Machado de Assis"]
}
```

### 4. Atualizar Livro
```http
PUT /api/livros/{id}
Authorization: Bearer {token}
Content-Type: application/json
```

**Request Body:**
```json
{
  "titulo": "Dom Casmurro - Edição Revisada",
  "dataDePublicacao": "1899-12-01",
  "categoria": "LITERATURA"
}
```

**Response 200 OK:**
```json
{
  "idLivro": 1,
  "titulo": "Dom Casmurro - Edição Revisada",
  "isbn": "978-85-359-0277-5",
  "dataDePublicacao": "1899-12-01",
  "categoriaNome": "Literatura",
  "autoresNome": ["Machado de Assis"]
}
```

### 5. Deletar Livro
```http
DELETE /api/livros/{id}
Authorization: Bearer {token}
```

**Response 204 No Content:**
```
(sem conteúdo)
```

---

## 📊 CÓDIGOS DE STATUS HTTP

### Sucesso (2xx)
- **200 OK** - Requisição bem-sucedida
- **201 Created** - Recurso criado com sucesso
- **204 No Content** - Sucesso sem retorno de dados

### Erro do Cliente (4xx)
- **400 Bad Request** - Dados inválidos ou malformados
- **401 Unauthorized** - Token ausente, inválido ou expirado
- **403 Forbidden** - Sem permissão para acessar recurso
- **404 Not Found** - Recurso não encontrado
- **409 Conflict** - Conflito de dados (email/ISBN duplicado)

### Erro do Servidor (5xx)
- **500 Internal Server Error** - Erro interno do servidor

---

## 🔍 VALIDAÇÕES DE CAMPOS

### Autor
```json
{
  "nome": {
    "required": true,
    "maxLength": 100,
    "message": "O nome do autor é obrigatório!"
  },
  "email": {
    "required": true,
    "format": "email",
    "unique": true,
    "message": "O email deve ter um formato válido!"
  },
  "cep": {
    "required": true,
    "pattern": "\\d{5}-\\d{3}",
    "message": "O CEP deve estar no formato XXXXX-XXX"
  },
  "telefone": {
    "required": false,
    "pattern": "\\(\\d{2}\\) \\d{5}-\\d{4}",
    "message": "O telefone deve estar no formato (XX) XXXXX-XXXX"
  }
}
```

### Livro
```json
{
  "titulo": {
    "required": true,
    "maxLength": 100,
    "message": "O titulo do livro é obrigatório!"
  },
  "isbn": {
    "required": true,
    "minLength": 10,
    "maxLength": 17,
    "pattern": "^[0-9-]+$",
    "unique": true,
    "message": "O ISBN deve conter apenas números e hífens"
  },
  "dataDePublicacao": {
    "required": true,
    "format": "date",
    "pastOrPresent": true,
    "message": "A data de publicação não pode ser no futuro"
  },
  "categoria": {
    "required": true,
    "enum": ["LITERATURA", "CIENCIA", "BIOGRAFIA", "FANTASIA", "MISTERIO"],
    "message": "A categoria é obrigatória"
  },
  "autoresIds": {
    "required": true,
    "minSize": 1,
    "message": "O livro deve ter pelo menos um autor associado"
  }
}
```

---

## 🌐 HEADERS OBRIGATÓRIOS

### Para todas as requisições
```http
Content-Type: application/json
Accept: application/json
```

### Para endpoints protegidos
```http
Authorization: Bearer {jwt_token}
```

### CORS Headers (automático)
```http
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: *
```

---

## 📝 EXEMPLOS DE PAYLOADS COMPLETOS

### Criar Autor Completo
```json
{
  "nome": "Clarice Lispector",
  "email": "clarice@literatura.com",
  "cep": "22071-900",
  "telefone": "(21) 99999-8888"
}
```

### Criar Livro com Múltiplos Autores
```json
{
  "titulo": "Antologia Brasileira",
  "isbn": "978-85-123-4567-8",
  "dataDePublicacao": "2020-06-15",
  "categoria": "LITERATURA",
  "autoresIds": [1, 2, 3]
}
```

### Atualizar Dados Parciais
```json
{
  "nome": "Nome Atualizado",
  "telefone": "(11) 88888-7777"
}
```

---

## 🚨 TRATAMENTO DE ERROS

### Erro de Validação (400)
```json
{
  "timestamp": "2024-01-15T10:30:00.123",
  "status": 400,
  "message": "Dados inválidos. Verifique os campos.",
  "fieldErrors": {
    "nome": "O nome do autor é obrigatório!",
    "email": "O email deve ter um formato válido!",
    "cep": "O CEP deve estar no formato XXXXX-XXX",
    "telefone": "O telefone deve estar no formato (XX) XXXXX-XXXX"
  }
}
```

### Erro de Autenticação (401)
```json
{
  "timestamp": "2024-01-15T10:30:00.123",
  "status": 401,
  "message": "Token JWT inválido ou expirado"
}
```

### Erro de Recurso Não Encontrado (404)
```json
{
  "timestamp": "2024-01-15T10:30:00.123",
  "status": 404,
  "message": "O autor de ID: 999 não foi encontrado!"
}
```

### Erro de Conflito (409)
```json
{
  "timestamp": "2024-01-15T10:30:00.123",
  "status": 409,
  "message": "Email machado@literatura.com já está em uso!"
}
```

---

## 🎯 CHECKLIST DE INTEGRAÇÃO

### Antes de Integrar
- [ ] Obter token JWT via `/api/auth/login`
- [ ] Configurar header `Authorization: Bearer {token}`
- [ ] Configurar `Content-Type: application/json`
- [ ] Validar formato dos dados antes de enviar

### Tratamento de Respostas
- [ ] Verificar status code da resposta
- [ ] Tratar erro 401 (token expirado) → fazer novo login
- [ ] Tratar erro 400 (validação) → mostrar erros de campo
- [ ] Tratar erro 404 (não encontrado) → mostrar mensagem amigável
- [ ] Tratar erro 409 (conflito) → mostrar mensagem de duplicidade

### Boas Práticas
- [ ] Armazenar token no localStorage/sessionStorage
- [ ] Implementar refresh automático de token
- [ ] Validar dados no frontend antes de enviar
- [ ] Implementar loading states
- [ ] Mostrar mensagens de erro amigáveis