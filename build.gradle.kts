plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(files("libs/poseidon.jar"))
}