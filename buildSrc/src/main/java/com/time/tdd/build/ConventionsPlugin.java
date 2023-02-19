package com.time.tdd.build;

import com.time.tdd.build.Versions;
import java.util.List;
import java.util.Objects;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.testing.Test;

public class ConventionsPlugin implements Plugin<Project> {

    private static final String enablePreview = "--enable-preview";

    @Override
    public void apply(Project project) {
        var rootProject = project.getRootProject();

        /// plugins
        project.apply(c -> c.plugin("java-library")
            .plugin("maven-publish")
            .plugin("eclipse")
            .plugin("com.diffplug.eclipse.apt"));

        /*设置respository*/
        {
            RepositoryHandler repos = project.getRepositories();
            // 设置本地jar存储路径
            repos.flatDir(f -> {
                // 本项目(使用本插件的项目)libs目录
                f.dir(project.file("libs"));
                // 根项目libs目录
                f.dir(rootProject.file("libs"));
            });
            // 阿里云
            repos.maven(m -> {
                m.setUrl("https://maven.aliyun.com/nexus/content/groups/public/");
            });
            // 中央仓库
            repos.mavenCentral();
            // camunda
            repos.maven(m -> {
                m.setUrl("https://app.camunda.com/nexus/content/groups/public");
            });
            repos.maven(m -> {
                m.setUrl("https://maven.alfresco.com/nexus/content/groups/public");
            });
            repos.maven(m -> {
                m.setUrl("https://repo.maven.apache.org/maven2/");
            });
            repos.maven(m -> {
                m.setUrl("https://oss.sonatype.org/content/repositories/snapshots");
            });
        }

        /// dependencies
        var dep = project.getDependencies();
        dep.add("implementation", dep.platform("org.springframework.boot:spring-boot-dependencies:" + Versions.springboot));
        dep.constraints(act -> {
            act.add("implementation", "org.mybatis:mybatis:3.5.10-SNAPSHOT")
                .because("ognl表达式在jdk 17下有bug");
        });

        dep.add("testImplementation", "org.junit.jupiter:junit-jupiter-api");
        dep.add("testImplementation", "org.junit.jupiter:junit-jupiter-params");
        dep.add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine");

        /// tasks

//        project.getTasks().withType(ProcessResources.class, act -> {
//            act.from("src/main/java").include("**/*.xml");
//            act.setIncludeEmptyDirs(false);
//        });

        project.getTasks()
            .withType(JavaCompile.class, act -> {
                act.getOptions()
                    .setEncoding("UTF-8");
                act.getOptions()
                    .getCompilerArgs()
                    .addAll(List.of(enablePreview, "-parameters"));
            });

        project.getTasks()
            .withType(Test.class, act -> {
                act.useJUnitPlatform();
                act.jvmArgs(enablePreview);
            });

        project.getTasks()
            .withType(JavaExec.class, act -> {
                act.jvmArgs(enablePreview);
            });

        SourceSetContainer sourceSets = Objects.requireNonNull(project.getExtensions()
            .getByType(SourceSetContainer.class));
//        sourceSets.create("main", act -> {
////            act.getOutput().getClassesDirs()
//        });
    }


}
