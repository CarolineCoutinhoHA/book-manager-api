# 📮 POSTMAN - GUIA COMPLETO PARA TESTAR A API

## 🎯 O QUE É POSTMAN?

Postman é uma ferramenta para testar APIs REST de forma visual e organizada.

### Por que usar Postman?
- ✅ **Interface amigável** - Não precisa escrever código
- ✅ **Organização** - Collections para agrupar requests
- ✅ **Variáveis** - Reutilizar URLs, tokens, etc.
- ✅ **Testes automatizados** - Scripts para validar respostas
- ✅ **Documentação** - Gerar docs automaticamente
- ✅ **Colaboração** - Compartilhar collections com equipe

---

## 🚀 CONFIGURAÇÃO INICIAL

### 1. Download e Instalação
```
1. Acesse: https://www.postman.com/downloads/
2. Baixe a versão para seu sistema operacional
3. Instale e crie uma conta (gratuita)
4. Abra o Postman
```

### 2. Criar Workspace
```
1. Clique em "Workspaces" no topo
2. Clique em "Create Workspace"
3. Nome: "Hackathon Biblioteca"
4. Tipo: "Personal"
5. Clique em "Create"
```

---

## 📁 CRIANDO COLLECTION

### 1. Nova Collection
```
1. Clique no botão "+" ao lado de "Collections"
2. Nome: "API Biblioteca"
3. Descrição: "API completa para gerenciamento de biblioteca"
4. Clique em "Create"
```

### 2. Configurar Variáveis da Collection
```
1. Clique na Collection "API Biblioteca"
2. Aba "Variables"
3. Adicionar variáveis:

Variable Name    | Initial Value              | Current Value
baseUrl         | http://localhost:8080/api  | http://localhost:8080/api
token           |                            |
username        | admin                      | admin
password        | 123456                     | 123456
```

---

## 🔐 CONFIGURANDO AUTENTICAÇÃO

### 1. Request de Login
```
Método: POST
URL: {{baseUrl}}/auth/login
Headers:
  Content-Type: application/json

Body (raw JSON):
{
  "username": "{{username}}",
  "password": "{{password}}"
}
```

### 2. Script para Capturar Token
```javascript
// Aba "Tests" do request de login
pm.test("Login successful", function () {
    pm.response.to.have.status(200);
    
    // Capturar token da resposta
    const responseJson = pm.response.json();
    const token = responseJson.token;
    
    // Salvar token na variável da collection
    pm.collectionVariables.set("token", token);
    
    console.log("Token salvo:", token);
});
```

### 3. Configurar Authorization na Collection
```
1. Clique na Collection "API Biblioteca"
2. Aba "Authorization"
3. Type: "Bearer Token"
4. Token: {{token}}
5. Salvar
```

---

## 📚 REQUESTS DE AUTORES

### 1. Criar Autor
```
Nome: "Criar Autor"
Método: POST
URL: {{baseUrl}}/autores
Authorization: Inherit from parent (usa o token da collection)

Headers:
  Content-Type: application/json

Body (raw JSON):
{
  "nome": "Machado de Assis",
  "email": "machado@literatura.com",
  "cep": "20040-020",
  "telefone": "(21) 99999-9999"
}

Tests:
pm.test("Autor criado com sucesso", function () {
    pm.response.to.have.status(201);
    
    const responseJson = pm.response.json();
    pm.expect(responseJson.nome).to.eql("Machado de Assis");
    pm.expect(responseJson.idAutor).to.be.a('number');
    
    // Salvar ID do autor para outros requests
    pm.collectionVariables.set("autorId", responseJson.idAutor);
});
```

### 2. Listar Autores
```
Nome: "Listar Autores"
Método: GET
URL: {{baseUrl}}/autores
Authorization: Inherit from parent

Params:
  page: 0
  size: 10

Tests:
pm.test("Lista de autores retornada", function () {
    pm.response.to.have.status(200);
    
    const responseJson = pm.response.json();
    pm.expect(responseJson.content).to.be.an('array');
    pm.expect(responseJson.totalElements).to.be.a('number');
});
```

### 3. Buscar Autor por ID
```
Nome: "Buscar Autor por ID"
Método: GET
URL: {{baseUrl}}/autores/{{autorId}}
Authorization: Inherit from parent

Tests:
pm.test("Autor encontrado", function () {
    pm.response.to.have.status(200);
    
    const responseJson = pm.response.json();
    pm.expect(responseJson.idAutor).to.eql(parseInt(pm.collectionVariables.get("autorId")));
});
```

### 4. Atualizar Autor
```
Nome: "Atualizar Autor"
Método: PUT
URL: {{baseUrl}}/autores/{{autorId}}
Authorization: Inherit from parent

Headers:
  Content-Type: application/json

Body (raw JSON):
{
  "nome": "Machado de Assis Atualizado",
  "email": "machado.novo@literatura.com",
  "cep": "21000-000",
  "telefone": "(21) 88888-8888"
}

Tests:
pm.test("Autor atualizado", function () {
    pm.response.to.have.status(200);
    
    const responseJson = pm.response.json();
    pm.expect(responseJson.nome).to.eql("Machado de Assis Atualizado");
});
```

### 5. Deletar Autor
```
Nome: "Deletar Autor"
Método: DELETE
URL: {{baseUrl}}/autores/{{autorId}}
Authorization: Inherit from parent

Tests:
pm.test("Autor deletado", function () {
    pm.response.to.have.status(204);
});
```

---

## 📖 REQUESTS DE LIVROS

### 1. Criar Livro
```
Nome: "Criar Livro"
Método: POST
URL: {{baseUrl}}/livros
Authorization: Inherit from parent

Headers:
  Content-Type: application/json

Body (raw JSON):
{
  "titulo": "Dom Casmurro",
  "isbn": "978-85-359-0277-5",
  "dataDePublicacao": "1899-12-01",
  "categoria": "LITERATURA",
  "autoresIds": [{{autorId}}]
}

Tests:
pm.test("Livro criado com sucesso", function () {
    pm.response.to.have.status(201);
    
    const responseJson = pm.response.json();
    pm.expect(responseJson.titulo).to.eql("Dom Casmurro");
    pm.expect(responseJson.idLivro).to.be.a('number');
    
    // Salvar ID do livro
    pm.collectionVariables.set("livroId", responseJson.idLivro);
});
```

### 2. Listar Livros
```
Nome: "Listar Livros"
Método: GET
URL: {{baseUrl}}/livros
Authorization: Inherit from parent

Tests:
pm.test("Lista de livros retornada", function () {
    pm.response.to.have.status(200);
    
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
});
```

---

## 🧪 TESTES AUTOMATIZADOS

### Scripts Úteis para Tests

#### Validar Status Code
```javascript
pm.test("Status code é 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Status code é 201", function () {
    pm.response.to.have.status(201);
});
```

#### Validar Headers
```javascript
pm.test("Content-Type é JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});
```

#### Validar Response Body
```javascript
pm.test("Response tem campo nome", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.have.property('nome');
});

pm.test("Nome não está vazio", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.nome).to.not.be.empty;
});
```

#### Validar Arrays
```javascript
pm.test("Response é um array", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson).to.be.an('array');
});

pm.test("Array não está vazio", function () {
    const responseJson = pm.response.json();
    pm.expect(responseJson.length).to.be.above(0);
});
```

#### Salvar Dados para Próximos Requests
```javascript
pm.test("Salvar ID do recurso", function () {
    const responseJson = pm.response.json();
    pm.collectionVariables.set("resourceId", responseJson.id);
});
```

---

## 🔄 EXECUTANDO COLLECTION

