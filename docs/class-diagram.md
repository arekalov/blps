# Class Diagram

```mermaid
classDiagram
    %% ============================================
    %% CONTROLLER LAYER
    %% ============================================
    class AuthController {
        -AuthService authService
        +register(RegisterRequest) AuthResponse
        +login(LoginRequest) AuthResponse
        +refreshToken(RefreshTokenRequest) AuthResponse
    }

    class VacancyController {
        -VacancyService vacancyService
        +getAllVacancies(status, pageable) PagedResponse~VacancyResponse~
        +getVacancyById(id) VacancyResponse
        +getMyVacancies(auth, status, pageable) PagedResponse~VacancyResponse~
        +createVacancy(auth, request) VacancyResponse
        +updateVacancy(auth, id, request) VacancyResponse
        +deleteVacancy(auth, id) void
        +selectTariff(auth, id, tariffId) VacancyResponse
        +publishVacancy(auth, id) VacancyResponse
        +archiveVacancy(auth, id) VacancyResponse
        -getUserRole(auth) UserRole
    }

    class TariffController {
        -TariffService tariffService
        +getAllTariffs(pageable) PagedResponse~TariffResponse~
        +getTariffById(id) TariffResponse
        +createTariff(request) TariffResponse
        +updateTariff(id, request) TariffResponse
        +deleteTariff(id) void
    }

    class UserController {
        -UserRepository userRepository
        -VacancyRepository vacancyRepository
        +getCurrentUser(auth) UserResponse
        +deleteUser(userId) void
    }

    %% ============================================
    %% SERVICE LAYER
    %% ============================================
    class AuthService {
        -UserRepository userRepository
        -PasswordEncoder passwordEncoder
        -JwtTokenProvider jwtTokenProvider
        +register(RegisterRequest) AuthResponse
        +login(LoginRequest) AuthResponse
        +refreshToken(String) AuthResponse
    }

    class VacancyService {
        -VacancyRepository vacancyRepository
        -UserRepository userRepository
        -TariffRepository tariffRepository
        -SkillRepository skillRepository
        +getAllVacancies(status, pageable) PagedResponse~VacancyResponse~
        +getVacancyById(id) VacancyResponse
        +getMyVacancies(userId, status, pageable) PagedResponse~VacancyResponse~
        +createVacancy(userId, request) VacancyResponse
        +updateVacancy(userId, id, role, request) VacancyResponse
        +deleteVacancy(userId, id, role) void
        +selectTariff(userId, id, tariffId, role) VacancyResponse
        +publishVacancy(userId, id, role) VacancyResponse
        +archiveVacancy(userId, id, role) VacancyResponse
        -applyVacancyUpdates(vacancy, request) void
        -resolveSkills(skillNames) List~Skill~
        -checkPermission(userId, vacancy, role) void
    }

    class TariffService {
        -TariffRepository tariffRepository
        +getAllTariffs(pageable) PagedResponse~TariffResponse~
        +getTariffById(id) TariffResponse
        +createTariff(request) TariffResponse
        +updateTariff(id, request) TariffResponse
        +deleteTariff(id) void
    }

    %% ============================================
    %% REPOSITORY LAYER
    %% ============================================
    class UserRepository {
        <<interface>>
        +findByEmail(email) User?
        +existsByEmail(email) Boolean
        +existsById(id) Boolean
    }

    class VacancyRepository {
        <<interface>>
        +findByEmployerId(employerId, pageable) Page~Vacancy~
        +findByEmployerId(employerId) List~Vacancy~
        +findByStatus(status, pageable) Page~Vacancy~
        +findByEmployerIdAndStatus(employerId, status, pageable) Page~Vacancy~
    }

    class TariffRepository {
        <<interface>>
        +findById(id) Optional~Tariff~
        +existsById(id) Boolean
    }

    class SkillRepository {
        <<interface>>
        +findByNameIn(names) List~Skill~
    }

    %% ============================================
    %% ENTITY LAYER
    %% ============================================
    class User {
        -UUID id
        -String email
        -String passwordHash
        -String companyName
        -UserRole role
        -LocalDateTime createdAt
        -Set~Vacancy~ vacancies
    }

    class Vacancy {
        -UUID id
        -String title
        -String description
        -ExperienceLevel experienceLevel
        -BigDecimal salaryFrom
        -BigDecimal salaryTo
        -EmploymentType employmentType
        -WorkFormat workFormat
        -EmploymentFormat employmentFormat
        -WorkSchedule workSchedule
        -String city
        -String address
        -String companyDescription
        -List~Skill~ additionalSkills
        -VacancyStatus status
        -User employer
        -Tariff tariff
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -LocalDateTime publishedAt
    }

    class Tariff {
        -UUID id
        -String name
        -BigDecimal price
        -Int durationDays
        -String description
    }

    class Skill {
        -UUID id
        -String name
        -List~Vacancy~ vacancies
    }

    %% ============================================
    %% DTO LAYER - AUTH
    %% ============================================
    class RegisterRequest {
        +String email
        +String password
        +String companyName
        +Boolean isAdmin
    }

    class LoginRequest {
        +String email
        +String password
    }

    class RefreshTokenRequest {
        +String refreshToken
    }

    class AuthResponse {
        +String accessToken
        +String refreshToken
        +String userId
        +String email
        +String role
    }

    %% ============================================
    %% DTO LAYER - USER
    %% ============================================
    class UserResponse {
        +String id
        +String email
        +String companyName
        +String role
    }

    %% ============================================
    %% DTO LAYER - VACANCY
    %% ============================================
    class CreateVacancyRequest {
        +String title
        +String description
        +ExperienceLevel experienceLevel
        +BigDecimal salaryFrom
        +BigDecimal salaryTo
        +EmploymentType employmentType
        +WorkFormat workFormat
        +EmploymentFormat employmentFormat
        +WorkSchedule workSchedule
        +String city
        +String address
        +String companyDescription
        +List~String~ additionalSkills
    }

    class UpdateVacancyRequest {
        +String title
        +String description
        +ExperienceLevel experienceLevel
        +BigDecimal salaryFrom
        +BigDecimal salaryTo
        +EmploymentType employmentType
        +WorkFormat workFormat
        +EmploymentFormat employmentFormat
        +WorkSchedule workSchedule
        +String city
        +String address
        +String companyDescription
        +List~String~ additionalSkills
    }

    class VacancyResponse {
        +String id
        +String title
        +String description
        +String experienceLevel
        +BigDecimal salaryFrom
        +BigDecimal salaryTo
        +String employmentType
        +String workFormat
        +String employmentFormat
        +String workSchedule
        +String city
        +String address
        +String companyDescription
        +List~String~ additionalSkills
        +String status
        +String employerId
        +String employerCompanyName
        +String tariffId
        +String tariffName
        +LocalDateTime createdAt
        +LocalDateTime updatedAt
        +LocalDateTime publishedAt
    }

    %% ============================================
    %% DTO LAYER - TARIFF
    %% ============================================
    class CreateTariffRequest {
        +String name
        +BigDecimal price
        +Int durationDays
        +String description
    }

    class UpdateTariffRequest {
        +String name
        +BigDecimal price
        +Int durationDays
        +String description
    }

    class TariffResponse {
        +String id
        +String name
        +BigDecimal price
        +Int durationDays
        +String description
    }

    %% ============================================
    %% DTO LAYER - COMMON
    %% ============================================
    class PagedResponse~T~ {
        +List~T~ content
        +Int page
        +Int size
        +Long totalElements
        +Int totalPages
    }

    class ErrorResponse {
        +LocalDateTime timestamp
        +Int status
        +String error
        +String message
        +String path
    }

    %% ============================================
    %% MAPPER LAYER
    %% ============================================
    class UserMapper {
        <<extension functions>>
        +User.toResponse() UserResponse
        +User.toAuthResponse(accessToken, refreshToken) AuthResponse
        +RegisterRequest.toEntity(encodedPassword) User
    }

    class VacancyMapper {
        <<extension functions>>
        +Vacancy.toResponse() VacancyResponse
        +CreateVacancyRequest.toEntity(employer, skills) Vacancy
    }

    class TariffMapper {
        <<extension functions>>
        +Tariff.toResponse() TariffResponse
        +CreateTariffRequest.toEntity() Tariff
    }

    class PageMapper {
        <<extension functions>>
        +Page~T~.toPagedResponse(mapper) PagedResponse~R~
    }

    %% ============================================
    %% ENUMS
    %% ============================================
    class UserRole {
        <<enumeration>>
        EMPLOYER
        ADMIN
    }

    class VacancyStatus {
        <<enumeration>>
        DRAFT
        PUBLISHED
        ARCHIVED
        CLOSED
    }

    class ExperienceLevel {
        <<enumeration>>
        NO_EXPERIENCE
        UP_TO_ONE
        ONE_TO_THREE
        THREE_TO_SIX
        SIX_PLUS
    }

    class EmploymentType {
        <<enumeration>>
        FULL_TIME
        PART_TIME
        PROJECT
        INTERNSHIP
        VOLUNTEER
    }

    class WorkFormat {
        <<enumeration>>
        OFFICE
        REMOTE
        HYBRID
    }

    class EmploymentFormat {
        <<enumeration>>
        EMPLOYMENT_CONTRACT
        CONTRACT
        GIG
    }

    class WorkSchedule {
        <<enumeration>>
        FULL_DAY
        SHIFT
        FLEXIBLE
        REMOTE_WORK
        FLY_IN_FLY_OUT
    }

    %% ============================================
    %% EXCEPTION LAYER
    %% ============================================
    class NotFoundException {
        +String message
    }

    class ValidationException {
        +String message
    }

    class UnauthorizedException {
        +String message
    }

    class ForbiddenException {
        +String message
    }

    class GlobalExceptionHandler {
        -Logger logger
        +handleNotFoundException(ex, request) ResponseEntity~ErrorResponse~
        +handleValidationException(ex, request) ResponseEntity~ErrorResponse~
        +handleUnauthorizedException(ex, request) ResponseEntity~ErrorResponse~
        +handleForbiddenException(ex, request) ResponseEntity~ErrorResponse~
        +handleMethodArgumentNotValidException(ex, request) ResponseEntity~ErrorResponse~
        +handleAuthenticationException(ex, request) ResponseEntity~ErrorResponse~
        +handleAccessDeniedException(ex, request) ResponseEntity~ErrorResponse~
        +handleIllegalArgumentException(ex, request) ResponseEntity~ErrorResponse~
        +handleGeneralException(ex, request) ResponseEntity~ErrorResponse~
    }

    %% ============================================
    %% SECURITY LAYER
    %% ============================================
    class JwtTokenProvider {
        -String jwtSecret
        -Long jwtExpiration
        -SecretKey key
        +generateAccessToken(userId, role) String
        +generateRefreshToken(userId) String
        +getUserIdFromToken(token) UUID
        +getRoleFromToken(token) String
        +validateToken(token) Boolean
        -getClaims(token) Claims
    }

    class JwtAuthenticationFilter {
        -JwtTokenProvider jwtTokenProvider
        -UserRepository userRepository
        -Logger logger
        +doFilterInternal(request, response, chain) void
        -extractToken(request) String?
    }

    class SecurityConfig {
        -JwtAuthenticationFilter jwtAuthenticationFilter
        +securityFilterChain(http) SecurityFilterChain
        +passwordEncoder() PasswordEncoder
    }

    %% ============================================
    %% CONFIG LAYER
    %% ============================================
    class OpenApiConfig {
        +openAPI() OpenAPI
    }

    class PaginationConstants {
        <<object>>
        +DEFAULT_PAGE_SIZE int
    }

    %% ============================================
    %% RELATIONSHIPS - Controllers to Services
    %% ============================================
    AuthController --> AuthService : uses
    VacancyController --> VacancyService : uses
    TariffController --> TariffService : uses
    UserController --> UserRepository : uses
    UserController --> VacancyRepository : uses

    %% ============================================
    %% RELATIONSHIPS - Services to Repositories
    %% ============================================
    AuthService --> UserRepository : uses
    AuthService --> JwtTokenProvider : uses
    VacancyService --> VacancyRepository : uses
    VacancyService --> UserRepository : uses
    VacancyService --> TariffRepository : uses
    VacancyService --> SkillRepository : uses
    TariffService --> TariffRepository : uses

    %% ============================================
    %% RELATIONSHIPS - Repositories to Entities
    %% ============================================
    UserRepository --> User : manages
    VacancyRepository --> Vacancy : manages
    TariffRepository --> Tariff : manages
    SkillRepository --> Skill : manages

    %% ============================================
    %% RELATIONSHIPS - Entities to Entities
    %% ============================================
    User "1" --> "0..*" Vacancy : employer
    Tariff "1" --> "0..*" Vacancy : tariff
    Vacancy "0..*" --> "0..*" Skill : additionalSkills

    %% ============================================
    %% RELATIONSHIPS - Entities to Enums
    %% ============================================
    User --> UserRole : role
    Vacancy --> VacancyStatus : status
    Vacancy --> ExperienceLevel : experienceLevel
    Vacancy --> EmploymentType : employmentType
    Vacancy --> WorkFormat : workFormat
    Vacancy --> EmploymentFormat : employmentFormat
    Vacancy --> WorkSchedule : workSchedule

    %% ============================================
    %% RELATIONSHIPS - Controllers to DTOs
    %% ============================================
    AuthController ..> RegisterRequest : uses
    AuthController ..> LoginRequest : uses
    AuthController ..> RefreshTokenRequest : uses
    AuthController ..> AuthResponse : returns
    
    VacancyController ..> CreateVacancyRequest : uses
    VacancyController ..> UpdateVacancyRequest : uses
    VacancyController ..> VacancyResponse : returns
    VacancyController ..> PagedResponse : returns
    
    TariffController ..> CreateTariffRequest : uses
    TariffController ..> UpdateTariffRequest : uses
    TariffController ..> TariffResponse : returns
    TariffController ..> PagedResponse : returns
    
    UserController ..> UserResponse : returns

    %% ============================================
    %% RELATIONSHIPS - Services to DTOs
    %% ============================================
    AuthService ..> RegisterRequest : uses
    AuthService ..> LoginRequest : uses
    AuthService ..> AuthResponse : returns
    
    VacancyService ..> CreateVacancyRequest : uses
    VacancyService ..> UpdateVacancyRequest : uses
    VacancyService ..> VacancyResponse : returns
    VacancyService ..> PagedResponse : returns
    
    TariffService ..> CreateTariffRequest : uses
    TariffService ..> UpdateTariffRequest : uses
    TariffService ..> TariffResponse : returns
    TariffService ..> PagedResponse : returns

    %% ============================================
    %% RELATIONSHIPS - Mappers
    %% ============================================
    UserMapper ..> User : converts
    UserMapper ..> RegisterRequest : converts
    UserMapper ..> UserResponse : converts
    UserMapper ..> AuthResponse : converts
    
    VacancyMapper ..> Vacancy : converts
    VacancyMapper ..> CreateVacancyRequest : converts
    VacancyMapper ..> VacancyResponse : converts
    
    TariffMapper ..> Tariff : converts
    TariffMapper ..> CreateTariffRequest : converts
    TariffMapper ..> TariffResponse : converts
    
    PageMapper ..> PagedResponse : converts

    %% ============================================
    %% RELATIONSHIPS - Services use Mappers
    %% ============================================
    AuthService ..> UserMapper : uses
    VacancyService ..> VacancyMapper : uses
    VacancyService ..> PageMapper : uses
    TariffService ..> TariffMapper : uses
    TariffService ..> PageMapper : uses

    %% ============================================
    %% RELATIONSHIPS - Security
    %% ============================================
    JwtAuthenticationFilter --> JwtTokenProvider : uses
    JwtAuthenticationFilter --> UserRepository : uses
    SecurityConfig --> JwtAuthenticationFilter : configures
    AuthService --> JwtTokenProvider : uses

    %% ============================================
    %% RELATIONSHIPS - Exception Handling
    %% ============================================
    GlobalExceptionHandler ..> ErrorResponse : returns
    GlobalExceptionHandler ..> NotFoundException : handles
    GlobalExceptionHandler ..> ValidationException : handles
    GlobalExceptionHandler ..> UnauthorizedException : handles
    GlobalExceptionHandler ..> ForbiddenException : handles
    
    AuthService ..> ValidationException : throws
    AuthService ..> UnauthorizedException : throws
    VacancyService ..> NotFoundException : throws
    VacancyService ..> ForbiddenException : throws
    VacancyService ..> ValidationException : throws
    TariffService ..> NotFoundException : throws
    UserController ..> NotFoundException : throws

    %% ============================================
    %% RELATIONSHIPS - Constants
    %% ============================================
    VacancyController ..> PaginationConstants : uses
    TariffController ..> PaginationConstants : uses
```

