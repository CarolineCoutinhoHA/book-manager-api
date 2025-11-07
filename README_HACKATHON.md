# 🚀 GUIA COMPLETO PARA HACKATHON - API BIBLIOTECA

## 📋 RESUMO DO PROJETO

API REST completa para gerenciamento de biblioteca com:
- **CRUD de Autores e Livros**
- **Relacionamento Many-to-Many** (Autor ↔ Livro)
- **Autenticação JWT** completa
- **Validações robustas** com Bean Validation
- **Documentação Swagger** interativa
- **Testes automatizados** (15 testes passando)
- **CORS configurado** para frontend

---

## 🛠️ TECNOLOGIAS UTILIZADAS

### Backend Core
- **Spring Boot 3.5.7** - Framework principal
- **Spring Data JPA** - Persistência de dados
- **Spring Security** - Segurança e autenticação
- **H2 Database** - Banco em memória para desenvolvimento

### Autenticação & Segurança
- **JWT (JSON Web Tokens)** - Autenticação stateless
- **BCrypt** - Hash seguro de senhas
- **CORS** - Comunicação com frontend

### Documentação & Testes
- **Swagger/OpenAPI 3** - Documentação interativa
- **JUnit 5** - Testes automatizados
- **Mockito** - Mocks para testes

### Utilitários
- **MapStruct** - Mapeamento DTO ↔ Entity
- **Lombok** - Redução de boilerplate
- **Bean Validation** - Validações automáticas

---

## 🚀 COMO EXECUTAR

### 1. Clonar e Executar
```bash
git clone <seu-repositorio>
cd demo
./gradlew bootRun
```

### 2. Acessar Interfaces
- **API Base**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console

### 3. Configuração H2 Console
- **JDBC URL**: `jdbc:h2:mem:bookdb`
- **Username**: `sa`
- **Password**: (vazio)

---

## 🔐 AUTENTICAÇÃO JWT - GUIA RÁPIDO

### 1. Registrar Usuário
```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

### 2. Fazer Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "123456"
}
```

### 3. Usar Token
```http
GET /api/autores
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📚 ENDPOINTS PRINCIPAIS

### 🔓 Públicos (sem autenticação)
- `POST /api/auth/register` - Criar conta
- `POST /api/auth/login` - Fazer login
- `GET /api/auth/me` - Verificar token atual

### 🔒 Protegidos (precisam de JWT)

#### Autores
- `GET /api/autores` - Listar todos
- `GET /api/autores/{id}` - Buscar por ID
- `POST /api/autores` - Criar novo
- `PUT /api/autores/{id}` - Atualizar
- `DELETE /api/autores/{id}` - Deletar (soft delete)

#### Livros
- `GET /api/livros` - Listar todos
- `GET /api/livros/{id}` - Buscar por ID
- `POST /api/livros` - Criar novo (com autores)
- `PUT /api/livros/{id}` - Atualizar
- `DELETE /api/livros/{id}` - Deletar (soft delete)

---

## 📝 EXEMPLOS DE PAYLOADS

### Criar Autor
```json
{
  "nome": "Machado de Assis",
  "email": "machado@literatura.com",
  "cep": "20040-020",
  "telefone": "(21) 99999-9999"
}
```

### Criar Livro
```json
{
  "titulo": "Dom Casmurro",
  "isbn": "978-85-359-0277-5",
  "dataDePublicacao": "1899-12-01",
  "categoria": "LITERATURA",
  "autoresIds": [1, 2]
}
```

---

## 🎯 VALIDAÇÕES IMPLEMENTADAS

### Autor
- **Nome**: Obrigatório, máx 100 caracteres
- **Email**: Formato válido, único no sistema
- **CEP**: Formato XXXXX-XXX
- **Telefone**: Formato (XX) XXXXX-XXXX

### Livro
- **Título**: Obrigatório, máx 100 caracteres
- **ISBN**: 10-17 caracteres, apenas números e hífens, único
- **Data**: Não pode ser futura
- **Categoria**: LITERATURA, CIENCIA, BIOGRAFIA, FANTASIA, MISTERIO
- **Autores**: Pelo menos 1 autor obrigatório

---

## 🧪 TESTES AUTOMATIZADOS

### Executar Todos os Testes
```bash
./gradlew test
```

### Cobertura Atual: 15/15 testes (100%)
- **AutorController**: 5 testes
- **LivroController**: 3 testes  
- **AutorService**: 4 testes
- **LivroService**: 3 testes

---

## 🏗️ ARQUITETURA DO PROJETO

```
src/main/java/com/example/demo/
├── config/           # Configurações (CORS, Swagger, Security)
├── controller/       # Controllers REST
├── dto/             # DTOs (Request/Response)
├── exception/       # Tratamento de exceções
├── mapper/          # MapStruct mappers
├── model/           # Entidades JPA
├── repository/      # Repositórios Spring Data
├── security/        # JWT e configurações de segurança
└── service/         # Lógica de negócio
```

---

## 💡 DICAS PARA HACKATHON

### 1. Desenvolvimento Rápido
- Use o **Swagger** para testar endpoints rapidamente
- **H2 Console** para verificar dados no banco
- **Logs** estão configurados para debug

### 2. Extensões Fáceis
- Adicionar novos campos: Apenas atualizar DTO e Entity
- Novos endpoints: Seguir padrão dos controllers existentes
- Novas validações: Usar anotações Bean Validation

### 3. Frontend Integration
- CORS já configurado para `localhost:3000`
- JWT no header: `Authorization: Bearer {token}`
- Todas as respostas são JSON padronizadas

### 4. Troubleshooting
- **401 Unauthorized**: Token inválido ou expirado
- **403 Forbidden**: Endpoint protegido sem token
- **400 Bad Request**: Dados inválidos (verificar validações)
- **404 Not Found**: Recurso não existe

---

## 🔧 CONFIGURAÇÕES IMPORTANTES

### application.properties
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:bookdb
spring.h2.console.enabled=true

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true

# Swagger
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
```

### JWT Configuration
- **Algoritmo**: HS256
- **Expiração**: 24 horas
- **Header**: `Authorization: Bearer {token}`

---

## 🎉 PRONTO PARA O HACKATHON!

Este projeto fornece uma base sólida com:
- ✅ Autenticação completa
- ✅ CRUD funcional
- ✅ Validações robustas
- ✅ Documentação interativa
- ✅ Testes automatizados
- ✅ Arquitetura escalável

**Foque no seu diferencial e boa sorte! 🚀**