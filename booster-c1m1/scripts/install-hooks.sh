#!/bin/bash

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}🔧 Installing Git hooks...${NC}"

mkdir -p scripts

cp scripts/pre-commit .git/hooks/ 2>/dev/null || {
    cat > .git/hooks/pre-commit << 'EOF'
#!/bin/bash

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${YELLOW}🔍 Running pre-commit checks...${NC}"

echo -e "${YELLOW}📋 Running Checkstyle...${NC}"
if ! mvn checkstyle:check -q; then
    echo -e "${RED}❌ Checkstyle violations found!${NC}"
    echo -e "${YELLOW}💡 Fix style issues and try again${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Checkstyle passed${NC}"

echo -e "${YELLOW}🧪 Running tests...${NC}"
if ! mvn test -q; then
    echo -e "${RED}❌ Tests failed!${NC}"
    echo -e "${YELLOW}💡 Fix failing tests and try again${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Tests passed${NC}"

echo -e "${YELLOW}📊 Checking code coverage (minimum 65%)...${NC}"
if ! mvn jacoco:check -q; then
    echo -e "${RED}❌ Code coverage below 65%!${NC}"
    echo -e "${YELLOW}💡 Add more tests to increase coverage${NC}"
    echo -e "${YELLOW}📈 Check coverage report: target/site/jacoco/index.html${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Code coverage meets minimum requirements${NC}"

echo -e "${YELLOW}🐛 Running SpotBugs...${NC}"
if ! mvn spotbugs:check -q; then
    echo -e "${RED}❌ SpotBugs found potential issues!${NC}"
    echo -e "${YELLOW}💡 Check and fix reported issues${NC}"
    exit 1
fi
echo -e "${GREEN}✅ SpotBugs passed${NC}"

echo -e "${GREEN}🎉 All pre-commit checks passed! Proceeding with commit...${NC}"
exit 0
EOF
}

cp scripts/pre-push .git/hooks/ 2>/dev/null || {
    # Se não existir em scripts/, criar diretamente
    cat > .git/hooks/pre-push << 'EOF'
#!/bin/bash

# Cores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🚀 Running pre-push checks...${NC}"

# 1. Build completo
echo -e "${YELLOW}🔨 Running full build...${NC}"
if ! mvn clean package -q; then
    echo -e "${RED}❌ Build failed!${NC}"
    echo -e "${YELLOW}💡 Fix build errors before pushing${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Build successful${NC}"

# 2. Executar testes de integração
echo -e "${YELLOW}🧪 Running integration tests...${NC}"
if ! mvn verify -q; then
    echo -e "${RED}❌ Integration tests failed!${NC}"
    echo -e "${YELLOW}💡 Fix integration test failures${NC}"
    exit 1
fi
echo -e "${GREEN}✅ Integration tests passed${NC}"

# 3. Verificar se há commits não commitados
if ! git diff --quiet HEAD; then
    echo -e "${RED}❌ You have uncommitted changes!${NC}"
    echo -e "${YELLOW}💡 Commit or stash your changes before pushing${NC}"
    exit 1
fi

echo -e "${GREEN}🎉 All pre-push checks passed! Proceeding with push...${NC}"
exit 0
EOF
}

# Tornar hooks executáveis
chmod +x .git/hooks/pre-commit
chmod +x .git/hooks/pre-push

echo -e "${GREEN}✅ Git hooks installed successfully!${NC}"
echo -e "${YELLOW}📝 Now every commit will check:${NC}"
echo -e "  • Code style (Checkstyle)"
echo -e "  • Tests passing"
echo -e "  • Code coverage ≥ 65%"
echo -e "  • Bug analysis (SpotBugs)"
echo ""
echo -e "${YELLOW}📤 And every push will check:${NC}"
echo -e "  • Full build success"
echo -e "  • Integration tests"
echo -e "  • No uncommitted changes"