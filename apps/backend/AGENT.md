# InsideMovie Backend - AI Agent Guide

> **Note**: This project is under active refactoring. Conventions and structures described here are guidelines, not strict rules. Always ask before making assumptions about what should/shouldn't change.

## 1. Project Context

### Overview
InsideMovie is a movie review and emotion analysis platform that helps users discover movies based on their emotional preferences.

### Tech Stack
- **Framework**: Spring Boot 3.5.3, Java 17
- **Database**: MySQL (production), H2 (local)
- **ORM**: Spring Data JPA
- **Cache**: Spring Data Redis
- **Batch**: Spring Batch
- **Security**: Spring Security + JWT
- **HTTP Client**: WebClient (Spring WebFlux)
- **API Docs**: Springdoc OpenAPI (Swagger)
- **Build Tool**: Gradle

### Domain Structure
```
api/
├── admin/       # Admin dashboard and reporting
├── member/      # User management, OAuth, emotion summaries
├── movie/       # Movie data, box office, TMDB/KOBIS integration
├── review/      # Reviews, ratings, emotion analysis
├── match/       # Movie voting/matching system
├── recommend/   # Emotion-based movie recommendations
├── report/      # User/review reporting system
└── constant/    # Enums (Authority, EmotionType, GenreType, etc.)
```

## 2. Development Principles (Kent Beck Style)

### Test-Driven Development (TDD)
**Current State**: Minimal test coverage (only `BackendApplicationTests` exists)
**Context**: Project is under active refactoring

**When Writing New Features**:
1. **Red**: Write the simplest failing test first
2. **Green**: Implement minimal code to make it pass
3. **Refactor**: Clean up while keeping tests green

**When Refactoring Existing Code**:
1. **Option A** (with tests): Add characterization tests → refactor → verify tests pass
2. **Option B** (without tests): Make small, verifiable changes → manually test → consider adding tests after
3. **Prioritize**: Refactoring for better structure over achieving test coverage initially
4. **Discuss**: If uncertain whether to add tests during refactoring, ask before proceeding

**Test Priority** (introduce incrementally):
1. Service layer unit tests (business logic)
2. Controller integration tests (`@WebMvcTest`)
3. Repository tests (only for complex queries)

### Separation of Concerns
- **Structural changes**: Renaming, moving code, extracting methods, package reorganization
- **Behavioral changes**: Modifying logic, adding features, fixing bugs
- **Refactoring guideline**: Make structural changes first, commit separately, then add behavior
- **During active refactoring**: Batch related structural changes together, but keep commits logical

### Code Quality Principles
- **Single Responsibility**: One method does one thing
- **DRY Principle**: Eliminate duplication (but avoid premature abstraction)
- **Method Size**: Keep methods small and focused
- **Naming**: Use descriptive names that reveal intent
- **Refactoring**: Improve design incrementally, not all at once

## 3. InsideMovie Code Conventions

### Package Structure
**Current structure** (subject to refactoring):
```
api/{domain}/
├── controller/     # REST endpoints
├── service/        # Business logic
├── repository/     # Data access
├── entity/         # JPA entities
├── dto/            # Data transfer objects
└── scheduler/      # Background jobs (if applicable)
```

**Principles** (maintain during refactoring):
- Keep domain logic grouped together
- Separate infrastructure concerns from business logic
- Follow Spring Boot best practices for layering

### Naming Conventions
**Current conventions** (discuss changes during refactoring):
- **DTOs**: `*RequestDto`, `*ResponseDto`, `*DTO` (inconsistent - consider standardizing)
- **Entities**: Singular noun (e.g., `Member`, `Movie`, `Review`)
- **Repositories**: `{Entity}Repository`
- **Services**: `{Entity}Service`
- **Controllers**: `{Domain}Controller`

**Note**: If refactoring changes these conventions, update this document accordingly.

### Lombok Usage
**Default approach** (adjust based on refactoring needs):

**DTOs** - Use Lombok liberally:
```java
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoDto {
    private Long memberId;
    private String email;
    private String nickname;
}
```

**Entities** - Use Lombok conservatively:
```java
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String nickname;

    // Explicit state-changing methods (avoid @Setter)
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}
```

