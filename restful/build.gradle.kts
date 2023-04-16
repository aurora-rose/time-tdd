plugins {
    id("com.time.tdd.java-conventions")
}

dependencies {
    implementation(libs.lombok)
    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    implementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
    implementation(projects.container)
    implementation("org.slf4j:slf4j-api:2.0.7")
    testImplementation("org.slf4j:slf4j-log4j12:2.0.7")



    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.mockito:mockito-core:4.3.1")
    testImplementation("org.eclipse.jetty:jetty-server:11.0.14")
    testImplementation("org.eclipse.jetty:jetty-servlet:11.0.14")
}