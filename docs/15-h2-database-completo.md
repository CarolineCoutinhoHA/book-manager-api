# H2 Database - Guia Completo e Detalhado

## 🎯 Visão Geral

H2 é um banco de dados em memória escrito em Java, perfeito para desenvolvimento, testes e prototipagem. Este guia explica TUDO sobre configuração, uso e troubleshooting do H2.

## 🏗️ O que é H2 Database

### Características:
- **Em Memória**: Dados armazenados na RAM (rápido, mas temporário)
- **Embedded**: Roda dentro da aplicação Java
- **SQL Completo**: Suporta SQL padrão
- **Console Web**: Interface gráfica para consultas
- **Zero Configuração**: Funciona out-of-the-box
- **Compatível**: Simula outros bancos (MySQL, PostgreSQL, etc.)

### Vantagens:
- ✅ **Rápido**: Acesso direto à memória
- ✅ **Simples**: Não precisa instalar servidor
- ✅ **Portável**: Funciona em qualquer OS
- ✅ **Ideal para testes**: Dados limpos a cada execução
- ✅ **Console integrado**: Debug visual fácil

### Desvantagens:
- ❌ **Temporário**: Dados perdidos ao parar aplicação
- ❌ **Limitado**: Não para produção com muitos dados
- ❌ **Memória**: Consome RAM

## 🔧 Configuração Completa

### 1. Dependências (build.gradle)

```gradle
dependencies {
    // H2 Database
    runtimeOnly 'com.h2database:h2'
    
    // Spring Data JPA (para usar com H2)
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    
    // Outras dependências...
}
```

### 2. Configuração Básica (application.properties)

```properties
# ====================================================
# CONFIGURAÇÃO H2 DATABASE
# ====================================================

# URL de conexão (em memória)
spring.datasource.url=jdbc:h2:mem:bookdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password

# Habilitar console web do H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# ====================================================
# CONFIGURAÇÃO JPA/HIBERNATE
# ====================================================

# Dialeto SQL para H2
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Estratégia de criação de tabelas
spring.jpa.hibernate.ddl-auto=update

# Mostrar SQL gerado (útil para debug)
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# ====================================================
# CONFIGURAÇÕES AVANÇADAS H2
# ====================================================

# Permitir acesso remoto ao console (CUIDADO EM PRODUÇÃO!)
spring.h2.console.settings.web-allow-others=false

# Trace level para debug
# spring.h2.console.settings.trace=true
```

### 3. Configuração Avançada (application.yml)

```yaml
spring:
  # Configuração da fonte de dados
  datasource:
    url: jdbc:h2:mem:bookdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    driver-class-name: org.h2.Driver
    username: sa
    password: password
    
  # Console H2
  h2:
    console:
      enabled: true
      path: /h2-console
      settings:
        web-allow-others: false  # Segurança
        trace: false            # Debug
        
  # JPA/Hibernate
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

## 🌐 Acessando o Console H2

### 1. Iniciar Aplicação
```bash
./gradlew bootRun
```

### 2. Abrir Console
**URL**: `http://localhost:8080/h2-console`

### 3. Configuração de Login
```
JDBC URL: jdbc:h2:mem:bookdb
User Name: sa
Password: password
Driver Class: org.h2.Driver
```

### 4. Conectar
Clique em **"Connect"**

## 📊 Usando o Console H2

### Interface do Console:
- **Painel Esquerdo**: Lista de tabelas e estrutura
- **Painel Central**: Editor SQL
- **Painel Inferior**: Resultados das consultas

### Consultas Básicas:

#### 1. Ver Todas as Tabelas
```sql
SELECT * FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'PUBLIC';
```

#### 2. Ver Estrutura de Tabela
```sql
DESCRIBE AUTOR;
-- ou
SHOW COLUMNS FROM AUTOR;
```

#### 3. Consultas de Dados
```sql
-- Ver todos os autores
SELECT * FROM AUTOR;

-- Ver todos os livros
SELECT * FROM LIVRO;

-- Ver relacionamentos
SELECT * FROM AUTOR_LIVRO;

-- Ver usuários do sistema
SELECT * FROM USUARIO;
```

#### 4. Consultas com JOIN
```sql
-- Autores e seus livros
SELECT 
    a.nome AS autor,
    l.titulo AS livro,
    l.isbn,
    l.data_de_publicacao
FROM AUTOR a
JOIN AUTOR_LIVRO al ON a.id_autor = al.id_autor
JOIN LIVRO l ON al.id_livro = l.id_livro
WHERE a.disponibilidade = true;
```

#### 5. Estatísticas
```sql
-- Contar registros por tabela
SELECT 'AUTORES' AS tabela, COUNT(*) AS total FROM AUTOR
UNION ALL
SELECT 'LIVROS', COUNT(*) FROM LIVRO
UNION ALL
SELECT 'USUARIOS', COUNT(*) FROM USUARIO
UNION ALL
SELECT 'RELACIONAMENTOS', COUNT(*) FROM AUTOR_LIVRO;
```

