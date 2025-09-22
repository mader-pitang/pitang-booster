# 🚨 Troubleshooting Guide

Soluções para problemas comuns no projeto Pitang Booster C1M1.

## 🔧 **Problemas de Setup**

### ❌ **Hooks não executam**

**Sintomas:**
- Commit passa sem executar verificações
- Mensagens dos hooks não aparecem

**Soluções:**
```bash
# 1. Verificar permissões
ls -la .git/hooks/pre-commit
# Deve mostrar: -rwxr-xr-x (executável)

# 2. Reinstalar hooks
./scripts/install-hooks.sh

# 3. Executar manualmente para testar
.git/hooks/pre-commit

# 4. Verificar se Git está configurado corretamente
git config --list | grep hook
```

### ❌ **Banco de dados não conecta**

**Sintomas:**
- `Connection refused` ao executar aplicação
- Testes de integração falham

**Soluções:**

#### **PostgreSQL Docker**
```bash
# Verificar se container está rodando
docker ps | grep postgres

# Iniciar container
docker start postgres-booster

# Ou criar novo container
docker run -d \
  --name postgres-booster \
  -e POSTGRES_DB=booster_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15

# Testar conexão
docker exec -it postgres-booster psql -U postgres -d booster_db -c "SELECT version();"
```

#### **PostgreSQL Local**
```bash
# Verificar se PostgreSQL está rodando
sudo systemctl status postgresql

# Iniciar PostgreSQL (Ubuntu/Debian)
sudo systemctl start postgresql

# Verificar portas
netstat -tulpn | grep 5432

# Testar conexão
psql -h localhost -U postgres -d booster_db
```

### ❌ **Permissões negadas no script**

**Sintomas:**
- `Permission denied: ./scripts/install-hooks.sh`

**Solução:**
```bash
# Dar permissão de execução
chmod +x scripts/install-hooks.sh

# Verificar permissões
ls -la scripts/install-hooks.sh
```

## 🧪 **Problemas de Testes**

### ❌ **Testes falhando aleatoriamente**

**Sintomas:**
- Testes passam às vezes, falham outras
- Problemas de concorrência

**Soluções:**
```bash
# 1. Limpar cache do Maven
mvn clean

# 2. Executar testes isoladamente
mvn test -Dtest=UserServiceTest

# 3. Verificar isolamento de testes
mvn test -Dtest=UserServiceTest -Dmaven.surefire.forkCount=1 -Dmaven.surefire.reuseForks=false

# 4. Debug específico
mvn test -Dtest=UserServiceTest -Dmaven.surefire.debug
```

### ❌ **Cobertura abaixo de 65%**

**Sintomas:**
- `BUILD FAILURE` com mensagem de cobertura
- JaCoCo check falhando

**Soluções:**
```bash
# 1. Gerar relatório de cobertura
mvn test jacoco:report

# 2. Abrir relatório HTML
open target/site/jacoco/index.html
# ou no Windows: start target/site/jacoco/index.html

# 3. Identificar classes sem cobertura
# No relatório, procurar por linhas vermelhas

# 4. Adicionar testes para métodos não cobertos
# Focar em:
# - Casos de erro (exceptions)
# - Validações
# - Branches condicionais
```

### ❌ **H2 Database conflicts**

**Sintomas:**
- Testes falhando com erros de banco
- `Table not found` em testes

**Soluções:**
```bash
# 1. Limpar target
rm -rf target/

# 2. Verificar profile de teste
mvn test -Dspring.profiles.active=test

# 3. Executar com logs SQL
mvn test -Dlogging.level.org.hibernate.SQL=DEBUG

# 4. Verificar application-test.properties
cat src/test/resources/application-test.properties
```

## 🔍 **Problemas de Qualidade**

### ❌ **Checkstyle violations**

**Sintomas:**
- Muitas violações de estilo
- Build falhando no Checkstyle

**Soluções Comuns:**

#### **Import statements**
```bash
# Erro: Unused imports
# Solução: Remover imports não utilizados
# No IntelliJ: Ctrl+Alt+O
# No Eclipse: Ctrl+Shift+O
```

#### **Line length**
```bash
# Erro: Line is longer than 120 characters
# Solução: Quebrar linha longa
// ❌ Muito longo
public UserDTO createUser(String name, String email, String password, String address, String phone) {

// ✅ Correto
public UserDTO createUser(String name, String email, String password,
                         String address, String phone) {
```

#### **Missing Javadoc**
```java
// ❌ Sem documentação
public void deleteUser(Long id) {

// ✅ Com documentação
/**
 * Deletes a user by ID.
 * @param id the user ID
 * @throws UserNotFoundException if user not found
 */
public void deleteUser(Long id) {
```

### ❌ **SpotBugs issues**

**Sintomas:**
- SpotBugs detecta bugs potenciais
- Build falhando no SpotBugs

**Soluções Comuns:**

#### **Null pointer potential**
```java
// ❌ Possível NPE
public String getUserName(User user) {
    return user.getName().toUpperCase();
}

// ✅ Com null check
public String getUserName(User user) {
    return user != null && user.getName() != null
        ? user.getName().toUpperCase()
        : "UNKNOWN";
}
```

