# Class Diagram

```mermaid
classDiagram
    %% ============================================
    %% CONTROLLER LAYER
    %% ============================================
    class AuthController {
        -AuthService authService
        +register(RegisterRequest) UserResponse
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
        +register(RegisterRequest) UserResponse
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
        ONE_TO_THREE
        THREE_TO_SIX
        MORE_THAN_SIX
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
        CIVIL_CONTRACT
        SELF_EMPLOYED
    }

    class WorkSchedule {
        <<enumeration>>
        NINE_TO_EIGHTEEN
        TEN_TO_NINETEEN
        EIGHT_TO_SEVENTEEN
        FLEXIBLE
        SHIFT
        REMOTE_FLEXIBLE
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
    class SecurityConfig {
        +securityFilterChain(http) SecurityFilterChain
        +passwordEncoder() PasswordEncoder
        +jaasAuthenticationProvider() DefaultJaasAuthenticationProvider
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
    AuthController ..> UserResponse : returns
    
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
    AuthService ..> UserResponse : returns
    
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
