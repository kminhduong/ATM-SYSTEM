plugins {
    id("java")
    id("application")
    id("org.springframework.boot") version "3.2.3" // Đảm bảo đây là phiên bản mới nhất
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.atm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Dependencies hiện tại của bạn với các phiên bản tối ưu
    implementation("com.mysql:mysql-connector-j:8.2.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Cấu hình test chính xác để tránh lỗi deprecated
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0") // Cụ thể hơn

    // Tối ưu hóa các thư viện JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.0")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("org.json:json:20250107")

    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")

    // Thêm Jakarta Servlet API với phiên bản hiện đại
    implementation("jakarta.servlet:jakarta.servlet-api:6.0.0")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf:3.4.4")

}

tasks.test {
    useJUnitPlatform()
    // Đảm bảo cấu hình đầy đủ cho test framework
    testLogging {
        events ("PASSED", "FAILED", "SKIPPED")
    }
}

application {
    mainClass.set("com.atm.Main")
}