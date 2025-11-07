# Postman - Guia Passo a Passo COMPLETO

## 🚀 Preparação

### 1. Iniciar o Projeto
```bash
# No terminal, dentro da pasta do projeto:
gradlew.bat bootRun
```
**Aguarde até ver**: `Started DemoApplication in X.XXX seconds`

### 2. Abrir Postman
- Download: https://www.postman.com/downloads/
- Ou versão web: https://web.postman.com/

## 🔐 Autenticação (OBRIGATÓRIO)

### Passo 1: Registrar Usuário
**Configuração:**
- **Método**: `POST`
- **URL**: `http://localhost:8080/api/auth/register`

**Headers:**
- `Content-Type: application/json`

**Body (raw JSON):**
```json
{
    "username": "admin",
    "password": "123456"
}
```

**Resultado Esperado**: Status 200 OK
```json
{
    "message": "Usuário registrado com sucesso"
}
```

### Passo 2: Fazer Login
**Configuração:**
- **Método**: `POST`
- **URL**: `http://localhost:8080/api/auth/login`

**Headers:**
- `Content-Type: application/json`

**Body (raw JSON):**
```json
{
    "username": "admin",
    "password": "123456"
}
```

**Resultado Esperado**: Status 200 OK
```json
{
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTc2MjQ4MjQwMiwiZXhwIjoxNzYyNTY4ODAyfQ.UdDUm1tSe8NzZ1NZzkfL-GV440NhjOV2T4Hy81xCwWQ"
}
```

### Passo 3: Copiar o Token
**⚠️ IMPORTANTE**: Copie o token completo da resposta. Você vai usar em TODAS as próximas requisições.

## 👤 Testando Endpoints de Autor

### 1. Criar Autor
**Configuração:**
- **Método**: `POST`
- **URL**: `http://localhost:8080/api/autores`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer SEU_TOKEN_AQUI`

**Body (raw JSON):**
```json
{
    "nome": "João Silva",
    "email": "joao@email.com",
    "cep": "12345-678",
    "telefone": "(11) 99999-9999"
}
```

**Resultado Esperado**: Status 201 Created
```json
{
    "idAutor": 1,
    "nome": "João Silva",
    "email": "joao@email.com"
}
```

### 2. Listar Autores
**Configuração:**
- **Método**: `GET`
- **URL**: `http://localhost:8080/api/autores`

**Headers:**
- `Authorization: Bearer SEU_TOKEN_AQUI`

**Resultado Esperado**: Status 200 OK
```json
{
    "content": [
        {
            "idAutor": 1,
            "nome": "João Silva"
        }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
}
```

### 3. Buscar Autor por ID
**Configuração:**
- **Método**: `GET`
- **URL**: `http://localhost:8080/api/autores/1`

**Headers:**
- `Authorization: Bearer SEU_TOKEN_AQUI`

**Resultado Esperado**: Status 200 OK
```json
{
    "idAutor": 1,
    "nome": "João Silva",
    "email": "joao@email.com"
}
```

### 4. Atualizar Autor
**Configuração:**
- **Método**: `PUT`
- **URL**: `http://localhost:8080/api/autores/1`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer SEU_TOKEN_AQUI`

**Body (raw JSON):**
```json
{
    "nome": "João Santos",
    "email": "joao.santos@email.com",
    "cep": "54321-876",
    "telefone": "(11) 88888-8888"
}
```

**Resultado Esperado**: Status 200 OK com dados atualizados

### 5. Buscar Autor por Email
**Configuração:**
- **Método**: `GET`
- **URL**: `http://localhost:8080/api/autores/busca?email=joao@email.com`

**Headers:**
- `Authorization: Bearer SEU_TOKEN_AQUI`

### 6. Deletar Autor (Soft Delete)
**Configuração:**
- **Método**: `DELETE`
- **URL**: `http://localhost:8080/api/autores/1`

**Headers:**
- `Authorization: Bearer SEU_TOKEN_AQUI`

**Resultado Esperado**: Status 204 No Content

## 📚 Testando Endpoints de Livro

### 1. Criar Livro
**Configuração:**
- **Método**: `POST`
- **URL**: `http://localhost:8080/api/livros`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer SEU_TOKEN_AQUI`

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

**Resultado Esperado**: Status 201 Created
```json
{
    "uuid": "550e8400-e29b-41d4-a716-446655440000",
    "titulo": "Clean Code",
    "isbn": "978-0132350884",
    "dataDePublicacao": "2008-08-01",
    "categoriaNome": "Tecnologia",
    "autoresNome": ["João Silva"]
}
```

### 2. Listar Livros
**Configuração:**
- **Método**: `GET`
- **URL**: `http://localhost:8080/api/livros`

