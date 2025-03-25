plugins {
    id("java")
    id("application")
}

group = "com.atm"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

// Khai báo Main Class để có thể chạy ứng dụng
application {
    mainClass.set("com.atm.Main")
}
