plugins {
    id("java")
    id("com.gradleup.shadow") version "8.3.0+"
    id("application")
}

group = "com.kryeit"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("io.javalin:javalin:6.4.0")
    implementation("org.slf4j:slf4j-simple:2.0.7")

    implementation("org.jsoup:jsoup:1.17.2")
    implementation("org.json:json:20240303")
    implementation("org.jdbi:jdbi3-core:3.45.1")
    implementation("org.jdbi:jdbi3-jackson2:3.45.1")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("de.mkammerer.snowflake-id:snowflake-id:0.0.2")
    implementation("io.javalin.community.ssl:ssl-plugin:6.3.0")
    implementation("org.postgresql:postgresql:42.7.4")
    // Geospatial support for PostgreSQL
    implementation("net.postgis:postgis-jdbc:2.5.1")

    implementation("com.auth0:java-jwt:4.4.0")
    implementation("org.mindrot:jbcrypt:0.4")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.sejda.imageio:webp-imageio:0.1.6")

    // Spherical geometry
    implementation("com.google.geometry:s2-geometry:2.0.0")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "com.kryeit.Main")
    }
}

application {
    mainClass.set("com.kryeit.Main")
}

tasks.test {
    useJUnitPlatform()
}