**Headers:**
- `Authorization: Bearer SEU_TOKEN_AQUI`

### 3. Buscar Livro por ID
**Configuração:**
- **Método**: `GET`
- **URL**: `http://localhost:8080/api/livros/1`

**Headers:**
- `Authorization: Bearer SEU_TOKEN_AQUI`

### 4. Atualizar Livro
**Configuração:**
- **Método**: `PUT`
- **URL**: `http://localhost:8080/api/livros/1`

**Headers:**
- `Content-Type: application/json`
- `Authorization: Bearer SEU_TOKEN_AQUI`

**Body (raw JSON):**
```json
{
    "titulo": "Clean Code - Edição Atualizada",
    "dataDePublicacao": "2008-08-01",
    "categoria": "TECNOLOGIA"
}
```

### 5. Deletar Livro
**Configuração:**
- **Método**: `DELETE`
- **URL**: `http://localhost:8080/api/livros/1`

**Headers:**
- `Authorization: Bearer SEU_TOKEN_AQUI`

## 📝 Como Configurar Headers no Postman

### Método 1: Aba Headers
1. Na requisição, clique na aba **"Headers"**
2. Adicione os headers:
   - **Key**: `Content-Type` | **Value**: `application/json`
   - **Key**: `Authorization` | **Value**: `Bearer SEU_TOKEN`

### Método 2: Body JSON (Automático)
1. Na aba **"Body"**, selecione **"raw"**
2. No dropdown ao lado, selecione **"JSON"**
3. O `Content-Type: application/json` é adicionado automaticamente

## 🔍 Categorias Disponíveis

Para o campo `categoria` nos livros, use:
- `"FICCAO"`
- `"NAO_FICCAO"`
- `"TECNOLOGIA"`
- `"CIENCIA"`
- `"HISTORIA"`

## ⚠️ Problemas Comuns e Soluções

### 403 Forbidden
**Causa**: Token JWT ausente ou inválido
**Solução**: 
1. Fazer login novamente
2. Copiar o novo token
3. Adicionar no header Authorization

### 400 Bad Request
**Causa**: JSON malformado ou validação falhou
**Solução**:
1. Verificar se o JSON está correto
2. Verificar se todos os campos obrigatórios estão preenchidos
3. Verificar formato dos dados (email, CEP, telefone)

### 409 Conflict
**Causa**: Email ou ISBN duplicado
**Solução**: Usar email/ISBN diferente

### 404 Not Found
**Causa**: Recurso não encontrado
**Solução**: Verificar se o ID existe

### 500 Internal Server Error
**Causa**: Erro no servidor
**Solução**: Verificar logs do Spring Boot no console

## 🎯 Sequência de Testes Recomendada

### Primeira Execução:
1. **POST** `/api/auth/register` (criar usuário)
2. **POST** `/api/auth/login` (obter token)
3. **POST** `/api/autores` (criar autor)
4. **GET** `/api/autores` (listar autores)
5. **POST** `/api/livros` (criar livro)
6. **GET** `/api/livros` (listar livros)

### Testes de Validação:
1. Criar autor com email inválido
2. Criar autor com email duplicado
3. Buscar autor inexistente (ID 999)
4. Criar livro com ISBN duplicado
5. Tentar acessar endpoint sem token

## 💡 Dicas Importantes

1. **Sempre copie o token completo** (incluindo pontos e caracteres especiais)
2. **Token expira em 24 horas** - faça login novamente se necessário
3. **Use Content-Type: application/json** para POST e PUT
4. **IDs são auto-incrementais** (1, 2, 3, 4...)
5. **UUIDs são gerados automaticamente** pelo sistema
6. **Soft Delete**: Recursos deletados não aparecem nas listagens mas permanecem no banco

## 🚀 Collection do Postman

Para facilitar, você pode criar uma Collection no Postman:

1. Clique em "New" → "Collection"
2. Nome: "Biblioteca API"
3. Adicione todas as requisições na collection
4. Configure variáveis de ambiente:
   - `baseUrl`: `http://localhost:8080/api`
   - `token`: (cole seu token JWT aqui)

Assim você pode usar `{{baseUrl}}/autores` e `{{token}}` nas requisições!