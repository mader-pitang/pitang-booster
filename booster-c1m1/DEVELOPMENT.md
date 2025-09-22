# 🛠️ Development Guide

Guia completo para desenvolvedores do projeto Pitang Booster C1M1.

## 📋 **Setup Inicial**

### **1. Ambiente Local**
```bash
# Clone o repositório
git clone <repository-url>
cd booster-c1m1

# Instalar hooks de qualidade (OBRIGATÓRIO)
chmod +x scripts/install-hooks.sh
./scripts/install-hooks.sh
```

### **2. Banco de Dados**

#### **Opção A: Docker (Recomendado)**
```bash
docker run -d \
  --name postgres-booster \
  -e POSTGRES_DB=booster_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:15
```

#### **Opção B: PostgreSQL Local**
```sql
CREATE DATABASE booster_db;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE booster_db TO postgres;
```

### **3. Profiles de Ambiente**

#### **Development (application.properties)**
```properties
spring.profiles.active=dev
spring.datasource.url=jdbc:postgresql://localhost:5432/booster_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

#### **Test (application-test.properties)**
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

## 🔄 **Workflow de Desenvolvimento**

### **1. Criar Feature Branch**
```bash
git checkout -b feature/nome-da-funcionalidade
```

### **2. Padrões de Código**

#### **Convenções**
- **Classes**: `PascalCase` (ex: `UserService`)
- **Methods**: `camelCase` (ex: `getUserById`)
- **Variables**: `camelCase` (ex: `userId`)
- **Constants**: `UPPER_SNAKE_CASE` (ex: `MAX_PAGE_SIZE`)

#### **Estrutura de Pastas**
```
com.pitang.booster_c1m1/
├── controller/         # REST endpoints
├── service/           # Business logic
├── repository/        # Data access
├── domain/           # JPA entities
├── dto/              # Data transfer objects
├── mapper/           # MapStruct mappers
├── config/           # Configuration classes
└── exception/        # Custom exceptions

## 🧪 **Testes**

### **Estrutura de Testes**
```
test/java/
├── controller/        # @WebMvcTest
├── service/          # @ExtendWith(MockitoExtension.class)
└── integration/      # @SpringBootTest + @Testcontainers
```

### **Padrões de Teste**

#### **Testes Unitários**
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("Should create user when valid input")
    void createUser_ShouldReturnUser_WhenValidInput() {
        // Given
        CreateUserDTO dto = CreateUserDTO.builder()
            .name("João Silva")
            .email("joao@email.com")
            .password("senha123")
            .build();

        User savedUser = User.builder()
            .id(1L)
            .name("João Silva")
            .email("joao@email.com")
            .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        UserDTO result = userService.createUser(dto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("João Silva");
        verify(userRepository).save(any(User.class));
    }
}
```

#### **Testes de Integração**
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Test
    void createUser_ShouldReturn201_WhenValidInput() throws Exception {
        // Test implementation
    }
}
```

### **Cobertura de Testes**
```bash
# Executar testes com cobertura
mvn test jacoco:report

# Verificar cobertura (≥ 65%)
mvn jacoco:check

# Ver relatório HTML
open target/site/jacoco/index.html
```

## 🔍 **Qualidade de Código**

### **Pre-commit Hooks**
Os hooks executam automaticamente a cada commit:

```bash
git commit -m "feat: implementar nova funcionalidade"
# 🔍 Running pre-commit checks...
# 📋 Running Checkstyle...
# ✅ Checkstyle passed
# 🧪 Running tests...
# ✅ Tests passed
# 📊 Checking code coverage...
# ✅ Code coverage meets requirements
# 🐛 Running SpotBugs...
# ✅ SpotBugs passed
```

### **Comandos de Qualidade**

```bash
# Checkstyle (estilo de código)
mvn checkstyle:check

# PMD (análise estática)
mvn pmd:check

# SpotBugs (detecção de bugs)
mvn spotbugs:check

# Todos juntos
mvn checkstyle:check pmd:check spotbugs:check
```

## 📖 **API Development**

### **Criando Novos Endpoints**

#### **1. Controller**
```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management operations")
public class UserController {

    @PostMapping
    @Operation(summary = "Create a new user")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input"),
        @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<UserDTO> createUser(
            @Valid @RequestBody CreateUserDTO createUserDTO) {
        UserDTO user = userService.createUser(createUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

#### **2. Service**
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final Counter userCreatedCounter;

    public UserDTO createUser(CreateUserDTO createUserDTO) {
        log.debug("Creating user with email: {}", createUserDTO.getEmail());

        // Validations
        if (userRepository.existsByEmail(createUserDTO.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        // Business logic
        User user = UserMapper.INSTANCE.toUser(createUserDTO);
        user.setCreatedAt(Instant.now().toString());

        User savedUser = userRepository.save(user);
        userCreatedCounter.increment();

        log.info("User created with id: {}", savedUser.getId());
        return UserMapper.INSTANCE.toDto(savedUser);
    }
}
```

#### **3. DTO com Validação**
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateUserDTO {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;
}
```

### **Documentação OpenAPI**
A documentação é gerada automaticamente. Acesse:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

## 🔧 **Debugging**

### **1. Application Properties para Debug**
```properties
# Logging
logging.level.com.pitang.booster_c1m1=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG

# JPA
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### **2. Debug no IDE**
```bash
# Executar em modo debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

### **3. Profiles de Debug**
```bash
# Executar com profile específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev,debug
```

## 📦 **Build e Deploy**

### **1. Build Local**
```bash
# Desenvolvimento
mvn clean compile

# Build completo
mvn clean package

# Pular testes (não recomendado)
mvn clean package -DskipTests
```

### **2. Docker (Futuro)**
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/booster-c1m1-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🚨 **Troubleshooting**

### **Problemas Comuns**

#### **1. Hooks não executam**
```bash
# Verificar permissões
ls -la .git/hooks/pre-commit

# Reinstalar hooks
./scripts/install-hooks.sh
```

#### **2. Testes falhando**
```bash
# Limpar cache Maven
mvn clean

# Executar teste específico
mvn test -Dtest=UserServiceTest

# Debug de teste
mvn test -Dtest=UserServiceTest -Dmaven.surefire.debug
```

#### **3. Banco de dados**
```bash
# Verificar conexão
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev --debug"

# Reset do banco H2 (testes)
rm -rf target/
```

## 📋 **Checklist para PR**

Antes de abrir um Pull Request:

- [ ] ✅ Testes passando (`mvn test`)
- [ ] ✅ Cobertura ≥ 65% (`mvn jacoco:check`)
- [ ] ✅ Checkstyle sem violações (`mvn checkstyle:check`)
- [ ] ✅ Build completo (`mvn clean package`)
- [ ] ✅ Documentação atualizada
- [ ] ✅ Commits seguem padrão conventional
- [ ] ✅ Branch atualizada com `dev`

## 🔗 **Links Úteis**

- 📖 [Spring Boot Docs](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
- 🧪 [JUnit 5 Guide](https://junit.org/junit5/docs/current/user-guide/)
- 📊 [JaCoCo Documentation](https://www.jacoco.org/jacoco/trunk/doc/)
- 🔍 [Checkstyle Rules](https://checkstyle.sourceforge.io/checks.html)
- 📋 [OpenAPI Specification](https://swagger.io/specification/)

---

**💡 Dica**: Use `mvn clean compile` regularmente para verificar qualidade antes de commits!