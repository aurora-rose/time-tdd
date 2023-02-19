import name.remal.gradle_plugins.dsl.extensions.*

plugins {
    id("com.time.tdd.java-conventions")
    alias(libs.plugins.springBoot)
//    alias(libs.plugins.springDependencyManagement)
}


dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    api("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.0.2")
    implementation("com.h2database:h2")
    implementation("javax.ws.rs:javax.ws.rs-api:2.1.1")
    implementation("javax.inject:javax.inject:1")
    implementation("org.webjars.npm:path:0.12.7")
    implementation("org.glassfish.jersey.containers:jersey-container-jetty-http:3.1.1")
    implementation("org.eclipse.persistence:javax.persistence:2.2.1")
    implementation("jakarta.persistence:jakarta.persistence-api:3.1.0")



    testImplementation("org.springframework.boot:spring-boot-starter-test")



    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)

}




