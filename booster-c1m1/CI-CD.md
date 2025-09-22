# Pipeline CI/CD - Pitang Booster C1M1

Este projeto conta com um pipeline de CI/CD completo usando GitHub Actions que garante a qualidade do código e a integridade das funcionalidades.

## 🏗️ Estrutura do Pipeline

O pipeline é composto por 4 jobs principais que executam em paralelo quando possível:

### 1. **Test Job** 🧪
- **Objetivo**: Executar testes unitários e de integração
- **Serviços**: PostgreSQL 15 para testes de integração
- **Comandos**:
  - `mvn clean test` - Testes unitários
  - `mvn verify` - Testes de integração
  - `mvn jacoco:report` - Relatório de cobertura
- **Artifacts**: Relatórios de cobertura enviados para Codecov

### 2. **Lint Job** 🔍
- **Objetivo**: Análise estática de código e qualidade
- **Ferramentas**:
  - **Checkstyle**: Verificação de estilo de código
  - **PMD**: Detecção de problemas de código
  - **SpotBugs**: Análise de bugs potenciais
- **Configuração**: Não falha o build, apenas reporta warnings

### 3. **Build Job** 🔨
- **Objetivo**: Compilar e empacotar a aplicação
- **Dependências**: Aguarda conclusão dos jobs de Test e Lint
- **Comandos**:
  - `mvn clean package -DskipTests` - Build da aplicação
  - `docker build` - Build da imagem Docker (se Dockerfile existir)
- **Artifacts**: JAR da aplicação

### 4. **OpenAPI Validation Job** 📋
- **Objetivo**: Validar especificação OpenAPI/Swagger
- **Dependências**: Aguarda conclusão do Build Job
- **Validações**:
  - Geração da especificação OpenAPI em `/v3/api-docs`
  - Acessibilidade do Swagger UI em `/swagger-ui/index.html`
- **Artifacts**: Especificação OpenAPI em formato JSON

## 🛠️ Ferramentas de Qualidade Configuradas

### Maven Plugins Adicionados

#### Checkstyle Plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-checkstyle-plugin</artifactId>
    <version>3.5.0</version>
</plugin>
```
- **Configuração**: `checkstyle.xml` personalizado
- **Regras**: Baseado em padrões de qualidade, com linha máxima de 120 caracteres

#### SpotBugs Plugin
```xml
<plugin>
    <groupId>com.github.spotbugs</groupId>
    <artifactId>spotbugs-maven-plugin</artifactId>
    <version>4.8.6.4</version>
</plugin>
```
- **Configuração**: Máximo esforço, threshold baixo
- **Objetivo**: Detectar bugs potenciais e vulnerabilidades

#### PMD Plugin
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-pmd-plugin</artifactId>
    <version>3.25.0</version>
</plugin>
```
- **Rulesets**: Best practices, code style, design, error prone, performance

#### JaCoCo Plugin
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.12</version>
</plugin>
```
- **Funcionalidade**: Cobertura de código
- **Relatórios**: HTML e XML para integração com ferramentas externas

## 🚀 Triggers do Pipeline

O pipeline é executado automaticamente nos seguintes eventos:

- **Push** para branches `main` e `dev`
- **Pull Request** para branches `main` e `dev`

## 📊 Monitoramento e Relatórios

### Cobertura de Código
- **Ferramenta**: JaCoCo + Codecov
- **Relatórios**: Disponível no Codecov após cada execução
- **Threshold**: Configurado para não falhar o build

### Qualidade de Código
- **Checkstyle**: Warnings reportados no console
- **PMD**: Análise de qualidade estrutural
- **SpotBugs**: Detecção de bugs e vulnerabilidades

## 🔧 Executando Localmente

### Comandos Disponíveis

```bash
# Compilação completa com qualidade
mvn clean compile

# Executar apenas testes
mvn test

# Executar análise de qualidade
mvn checkstyle:check pmd:check spotbugs:check

# Build completo
mvn clean package

# Relatório de cobertura
mvn jacoco:report
```

### Requisitos
- **Java 17+**
- **Maven 3.6+**
- **Docker** (opcional, para containerização)

## 📝 Configurações Importantes

### Profiles Maven
- **test**: Configurado para usar H2 database em testes
- **dev**: Profile padrão para desenvolvimento local

### Banco de Dados
- **Produção**: PostgreSQL
- **Testes**: H2 in-memory database
- **CI/CD**: PostgreSQL 15 em container

### OpenAPI/Swagger
- **Endpoint**: `http://localhost:8080/v3/api-docs`
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **Documentação**: Gerada automaticamente pelos controllers

## 🔒 **Pre-commit Hooks - Qualidade Garantida**

### Hooks Configurados

O projeto conta com **Git hooks** que executam **antes de cada commit/push** para garantir qualidade:

#### 🔍 **Pre-commit** (Executa em cada `git commit`)
- ✅ **Checkstyle** - Verifica estilo de código
- ✅ **Testes** - Executa todos os testes unitários
- ✅ **Cobertura ≥ 65%** - Falha se cobertura for menor que 65%
- ✅ **SpotBugs** - Detecta bugs potenciais

#### 🚀 **Pre-push** (Executa em cada `git push`)
- ✅ **Build completo** - `mvn clean package`
- ✅ **Testes de integração** - `mvn verify`
- ✅ **Verificação de changes** - Bloqueia push com mudanças não commitadas

### 📦 **Instalação dos Hooks**

```bash
# Executar uma vez no projeto
chmod +x scripts/install-hooks.sh
./scripts/install-hooks.sh
```

### 🚫 **O que acontece se falhar?**

- **❌ Commit rejeitado** se qualquer verificação falhar
- **❌ Push rejeitado** se build/testes falharem
- **💡 Mensagens claras** indicando o que precisa ser corrigido

### ⚙️ **Configuração de Cobertura**

```xml
<!-- Mínimo 65% cobertura de linhas -->
<!-- Mínimo 60% cobertura de branches -->
<minimum>0.65</minimum>
<minimum>0.60</minimum>
```