**Guiding principles** (not strict rules):
- ✅ Use `@Getter`, `@Builder` for DTOs and entities
- ✅ Use `@NoArgsConstructor`, `@AllArgsConstructor` for DTOs
- ⚠️ Avoid `@Setter` on entities (prefer explicit methods)
- ⚠️ Avoid `@Data` (too implicit, hides intent)
- 💬 **When refactoring**: Discuss tradeoffs if changing Lombok usage patterns

### Entity Design Guidelines
- Extend `BaseTimeEntity` for audit fields (or use alternative approach if refactoring)
- Use `@Builder.Default` for collections and default values
- Prefer `FetchType.LAZY` for associations (adjust if performance tuning)
- Implement explicit state-changing methods (immutability preference)

## 4. External API Integration

### TMDB API (The Movie Database)
- **Purpose**: Movie metadata, posters, cast, ratings
- **Config**: `tmdb.api.key`, `tmdb.api.base-url`
- **Language**: Korean (`ko-KR`)
- **Service**: `MovieService`, `MovieDetailService`

### KOBIS API (Korean Box Office)
- **Purpose**: Daily/weekly box office rankings
- **Config**: `kobis.api.key`, `kobis.api.base-url`
- **Schedulers**: `DailyBoxOfficeScheduler`, `WeeklyBoxOfficeScheduler`
- **Service**: `BoxOfficeService`

### FastAPI Server (Emotion Analysis)
- **Purpose**: Sentiment/emotion prediction from review text
- **Client**: `RestTemplate` bean named `fastApiRestTemplate`
- **DTOs**: `PredictRequestDTO`, `PredictResponseDTO`
- **Service**: `ReviewService`

### Kakao OAuth
- **Purpose**: Social login
- **Config**: `spring.security.oauth2.client.registration.kakao`
- **Service**: `OAuthService`

### HTTP Client Pattern
```java
@Service
@RequiredArgsConstructor
public class ExampleService {
    private final RestTemplate tmdbRestTemplate; // or fastApiRestTemplate

    @Value("${tmdb.api.base-url}")
    private String baseUrl;

    public MovieData fetchMovie(Long id) {
        String url = baseUrl + "/movie/" + id;
        ResponseEntity<MovieData> response = tmdbRestTemplate
            .getForEntity(url, MovieData.class);
        return response.getBody();
    }
}
```

## 5. Testing Strategy

### Current State
- **Production code**: ~100+ classes
- **Test coverage**: ~1% (only smoke test)

### Incremental Testing Approach
1. **New features**: Must include tests (TDD cycle)
2. **Bug fixes**: Add regression test first
3. **Refactoring**: Add characterization tests before refactoring

### Unit Test Template (Service Layer)
```java
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReview_success() {
        // given
        ReviewCreateDTO request = ReviewCreateDTO.builder()
            .movieId(1L)
            .rating(4.5)
            .content("Great movie!")
            .build();

        // when
        ReviewCreatedResponseDTO result = reviewService.createReview(1L, request);

        // then
        assertNotNull(result.getReviewId());
        verify(reviewRepository).save(any(Review.class));
    }
}
```

### Integration Test Template (Controller)
```java
@WebMvcTest(ReviewController.class)
class ReviewControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Test
    void getReview_returns200() throws Exception {
        // given
        ReviewResponseDTO response = ReviewResponseDTO.builder()
            .reviewId(1L)
            .content("Test review")
            .build();
        given(reviewService.getReview(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/reviews/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reviewId").value(1));
    }
}
```

## 6. Schedulers and Background Jobs

### Configured Schedulers
- **Daily Box Office**: Every day at 16:30 KST (`scheduler.cron.daily`)
- **Weekly Box Office**: Every Monday at 16:30 KST (`scheduler.cron.weekly`)
- **Movie Updates**: Controlled by `movie.update-enabled` flag
- **Movie Seeding**: Controlled by `movie.seed-enabled` flag (disable after initial seed)
- **Match Scheduler**: Weekly match result aggregation

