plugins {
    id("java")
    id ("application")
}

group = "com.atm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
//    implementation ("org.springframework.boot:spring-boot-starter")
//    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.7.1")
//    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

tasks.test {
    useJUnitPlatform()
}
//application {
//    var mainClass = ("com.atm.main")
//}