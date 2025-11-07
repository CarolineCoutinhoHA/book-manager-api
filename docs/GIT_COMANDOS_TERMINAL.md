# 🔥 GIT - COMANDOS ESSENCIAIS NO TERMINAL

## 📋 **CONFIGURAÇÃO INICIAL**

```bash
# Configurar nome e email (obrigatório)
git config --global user.name "Seu Nome"
git config --global user.email "seu.email@exemplo.com"

# Verificar configurações
git config --list
```

## 🚀 **INICIALIZAR REPOSITÓRIO**

```bash
# Criar novo repositório local
git init

# Clonar repositório existente
git clone https://github.com/usuario/repositorio.git
git clone https://github.com/usuario/repositorio.git nome-pasta
```

## 📁 **COMANDOS BÁSICOS**

```bash
# Ver status dos arquivos
git status

# Adicionar arquivos ao staging
git add arquivo.txt
git add .                    # Todos os arquivos
git add *.java              # Todos os .java
git add src/                # Pasta específica

# Fazer commit
git commit -m "Mensagem do commit"
git commit -am "Add e commit junto"  # Para arquivos já rastreados

# Ver histórico
git log
git log --oneline           # Resumido
git log --graph            # Com gráfico
```

## 🌿 **BRANCHES**

```bash
# Listar branches
git branch                  # Locais
git branch -r              # Remotos
git branch -a              # Todos

# Criar branch
git branch nova-feature
git checkout -b nova-feature  # Criar e mudar

# Mudar de branch
git checkout main
git switch main            # Comando novo

# Deletar branch
git branch -d feature-antiga
git branch -D feature-antiga  # Forçar
```

## 🔄 **SINCRONIZAÇÃO COM REMOTO**

```bash
# Adicionar repositório remoto
git remote add origin https://github.com/usuario/repo.git

# Ver remotos
git remote -v

# Enviar para remoto
git push
git push origin main
git push -u origin main    # Primeira vez

# Baixar do remoto
git pull
git pull origin main
git fetch                  # Só baixa, não merge
```

## 🔧 **DESFAZER ALTERAÇÕES**

```bash
# Desfazer mudanças não commitadas
git checkout -- arquivo.txt
git restore arquivo.txt    # Comando novo

# Remover do staging
git reset arquivo.txt
git restore --staged arquivo.txt

# Desfazer último commit (mantém arquivos)
git reset --soft HEAD~1

# Desfazer último commit (remove arquivos)
git reset --hard HEAD~1
```

## 🔍 **COMANDOS ÚTEIS**

```bash
# Ver diferenças
git diff                   # Working vs staging
git diff --staged         # Staging vs último commit
git diff HEAD~1           # Último commit vs anterior

# Buscar no código
git grep "texto"
git log --grep="bug"      # Buscar em mensagens

# Ver quem modificou cada linha
git blame arquivo.txt

# Salvar trabalho temporário
git stash
git stash pop             # Recuperar
git stash list           # Listar
```

## ⚡ **COMANDOS AVANÇADOS**

```bash
# Merge
git merge feature-branch

# Rebase
git rebase main

# Cherry-pick (pegar commit específico)
git cherry-pick abc1234

# Resetar para commit específico
git reset --hard abc1234

# Criar tag
git tag v1.0.0
git push --tags
```

## 🆘 **RESOLUÇÃO DE PROBLEMAS**

```bash
# Conflitos de merge
git status                # Ver arquivos em conflito
# Editar arquivos manualmente
git add .
git commit

# Cancelar merge
git merge --abort

# Ver histórico de comandos
git reflog

# Limpar arquivos não rastreados
git clean -f              # Arquivos
git clean -fd             # Arquivos e pastas
```

## 📝 **FLUXO TÍPICO DE TRABALHO**

```bash
# 1. Clonar projeto
git clone https://github.com/usuario/projeto.git
cd projeto

# 2. Criar branch para feature
git checkout -b minha-feature

# 3. Fazer alterações e commits
git add .
git commit -m "Implementar nova funcionalidade"

# 4. Enviar para remoto
git push -u origin minha-feature

# 5. Voltar para main e atualizar
git checkout main
git pull origin main

# 6. Fazer merge da feature
git merge minha-feature
git push origin main

# 7. Deletar branch
git branch -d minha-feature
git push origin --delete minha-feature
```

