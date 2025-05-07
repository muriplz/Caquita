plugins {
    id("java")
}

group = "com.kryeit"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    implementation("net.dv8tion:JDA:5.4.0")

    implementation("org.json:json:20250107")
}

tasks.test {
    useJUnitPlatform()
}