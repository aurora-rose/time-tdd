import Versions.springboot

object Versions {
  const val springboot = "2.5.0"
}

object Deps {
  const val springbootStarter = "org.springframework.boot:spring-boot-starter:${springboot}"
  const val springbootDependencies = "org.springframework.boot:spring-boot-dependencies:${springboot}"
}

object Plugins {
  val web = "com.time.tdd.java-conventions"
  const val springboot = "org.springframework.boot"
  const val springDependencyManagement = "io.spring.dependency-management"
}
