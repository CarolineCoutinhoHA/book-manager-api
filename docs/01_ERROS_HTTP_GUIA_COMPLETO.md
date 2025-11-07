# 🚨 GUIA COMPLETO DE ERROS HTTP - TROUBLESHOOTING

## 📋 CÓDIGOS DE STATUS HTTP MAIS COMUNS

### 🟢 2xx - SUCESSO
- **200 OK** - Requisição bem-sucedida
- **201 Created** - Recurso criado com sucesso
- **204 No Content** - Sucesso sem retorno de dados

### 🟡 4xx - ERRO DO CLIENTE
- **400 Bad Request** - Dados inválidos
- **401 Unauthorized** - Não autenticado
- **403 Forbidden** - Sem permissão
- **404 Not Found** - Recurso não encontrado
- **409 Conflict** - Conflito de dados

### 🔴 5xx - ERRO DO SERVIDOR
- **500 Internal Server Error** - Erro interno
- **503 Service Unavailable** - Serviço indisponível

---

## 🔍 ERRO 404 - NOT FOUND

### Quando Acontece
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 404,
  "message": "O autor de ID: 999 não foi encontrado!"
}
```

### Causas Comuns
1. **ID inexistente** - Buscar autor/livro que não existe
2. **URL incorreta** - `/api/autor` em vez de `/api/autores`
3. **Soft Delete** - Recurso foi "deletado" (disponibilidade = false)

### Como Resolver
```http
# ❌ ERRO: ID não existe
GET /api/autores/999

# ✅ SOLUÇÃO: Verificar IDs válidos
GET /api/autores  # Lista todos primeiro

# ❌ ERRO: URL errada
GET /api/autor/1

# ✅ SOLUÇÃO: URL correta
GET /api/autores/1
```

---

## 🔐 ERRO 401 - UNAUTHORIZED

### Quando Acontece
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "message": "Token JWT inválido ou expirado"
}
```

### Causas Comuns
1. **Token ausente** - Não enviou Authorization header
2. **Token expirado** - JWT passou de 24 horas
3. **Token malformado** - Formato incorreto
4. **Chave inválida** - Token assinado com chave diferente

### Como Resolver
```http
# ❌ ERRO: Sem token
GET /api/autores

# ✅ SOLUÇÃO: Com token válido
GET /api/autores
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

# ❌ ERRO: Token expirado
Authorization: Bearer {token_antigo}

# ✅ SOLUÇÃO: Fazer login novamente
POST /api/auth/login
{
  "username": "admin",
  "password": "123456"
}
```

---

## ⚠️ ERRO 400 - BAD REQUEST

### Quando Acontece
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "message": "O email deve ter um formato válido"
}
```

### Causas Comuns - Validações
1. **Email inválido** - Formato incorreto
2. **CEP inválido** - Não segue padrão XXXXX-XXX
3. **Telefone inválido** - Não segue padrão (XX) XXXXX-XXXX
4. **ISBN duplicado** - Já existe no sistema
5. **Campos obrigatórios** - Valores null/vazios

### Exemplos de Validação
```json
// ❌ ERRO: Email inválido
{
  "nome": "João",
  "email": "joao@",
  "cep": "12345-678",
  "telefone": "(11) 99999-9999"
}

// ✅ SOLUÇÃO: Email correto
{
  "nome": "João",
  "email": "joao@email.com",
  "cep": "12345-678",
  "telefone": "(11) 99999-9999"
}
```

---

## 🚨 CHECKLIST DE TROUBLESHOOTING

### Antes de Pedir Ajuda
- [ ] Verificou se a aplicação está rodando?
- [ ] Testou no Swagger primeiro?
- [ ] Conferiu o formato do JSON?
- [ ] Validou o token JWT?
- [ ] Checou os logs da aplicação?
- [ ] Verificou se o endpoint existe?
- [ ] Confirmou o método HTTP (GET/POST/PUT/DELETE)?