# Project Skill Guide

## Tech Stack
- Backend: Spring Boot (Java)
- Database: PostgreSQL
- ORM: JPA / Hibernate
- Build: Maven

## Database
- Local DB: localhost:5432
- Username: kelvingao
- Config: application.properties

## Architecture Flow
Controller → Service (Interface + Impl) → Repository → Model/Entity

## Layers

### Model
- JPA Entity classes
- Annotated with @Entity, @Table, @Id, @GeneratedValue

### DTO
- Separate DTOs for Request and Response
- Never expose Entity directly in API response

### Repository
- Extends JpaRepository<Entity, ID>
- Interface only, no implementation needed

### Service
- Always create an Interface first (e.g. UserService)
- Implementation class (e.g. UserServiceImpl) implements the interface
- Annotated with @Service
- Throw custom exceptions (never return null)

### Controller
- Annotated with @RestController
- Inject service interface (not impl)
- Follow RESTful naming conventions
- Always wrap response with ResponseEntity<T>
- Always return appropriate HTTP status code
- Do NOT handle exceptions in controller (let @ControllerAdvice handle it)

## HTTP Status Conventions
- GET (fetch all / fetch by id) → 200 OK
- POST (create) → 201 CREATED
- PUT / PATCH (update) → 200 OK
- DELETE → 204 NO CONTENT
- Not found → 404 NOT FOUND
- Bad request / validation error → 400 BAD REQUEST
- Server error → 500 INTERNAL SERVER ERROR

## Controller Example
```java
@GetMapping("/{id}")
public ResponseEntity<UserResponseDTO> getById(@PathVariable Long id) {
    return ResponseEntity.ok(userService.getById(id));
}

@PostMapping
public ResponseEntity<UserResponseDTO> create(@RequestBody UserRequestDTO dto) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(dto));
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
}
```

## Error Handling

### Custom Exception Classes
- Create a custom exception per case
- Example: ResourceNotFoundException, BadRequestException

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

### Error Response DTO
- Always return a consistent error response body

```java
public class ErrorResponseDTO {
    private int status;
    private String message;
    private LocalDateTime timestamp;
}
```

### Global Exception Handler
- Use @ControllerAdvice + @ExceptionHandler
- One central class handles all exceptions
- Never handle exceptions inside Controller or Service directly

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleNotFound(ResourceNotFoundException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            404, ex.getMessage(), LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadRequest(BadRequestException ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            400, ex.getMessage(), LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneral(Exception ex) {
        ErrorResponseDTO error = new ErrorResponseDTO(
            500, "Internal server error", LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### Exception Naming Conventions
- ResourceNotFoundException → 404
- BadRequestException → 400
- UnauthorizedException → 401
- ForbiddenException → 403

## Dependency Injection
- Always use Constructor Injection (never @Autowired on fields)
- Example:
  private final UserService userService;

  public UserController(UserService userService) {
  this.userService = userService;
  }

## Naming Conventions
- Entity: User
- DTO: UserRequestDTO, UserResponseDTO
- Repository: UserRepository
- Service Interface: UserService
- Service Impl: UserServiceImpl
- Controller: UserController
- Exception: ResourceNotFoundException, BadRequestException
- Global Handler: GlobalExceptionHandler

## Deployment
- Use environment variables for DB credentials in production
- Local: application.properties
- Prod: application-prod.properties or env vars