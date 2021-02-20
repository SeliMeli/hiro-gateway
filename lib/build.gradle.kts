import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.kotlin.jvm") version "1.4.30"

  // Apply the java-library plugin for API and implementation separation.
  `java-library`
}

repositories {
  jcenter()
  mavenCentral()
}

dependencies {
  implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
  implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")
  implementation ("io.netty:netty-all:4.1.59.Final")

  testImplementation("io.mockk:mockk:1.10.6")

  testImplementation ("org.assertj:assertj-core:3.19.0")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

sourceSets.main {
  java.srcDirs("src/main/java", "src/main/kotlin")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}
