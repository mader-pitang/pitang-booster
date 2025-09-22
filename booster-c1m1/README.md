# 🚀 Pitang Booster C1M1 - User Management API

[![CI Pipeline](https://github.com/mader-pitang/pitang-booster/actions/workflows/ci.yml/badge.svg)](https://github.com/mader-pitang/pitang-booster/actions/workflows/ci.yml)
[![Code Coverage](https://img.shields.io/badge/Coverage-%E2%89%A565%25-brightgreen)](target/site/jacoco/index.html)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen)
![Maven](https://img.shields.io/badge/Maven-3.6+-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![License](https://img.shields.io/badge/License-MIT-green)

> **Pitang Booster Ciclo 1 Meta 1** - REST API completa para gerenciamento de usuários e produtos

## 📋 **Visão Geral**

Esta é uma aplicação **Spring Boot** que implementa um sistema completo de gerenciamento de usuários com:

- ✅ **CRUD completo** de usuários e produtos
- ✅ **Validações robustas** com Bean Validation
- ✅ **Documentação OpenAPI/Swagger** automática
- ✅ **Métricas e monitoramento** com Actuator + Prometheus
- ✅ **Pipeline CI/CD** completo no GitHub Actions
- ✅ **Pre-commit hooks** para qualidade garantida
- ✅ **Cobertura de testes ≥ 65%** obrigatória
- ✅ **Análise estática** com Checkstyle, PMD e SpotBugs

## 🏗️ **Arquitetura**

```
src/
├── main/java/com/pitang/booster_c1m1/
│   ├── controller/     # REST Controllers
│   ├── service/        # Business Logic
│   ├── repository/     # Data Access Layer
│   ├── domain/         # Entities
│   ├── dto/            # Data Transfer Objects
│   ├── mapper/         # MapStruct Mappers
│   └── config/         # Configuration Classes
└── test/java/
    ├── controller/     # Controller Tests
    ├── service/        # Service Tests
    └── integration/    # Integration Tests
```

## 🚀 **Quick Start**

### **Pré-requisitos**
- ☕ Java 17+
- 📦 Maven 3.6+
- 🐘 PostgreSQL 15+ (ou Docker)

### **1. Clone e Configure**
```bash
git clone https://github.com/mader-pitang/pitang-booster.git
cd booster-c1m1

# Instalar hooks de qualidade (recomendado)
chmod +x scripts/install-hooks.sh
./scripts/install-hooks.sh
```

### **2. Configure o Banco**
```bash
# Opção 1: Docker (recomendado)
docker run -d \
  --name postgres-booster \
  -e POSTGRES_DB=booster_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15

# Opção 2: PostgreSQL local
# Configure as credenciais em application.properties
```

### **3. Execute a Aplicação**
```bash
mvn spring-boot:run
```

### **4. Acesse a Documentação**
- 📖 **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- 📄 **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- 📊 **Métricas**: http://localhost:8080/actuator/metrics
- 💓 **Health Check**: http://localhost:8080/actuator/health

## 🔧 **Desenvolvimento**

### **Comandos Principais**
```bash
# Compilar e validar qualidade
mvn clean compile

# Executar testes
mvn test

# Executar com cobertura
mvn test jacoco:report

# Build completo
mvn clean package

# Verificar qualidade (separadamente)
mvn checkstyle:check pmd:check spotbugs:check
```

### **Estrutura de Testes**
- 🧪 **77 testes** implementados
- 📊 **Cobertura ≥ 65%** obrigatória
- 🔄 **Testes unitários** para todas as camadas
- 🌐 **Testes de integração** com TestContainers

## 📊 **API Endpoints**

### **Users API**
```http
GET    /api/users              # Listar usuários (paginado)
GET    /api/users/{id}         # Buscar por ID
GET    /api/users/search       # Buscar por nome
POST   /api/users              # Criar usuário
PUT    /api/users/{id}         # Atualizar usuário
DELETE /api/users/{id}         # Deletar usuário
```

### **Exemplo de Uso**
```bash
# Criar usuário
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "João Silva",
    "email": "joao@email.com",
    "password": "senha123"
  }'

# Listar usuários
curl http://localhost:8080/api/users?page=0&size=10
```

## 🛡️ **Qualidade de Código**

### **🔒 Pre-commit Hooks Automáticos**
O projeto inclui hooks Git que executam **automaticamente**:

#### **Antes de cada commit:**
- ✅ Checkstyle (estilo de código)
- ✅ Testes unitários
- ✅ Verificação de cobertura ≥ 65%
- ✅ SpotBugs (detecção de bugs)

#### **Antes de cada push:**
- ✅ Build completo
- ✅ Testes de integração
- ✅ Verificações adicionais

### **📈 Métricas de Qualidade**
- **Cobertura de linha**: ≥ 65%
- **Cobertura de branch**: ≥ 60%
- **Checkstyle**: Google style guide
- **PMD**: Best practices + performance
- **SpotBugs**: Detecção de bugs

## 🔄 **CI/CD Pipeline**

### **GitHub Actions**
O pipeline executa automaticamente em `push` e `pull_request`:

1. 🧪 **Test Job** - Testes + cobertura
2. 🔍 **Lint Job** - Análise estática
3. 🔨 **Build Job** - Compilação + empacotamento
4. 📋 **OpenAPI Job** - Validação da documentação

### **Branches Protegidos**
- `main` - Produção
- `dev` - Desenvolvimento

## 🏃‍♂️ **Workflow de Desenvolvimento**

```bash
# 1. Criar branch
git checkout -b feature/nova-funcionalidade

# 2. Desenvolver (hooks rodam automaticamente)
git add .
git commit -m "feat: implementar nova funcionalidade"

# 3. Push (hooks + CI rodam automaticamente)
git push origin feature/nova-funcionalidade

# 4. Criar Pull Request
# 5. Merge após aprovação + CI verde
```

## 📁 **Documentação Adicional**

- 📖 **[CI/CD Pipeline](./CI-CD.md)** - Documentação completa do pipeline
- 🔧 **[Development Guide](./DEVELOPMENT.md)** - Guia para desenvolvedores
- 🐛 **[Troubleshooting](./TROUBLESHOOTING.md)** - Soluções para problemas comuns

## 🛠️ **Stack Tecnológica**

### **Backend**
- ☕ **Java 17** - Linguagem
- 🍃 **Spring Boot 3.5.5** - Framework
- 🗃️ **Spring Data JPA** - ORM
- ✅ **Bean Validation** - Validações
- 🔄 **MapStruct** - Object mapping

### **Banco de Dados**
- 🐘 **PostgreSQL 15** - Produção
- 🧪 **H2** - Testes

### **Documentação**
- 📖 **SpringDoc OpenAPI** - Documentação automática
- 🎨 **Swagger UI** - Interface interativa

### **Qualidade & Testes**
- 🧪 **JUnit 5** - Testes unitários
- 🌐 **TestContainers** - Testes de integração
- 📊 **JaCoCo** - Cobertura de código
- 📋 **Checkstyle** - Estilo de código
- 🔍 **PMD** - Análise estática
- 🐛 **SpotBugs** - Detecção de bugs

### **Monitoramento**
- 📊 **Actuator** - Métricas e health checks
- 📈 **Micrometer** - Métricas para Prometheus
- 📝 **Logback** - Logging estruturado

### **Build & Deploy**
- 📦 **Maven** - Build tool
- 🔄 **GitHub Actions** - CI/CD
- 🐳 **Docker** - Containerização (futuro)

### **Padrões de Commit**
```
feat: adicionar nova funcionalidade
fix: corrigir bug
docs: atualizar documentação
style: formatação de código
refactor: refatoração
test: adicionar testes
chore: tarefas de manutenção
```

## 📄 **Licença**

Este projeto está licenciado sob a [MIT License](LICENSE).

---

<div align="center">

**🚀 Desenvolvido com ❤️ pela equipe Pitang**

[![GitHub](https://img.shields.io/badge/GitHub-pitang--booster-blue?logo=github)](https://github.com/your-org/booster-c1m1)

</div>