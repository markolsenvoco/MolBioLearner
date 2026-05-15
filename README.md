# MolBioLearner 🧬

An interactive web app for learning molecular biology — from organic chemistry fundamentals to graduate-level topics.

## Tech Stack
- **Backend:** Spring Boot 3.5 (Java 25, Maven)
- **Frontend:** HTML + Tailwind CSS (CDN) + Alpine.js (CDN) — no build step
- **Auth:** GitHub OAuth2 via Spring Security
- **Database:** H2 (local dev) → Azure Cosmos DB (production)
- **Hosting:** Azure App Service

## Local Development

### Prerequisites
- Java 25+ (`JAVA_HOME` set)
- Maven 3.9+ (or use included `mvnw`)

### Running locally

```bash
# Set your Maven bin on PATH first, then:
mvn spring-boot:run
```

Open http://localhost:8080

> **Note:** GitHub OAuth2 login is optional locally. Lessons and quizzes work without signing in. Progress saving requires a signed-in user.

### Dev conveniences
- H2 Console at http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:molbiolearner`)
- `DEV_MODE=true` (default) — relaxed security, H2 console enabled

### Adding GitHub OAuth2 locally (optional)
1. Go to https://github.com/settings/applications/new
2. Homepage URL: `http://localhost:8080`
3. Callback URL: `http://localhost:8080/login/oauth2/code/github`
4. Copy Client ID and Secret, then run:

```bash
GITHUB_CLIENT_ID=your_id GITHUB_CLIENT_SECRET=your_secret mvn spring-boot:run
```

## Project Structure

```
src/main/
├── java/com/molbiolearner/
│   ├── config/SecurityConfig.java
│   ├── controller/
│   │   ├── LessonController.java     # GET /api/modules, /api/lessons/{mod}/{id}
│   │   ├── QuizController.java       # POST /api/quiz/submit/{lessonId}
│   │   ├── ProgressController.java   # GET/POST /api/progress/**
│   │   └── UserController.java       # GET /api/user
│   ├── model/UserProgress.java
│   ├── model/QuizAttempt.java
│   ├── repository/
│   └── service/ProgressService.java
└── resources/
    ├── application.yml
    └── static/
        ├── index.html              # Single-page app
        ├── js/app.js               # Alpine.js application
        └── content/
            ├── modules.json        # Curriculum index
            ├── orgchem/            # Organic chemistry lessons
            ├── biochem/            # Biochemistry lessons
            ├── molbio/             # Molecular biology lessons
            └── advanced/           # Graduate-level lessons
```

## Adding Lesson Content

Each lesson is a JSON file at `src/main/resources/static/content/{moduleId}/{lessonId}.json`.

```json
{
  "id": "lesson-id",
  "moduleId": "orgchem",
  "title": "Lesson Title",
  "duration": 15,
  "sections": [
    { "id": "s1", "type": "text",    "content": "<p>HTML content here</p>" },
    { "id": "s2", "type": "callout", "content": "<strong>Key point</strong>" }
  ],
  "quiz": {
    "questions": [
      {
        "id": "q1",
        "type": "multiple-choice",
        "question": "Question text?",
        "options": ["A", "B", "C", "D"],
        "correct": 0,
        "explanation": "Explanation shown after submitting."
      }
    ]
  }
}
```

## Curriculum

| Level | Module | Lessons |
|-------|--------|---------|
| 1 🧪 | Organic Chemistry | Atoms & Bonds, Functional Groups, Isomers, Reactions |
| 2 🔬 | Biochemistry | Amino Acids, Nucleotides, Carbs & Lipids, Enzymes, Metabolism |
| 3 🧬 | Molecular Biology | DNA Replication, Transcription, Translation, Gene Regulation, Techniques |
| 4 🎓 | Advanced/Graduate | Epigenetics, CRISPR, Signal Transduction, Omics |

## Deploying to Azure

See `.github/workflows/azure-deploy.yml`. You need:
1. An Azure App Service (free F1 tier to start)
2. A publish profile secret: `AZURE_WEBAPP_PUBLISH_PROFILE` in GitHub repo secrets
3. Update `AZURE_WEBAPP_NAME` in the workflow file