#### 6. Consultas Avançadas
```sql
-- Autores com mais livros
SELECT 
    a.nome,
    COUNT(l.id_livro) as total_livros
FROM AUTOR a
LEFT JOIN AUTOR_LIVRO al ON a.id_autor = al.id_autor
LEFT JOIN LIVRO l ON al.id_livro = l.id_livro
GROUP BY a.id_autor, a.nome
ORDER BY total_livros DESC;

-- Livros por categoria
SELECT 
    categoria,
    COUNT(*) as quantidade
FROM LIVRO
WHERE disponibilidade = true
GROUP BY categoria;

-- Buscar por texto
SELECT * FROM AUTOR 
WHERE UPPER(nome) LIKE UPPER('%silva%');
```

## 🔍 Estrutura das Tabelas

### Tabela AUTOR
```sql
CREATE TABLE AUTOR (
    ID_AUTOR BIGINT AUTO_INCREMENT PRIMARY KEY,
    UUID UUID NOT NULL UNIQUE,
    NOME VARCHAR(255) NOT NULL,
    EMAIL VARCHAR(255) NOT NULL UNIQUE,
    CEP VARCHAR(255) NOT NULL,
    TELEFONE VARCHAR(255),
    DISPONIBILIDADE BOOLEAN NOT NULL DEFAULT TRUE
);
```

### Tabela LIVRO
```sql
CREATE TABLE LIVRO (
    ID_LIVRO BIGINT AUTO_INCREMENT PRIMARY KEY,
    UUID UUID NOT NULL UNIQUE,
    TITULO VARCHAR(255) NOT NULL,
    ISBN VARCHAR(255) NOT NULL UNIQUE,
    DATA_DE_PUBLICACAO DATE NOT NULL,
    CATEGORIA TINYINT NOT NULL,
    DISPONIBILIDADE BOOLEAN NOT NULL DEFAULT TRUE
);
```

### Tabela AUTOR_LIVRO (Relacionamento)
```sql
CREATE TABLE AUTOR_LIVRO (
    ID_LIVRO BIGINT NOT NULL,
    ID_AUTOR BIGINT NOT NULL,
    PRIMARY KEY (ID_LIVRO, ID_AUTOR),
    FOREIGN KEY (ID_AUTOR) REFERENCES AUTOR(ID_AUTOR),
    FOREIGN KEY (ID_LIVRO) REFERENCES LIVRO(ID_LIVRO)
);
```

### Tabela USUARIO (Autenticação)
```sql
CREATE TABLE USUARIO (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    USERNAME VARCHAR(255) NOT NULL UNIQUE,
    PASSWORD VARCHAR(255) NOT NULL,
    ROLE VARCHAR(255) NOT NULL DEFAULT 'USER'
);
```

## 🧪 Testando com Dados

### 1. Inserir Dados de Teste
```sql
-- Inserir autor
INSERT INTO AUTOR (UUID, NOME, EMAIL, CEP, TELEFONE, DISPONIBILIDADE) 
VALUES (RANDOM_UUID(), 'João Silva', 'joao@email.com', '12345-678', '(11) 99999-9999', true);

-- Inserir livro
INSERT INTO LIVRO (UUID, TITULO, ISBN, DATA_DE_PUBLICACAO, CATEGORIA, DISPONIBILIDADE)
VALUES (RANDOM_UUID(), 'Clean Code', '978-0132350884', '2008-08-01', 2, true);

-- Relacionar autor e livro
INSERT INTO AUTOR_LIVRO (ID_AUTOR, ID_LIVRO) VALUES (1, 1);
```

### 2. Atualizar Dados
```sql
-- Atualizar autor
UPDATE AUTOR 
SET NOME = 'João Santos', EMAIL = 'joao.santos@email.com'
WHERE ID_AUTOR = 1;

-- Soft delete (marcar como indisponível)
UPDATE AUTOR 
SET DISPONIBILIDADE = false
WHERE ID_AUTOR = 1;
```

### 3. Deletar Dados
```sql
-- Deletar relacionamento
DELETE FROM AUTOR_LIVRO WHERE ID_AUTOR = 1;

-- Deletar autor
DELETE FROM AUTOR WHERE ID_AUTOR = 1;

-- Limpar tabela
TRUNCATE TABLE AUTOR_LIVRO;
```

## 🔧 Configurações Avançadas

### 1. Persistência em Arquivo
```properties
# Em vez de memória, salvar em arquivo
spring.datasource.url=jdbc:h2:file:./data/bookdb
```

### 2. Modo Compatibilidade
```properties
# Simular MySQL
spring.datasource.url=jdbc:h2:mem:bookdb;MODE=MySQL

# Simular PostgreSQL
spring.datasource.url=jdbc:h2:mem:bookdb;MODE=PostgreSQL
```

