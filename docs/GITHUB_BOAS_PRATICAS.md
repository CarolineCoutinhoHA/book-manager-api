# 🏆 GITHUB - BOAS PRÁTICAS E CONVENÇÕES

## 🌿 **ESTRATÉGIA DE BRANCHES**

### **Git Flow Simplificado**
```
main (produção)
├── develop (desenvolvimento)
│   ├── feature/login-usuario
│   ├── feature/crud-produtos
│   └── feature/api-pagamento
├── hotfix/corrigir-bug-critico
└── release/v1.2.0
```

### **Nomenclatura de Branches**
```bash
# ✅ CORRETO
feature/implementar-login
feature/crud-usuarios
bugfix/corrigir-validacao-email
hotfix/resolver-erro-500
release/v1.0.0
docs/atualizar-readme

# ❌ INCORRETO
nova-funcionalidade
bug
teste
branch-do-joao
```

### **Tipos de Branches**
- **`main`** - Código em produção (sempre estável)
- **`develop`** - Integração de features (desenvolvimento)
- **`feature/`** - Novas funcionalidades
- **`bugfix/`** - Correção de bugs
- **`hotfix/`** - Correções urgentes em produção
- **`release/`** - Preparação para nova versão
- **`docs/`** - Atualizações de documentação

## 📝 **CONVENÇÕES DE COMMIT**

### **Formato Padrão**
```
tipo(escopo): descrição curta

Descrição detalhada (opcional)

Closes #123
```

### **Tipos de Commit**
```bash
feat: nova funcionalidade
fix: correção de bug
docs: documentação
style: formatação (sem mudança de código)
refactor: refatoração de código
test: adição/correção de testes
chore: tarefas de manutenção
perf: melhoria de performance
ci: configuração de CI/CD
build: mudanças no build
```

### **Exemplos de Commits**
```bash
# ✅ CORRETO
feat(auth): implementar login com JWT
fix(api): corrigir validação de email
docs(readme): adicionar guia de instalação
refactor(service): simplificar lógica de validação
test(controller): adicionar testes para AuthController

# ❌ INCORRETO
mudança no login
bug corrigido
atualização
código novo
```

## 🔄 **WORKFLOW DE DESENVOLVIMENTO**

### **1. Criar Nova Feature**
```bash
# 1. Atualizar main
git checkout main
git pull origin main

# 2. Criar branch da feature
git checkout -b feature/nome-da-feature

# 3. Desenvolver e fazer commits
git add .
git commit -m "feat(feature): implementar funcionalidade X"

# 4. Enviar para remoto
git push -u origin feature/nome-da-feature
```

### **2. Pull Request (PR)**
```bash
# 1. Ir para GitHub
# 2. Clicar "Compare & pull request"
# 3. Preencher template:

Título: feat(auth): Implementar sistema de login

## Descrição
- Adicionar endpoint de login
- Implementar validação JWT
- Criar middleware de autenticação

## Tipo de mudança
- [x] Nova funcionalidade
- [ ] Correção de bug
- [ ] Documentação

## Checklist
- [x] Código testado
- [x] Documentação atualizada
- [x] Sem conflitos

## Screenshots (se aplicável)
![Login Screen](url-da-imagem)

Closes #123
```

### **3. Code Review**
```bash
# Revisor deve verificar:
- Código segue padrões do projeto
- Testes estão incluídos
- Documentação foi atualizada
- Não há código duplicado
- Performance não foi impactada
```

## 📋 **TEMPLATES DE ISSUE**

### **Bug Report**
```markdown
## 🐛 Descrição do Bug
Descrição clara do que está acontecendo.

## 🔄 Passos para Reproduzir
1. Ir para '...'
2. Clicar em '...'
3. Ver erro

## ✅ Comportamento Esperado
O que deveria acontecer.

## 📱 Ambiente
- OS: [Windows/Mac/Linux]
- Browser: [Chrome/Firefox/Safari]
- Versão: [v1.0.0]

## 📸 Screenshots
Se aplicável, adicione screenshots.
```

### **Feature Request**
```markdown
## 🚀 Descrição da Feature
Descrição clara da funcionalidade desejada.

## 💡 Motivação
Por que essa feature é importante?

## 📝 Solução Proposta
Como você imagina que isso deveria funcionar?

## 🔄 Alternativas
Outras soluções consideradas.

## ✅ Critérios de Aceitação
- [ ] Critério 1
- [ ] Critério 2
- [ ] Critério 3
```

