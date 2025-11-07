# ⚡ GRADLE - COMANDOS ESSENCIAIS NO TERMINAL

## 🚀 **COMANDOS BÁSICOS**

```bash
# Verificar versão do Gradle
gradle --version
gradlew --version          # Usando wrapper

# Listar todas as tasks disponíveis
gradle tasks
gradlew tasks

# Listar tasks de um grupo específico
gradle tasks --group build
gradlew tasks --group application
```

## 🏗️ **BUILD E COMPILAÇÃO**

```bash
# Compilar o projeto
gradle build
gradlew build

# Compilar sem executar testes
gradle build -x test
gradlew build -x test

# Limpar build anterior
gradle clean
gradlew clean

# Limpar e compilar
gradle clean build
gradlew clean build
```

## ☕ **SPRING BOOT ESPECÍFICO**

```bash
# Executar aplicação Spring Boot
gradle bootRun
gradlew bootRun

# Executar em porta específica
gradle bootRun --args='--server.port=8081'
gradlew bootRun --args='--server.port=8081'

# Criar JAR executável
gradle bootJar
gradlew bootJar

# Executar JAR criado
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar
```

## 🧪 **TESTES**

```bash
# Executar todos os testes
gradle test
gradlew test

# Executar testes específicos
gradle test --tests "AutorControllerTest"
gradlew test --tests "*Controller*"

# Executar testes com relatório detalhado
gradle test --info
gradlew test --debug

# Pular testes
gradle build -x test
gradlew build -x test
```

## 📦 **DEPENDÊNCIAS**

```bash
# Ver todas as dependências
gradle dependencies
gradlew dependencies

# Ver dependências de configuração específica
gradle dependencies --configuration compileClasspath
gradlew dependencies --configuration runtimeClasspath

# Verificar dependências desatualizadas
gradle dependencyUpdates
gradlew dependencyUpdates

# Baixar dependências
gradle build --refresh-dependencies
gradlew build --refresh-dependencies
```

## 🔧 **DESENVOLVIMENTO**

```bash
# Executar com live reload (Spring Boot DevTools)
gradlew bootRun

# Compilar apenas código Java
gradle compileJava
gradlew compileJava

# Compilar testes
gradle compileTestJava
gradlew compileTestJava

# Processar recursos
gradle processResources
gradlew processResources
```

## 📊 **RELATÓRIOS E ANÁLISE**

```bash
# Gerar relatório de testes
gradle test
# Relatório em: build/reports/tests/test/index.html

# Verificar qualidade do código (se configurado)
gradle check
gradlew check

# Gerar documentação Javadoc
gradle javadoc
gradlew javadoc
```

## 🐛 **DEBUG E TROUBLESHOOTING**

```bash
# Executar com logs detalhados
gradle build --info
gradlew build --debug

# Ver stack trace completo
gradle build --stacktrace
gradlew build --stacktrace

# Executar em modo offline
gradle build --offline
gradlew build --offline

# Limpar cache do Gradle
gradle clean --refresh-dependencies
gradlew clean --refresh-dependencies
```

## 🔄 **GRADLE WRAPPER**

```bash
# Gerar wrapper (se não existir)
gradle wrapper

# Atualizar wrapper para versão específica
gradle wrapper --gradle-version 8.5

# Verificar integridade do wrapper
gradlew --version
```

## 📁 **ESTRUTURA DE ARQUIVOS**

```bash
# Ver estrutura do projeto
gradle projects
gradlew projects

# Executar task específica
gradle :subprojeto:build
gradlew :subprojeto:test
```

## ⚡ **COMANDOS MAIS USADOS**

```bash
# Desenvolvimento diário
gradlew bootRun           # Executar aplicação
gradlew build -x test     # Build rápido
gradlew test             # Executar testes
gradlew clean build      # Build completo

# Resolução de problemas
gradlew clean            # Limpar tudo
gradlew build --refresh-dependencies  # Recarregar deps
gradlew build --stacktrace          # Ver erros detalhados
```

## 🎯 **SPRING BOOT - COMANDOS ESPECÍFICOS**

```bash
# Executar com perfil específico
gradlew bootRun --args='--spring.profiles.active=dev'

# Executar com propriedades customizadas
gradlew bootRun --args='--server.port=9090 --debug'

# Gerar JAR e executar
gradlew bootJar
java -jar build/libs/demo-0.0.1-SNAPSHOT.jar

# Executar com JVM options
gradlew bootRun -Dspring.profiles.active=prod
```

## 🔥 **PERFORMANCE E OTIMIZAÇÃO**

```bash
# Build paralelo (mais rápido)
gradlew build --parallel

# Build com daemon (mais rápido)
gradlew build --daemon

# Build incremental
gradlew build --build-cache

# Configurar daemon permanente
echo "org.gradle.daemon=true" >> gradle.properties
echo "org.gradle.parallel=true" >> gradle.properties
```

## 🆘 **RESOLUÇÃO DE PROBLEMAS COMUNS**

```bash
# Erro de permissão no gradlew (Linux/Mac)
chmod +x gradlew

# Limpar cache completamente
rm -rf ~/.gradle/caches/
gradlew clean build

# Problema com porta ocupada
netstat -ano | findstr :8080    # Windows
lsof -i :8080                   # Linux/Mac

# Matar processo Java
taskkill /F /IM java.exe        # Windows
pkill -f java                   # Linux/Mac
```

## 📝 **FLUXO TÍPICO DE DESENVOLVIMENTO**

```bash
# 1. Clonar projeto
git clone https://github.com/usuario/projeto.git
cd projeto

# 2. Verificar se Gradle funciona
gradlew --version

# 3. Build inicial
gradlew clean build

# 4. Executar aplicação
gradlew bootRun

# 5. Durante desenvolvimento
gradlew test                    # Testar mudanças
gradlew build -x test          # Build rápido
gradlew bootRun               # Executar com live reload

# 6. Antes de commit
gradlew clean build           # Build completo
gradlew test                  # Todos os testes
```

## ⚠️ **DICAS IMPORTANTES**

- **Sempre** use `gradlew` em vez de `gradle` (wrapper)
- **Sempre** execute `gradlew clean` quando houver problemas estranhos
- Use `-x test` para builds mais rápidos durante desenvolvimento
- Configure `gradle.properties` para melhor performance
- Mantenha o Gradle Wrapper atualizado