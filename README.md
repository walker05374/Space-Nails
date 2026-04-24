# Space Nails (Cantinho)

Um sistema completo de agendamento e gerenciamento, dividido em Backend (Spring Boot) e Frontend (Vue 3 + Vite).

## 🚀 Tecnologias

- **Frontend:** Vue 3, Vite, Tailwind CSS, Pinia, Vue Router
- **Backend:** Java 17+, Spring Boot, JPA/Hibernate
- **Banco de Dados:** PostgreSQL 15
- **Infraestrutura:** Docker e Docker Compose

## 📁 Estrutura do Projeto

- `BACKEND-SPACE/`: API em Spring Boot.
- `FRONTEND-SPACE/`: Aplicação SPA em Vue 3.
- `docker-compose.yml`: Orquestração de containers (Backend, Frontend e Banco de Dados).
- `Iniciar-Sistema.bat` e `start-local.ps1`: Scripts de conveniência para iniciar o sistema localmente.

## 🛠️ Como Executar

### Usando Docker (Recomendado)

Na raiz do projeto, execute:
```bash
docker-compose up --build
```
Isso irá levantar o banco de dados PostgreSQL, a API Spring Boot na porta `8080` e o Frontend Nginx na porta `3000`. Acesse `http://localhost:3000`.

### Executando Localmente (Desenvolvimento)

1. **Banco de Dados:**
   Certifique-se de ter um PostgreSQL rodando com um banco chamado `space_db` (ou suba apenas o serviço `db` do compose: `docker-compose up db -d`).
   
2. **Backend:**
   Entre na pasta `BACKEND-SPACE` e execute o Maven:
   ```bash
   cd BACKEND-SPACE
   ./mvnw spring-boot:run
   ```

3. **Frontend:**
   Entre na pasta `FRONTEND-SPACE`, instale as dependências e rode o Vite:
   ```bash
   cd FRONTEND-SPACE
   npm install
   npm run dev
   ```

## 📄 Testes
Para rodar todos os testes automatizados, utilize o script na raiz:
```bash
./run_all_tests.ps1
```