## 🔥 **COMANDOS MAIS USADOS NO DIA A DIA**

```bash
git status              # Ver o que mudou
git add .              # Adicionar tudo
git commit -m "msg"    # Fazer commit
git push               # Enviar
git pull               # Baixar atualizações
git checkout -b nome   # Nova branch
git merge branch       # Juntar branches
```

## 📋 **PASSO A PASSO: CRIAR REPOSITÓRIO DO ZERO**

```bash
# 1. Criar pasta do projeto
mkdir meu-projeto
cd meu-projeto

# 2. Inicializar Git
git init

# 3. Criar arquivo README
echo "# Meu Projeto" > README.md

# 4. Adicionar e fazer primeiro commit
git add .
git commit -m "Initial commit"

# 5. Criar repositório no GitHub (via web)
# - Ir para github.com
# - Clicar em "New repository"
# - Dar nome ao repositório
# - NÃO marcar "Initialize with README"
# - Clicar "Create repository"

# 6. Conectar com repositório remoto
git remote add origin https://github.com/SEU_USUARIO/SEU_REPOSITORIO.git

# 7. Enviar código para GitHub
git branch -M main
git push -u origin main
```

## 📋 **PASSO A PASSO: CLONAR REPOSITÓRIO**

```bash
# 1. Copiar URL do repositório no GitHub
# - Ir para o repositório no GitHub
# - Clicar no botão verde "Code"
# - Copiar a URL HTTPS

# 2. Clonar repositório
git clone https://github.com/usuario/repositorio.git

# 3. Entrar na pasta
cd repositorio

# 4. Verificar se está tudo OK
git status
git log --oneline

# 5. Criar sua branch para trabalhar
git checkout -b minha-feature

# 6. Fazer suas alterações
# ... editar arquivos ...

# 7. Commit e push
git add .
git commit -m "Minha alteração"
git push -u origin minha-feature
```

## 📋 **PASSO A PASSO: FAZER MERGE**

### **MÉTODO 1: Merge Direto (Simples)**
```bash
# 1. Ir para branch de destino (geralmente main)
git checkout main

# 2. Atualizar branch main
git pull origin main

# 3. Fazer merge da sua branch
git merge minha-feature

# 4. Enviar para remoto
git push origin main

# 5. Deletar branch local (opcional)
git branch -d minha-feature

# 6. Deletar branch remota (opcional)
git push origin --delete minha-feature
```

### **MÉTODO 2: Merge via Pull Request (Recomendado)**
```bash
# 1. Enviar sua branch para GitHub
git push -u origin minha-feature

# 2. Ir para GitHub no navegador
# 3. Clicar em "Compare & pull request"
# 4. Preencher título e descrição
# 5. Clicar "Create pull request"
# 6. Aguardar revisão (se necessário)
# 7. Clicar "Merge pull request"
# 8. Clicar "Confirm merge"

# 9. Atualizar seu repositório local
git checkout main
git pull origin main

# 10. Deletar branch local
git branch -d minha-feature
```

### **RESOLVER CONFLITOS DE MERGE**
```bash
# 1. Tentar fazer merge
git merge minha-feature
# Se houver conflito, aparecerá mensagem

# 2. Ver arquivos em conflito
git status

# 3. Editar arquivos manualmente
# Procurar por:
# <<<<<<< HEAD
# código da branch atual
# =======
# código da branch sendo merged
# >>>>>>> minha-feature

# 4. Escolher qual código manter e remover marcadores

# 5. Adicionar arquivos resolvidos
git add arquivo-resolvido.txt

# 6. Finalizar merge
git commit -m "Resolver conflitos de merge"

# 7. Enviar para remoto
git push origin main
```

## ⚠️ **DICAS IMPORTANTES**

- **Sempre** faça `git status` antes de qualquer comando
- **Nunca** faça `git push --force` em branch compartilhada
- **Sempre** teste antes de fazer merge para main
- Use mensagens de commit **descritivas**
- Faça commits **pequenos e frequentes**
- **Sempre** atualize a main antes de fazer merge
- Use **Pull Requests** para projetos em equipe