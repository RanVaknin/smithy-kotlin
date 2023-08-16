/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
import aws.sdk.kotlin.gradle.dsl.skipPublishing
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlinx.benchmark")
}

skipPublishing()

val optinAnnotations = listOf("kotlin.RequiresOptIn", "aws.smithy.kotlin.runtime.InternalApi")

kotlin {
    sourceSets {
        all {
            optinAnnotations.forEach { languageSettings.optIn(it) }
        }

        val kotlinxBenchmarkVersion: String by project
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:$kotlinxBenchmarkVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(project(":runtime:protocol:http-client-engines:http-client-engine-okhttp"))
                implementation(project(":runtime:protocol:http-client-engines:http-client-engine-crt"))

                val ktorVersion: String by project
                // mock/embedded server
                implementation("io.ktor:ktor-server-netty:$ktorVersion")
            }
        }
    }
}

benchmark {
    targets {
        register("jvm")
    }

    configurations {
        getByName("main") {
            iterations = 5
            iterationTime = 5
            iterationTimeUnit = "s"
            warmups = 3
            outputTimeUnit = "s"
            reportFormat = "text"
            advanced("jvmForks", "1")
        }
    }
}

// Workaround for https://github.com/Kotlin/kotlinx-benchmark/issues/39
afterEvaluate {
    tasks.named<org.gradle.jvm.tasks.Jar>("jvmBenchmarkJar") {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
}
