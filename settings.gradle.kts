rootProject.name = "time-tdd"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
enableFeaturePreview("VERSION_CATALOGS")

pluginManagement {
    repositories {
        flatDir {
            dirs("buildLibs")
        }


        maven {
            url = uri("https://maven.aliyun.com/nexus/content/groups/public/")

            // isAllowInsecureProtocol = true
        }

        gradlePluginPortal()

        maven {
            url = uri("https://maven.aliyun.com/nexus/content/groups/public/")

            // isAllowInsecureProtocol = true
        }

        mavenCentral()
    }
}

// see also: https://docs.gradle.org/current/userguide/platforms.html
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // FIXME 这个位置无法引用buildSrc下定义的符号, sprinboot的版本号要重复配置
            // version("springBoot", Versions.springboot)
            version("springBoot", "2.6.7")

            version("mybatisPlus", "3.5.1")
            version("mapstruct", "1.4.2.Final")
            version("lombok", "1.18.22")
            version("guava", "30.1-jre")
            version("webErrors", "1.4.0")
            version("commonsLang", "3.4")
            version("beanutils", "1.9.4")
            version("checkstyle", "9.3")
            version("fastjson.version", "1.2.79")
            version("servlet-api.version", "4.0.1")
            version("commons-collections4.version", "4.4")
            version("mongo-driverCore", "4.2.3")
            version("spring-stateMachine", "3.0.1")
            version("hutool.version", "5.7.22")
            version("reflections", "0.10.2")
            version("querydsl", "5.0.0")

            library(
                "springBoot-dependencies",
                "org.springframework.boot",
                "spring-boot-dependencies"
            ).versionRef("springBoot")
            library(
                "springBoot-starter",
                "org.springframework.boot",
                "spring-boot-starter"
            ).versionRef("springBoot")
            library(
                "springBoot-web-starter",
                "org.springframework.boot",
                "spring-boot-starter-web"
            ).versionRef("springBoot")
            library(
                "springBoot-validation-starter",
                "org.springframework.boot",
                "spring-boot-starter-validation"
            ).versionRef(
                "springBoot"
            )
            library(
                "spring-stateMachine",
                "org.springframework.statemachine",
                "spring-statemachine-core"
            ).versionRef("spring-stateMachine")

            library(
                "springBoot-jpa-starter",
                "org.springframework.boot",
                "spring-boot-starter-data-jpa"
            ).versionRef("springBoot")
            library(
                "mybatisPlus-starter",
                "com.baomidou",
                "mybatis-plus-boot-starter"
            ).versionRef("mybatisPlus")

            library("minio", "io.minio:minio:8.0.1")
            library(
                "mongo-driverCore",
                "org.mongodb",
                "mongodb-driver-core"
            ).versionRef("mongo-driverCore")

            library("lombok", "org.projectlombok", "lombok").versionRef("lombok")
            library("mapstruct-core", "org.mapstruct", "mapstruct").versionRef("mapstruct")
            library("mapstruct-processor", "org.mapstruct", "mapstruct-processor").versionRef("mapstruct")
            library("mapstruct-lombokBinding", "org.projectlombok:lombok-mapstruct-binding:0.1.0")

            library("jjwt", "io.jsonwebtoken:jjwt:0.9.1")

            library("webErrors", "me.alidg", "errors-spring-boot-starter").versionRef("webErrors")
            library("commonsLang", "org.apache.commons", "commons-lang3").versionRef("commonsLang")
            library("beanutils", "commons-beanutils", "commons-beanutils").versionRef("beanutils")
            library("guava", "com.google.guava", "guava").versionRef("guava")
            library("fastjson", "com.alibaba", "fastjson").versionRef("fastjson.version")
            library("servletApi", "javax.servlet", "javax.servlet-api").versionRef("servlet-api.version")
            library(
                "commonsCollections4",
                "org.apache.commons",
                "commons-collections4"
            ).versionRef("commons-collections4.version")
            library("hutool", "cn.hutool", "hutool-all").versionRef("hutool.version")
            library("reflections", "org.reflections", "reflections").versionRef("reflections")
            // querydsl
            library("querydsl-jpa", "com.querydsl", "querydsl-jpa").versionRef("querydsl")
            library("querydsl-apt", "com.querydsl", "querydsl-apt").versionRef("querydsl")

            bundle(
                "springBoot-web",
                listOf("springBoot-starter", "springBoot-web-starter", "springBoot-validation-starter")
            )
            plugin("springBoot", "org.springframework.boot").versionRef("springBoot")
            plugin(
                "springDependencyManagement",
                "io.spring.dependency-management"
            ).version("1.0.11.RELEASE")
        }
    }
}


includeProject("usual", "usual")
includeProject("args", "time-args")
includeProject("args-other", "args-other")
includeProject("container", "di-container")
includeProject("restful", "restful", false)


fun includeProject(name: String, path: String, changeBuildFileName: Boolean = true) {
    include(":$name")
    project(":$name").projectDir = File(settingsDir, path)
    if (changeBuildFileName) {
        project(":$name").buildFileName = "${name}.gradle.kts"
    }
}


buildCache {
    local {
        directory = File(rootDir, ".build-cache")
        removeUnusedEntriesAfterDays = 10
    }
}