### Scheduler Example
```java
@Component
@RequiredArgsConstructor
@Slf4j
public class DailyBoxOfficeScheduler {
    private final BoxOfficeService boxOfficeService;

    @Scheduled(cron = "${scheduler.cron.daily}", zone = "${scheduler.zone}")
    public void fetchDailyBoxOffice() {
        log.info("Starting daily box office fetch");
        boxOfficeService.fetchAndSaveDailyBoxOffice();
    }
}
```

## 7. Security and Configuration

### Environment Variables
Store sensitive data in environment variables or `application-key.yml` (not committed):
- `tmdb.api.key`: TMDB API key
- `kobis.api.key`: KOBIS API key
- `kakao.client.id`, `kakao.client.secret`: OAuth credentials
- `EMAIL_USERNAME`, `EMAIL_PASSWORD`: Email service credentials

### JWT Authentication
- **Access Token**: Short-lived, sent in `Authorization` header
- **Refresh Token**: Long-lived, sent in `Authorization-Refresh` header
- **Provider**: `JwtProvider`
- **Filter**: `JwtFilter`

### Security Rules
- ❌ Never commit API keys or secrets to git
- ❌ Never log sensitive data (passwords, tokens, PII)
- ✅ Use `@Slf4j` for logging
- ✅ Use environment-specific configs (`application-local.yml`, `application-prod.yml`)

## 8. Common Patterns

### Exception Handling
```java
// Custom exceptions in common.exception package
throw new NotFoundException(ErrorStatus.MEMBER_NOT_FOUND);
throw new BadRequestException(ErrorStatus.INVALID_INPUT);
throw new UnAuthorizedException(ErrorStatus.INVALID_TOKEN);
```

### Pagination
```java
Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
Page<Review> reviews = reviewRepository.findAll(pageable);
```

### DTO Conversion
```java
// Entity → DTO
public static MemberInfoDto from(Member member) {
    return MemberInfoDto.builder()
        .memberId(member.getId())
        .email(member.getEmail())
        .nickname(member.getNickname())
        .build();
}

// DTO → Entity
public Member toEntity(PasswordEncoder encoder) {
    return Member.builder()
        .email(email)
        .password(encoder.encode(password))
        .authority(Authority.ROLE_USER)
        .build();
}
```

## 9. AI Agent Interaction Guidelines

### When Adding New Features
1. Ask clarifying questions about requirements
2. Propose a test-first approach (discuss if project is mid-refactoring)
3. Draft a small test case
4. Implement minimal code to pass
5. Suggest refactoring opportunities

### When Refactoring (Project is Currently Under Refactoring)
1. **Read first**: Understand current structure and dependencies
2. **Ask about scope**: Clarify what should/shouldn't be changed
3. **Propose changes**: Suggest refactoring approach before executing
4. **Small steps**: Make incremental, verifiable changes
5. **Document intent**: Explain why refactoring improves the design
6. **Respect ongoing work**: Don't assume current structure is final

### When Modifying Existing Code
1. Read relevant files first
2. Understand current behavior
3. Ask if tests are needed for this change
4. Make the change incrementally
5. Verify nothing else broke

### When Reviewing Code
- Check for Lombok consistency (but expect variation during refactoring)
- Verify proper exception handling
- Look for duplicated logic
- Suggest layering improvements (don't enforce rigidly during refactoring)
- Validate DTO/Entity separation

### Communication Style
- Be concise and direct
- Explain "why" not just "what"
- Suggest alternatives when saying "no"
- **Ask before making large structural changes** (especially important during refactoring)
- Treat conventions as guidelines, not laws
- Acknowledge when multiple approaches are valid

## 10. References

### Official Documentation
- Spring Boot: https://spring.io/projects/spring-boot
- Spring Data JPA: https://spring.io/projects/spring-data-jpa
- Lombok: https://projectlombok.org/
- TMDB API: https://developers.themoviedb.org/3
- KOBIS API: http://www.kobis.or.kr/kobisopenapi/homepg/apiservice/searchServiceInfo.do

### Testing Resources
- JUnit 5: https://junit.org/junit5/
- Mockito: https://site.mockito.org/
- Spring Boot Testing: https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing
