# 📅 Microsserviço Agendador de Tarefas (API Agendador-Tarefas)

Uma API RESTful desenvolvida com **Java 17** e **Spring Boot 3**, responsável pelo agendamento e gerenciamento de tarefas vinculadas a usuários. Faz parte de uma **arquitetura de microsserviços**, comunicando-se com o [microsserviço de Usuários](https://github.com/mauricioRodriguesDev/usuario) via **OpenFeign** para validação de autenticação. Utiliza **MongoDB** como banco de dados NoSQL e autenticação **stateless via JWT**.

---

## 🚀 Tecnologias Utilizadas

| Categoria | Tecnologia |
| :--- | :--- |
| **Linguagem** | Java 17 |
| **Framework** | Spring Boot 3.5.8 |
| **Banco de Dados** | MongoDB |
| **Segurança** | Spring Security + JWT (`io.jsonwebtoken:jjwt 0.13.0`) |
| **Comunicação entre Microsserviços** | Spring Cloud OpenFeign |
| **Mapeamento de Objetos** | MapStruct 1.5.3 |
| **Produtividade** | Lombok |
| **Build Tool** | Gradle (com Wrapper `gradlew`) |
| **CI/CD** | GitHub Actions (Workflow de build com Gradle) |

---

## 📌 Funcionalidades

- **Criar Tarefa:** Cadastro de novas tarefas vinculadas ao usuário autenticado (extraído automaticamente do token JWT).
- **Listar Tarefas por Usuário:** Consulta de todas as tarefas do usuário logado.
- **Buscar Tarefas por Período:** Filtro de tarefas agendadas entre datas de início e fim.
- **Atualizar Tarefa:** Edição completa dos dados de uma tarefa existente.
- **Alterar Status da Tarefa:** Atualização parcial (`PATCH`) do status de notificação (`PENDENTE`, `NOTIFICADO`, `CANCELADO`).
- **Excluir Tarefa:** Remoção de tarefas pelo ID.
- **Integração com Microsserviço de Usuários:** Validação de credenciais via Feign Client consumindo a API de Usuários.

---

## 🏛️ Arquitetura do Projeto

### Visão Geral — Microsserviços

```text
┌────────────────────┐        Feign Client (HTTP)        ┌────────────────────┐
│                    │  ──────────────────────────────▶   │                    │
│  Agendador-Tarefas │    GET /usuario?email=...         │     Usuário API    │
│   (porta 8081)     │    + Header Authorization         │   (porta 8080)     │
│                    │  ◀──────────────────────────────   │                    │
│    MongoDB         │        UsuarioDTO response        │    PostgreSQL      │
└────────────────────┘                                   └────────────────────┘
```

### Estrutura de Camadas

```text
com.javanauta.agendadortarefas
├── business/                    # Regras de negócio
│   ├── dto/                     # Data Transfer Objects (TarefasDTO, UsuarioDTO)
│   ├── mapper/                  # Converters MapStruct (TarefasConverter, TarefaUpdateConverter)
│   └── service/
│       └── TarefasService.java  # Lógica de negócio e orquestração
├── controller/                  # Controladores REST expostos
│   └── TarefasController.java
└── infrastructure/              # Infraestrutura técnica
    ├── client/                  # Feign Clients para comunicação inter-serviços
    │   └── UsuarioClient.java
    ├── entity/                  # Documentos MongoDB
    │   └── TarefasEntity.java
    ├── enums/                   # Enumerações (StatusNotificacaoEnum)
    ├── exceptions/              # Exceções customizadas
    ├── repository/              # Interfaces Spring Data MongoDB
    └── security/                # Filtros JWT e configuração de segurança
```

---

## 📊 Modelo de Dados

### Documento `tarefa` (MongoDB)

| Campo | Tipo | Descrição |
| :--- | :--- | :--- |
| `id` | `String` (ObjectId) | Identificador único gerado pelo MongoDB |
| `nomeTarefa` | `String` | Nome/título da tarefa |
| `descricao` | `String` | Descrição detalhada da tarefa |
| `emailUsuario` | `String` | E-mail do usuário proprietário (extraído do JWT) |
| `dataCriacao` | `LocalDateTime` | Data e hora de criação (preenchido automaticamente) |
| `dataEvento` | `LocalDateTime` | Data e hora do evento agendado |
| `dataAlteracao` | `LocalDateTime` | Data e hora da última alteração |
| `statusNotificacaoEnum` | `Enum` | Status da notificação: `PENDENTE`, `NOTIFICADO` ou `CANCELADO` |

### Status de Notificação

| Valor | Descrição |
| :--- | :--- |
| `PENDENTE` | Tarefa criada, aguardando notificação |
| `NOTIFICADO` | Tarefa já notificada ao usuário |
| `CANCELADO` | Tarefa cancelada |

---

## 📋 Pré-requisitos

Antes de iniciar, certifique-se de ter instalado:

- [JDK 17+](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
- [MongoDB](https://www.mongodb.com/try/download/community) (em execução na porta padrão `27017`)
- [Git](https://git-scm.com/)
- **Microsserviço de Usuários** rodando na porta `8080` → [repositório usuario](https://github.com/mauricioRodriguesDev/usuario)

---

## ⚙️ Configuração

As configurações recomendadas utilizam variáveis de ambiente com valores de fallback seguro no arquivo `src/main/resources/application.properties`:

```properties
spring.application.name=agendador-tarefas
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/db_agendador}

usuario.url=${USUARIO_URL:localhost:8080}

server.port=${SERVER_PORT:8081}
```

### 🔒 Variáveis de Ambiente Recomendadas

- **Linux / macOS:**
  ```bash
  export MONGODB_URI=mongodb://localhost:27017/db_agendador
  export USUARIO_URL=localhost:8080
  export SERVER_PORT=8081
  ```

- **Windows (PowerShell):**
  ```powershell
  $env:MONGODB_URI="mongodb://localhost:27017/db_agendador"
  $env:USUARIO_URL="localhost:8080"
  $env:SERVER_PORT="8081"
  ```

| Propriedade | Descrição |
| :--- | :--- |
| `spring.data.mongodb.uri` | URI de conexão com o MongoDB (banco `db_agendador`) |
| `usuario.url` | URL base do microsserviço de Usuários (Feign Client) |
| `server.port` | Porta da aplicação (padrão: `8081`) |

> ⚠️ **Importante:** O banco `db_agendador` é criado automaticamente pelo MongoDB na primeira inserção. O microsserviço de Usuários **precisa estar rodando** na porta `8080` para que a autenticação funcione.

> 🔒 **Controle de Acesso às Tarefas (Segurança contra IDOR):** As operações de alteração (`PUT`, `PATCH`) e deleção (`DELETE`) por `id` devem garantir que o registro pertença ao mesmo usuário extraído do token JWT autenticado, prevenindo que um usuário acesse ou modifique tarefas de terceiros.

---

## 🛠️ Como Executar

### 1. Inicie o microsserviço de Usuários primeiro (porta 8080):

```bash
cd /caminho/para/usuario
./gradlew bootRun
```

### 2. Clone e execute o Agendador de Tarefas (porta 8081):

```bash
git clone https://github.com/mauricioRodriguesDev/agendador-tarefas.git
cd agendador-tarefas
```

- **Linux / macOS:**
  ```bash
  ./gradlew bootRun
  ```
- **Windows:**
  ```powershell
  .\gradlew.bat bootRun
  ```

A aplicação estará acessível em: `http://localhost:8081`

---

## 🛣️ Endpoints da API

> **Todos os endpoints são protegidos** e requerem o header `Authorization: Bearer <token>` obtido via login no microsserviço de Usuários (`POST http://localhost:8080/usuario/login`).

### Criar Tarefa

**`POST /tarefas`**

Header: `Authorization: Bearer <token>`

```json
{
  "nomeTarefa": "Reunião de Sprint",
  "descricao": "Reunião de planejamento da sprint 15",
  "dataEvento": "25-10-2026 14:00:00"
}
```

**Resposta:**
```json
{
  "id": "671234abc567def890123456",
  "nomeTarefa": "Reunião de Sprint",
  "descricao": "Reunião de planejamento da sprint 15",
  "dataCriacao": "22-10-2026 15:00:00",
  "emailUsuario": "joao@email.com",
  "dataEvento": "25-10-2026 14:00:00",
  "dataAlteracao": null,
  "statusNotificacaoEnum": "PENDENTE"
}
```

---

### Listar Tarefas do Usuário Logado

**`GET /tarefas`**

Header: `Authorization: Bearer <token>`

---

### Buscar Tarefas por Período

**`GET /tarefas/eventos`**

| Parâmetro | Tipo | Exemplo |
| :--- | :--- | :--- |
| `dataInicial` | `LocalDateTime` (ISO) | `2026-10-01T00:00:00` |
| `dataFinal` | `LocalDateTime` (ISO) | `2026-10-31T23:59:59` |

```
GET /tarefas/eventos?dataInicial=2026-10-01T00:00:00&dataFinal=2026-10-31T23:59:59
```

---

### Atualizar Tarefa

**`PUT /tarefas?id={id}`**

```json
{
  "nomeTarefa": "Reunião de Sprint (atualizada)",
  "descricao": "Reunião remarcada para sala 2",
  "dataEvento": "26-10-2026 10:00:00"
}
```

---

### Alterar Status da Tarefa

**`PATCH /tarefas?status={status}&id={id}`**

Valores aceitos para `status`: `PENDENTE`, `NOTIFICADO`, `CANCELADO`

```
PATCH /tarefas?status=NOTIFICADO&id=671234abc567def890123456
```

---

### Excluir Tarefa

**`DELETE /tarefas?id={id}`**

```
DELETE /tarefas?id=671234abc567def890123456
```

---

## Resumo dos Endpoints

| Método | Endpoint | Parâmetros | Descrição |
| :--- | :--- | :--- | :--- |
| `POST` | `/tarefas` | Header `Authorization` | Cria uma nova tarefa para o usuário logado |
| `GET` | `/tarefas` | Header `Authorization` | Lista todas as tarefas do usuário logado |
| `GET` | `/tarefas/eventos` | `?dataInicial=...&dataFinal=...` | Busca tarefas agendadas por período |
| `PUT` | `/tarefas` | `?id={id}` + Body JSON | Atualiza todos os dados de uma tarefa |
| `PATCH` | `/tarefas` | `?status={status}&id={id}` | Altera apenas o status de notificação |
| `DELETE` | `/tarefas` | `?id={id}` | Exclui uma tarefa pelo ID |

---

## 🔐 Fluxo de Autenticação Inter-Serviços

```text
┌──────────┐                          ┌────────────────────┐                      ┌──────────────┐
│  Cliente  │   1. POST /usuario/login │   Usuário API      │                      │              │
│           │ ────────────────────────▶│   (porta 8080)     │                      │              │
│           │ ◀────────────────────────│                    │                      │              │
│           │   Bearer eyJhbGciOi...   │    PostgreSQL      │                      │              │
│           │                          └────────────────────┘                      │              │
│           │                                                                      │              │
│           │   2. POST /tarefas + Authorization: Bearer <token>                   │ Agendador    │
│           │ ────────────────────────────────────────────────────────────────────▶ │ Tarefas      │
│           │                                                                      │ (porta 8081) │
│           │   3. JwtRequestFilter valida o token e extrai o email                │              │
│           │                                                                      │              │
│           │   4. Feign Client → GET /usuario?email=... (valida no Usuário API)   │              │
│           │                                                                      │              │
│           │   5. Tarefa salva no MongoDB com o email do usuário autenticado       │   MongoDB    │
│           │ ◀────────────────────────────────────────────────────────────────────  │              │
│           │   TarefasDTO response                                                │              │
└──────────┘                                                                       └──────────────┘
```

1. O cliente se autentica no **microsserviço de Usuários** (`POST /usuario/login`) e recebe um token JWT.
2. O cliente envia requisições ao **Agendador de Tarefas** com o token no header `Authorization`.
3. O `JwtRequestFilter` intercepta, valida o token e extrai o e-mail do usuário.
4. O `UserDetailsServiceImpl` usa o **Feign Client** para buscar os dados do usuário na API de Usuários, validando que o token é legítimo.
5. A tarefa é processada e persistida no **MongoDB** com o e-mail do usuário autenticado.

---

## 🧪 Testes

```bash
# Linux / macOS
./gradlew test

# Windows
.\gradlew.bat test
```

---

## 📂 Estrutura Completa de Arquivos

```text
agendador-tarefas/
├── .github/
│   └── workflows/
│       └── gradle.yml                                  # CI/CD com GitHub Actions
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── src/
│   └── main/
│       ├── java/com/javanauta/agendadortarefas/
│       │   ├── AgendadorTarefasApplication.java        # Classe principal (@EnableFeignClients)
│       │   ├── business/
│       │   │   ├── dto/
│       │   │   │   ├── TarefasDTO.java                 # DTO de Tarefas (com @JsonFormat)
│       │   │   │   └── UsuarioDTO.java                 # DTO simplificado de Usuário (email, senha)
│       │   │   ├── mapper/
│       │   │   │   ├── TarefasConverter.java           # MapStruct: Entity ↔ DTO
│       │   │   │   └── TarefaUpdateConverter.java      # MapStruct: Update parcial (ignora nulls)
│       │   │   └── service/
│       │   │       └── TarefasService.java             # Serviço com regras de negócio
│       │   ├── controller/
│       │   │   └── TarefasController.java              # REST Controller
│       │   └── infrastructure/
│       │       ├── client/
│       │       │   └── UsuarioClient.java              # Feign Client → microsserviço Usuário
│       │       ├── entity/
│       │       │   └── TarefasEntity.java              # Documento MongoDB (@Document)
│       │       ├── enums/
│       │       │   └── StatusNotificacaoEnum.java      # PENDENTE, NOTIFICADO, CANCELADO
│       │       ├── exceptions/
│       │       │   └── ResourceNotFundException.java   # Exceção de recurso não encontrado
│       │       ├── repository/
│       │       │   └── TarefasRepository.java          # Spring Data MongoDB Repository
│       │       └── security/
│       │           ├── SecurityConfig.java             # Configuração Spring Security (stateless)
│       │           ├── JwtUtil.java                    # Geração e validação de tokens JWT
│       │           ├── JwtRequestFilter.java           # Filtro de interceptação JWT
│       │           └── UserDetailsServiceImpl.java     # Carrega usuário via Feign Client
│       └── resources/
│           └── application.properties                  # Configurações da aplicação
├── .gitattributes
├── .gitignore
├── build.gradle                                        # Dependências e plugins Gradle
├── gradlew                                             # Gradle Wrapper (Linux/macOS)
├── gradlew.bat                                         # Gradle Wrapper (Windows)
└── settings.gradle                                     # Configurações do projeto Gradle
```

---

## 🔗 Projetos Relacionados

| Microsserviço | Descrição | Repositório |
| :--- | :--- | :--- |
| **Usuário API** | Gerenciamento de usuários e autenticação JWT | [mauricioRodriguesDev/usuario](https://github.com/mauricioRodriguesDev/usuario) |
| **Agendador de Tarefas** | Agendamento e gestão de tarefas (este projeto) | [mauricioRodriguesDev/agendador-tarefas](https://github.com/mauricioRodriguesDev/agendador-tarefas) |

---

## 📄 Licença

Este projeto é de uso livre para fins de estudo e desenvolvimento.

---

## 👨‍💻 Autor

Desenvolvido por [mauricioRodriguesDev](https://github.com/mauricioRodriguesDev).
