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

## ⚠️ **DICAS IMPORTANTES**

- **Sempre** faça `git status` antes de qualquer comando
- **Nunca** faça `git push --force` em branch compartilhada
- **Sempre** teste antes de fazer merge para main
- Use mensagens de commit **descritivas**
- Faça commits **pequenos e frequentes**