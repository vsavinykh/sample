import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

java.sourceCompatibility = JavaVersion.VERSION_15
val testcontainersVersion = "1.16.0"

plugins {
    java
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.spring")
    id("org.springframework.boot") version Versions.springBoot
    id("io.spring.dependency-management") version Versions.springDependencyManagement
    id("org.openapi.generator") version Versions.openapiGenerator
    id("com.lanehealth.jooqDockerGenerator")
    id("com.bmuschko.docker-remote-api") version "7.1.0"
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-bootstrap:3.0.3")
    implementation("io.awspring.cloud:spring-cloud-aws-autoconfigure:2.3.1")
    implementation("io.awspring.cloud:spring-cloud-starter-aws-secrets-manager-config:2.3.1")
    implementation("net.devh:grpc-server-spring-boot-starter:2.12.0.RELEASE")
    implementation("net.devh:grpc-client-spring-boot-starter:2.12.0.RELEASE")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.flywaydb:flyway-core")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:${Versions.kotlin}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.kotlinCoroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-bom:${Versions.kotlinCoroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:${Versions.kotlinCoroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:${Versions.kotlinCoroutines}")
    implementation("org.hibernate.validator:hibernate-validator")
    implementation("software.amazon.awssdk:secretsmanager:2.17.20")
    implementation("software.amazon.awssdk:regions:2.17.20")
    implementation(Dependencies.jacksonDatabindNullable)
    implementation(Dependencies.mapstruct)
    implementation(Dependencies.logging)
    implementation("io.swagger:swagger-annotations:1.5.21")
    implementation(Dependencies.awsSqs)
    implementation(Dependencies.awsSts)
    implementation(Dependencies.springCloudAwsMessaging)
    implementation(Dependencies.jooq)
    implementation(project(":finch:finch-contract"))
    implementation("net.logstash.logback:logstash-logback-encoder:6.6")

    kapt(Dependencies.mapstructProcessor)
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    compileOnly(Dependencies.findbugsJsr305)

    runtimeOnly("io.micrometer:micrometer-registry-prometheus")
    runtimeOnly("io.r2dbc:r2dbc-postgresql:0.8.4.RELEASE")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:r2dbc:$testcontainersVersion")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.assertj:assertj-core:3.11.1")
    testImplementation("com.ninja-squad:springmockk:3.0.1")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.kotlinCoroutines}")
}

val generatedSourcesDir = "$buildDir/generated/finch-client"

task<GenerateTask>("generateClient") {
    inputSpec.set("$projectDir/src/main/resources/openapi/finch-contract.yaml")
    outputDir.set(generatedSourcesDir)
    generatorName.set("java")
    generateApiTests.set(false)
    generateModelTests.set(false)

    val basePackage = "com.lanehealth.payrollservice.finch.contract"
    configOptions.set(
        mapOf(
            "modelPackage" to "$basePackage.models",
            "apiPackage" to "$basePackage.client",
            "sourceFolder" to "finch-client",
            "library" to "webclient",
            "asyncNative" to "true",
            "useBeanValidation" to "true",
            "dateLibrary" to "java8",
            "serializationLibrary" to "jackson"
        )
    )
}

val jooqGeneratedDir = "$buildDir/generated/source/jooq"

jooqDockerGenerator {
    migrationDir = "$projectDir/src/main/resources/db/migration"
    outputDir = jooqGeneratedDir
    packageName = "com.lanehealth.payroll.model.generated"
}

kotlin {
    sourceSets {
        val main by getting {
            kotlin.srcDirs(jooqGeneratedDir, "$buildDir/generated/source/kapt/main", "$generatedSourcesDir/finch-client")
        }
    }
}

java {
    sourceSets {
        val main by getting {
            java.srcDirs(
                jooqGeneratedDir,
                "$buildDir/generated/source/kapt/main",
                "$generatedSourcesDir/finch-client"
            )
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    dependsOn(":finch:finch-api:generateClient")
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict", "-Xjvm-default=all-compatibility")
        jvmTarget = "15"
    }
}

tasks.create("buildFinchDockerImage", DockerBuildImage::class) {
    inputDir.set(projectDir)
    val imagePrefix = "481193184231.dkr.ecr.us-west-2.amazonaws.com"
    val tags = arrayOf("latest", version)
    tags.forEach { images.add("$imagePrefix/lh-finch:$it") }
    buildArgs.set(mapOf("VERSION" to version.toString()))
}
