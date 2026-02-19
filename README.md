# AI Chat Backend (Spring Boot)

A modular, provider-agnostic AI Chat backend built with Spring Boot.

This project demonstrates clean architecture principles, provider routing via Strategy Pattern, and environment-based configuration using Spring Profiles.

---

## 🚀 Features

- Clean REST API for AI chat
- Pluggable AI Provider Architecture (Strategy Pattern)
- Multiple AI Providers Supported:
    - Ollama (Local LLM)
    - OpenAI
    - Gemini
- Environment-based configuration (dev / qa / prod)
- Externalized API keys (no hardcoding)
- Production-ready profile structure

---

## 🏗 Architecture Overview

Controller layer does NOT know anything about AI providers.

Routing is handled inside the service layer:

Controller → ChatService → AiProviderStrategy → Concrete Provider

AI provider selection is determined by:

```yaml
spring.ai.default-provider
```

---

## 🚀 Project Structure
```
src/main/java/org/sweetie
│
├── controller        # REST Controllers
├── service           # Business Logic
│   └── provider      # AI Provider Strategy Implementations
├── config            # Configuration Properties
└── dto               # Request / Response DTOs
```
---
 Base Configuration
---

```yaml
spring:
  application:
    name: aichat
  profiles:
    active: dev
```
---
### Profiles
🧪 Dev Profile (Local Development)

application-dev.yaml
```yaml
spring:
  config:
    activate:
      on-profile: dev

  ai:
    default-provider: ollama

    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3
        temperature: 0.7
```

---
### QA Profile (Integration Testing)

Uses Gemini.

application-qa.yaml
```yaml
spring:
  config:
    activate:
      on-profile: qa

  ai:
    default-provider: gemini

    gemini:
      api-key: ${GEMINI_API_KEY}
      chat:
        model: gemini-1.5-pro
        temperature: 0.5
```
---

### Production Profile

Uses OpenAI.

application-prod.yaml

```yaml
spring:
  config:
    activate:
      on-profile: prod

  ai:
    default-provider: openai

    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        model: gpt-4o
        temperature: 0.3
```

---

### Environment Variables

API keys must be set as environment variables.

### Mac / Linux
```bash
export OPENAI_API_KEY=your_key_here
export GEMINI_API_KEY=your_key_here
```

### Windows PowerShell
```bash
setx OPENAI_API_KEY "your_key_here"
setx GEMINI_API_KEY "your_key_here"
```
---

### Running the Application


Run with Default Profile (dev)
```bash
mvn spring-boot:run
```

### Run with Specific Profile
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=qa

Or set environment variable:

SPRING_PROFILES_ACTIVE=prod
```
---
## API Endpoints
###  GET Chat
```http
GET /api/chat?message=Hello
```

### POST Chat
```http
POST /api/chat
Content-Type: application/json

{
  "message": "Explain risk reward ratio in trading"
}
```

---

### 🧩 Supported AI Providers

**Ollama** (Local LLM)\
**OpenAI**
\**Gemini**

---

### New providers can be added by implementing:

**nginx**\
**AiProviderStrategy**

---

### 🏛 Design Principles Applied

**Strategy Pattern**\
**Dependency Injection**\
**Profile-based Configuration**\
**Externalized Secrets**\
**Separation of Concerns**\
**Clean Architecture**\
---

### Future Improvements

**Provider fallback mechanism**\
**Rate limiting**\
**Observability (Micrometer / Prometheus)**\
**Docker support\
**CI/CD pipeline**\
**Integration tests per provider🏛 Design Principles Applied**

###Author: Dipak Ghosh

Built as part of a modular AI integration architecture project demonstrating enterprise-ready backend design with Spring Boot.






