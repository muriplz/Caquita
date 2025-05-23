# [Streetlight](https://caquita.app/api/v1) Backend API

Java 21 + [Javalin](https://javalin.io/) + [Gradle Kotlin DSL](https://docs.gradle.org/current/userguide/kotlin_dsl.html)

*Recommended editor: [IntelliJ IDEA](https://www.jetbrains.com/idea/)*

## Development

```bash
./gradlew run
```

#### Cleaning:

```bash
./gradlew clean
```

#### Building:

```bash
./gradlew build
```

*Note for deploying on production: Gradle uses shadow to create a fat jar with all dependencies bundled, therefore you have to use the jar that contains "-all" in the name.*