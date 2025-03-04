plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.10"
  id("org.jetbrains.intellij.platform") version "2.2.1"
}

group = "com.hardcoreeng"

version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  intellijPlatform { defaultRepositories() }
}

dependencies {
  intellijPlatform {
    intellijIdeaCommunity("2024.3")
  }
  implementation("com.caoccao.javet:javet:4.1.1")
  implementation("com.caoccao.javet:javet-node-windows-x86_64-i18n:4.1.1")
  implementation("com.caoccao.javet.buddy:javet-buddy:0.4.0")
  implementation("net.bytebuddy:byte-buddy:1.15.5")
}

tasks {
  // Set the JVM compatibility versions
  withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
  }
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> { kotlinOptions.jvmTarget = "17" }

  patchPluginXml {
    sinceBuild.set("243")
    untilBuild.set("251.*")
  }

  signPlugin {
    certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
    privateKey.set(System.getenv("PRIVATE_KEY"))
    password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
  }

  publishPlugin { token.set(System.getenv("PUBLISH_TOKEN")) }
}
