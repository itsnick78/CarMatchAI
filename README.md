# CarMatchAI

CarMatchAI is an intelligent recommendation service that helps users find the perfect car based on preferences, budget, and lifestyle using a rule-based engine with Redis caching.

## 🌟 Features

- **Rule-based recommendation engine** with intelligent filtering and scoring
- **Redis caching** for improved performance and response times
- **PostgreSQL database** with 30+ sample car records
- **RESTful API** with comprehensive validation
- **Multi-criteria filtering** based on budget, experience, use case, brand preferences, and fuel economy
- **Comprehensive test suite** with unit and integration tests

## 📦 Tech Stack

- **Java 17** + **Spring Boot 3.x**
- **PostgreSQL** - Primary database
- **Redis** - Caching layer
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
- **Health checks** for both services
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