## 🏷️ **SISTEMA DE TAGS E RELEASES**

### **Versionamento Semântico**
```
v1.2.3
│ │ │
│ │ └── PATCH (correções)
│ └──── MINOR (novas features)
└────── MAJOR (breaking changes)
```

### **Criar Release**
```bash
# 1. Criar tag
git tag -a v1.0.0 -m "Release v1.0.0"
git push origin v1.0.0

# 2. No GitHub:
# - Ir para "Releases"
# - Clicar "Create a new release"
# - Selecionar tag v1.0.0
# - Preencher changelog
```

### **Changelog Exemplo**
```markdown
# Changelog v1.0.0

## 🚀 Novas Features
- Implementar sistema de login (#123)
- Adicionar CRUD de usuários (#124)
- Integrar API de pagamento (#125)

## 🐛 Correções
- Corrigir validação de email (#126)
- Resolver erro 500 na API (#127)

## 📚 Documentação
- Atualizar README com guia de instalação
- Adicionar documentação da API

## ⚠️ Breaking Changes
- Alterar estrutura da resposta da API de usuários
```

## 🔒 **SEGURANÇA E PROTEÇÕES**

### **Configurações de Branch Protection**
```bash
# No GitHub, configurar:
- Require pull request reviews
- Require status checks to pass
- Require branches to be up to date
- Restrict pushes to matching branches
- Require linear history
```

### **Secrets e Variáveis**
```bash
# ❌ NUNCA fazer commit de:
- Senhas
- API Keys
- Tokens
- Certificados
- Dados sensíveis

# ✅ Usar GitHub Secrets:
- Settings > Secrets and variables > Actions
- Adicionar secrets como: DATABASE_URL, API_KEY
```

## 📊 **ORGANIZAÇÃO DO REPOSITÓRIO**

### **Estrutura de Pastas**
```
projeto/
├── .github/
│   ├── workflows/          # GitHub Actions
│   ├── ISSUE_TEMPLATE/     # Templates de issues
│   └── pull_request_template.md
├── docs/                   # Documentação
├── src/                    # Código fonte
├── tests/                  # Testes
├── .gitignore             # Arquivos ignorados
├── README.md              # Documentação principal
├── CONTRIBUTING.md        # Guia de contribuição
└── LICENSE               # Licença
```

### **README.md Completo**
```markdown
# 📚 Nome do Projeto

Descrição breve do projeto.

## 🚀 Tecnologias
- Java 21
- Spring Boot 3.x
- H2 Database

## 📋 Pré-requisitos
- Java 21+
- Gradle 8+

## 🔧 Instalação
```bash
git clone https://github.com/usuario/projeto.git
cd projeto
./gradlew bootRun
```

## 📖 Documentação
- [API Docs](http://localhost:8080/swagger-ui.html)
- [Guia de Contribuição](CONTRIBUTING.md)

## 🤝 Contribuindo
1. Fork o projeto
2. Crie sua branch (`git checkout -b feature/AmazingFeature`)
3. Commit suas mudanças (`git commit -m 'feat: Add AmazingFeature'`)
4. Push para a branch (`git push origin feature/AmazingFeature`)
5. Abra um Pull Request

## 📄 Licença
MIT License - veja [LICENSE](LICENSE) para detalhes.
```

## 🏆 **BOAS PRÁTICAS GERAIS**

### **✅ FAÇA**
- Use nomes descritivos para branches e commits
- Mantenha PRs pequenos e focados
- Escreva testes para novas funcionalidades
- Atualize documentação junto com código
- Faça code review cuidadoso
- Use templates de issue e PR
- Configure branch protection
- Mantenha histórico limpo

### **❌ NÃO FAÇA**
- Commit direto na main
- PRs gigantes com muitas mudanças
- Commits com mensagens vagas
- Ignorar conflitos de merge
- Fazer force push em branches compartilhadas
- Commitar secrets ou senhas
- Deixar branches órfãs
- Ignorar feedback de code review

## 🎯 **MÉTRICAS E QUALIDADE**

### **Indicadores de Qualidade**
- Cobertura de testes > 80%
- Tempo de review < 24h
- PRs pequenos (< 400 linhas)
- Build sempre verde
- Documentação atualizada

### **Ferramentas Recomendadas**
- **SonarQube** - Qualidade de código
- **Dependabot** - Atualizações de dependências
- **GitHub Actions** - CI/CD
- **CodeQL** - Análise de segurança