### 3. Configurações de Performance
```properties
# Cache maior
spring.datasource.url=jdbc:h2:mem:bookdb;CACHE_SIZE=10000

# Sem delay para fechar
spring.datasource.url=jdbc:h2:mem:bookdb;DB_CLOSE_DELAY=-1
```

### 4. Scripts de Inicialização
```properties
# Executar script na inicialização
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
spring.sql.init.data-locations=classpath:data.sql
```

**Arquivo schema.sql:**
```sql
-- Criar tabelas customizadas
CREATE TABLE IF NOT EXISTS CATEGORIA (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    NOME VARCHAR(100) NOT NULL UNIQUE
);
```

**Arquivo data.sql:**
```sql
-- Inserir dados iniciais
INSERT INTO CATEGORIA (NOME) VALUES 
('Ficção'), ('Não-Ficção'), ('Tecnologia'), ('Ciência'), ('História');
```

## 🚨 Troubleshooting

### 1. Erro "Database not found"
```
Database "bookdb" not found
```

**Causa**: URL incorreta ou banco não inicializado
**Solução**:
```properties
# Verificar URL
spring.datasource.url=jdbc:h2:mem:bookdb

# Verificar se aplicação está rodando
./gradlew bootRun
```

### 2. Erro "Wrong user name or password"
```
Wrong user name or password [28000-232]
```

**Causa**: Credenciais incorretas
**Solução**:
```
User Name: sa
Password: password  # Conforme configurado
```

### 3. Console H2 não abre
```
404 Not Found - /h2-console
```

**Causa**: Console desabilitado ou path incorreto
**Solução**:
```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### 4. Erro de Sintaxe SQL
```
Syntax error in SQL statement
```

**Causa**: SQL incompatível ou erro de sintaxe
**Solução**:
- Use SQL padrão
- Verifique nomes de tabelas/colunas
- Use aspas para nomes com espaços

### 5. Tabelas não aparecem
```
No tables found
```

**Causa**: Aplicação não criou tabelas ainda
**Solução**:
1. Fazer uma requisição para API (criar autor/livro)
2. Verificar logs do Hibernate
3. Verificar ddl-auto=update

## 🔒 Segurança do H2

### Configuração Segura:
```properties
# DESENVOLVIMENTO
spring.h2.console.enabled=true
spring.h2.console.settings.web-allow-others=false

# PRODUÇÃO (DESABILITAR!)
spring.h2.console.enabled=false
```

### Perfis de Ambiente:
```yaml
# application-dev.yml
spring:
  h2:
    console:
      enabled: true

# application-prod.yml  
spring:
  h2:
    console:
      enabled: false
```

## 📊 Monitoramento e Debug

### 1. Logs Úteis
```properties
# Logs do H2
logging.level.org.h2=DEBUG

# Logs do Hibernate
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```

### 2. Métricas de Performance
```sql
-- Ver estatísticas de tabelas
SELECT * FROM INFORMATION_SCHEMA.TABLE_STATISTICS;

-- Ver índices
SELECT * FROM INFORMATION_SCHEMA.INDEXES;

-- Ver constraints
SELECT * FROM INFORMATION_SCHEMA.CONSTRAINTS;
```

### 3. Backup e Restore
```sql
-- Backup
SCRIPT TO 'backup.sql';

-- Restore
RUNSCRIPT FROM 'backup.sql';
```

## 🚀 Migração para Produção

### Do H2 para PostgreSQL:
```properties
# Desenvolvimento (H2)
spring.datasource.url=jdbc:h2:mem:bookdb
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# Produção (PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/bookdb
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Considerações:
1. **Tipos de dados** podem diferir
2. **Sintaxe SQL** pode variar
3. **Performance** será diferente
4. **Configurações** específicas do banco

## 📋 Checklist H2

### Configuração:
- [ ] Dependência H2 adicionada
- [ ] Console habilitado
- [ ] Credenciais configuradas
- [ ] JPA configurado corretamente

### Teste:
- [ ] Console acessível
- [ ] Login funcionando
- [ ] Tabelas criadas automaticamente
- [ ] Dados inseridos via API aparecem
- [ ] Consultas SQL funcionam

### Debug:
- [ ] Logs SQL habilitados
- [ ] Estrutura de tabelas correta
- [ ] Relacionamentos funcionando
- [ ] Performance adequada

## 🎯 Resumo Final

H2 Database é uma ferramenta poderosa para desenvolvimento que oferece:

1. **Setup zero** - Funciona imediatamente
2. **Console web** - Interface gráfica integrada
3. **SQL completo** - Todas as operações SQL
4. **Debug fácil** - Visualização direta dos dados
5. **Performance** - Acesso rápido em memória

**Uso ideal:**
- ✅ Desenvolvimento local
- ✅ Testes automatizados
- ✅ Prototipagem rápida
- ✅ Demonstrações

**Não usar para:**
- ❌ Produção com dados críticos
- ❌ Aplicações com muitos usuários
- ❌ Dados que precisam persistir

O H2 é perfeito para o desenvolvimento da sua API de biblioteca! 🚀