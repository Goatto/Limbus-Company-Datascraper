plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Testy
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Scraping
    implementation("org.jsoup:jsoup:1.22.1")

    // Logging
    implementation("org.jboss.logging:jboss-logging:3.6.1.Final")

    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.44")
    annotationProcessor("org.projectlombok:lombok:1.18.44")
    testCompileOnly("org.projectlombok:lombok:1.18.44")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.44")

    // Jackson
    implementation("com.fasterxml.jackson.core:jackson-databind:2.20.1")

    // Dialects do komunikacji pomiędzy Jackson a typem danych JSONB
    implementation("org.hibernate.orm:hibernate-community-dialects:7.2.5.Final")

    // Połączenie z bazą danych
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:4.0.0")
    implementation("org.postgresql:postgresql:42.7.8")
}

tasks.test {
    useJUnitPlatform()
}