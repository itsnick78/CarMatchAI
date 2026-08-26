# CarMatchAI

CarMatchAI is an intelligent recommendation service that helps users find the perfect car based on preferences, budget, and lifestyle. A deterministic rule-based engine does the actual filtering and scoring; Spring AI (backed by a local Ollama model) sits on top of it to accept preferences typed in plain language and to phrase the recommendation explanations naturally - it never decides which cars match.

## 🌟 Features

- **Rule-based recommendation engine** with intelligent filtering and scoring
- **Redis caching** for improved performance and response times
- **Natural-language preference intake** - describe what you want in a sentence instead of filling a form; Spring AI extracts structured preferences from it
- **AI-generated recommendation explanations** with a deterministic template fallback when the model is unavailable
- **PostgreSQL database** with 30+ sample car records
- **RESTful API** with comprehensive validation
- **Multi-criteria filtering** based on budget, experience, use case, brand preferences, and fuel economy
- **Comprehensive test suite** with unit and integration tests

## 📦 Tech Stack

- **Java 17** + **Spring Boot 3.x**
- **PostgreSQL** - Primary database
- **Redis** - Caching layer
- **Spring AI** + **Ollama** - Natural-language preference parsing and recommendation explanations
- **Maven** - Build tool
- **Docker Compose** - Container orchestration
- **JUnit 5** + **MockMvc** - Testing framework

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker and Docker Compose

### Running the Application

1. **Start the required services** (PostgreSQL and Redis):
   ```bash
   docker-compose up -d
   ```

2. **Run the Spring Boot application**:
   ```bash
   mvn spring-boot:run
   ```

3. **The application will be available at** `http://localhost:8081`

### API Endpoints

Recommendations are based on preferences saved to the caller's account, not
posted with the request. Typical flow:

```http
POST /api/users/register
Content-Type: application/json

{ "username": "alex", "email": "alex@example.com", "password": "secret123" }
```

```http
POST /api/users/login
Content-Type: application/json

{ "email": "alex@example.com", "password": "secret123" }
```
Sets an httpOnly `AUTH_TOKEN` cookie (a bearer token also works via
`Authorization: Bearer <token>`).

```http
POST /api/users/preferences
Content-Type: application/json
Cookie: AUTH_TOKEN=<token>

{
  "budget": 50000.0,
  "experience": "intermediate",
  "useCase": "city",
  "brandPreferences": ["Toyota", "Honda"],
  "fuelEconomyPriority": true
}
```

#### Set Preferences from Free Text
```http
POST /api/users/preferences/from-text
Content-Type: application/json
Cookie: AUTH_TOKEN=<token>

{ "text": "нужна недорогая экономичная машина для города, до 30000" }
```
Spring AI extracts a `budget`/`experience`/`useCase`/`brandPreferences`/`fuelEconomyPriority`
object from the text, validates it with the same rules as the manual form, and
saves it exactly like `POST /api/users/preferences` would.

#### Get Car Recommendations
```http
GET /api/recommend
Cookie: AUTH_TOKEN=<token>
```

**Response:**
```json
[
  {
    "model": "Corolla",
    "reason": "excellent value for money, good fuel efficiency, compact size ideal for city driving",
    "score": 85.5,
    "brand": "Toyota",
    "price": 25000.0,
    "year": 2023,
    "horsePower": 139,
    "fuelConsumption": 5.8,
    "fuelType": "Gasoline",
    "compact": true,
    "drivetrainType": "FWD",
    "color": "White"
  }
]
```

#### Health Check
```http
GET /api/health
```

#### Application Info
```http
GET /api/info
```

### Filtering Rules

The recommendation engine applies the following filters:

1. **Budget Filter**: Only cars within the specified budget
2. **Experience Filter**: 
   - Novice drivers: ≤150 horsepower
   - Intermediate/Expert: No horsepower restrictions
3. **Use Case Filter**:
   - City driving: Compact cars only
   - Highway/Mixed/Offroad: No size restrictions
4. **Fuel Economy Filter**: ≤7.0 L/100km if fuel economy is prioritized
5. **Brand Preferences**: Only cars from preferred brands (if specified)

### Scoring Algorithm

The recommendation score (0-100) is calculated based on:
- **Price efficiency** (40 points): Lower price relative to budget = higher score
- **Fuel economy** (30 points): Lower consumption = higher score
- **Experience appropriateness** (20 points): Horsepower matching experience level
- **Use case suitability** (10 points): Car characteristics matching use case

### AI Integration (Spring AI + Ollama)

Two features use an LLM; neither one touches which cars get recommended or
their score - that stays entirely in `RecommendationService`'s deterministic
rule engine, so recommendations are reproducible and cheap to cache/test.

**1. Natural-language preference intake** (`PreferenceExtractionService`)
turns free text into the same `UserPreferencesUpdateRequest` the manual form
produces, via Spring AI's structured-output support
(`chatClient.prompt().call().entity(UserPreferencesUpdateRequest.class)`).
Spring AI generates a JSON schema from that DTO, appends it to the prompt as
a format instruction, and parses the model's JSON reply back into the DTO.
Because the extraction target is the *existing* request DTO - not a
separate AI-only model - the LLM's output is checked with the exact same
`@NotNull`/`@Pattern`/`@Min`/`@Max` Bean Validation rules a hand-filled form
would be, and both paths call the same `UserService.updateUserPreferences`.
There is only one code path that ever writes preferences to the database.

