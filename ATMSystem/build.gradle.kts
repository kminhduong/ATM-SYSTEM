plugins {
    id("java")
    id("application")

    // Thêm plugin Spring Boot
    id("org.springframework.boot") version "3.2.2"
    id("io.spring.dependency-management") version "1.1.4"
}

group = "com.atm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
//    // Driver MySQL để kết nối database
//    implementation("mysql:mysql-connector-java:8.0.33")
    // Dùng H2 Database thay cho MySQL
    implementation("com.h2database:h2")
    //có mysql sửa lại sau
    // Spring Boot Web để tạo REST API
    implementation("org.springframework.boot:spring-boot-starter-web")

    // Spring Boot JPA để làm việc với database
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")

    // H2 Database (hoặc thay bằng MySQL)
    implementation("com.h2database:h2")

    // Validation dữ liệu
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Thư viện test
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

// Khai báo Main Class để chạy ứng dụng
application {
    mainClass.set("com.atm.Main")
}