#### **Resource leak**
```java
// ❌ Possível resource leak
FileInputStream fis = new FileInputStream("file.txt");
// ... usar fis
fis.close(); // Pode não executar se exception antes

// ✅ Try-with-resources
try (FileInputStream fis = new FileInputStream("file.txt")) {
    // ... usar fis
} // Fechado automaticamente
```

### ❌ **PMD violations**

**Sintomas:**
- PMD detecta problemas de design
- Build falhando no PMD

**Soluções Comuns:**

#### **Unused variables**
```java
// ❌ Variável não usada
public void processUser(User user) {
    String name = user.getName(); // Não usada
    log.info("Processing user");
}

// ✅ Remover variável
public void processUser(User user) {
    log.info("Processing user: {}", user.getName());
}
```

#### **Empty catch block**
```java
// ❌ Catch vazio
try {
    riskyOperation();
} catch (Exception e) {
    // Vazio
}

// ✅ Log da exception
try {
    riskyOperation();
} catch (Exception e) {
    log.error("Error in risky operation", e);
    throw new ServiceException("Operation failed", e);
}
```

## 🚀 **Problemas de Build**

### ❌ **Maven dependency conflicts**

**Sintomas:**
- `ClassNotFoundException`
- Conflitos de versão

**Soluções:**
```bash
# 1. Verificar árvore de dependências
mvn dependency:tree

# 2. Resolver conflitos
mvn dependency:resolve

# 3. Forçar download
mvn dependency:purge-local-repository

# 4. Limpar e rebuildar
mvn clean install
```

### ❌ **OutOfMemoryError durante build**

**Sintomas:**
- `Java heap space` durante testes
- Build trava ou falha

**Soluções:**
```bash
# 1. Aumentar memória do Maven
export MAVEN_OPTS="-Xmx2g -XX:MaxPermSize=512m"

# 2. Ou via comando
mvn clean package -Dmaven.surefire.heap.size=2g

# 3. Para Windows (PowerShell)
$env:MAVEN_OPTS="-Xmx2g"
```

### ❌ **Port already in use**

**Sintomas:**
- `Port 8080 is already in use`
- Aplicação não inicia

**Soluções:**
```bash
# 1. Verificar o que está usando a porta
lsof -i :8080
# ou no Windows: netstat -ano | findstr :8080

# 2. Matar processo
kill -9 <PID>
# ou no Windows: taskkill /PID <PID> /F

# 3. Usar porta diferente
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

## 🔄 **Problemas de Git/CI**

### ❌ **GitHub Actions falhando**

**Sintomas:**
- Pipeline vermelho no GitHub
- Jobs falhando na CI

**Debug:**
```bash
# 1. Executar comandos da CI localmente
mvn clean compile
mvn test
mvn checkstyle:check pmd:check spotbugs:check
mvn clean package

# 2. Verificar logs no GitHub
# Actions tab > Failed job > Expandir steps com erro

# 3. Testar com mesmo Java version
sdk use java 17.0.2-tem  # Se usar SDKMAN
```

### ❌ **Pre-commit hook muito lento**

**Sintomas:**
- Commit demora muito
- Timeout nos hooks

**Soluções:**
```bash
# 1. Executar apenas testes rápidos no pre-commit
# Editar .git/hooks/pre-commit e mudar:
mvn test -Dtest="*Test" -DfailIfNoTests=false

# 2. Pular hooks temporariamente (emergência)
git commit --no-verify -m "emergency fix"

# 3. Otimizar testes
# Usar @QuickTest annotation para testes rápidos
```

### ❌ **Merge conflicts**

**Sintomas:**
- Conflitos ao fazer merge
- Git não consegue resolver automaticamente

**Soluções:**
```bash
# 1. Atualizar branch com main/dev
git checkout feature/sua-branch
git pull origin dev
git rebase dev

# 2. Resolver conflitos manualmente
# Editar arquivos marcados com <<<<<<< ======= >>>>>>>

# 3. Continuar rebase
git add .
git rebase --continue

# 4. Em caso de emergência, abortar
git rebase --abort
```

## 📞 **Quando Pedir Ajuda**

Se os problemas persistirem:

1. 📋 **Colete informações:**
   ```bash
   # Versões
   java -version
   mvn -version
   git --version

   # Logs completos
   mvn clean package -X > build.log 2>&1
   ```

2. 🐛 **Abra uma issue com:**
   - Descrição do problema
   - Passos para reproduzir
   - Logs de erro
   - Ambiente (OS, Java version, etc)

3. 📧 **Contatos:**
   - GitHub Issues: [Criar Issue](../../issues)
   - Email: mader.gabriel@pitang.com

## 🔗 **Links Úteis**

- 📖 [Maven Troubleshooting](https://maven.apache.org/guides/mini/guide-ide-eclipse.html)
- 🐛 [SpotBugs Bug Patterns](https://spotbugs.readthedocs.io/en/stable/bugDescriptions.html)
- 📋 [Checkstyle Rules](https://checkstyle.sourceforge.io/checks.html)
- 🔍 [PMD Rules](https://pmd.github.io/pmd-6.55.0/pmd_rules_java.html)

---

**💡 Dica**: Mantenha um log dos problemas encontrados e soluções para facilitar debugging futuro!