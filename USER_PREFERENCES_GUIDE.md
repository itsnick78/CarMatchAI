# User Preferences Feature Guide

## Overview

The User Preferences feature allows users to set and manage their car buying preferences, which are used by the AI recommendation system to provide personalized car suggestions. This feature is integrated with the user profile system and provides a comprehensive way to capture user requirements.

## Features

### 1. Preference Management
- **Budget Setting**: Users can set their car budget with validation ($1,000 - $200,000)
- **Experience Level**: Choose from novice, intermediate, or expert driving experience
- **Use Case**: Select primary use case (city, highway, mixed, offroad)
- **Brand Preferences**: Optional list of preferred car brands
- **Fuel Economy Priority**: Boolean flag indicating if fuel efficiency is important

### 2. Data Validation
- All preference fields have comprehensive validation
- Budget range validation with meaningful error messages
- Enum validation for experience and use case fields
- Required field validation for critical preferences

### 3. User Integration
- One-to-one relationship with User entity
- Automatic preference creation when updating
- Lazy loading for performance optimization

## API Endpoints

### Update User Preferences
```
PUT /api/users/preferences
```

**Authentication**: Required (JWT token)

**Request Body**:
```json
{
  "budget": 25000.0,
  "experience": "intermediate",
  "useCase": "mixed",
  "brandPreferences": ["Toyota", "Honda", "Mazda"],
  "fuelEconomyPriority": true
}
```

**Response**:
```json
{
  "message": "Preferences updated successfully",
  "user": {
    "id": 1,
    "username": "john_doe",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T14:45:00",
    "preferences": {
      "id": 1,
      "budget": 25000.0,
      "experience": "intermediate",
      "useCase": "mixed",
      "brandPreferences": ["Toyota", "Honda", "Mazda"],
      "fuelEconomyPriority": true
    }
  }
}
```

### Get User Profile (includes preferences)
```
GET /api/users/profile
```

**Authentication**: Required (JWT token)

**Response**: Same as above user object

## Data Model

### UserPreferences Entity
```java
@Entity
@Table(name = "user_preferences")
public class UserPreferences {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @NotNull(message = "Budget is required")
    @Min(value = 1000, message = "Budget must be at least $1,000")
    @Max(value = 200000, message = "Budget must not exceed $200,000")
    private Double budget;
    
    @NotNull(message = "Experience level is required")
    @Pattern(regexp = "novice|intermediate|expert", 
             message = "Experience must be novice, intermediate, or expert")
    private String experience;
    
    @NotNull(message = "Use case is required")
    @Pattern(regexp = "city|highway|mixed|offroad", 
             message = "Use case must be city, highway, mixed, or offroad")
    private String useCase;
    
    private List<String> brandPreferences;
    
    @NotNull(message = "Fuel economy priority is required")
    private Boolean fuelEconomyPriority;
}
```

## Validation Rules

### Budget
- **Required**: Yes
- **Range**: $1,000 - $200,000
- **Type**: Double
- **Error Messages**:
  - "Budget is required"
  - "Budget must be at least $1,000"
  - "Budget must not exceed $200,000"

### Experience Level
- **Required**: Yes
- **Valid Values**: novice, intermediate, expert
- **Type**: String
- **Error Message**: "Experience must be novice, intermediate, or expert"

### Use Case
- **Required**: Yes
- **Valid Values**: city, highway, mixed, offroad
- **Type**: String
- **Error Message**: "Use case must be city, highway, mixed, or offroad"

### Brand Preferences
- **Required**: No
- **Type**: List<String>
- **Description**: Optional list of preferred car brands

### Fuel Economy Priority
- **Required**: Yes
- **Type**: Boolean
- **Description**: Indicates if fuel efficiency is a priority

## Usage Examples

### Setting Initial Preferences
When a user first sets their preferences, the system will create a new UserPreferences record linked to their account.

### Updating Existing Preferences
Users can update their preferences at any time. The system will update the existing record rather than creating a new one.

### Partial Updates
The API requires all preference fields to be provided in the update request. This ensures data consistency and prevents incomplete preference records.

## Integration with Recommendation System

The UserPreferences are used by the RecommendationService to:
1. Filter cars within the user's budget range
2. Consider the user's experience level for appropriate car complexity
3. Match use case requirements with car capabilities
4. Prioritize preferred brands in recommendations
5. Weight fuel economy in the scoring algorithm

## Error Handling

### Validation Errors
- **400 Bad Request**: When validation fails
- **Error Response**:
```json
{
  "error": "Budget must be at least $1,000"
}
```

### Authentication Errors
- **401 Unauthorized**: When user is not authenticated
- **403 Forbidden**: When user doesn't have permission

### Server Errors
- **500 Internal Server Error**: When unexpected errors occur
- **Error Response**:
```json
{
  "error": "Failed to update preferences"
}
```

## Database Schema

### user_preferences Table
```sql
CREATE TABLE user_preferences (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    budget DECIMAL(10,2) NOT NULL,
    experience VARCHAR(20) NOT NULL,
    use_case VARCHAR(20) NOT NULL,
    brand_preferences JSON,
    fuel_economy_priority BOOLEAN NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

## Security Considerations

1. **Authentication Required**: All preference operations require valid JWT authentication
2. **User Isolation**: Users can only access and modify their own preferences
3. **Input Validation**: All inputs are validated to prevent malicious data
4. **SQL Injection Protection**: Using JPA/Hibernate prevents SQL injection attacks

## Performance Considerations

1. **Lazy Loading**: UserPreferences are loaded lazily to improve performance
2. **Database Indexing**: user_id column is indexed for fast lookups
3. **Transaction Management**: Updates are wrapped in transactions for data consistency

## Testing

The feature includes comprehensive tests:
- Unit tests for validation logic
- Integration tests for API endpoints
- Service layer tests for business logic

## Future Enhancements

Potential improvements for the UserPreferences feature:
1. **Preference History**: Track changes over time
2. **Preference Templates**: Save common preference sets
3. **Advanced Filtering**: More granular preference options
4. **Preference Analytics**: Insights into user preference patterns
5. **Preference Sharing**: Allow users to share preference profiles

## Troubleshooting

### Common Issues

1. **Preferences Not Saving**
   - Check authentication token validity
   - Verify all required fields are provided
   - Check validation error messages

2. **Validation Errors**
   - Ensure budget is within valid range
   - Verify experience and use case use exact valid values
   - Check that fuel economy priority is a boolean

3. **Performance Issues**
   - Check database connection
   - Verify proper indexing on user_id
   - Monitor transaction timeouts

### Debug Steps

1. Check application logs for detailed error messages
2. Verify database connectivity and table structure
3. Test API endpoints with valid authentication
4. Validate request payload format and content





