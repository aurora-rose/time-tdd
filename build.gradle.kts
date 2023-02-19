import name.remal.gradle_plugins.plugins.code_quality.sonar.SonarLintExtension

plugins {
//  checkstyle
    id("name.remal.sonarlint").version("1.5.0").apply(false)
    idea
}


idea {

}

allprojects {
    repositories {

        flatDir {
            dirs(rootProject.file("libs"))
        }

        maven {
            url = uri("https://maven.aliyun.com/nexus/content/groups/public/")

            // isAllowInsecureProtocol = true
        }

        mavenCentral()

        maven {
            url = uri("https://app.camunda.com/nexus/content/groups/public")
        }

        maven {
            url = uri("https://maven.alfresco.com/nexus/content/groups/public")
        }

        maven {
            url = uri("https://repo.maven.apache.org/maven2/")
        }

        maven {
            url = uri("https://oss.sonatype.org/content/repositories/snapshots")
        }
        maven {
            url = uri("https://repository.aspose.com/repo/")
        }
        // mavenLocal()
    }
}

subprojects {

    /// checkstyle
    apply(plugin = "checkstyle")
    configure<CheckstyleExtension> {
        toolVersion = rootProject.libs.versions.checkstyle.get()
        configFile = rootProject.file("google_checks.xml")
        isIgnoreFailures = true
    }

    /// sonarlint
    // see https://remal.gitlab.io/gradle-plugins/plugins/name.remal.sonarlint/
    apply(plugin = "name.remal.sonarlint")
    configure<SonarLintExtension> {
        // TODO 开启
//    isIgnoreFailures = false
        isIgnoreFailures = true
        excludes {
            message("java:S1214")
//        message("kotlin:S100")
            message("xml:S125")
        }
        includes {
            message("java:S4266") // Enable java:S4266 which is disabled by default
        }
        ruleParameter("java:S119", "format", "^[A-Z][a-zA-Z0-9]*\$") // Allow upper camel-case for type parameter names
    }


    apply(plugin = "idea")
    configure<org.gradle.plugins.ide.idea.model.IdeaModel> {
        module {
            outputDir = file("build/classes/main")
            resourceDirs = setOf(file("build/resources/main"))
            testOutputDir = file("build/classes/test")
            testResourceDirs = setOf(file("build/resources/test"))
        }
    }

    tasks.withType<ProcessResources> {
        from("src/main/java") {
            include("**/*.xml")
        }
        includeEmptyDirs = false
    }

    tasks.withType<Jar> {
        val name = project.name
        if (!name.startsWith("time-")) {
            archiveBaseName.set("time-${name}")
        }
    }

}
