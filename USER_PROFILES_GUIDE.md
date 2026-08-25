# User Profiles Guide for CarMatchAI

This guide explains how to use the user profile system in your CarMatchAI application, which allows registered users to save their car preferences and get personalized recommendations.

## Overview

The user profile system includes:
- **User Registration & Authentication**: Users can register with username, email, and password
- **Profile Management**: Users can update their basic information
- **Preference Management**: Users can save and update their car preferences
- **Personalized Recommendations**: Users can get recommendations based on their saved preferences

## Database Schema

### Users Table
- `id` - Primary key
- `username` - Unique username for login
- `email` - Unique email address
- `password` - Encrypted password
- `first_name` - User's first name
- `last_name` - User's last name
- `created_at` - Account creation timestamp
- `updated_at` - Last update timestamp
- `is_enabled` - Account status

### User Preferences Table
- `id` - Primary key
- `user_id` - Foreign key to users table
- `budget` - Maximum budget for car purchase
- `experience` - Driving experience level (novice/intermediate/expert)
- `use_case` - Primary use case (city/highway/mixed/offroad)
- `brand_preferences` - List of preferred car brands
- `fuel_economy_priority` - Whether fuel economy is a priority

## API Endpoints

### Public Endpoints (No Authentication Required)

#### 1. Register User
```http
POST /api/users/register
Content-Type: application/json

{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "message": "User registered successfully",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00",
    "preferences": null
  }
}
```

#### 2. Check Username Availability
```http
GET /api/users/check-username?username=john_doe
```

**Response:**
```json
{
  "username": "john_doe",
  "available": false
}
```

### Protected Endpoints (Authentication Required)

All protected endpoints require Basic Authentication using username and password.

#### 3. Get User Profile
```http
GET /api/users/profile
Authorization: Basic <base64(username:password)>
```

**Response:**
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00",
  "preferences": {
    "id": 1,
    "budget": 50000.0,
    "experience": "intermediate",
    "useCase": "city",
    "brandPreferences": ["Toyota", "Honda"],
    "fuelEconomyPriority": true
  }
}
```

#### 4. Update User Preferences
```http
PUT /api/users/preferences
Authorization: Basic <base64(username:password)>
Content-Type: application/json

{
  "budget": 60000.0,
  "experience": "expert",
  "useCase": "highway",
  "brandPreferences": ["BMW", "Audi", "Mercedes"],
  "fuelEconomyPriority": false
}
```

**Response:**
```json
{
  "message": "Preferences updated successfully",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:30:00",
    "preferences": {
      "id": 1,
      "budget": 60000.0,
      "experience": "expert",
      "useCase": "highway",
      "brandPreferences": ["BMW", "Audi", "Mercedes"],
      "fuelEconomyPriority": false
    }
  }
}
```

#### 5. Update Basic Profile Information
```http
PUT /api/users/profile?firstName=John&lastName=Smith&email=johnsmith@example.com
Authorization: Basic <base64(username:password)>
```

#### 6. Get Recommendations from Saved Preferences
```http
GET /api/recommend/my-preferences
Authorization: Basic <base64(username:password)>
```

**Response:**
```json
[
  {
    "model": "3 Series",
    "reason": "excellent value for money, powerful engine for experienced drivers, strong performance for highway driving, matches your preferred brand",
    "score": 92.5,
    "brand": "BMW",
    "price": 45000.0,
    "year": 2023,
    "horsePower": 255,
    "fuelConsumption": 7.2,
    "fuelType": "Gasoline",
    "isCompact": false,
    "drivetrainType": "RWD",
    "color": "Black"
  }
]
```

#### 7. Delete User Account
```http
DELETE /api/users/account
Authorization: Basic <base64(username:password)>
```

## How to Use User Profiles

### 1. User Registration Flow
1. User registers with username, email, and password
2. System creates user account with basic information
3. User can immediately start using the service

### 2. Setting Up Preferences
1. User calls `PUT /api/users/preferences` to set their car preferences
2. System saves preferences linked to the user account
3. User can update preferences anytime

### 3. Getting Personalized Recommendations
1. User calls `GET /api/recommend/my-preferences` to get recommendations based on saved preferences
2. System uses the user's saved preferences to generate personalized car recommendations
3. User can also still use `POST /api/recommend` with custom preferences for one-time recommendations

### 4. Profile Management
1. User can update basic information (name, email) via `PUT /api/users/profile`
2. User can update preferences via `PUT /api/users/preferences`
3. User can view their profile via `GET /api/users/profile`

## Security Features

- **Password Encryption**: All passwords are encrypted using BCrypt
- **Authentication Required**: Most endpoints require valid user authentication
- **Input Validation**: All inputs are validated using Bean Validation annotations
- **CORS Support**: Cross-origin requests are supported for frontend integration

## Error Handling

The API returns appropriate HTTP status codes and error messages:

- `400 Bad Request`: Invalid input data or validation errors
- `401 Unauthorized`: Authentication required or invalid credentials
- `404 Not Found`: User or resource not found
- `409 Conflict`: Username or email already exists
- `500 Internal Server Error`: Server-side errors

## Example Usage Scenarios

### Scenario 1: New User Registration and Setup
```bash
# 1. Register user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "car_lover",
    "email": "car@example.com",
    "password": "secure123",
    "firstName": "Car",
    "lastName": "Lover"
  }'

# 2. Set preferences
curl -X PUT http://localhost:8080/api/users/preferences \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic Y2FyX2xvdmVyOnNlY3VyZTEyMw==" \
  -d '{
    "budget": 40000.0,
    "experience": "intermediate",
    "useCase": "mixed",
    "brandPreferences": ["Toyota", "Honda", "Mazda"],
    "fuelEconomyPriority": true
  }'

# 3. Get personalized recommendations
curl -X GET http://localhost:8080/api/recommend/my-preferences \
  -H "Authorization: Basic Y2FyX2xvdmVyOnNlY3VyZTEyMw=="
```

### Scenario 2: Updating Preferences
```bash
# Update budget and add new brand preferences
curl -X PUT http://localhost:8080/api/users/preferences \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic Y2FyX2xvdmVyOnNlY3VyZTEyMw==" \
  -d '{
    "budget": 55000.0,
    "experience": "intermediate",
    "useCase": "mixed",
    "brandPreferences": ["Toyota", "Honda", "Mazda", "Subaru"],
    "fuelEconomyPriority": true
  }'
```

## Integration with Frontend

The user profile system is designed to work seamlessly with frontend applications:

1. **Registration Form**: Collect user information and call the registration endpoint
2. **Login System**: Implement Basic Authentication for protected endpoints
3. **Preferences Form**: Create a form to collect and update user preferences
4. **Recommendations Display**: Show personalized recommendations based on saved preferences
5. **Profile Management**: Allow users to view and update their profile information

## Database Migration

When you first run the application, the database tables will be created automatically. The system uses JPA with `create-drop` mode, so tables are recreated on each startup.

For production, you should:
1. Change `spring.jpa.hibernate.ddl-auto` to `validate` or `update`
2. Create proper database migrations
3. Set up proper database user permissions

## Next Steps

1. **Frontend Integration**: Build a web or mobile frontend that uses these APIs
2. **Advanced Features**: Add features like recommendation history, favorites, etc.
3. **Enhanced Security**: Consider implementing JWT tokens for better security
4. **Email Verification**: Add email verification for user registration
5. **Password Reset**: Implement password reset functionality





