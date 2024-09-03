plugins {
  application
}

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

application {
  mainClass.set("ar.emily.sponsors.Main")
}

repositories {
  mavenCentral()
}

fun DependencyHandlerScope.tinylogBinding(name: String): Dependency =
  create("org.tinylog:$name-tinylog:2.6.2")

dependencies {
  implementation(platform("com.fasterxml.jackson:jackson-bom:2.17.1"))
  implementation(platform("io.projectreactor:reactor-bom:2023.0.6"))
  implementation(platform("io.netty:netty-bom:4.1.110.Final"))
  implementation("io.projectreactor.netty:reactor-netty-core")
  implementation("io.projectreactor.netty:reactor-netty-http")
  implementation("com.discord4j:discord4j-core:3.2.6")
  runtimeOnly("io.netty", "netty-transport-native-epoll", classifier = "linux-aarch_64")

  implementation("org.tinylog:tinylog-api:2.6.2")
  runtimeOnly("org.tinylog:tinylog-impl:2.6.2")
  runtimeOnly("org.slf4j:slf4j-api:2.0.9")
  runtimeOnly(tinylogBinding("slf4j"))
  implementation(tinylogBinding("jul"))
  runtimeOnly(tinylogBinding("jsl"))

  compileOnly("org.jetbrains:annotations:24.0.1")
}

tasks {
  compileJava {
    options.encoding = "UTF-8"
  }

  processResources {
    filteringCharset = "UTF-8"
  }

  named<JavaExec>("run") {
    workingDir("run")
    standardInput = System.`in`
    standardOutput = System.out
    errorOutput = System.err
    doFirst {
      workingDir.mkdirs()
    }
  }
}