## Component Legend

### 📘 Controller Layer
- **AuthController**: Authentication and registration endpoints
- **VacancyController**: Vacancy CRUD and BPMN workflow operations
- **TariffController**: Tariff management endpoints
- **UserController**: User profile and admin operations

### 📙 Service Layer
- **AuthService**: Authentication logic, JWT token generation
- **VacancyService**: Vacancy business logic, BPMN process implementation
- **TariffService**: Tariff business logic

### 📕 Repository Layer
- **UserRepository**: User data access
- **VacancyRepository**: Vacancy data access with custom queries
- **TariffRepository**: Tariff data access
- **SkillRepository**: Skill data access

### 📗 Entity Layer
- **User**: Employer/Admin entity
- **Vacancy**: Job vacancy entity
- **Tariff**: Publication tariff entity
- **Skill**: Skill entity for vacancy requirements

### 📔 DTO Layer
- **Request DTOs**: Input validation
- **Response DTOs**: Output formatting
- **Common DTOs**: PagedResponse, ErrorResponse

### 🔧 Mapper Layer
- **UserMapper**: User ↔ DTO conversions
- **VacancyMapper**: Vacancy ↔ DTO conversions
- **TariffMapper**: Tariff ↔ DTO conversions
- **PageMapper**: Page ↔ PagedResponse conversions

### 🔐 Security Layer
- **JwtTokenProvider**: JWT token generation and validation
- **JwtAuthenticationFilter**: JWT authentication filter
- **SecurityConfig**: Spring Security configuration

### ⚠️ Exception Layer
- **Custom Exceptions**: NotFoundException, ValidationException, UnauthorizedException, ForbiddenException
- **GlobalExceptionHandler**: Centralized exception handling

### ⚙️ Config Layer
- **OpenApiConfig**: Swagger/OpenAPI configuration
- **PaginationConstants**: Shared constants
