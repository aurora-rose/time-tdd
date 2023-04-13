plugins {
    id("com.time.tdd.java-conventions")
}

dependencies {
    implementation(libs.lombok)

    implementation("jakarta.inject:jakarta.inject-api:2.0.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testImplementation("org.mockito:mockito-core:4.3.1")

    implementation("org.eclipse.jetty:jetty-server:11.0.14")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.14")

    implementation("jakarta.ws.rs:jakarta.ws.rs-api:3.1.0")
//    implementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
    implementation(projects.container)


}