**2. AI-generated recommendation explanations** (`AiExplanationService`)
rewrites the `reason` text on the already-scored, already-sorted top-5
results. It sends the model only the facts already computed for each car
(brand, price, fuel consumption, horsepower, score) plus the buyer's
preferences, and asks it to phrase one sentence per car - it is explicitly
told not to invent specs. One batched call covers the whole list instead of
one call per car. If Ollama is unreachable, slow, disabled, or returns a
malformed/mis-sized response, the service catches it, logs a warning, and
returns `null`; `RecommendationService` then keeps the deterministic
template reason it always computes first. The feature is additive and never
load-bearing - `/api/recommend` works identically with Ollama stopped.
Because reasons are baked into the `RecommendationResult` before it's
cached, a cache hit never re-invokes the model either.

**Why Ollama** instead of a hosted API: no API key to manage, rotate, or
accidentally leak from a portfolio project; runs locally in the same
`docker-compose` stack as Postgres/Redis for a fully reproducible dev setup
with zero billing risk. Spring AI's `ChatClient` abstracts the model
provider, so swapping in OpenAI/Anthropic/etc. later is a dependency and
config change, not a code rewrite.

**Why the flag** - `carmatch.ai.explanations-enabled` (`AI_EXPLANATIONS_ENABLED`
env var, defaults to `true`, forced to `false` under the `test` profile):
tests must not depend on a running Ollama instance or on non-deterministic
model output. `AiExplanationService` checks the flag before ever touching
`ChatClient`, so the full test suite runs without Ollama installed.
`PreferenceExtractionService` has no such flag since it's only exercised
behind an explicit user action; its tests mock the `ChatClient` bean
directly instead.

**Running it**: `docker compose up -d` also starts an `ollama` container and
a one-shot `ollama-pull` job that downloads the `llama3.2` model into a
named volume on first run (so it's not re-downloaded on every restart).

### Testing

**Run the complete test suite:**
```bash
mvn test
```

**Run specific test categories:**
```bash
# Unit tests only
mvn test -Dtest="*Test"

# Integration tests only  
mvn test -Dtest="*IntegrationTest"
```

### Project Structure

```
src/
├── main/
│   ├── java/ai/carmatch/
│   │   ├── config/        # App & Spring configuration (SecurityConfig, PasswordConfig, etc.)
│   │   ├── controller/    # REST controllers (RecommendationController, UserController, etc.)
│   │   ├── dto/           # Data Transfer Objects (CarDTO, UserPreferencesDTO, RecommendationResponseDTO)
│   │   ├── model/         # Domain models/entities (Car, UserPreferences, Recommendation)
│   │   ├── repository/    # Data access layer (CarRepository, UserRepository)
│   │   ├── security/      # Security layer (JWT filters, UserDetailsService, etc.)
│   │   ├── service/       # Business logic (RecommendationService, UserService)
│   │   └── CarMatchAiApplication.java 
│   └── resources/
│       ├── application.yml  # Application configuration
│       └── data.sql         # Sample car data (30+ records)
└── test/
    ├── java/ai/carmatch/
    │   ├── service/         # Unit tests for services
    │   ├── controller/      # Unit tests for controllers
    │   └── integration/     # Integration tests
    └── resources/
        └── application-test.yml  # Test configuration
```

### Docker Services

The `docker-compose.yml` includes:
- **PostgreSQL 16**: Database with persistent volumes
- **Redis 7**: Cache server with persistent volumes
- **Ollama**: Local LLM server for the Spring AI features, plus a one-shot
  `ollama-pull` job that downloads the chat model on first startup
- **Health checks** for all services
- **Exposed ports** for local development

### Sample Data

The application includes 30+ sample cars across different categories:
- Compact cars (Toyota Corolla, Honda Civic, etc.)
- Mid-size sedans (Toyota Camry, Honda Accord, etc.)
- Luxury sedans (BMW 3 Series, Mercedes C-Class, etc.)
- SUVs (Toyota RAV4, Honda CR-V, etc.)
- Performance cars (BMW M3, Mercedes AMG C63, etc.)
- Electric vehicles (Tesla Model 3, BMW i4, etc.)
- Budget-friendly options (Nissan Versa, Hyundai Accent, etc.)

### Performance Features

- **Redis caching** with 10-minute TTL for recommendation results
- **Efficient database queries** with JPA repositories
- **Optimized filtering** with stream operations
- **Connection pooling** for database and Redis connections

## 💡 Ideas for Future Development

- **User profiles & personalization** – store user history and improve recommendations over time
- **Integration with external APIs** – fetch up-to-date car prices and specifications
- **Advanced filtering** – more filters (mileage, body type, transmission, insurance, maintenance costs)
- **Machine Learning ranking** – enhance the recommendation engine using real user choices
- **Chatbot assistants** – Telegram/WhatsApp bots for convenient interaction
- **Admin dashboard** – manage the car database and analyze usage statistics
- **Partnership modules** – integrations with dealers, insurance providers, and financing services
- **Multi-language support** – localized interface and recommendations


## 📄 License

MIT