### 1. Executar Requests Individuais
```
1. Clique no request desejado
2. Clique em "Send"
3. Veja a resposta na parte inferior
4. Verifique os testes na aba "Test Results"
```

### 2. Executar Collection Completa
```
1. Clique nos "..." da Collection
2. Clique em "Run collection"
3. Selecione os requests a executar
4. Clique em "Run API Biblioteca"
5. Veja os resultados de todos os testes
```

### 3. Ordem de Execução Recomendada
```
1. Login (para obter token)
2. Criar Autor
3. Listar Autores
4. Buscar Autor por ID
5. Criar Livro (usando autorId)
6. Listar Livros
7. Atualizar Autor
8. Deletar Autor
```

---

## 📊 MONITORAMENTO E DEBUG

### 1. Console do Postman
```
1. View → Show Postman Console
2. Veja logs detalhados de requests/responses
3. Use console.log() nos scripts para debug
```

### 2. Variáveis de Ambiente
```
1. Clique no ícone de "olho" no canto superior direito
2. Veja todas as variáveis ativas
3. Edite valores se necessário
```

### 3. Histórico de Requests
```
1. Aba "History" na barra lateral esquerda
2. Veja todos os requests executados
3. Clique para re-executar
```

---

## 🚨 TROUBLESHOOTING

### Erro 401 - Unauthorized
```
Problema: Token inválido ou expirado
Solução:
1. Execute o request de Login novamente
2. Verifique se o token foi salvo na variável
3. Confirme que Authorization está configurado na Collection
```

### Erro 400 - Bad Request
```
Problema: Dados inválidos no body
Solução:
1. Verifique o formato JSON no body
2. Confirme que todos os campos obrigatórios estão presentes
3. Valide formatos (email, CEP, telefone)
```

### Erro de Conexão
```
Problema: Não consegue conectar com a API
Solução:
1. Verifique se a aplicação Spring Boot está rodando
2. Confirme a URL base: http://localhost:8080/api
3. Teste no navegador: http://localhost:8080/swagger-ui.html
```

### Variáveis Não Funcionam
```
Problema: {{variavel}} não é substituída
Solução:
1. Verifique se a variável está definida na Collection
2. Confirme o escopo (Collection vs Environment)
3. Use {{}} corretamente ao redor do nome
```

---

## 💡 DICAS PRO HACKATHON

### 1. Organização
```
- Crie pastas dentro da Collection (Auth, Autores, Livros)
- Use nomes descritivos para requests
- Adicione descrições nos requests
- Mantenha variáveis organizadas
```

### 2. Automação
```
- Configure testes em todos os requests
- Use scripts para capturar IDs automaticamente
- Configure Pre-request Scripts se necessário
- Execute Collection Runner para testes completos
```

### 3. Colaboração
```
- Exporte Collection para compartilhar com equipe
- Use Workspaces compartilhados
- Documente requests com exemplos
- Mantenha variáveis atualizadas
```

### 4. Backup
```
- Exporte Collection regularmente
- Salve Environment separadamente
- Mantenha versões diferentes se necessário
```

---

## 📋 CHECKLIST POSTMAN

### Configuração Inicial
- [ ] Postman instalado e conta criada
- [ ] Workspace criado
- [ ] Collection "API Biblioteca" criada
- [ ] Variáveis configuradas (baseUrl, token, etc.)

### Requests Básicos
- [ ] Login configurado com script para capturar token
- [ ] Authorization configurado na Collection
- [ ] CRUD de Autores completo
- [ ] CRUD de Livros completo
- [ ] Testes automatizados em todos os requests

### Validação
- [ ] Todos os requests funcionando
- [ ] Testes passando
- [ ] Variáveis sendo capturadas corretamente
- [ ] Collection Runner executando sem erros

### Documentação
- [ ] Requests com nomes descritivos
- [ ] Descrições adicionadas onde necessário
- [ ] Exemplos de body configurados
- [ ] Collection exportada para backup