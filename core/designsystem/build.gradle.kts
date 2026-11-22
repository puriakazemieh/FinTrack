plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.android.lint)
    alias(libs.plugins.composeMultiplatformPlugin)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.kotlinxSerialization)

//    id("convention.kotlin.multiplatform.compose")
}
compose.resources {
    publicResClass = true
}

val verifyTranslationFormats = tasks.register("verifyTranslationFormats") {
    group = "verification"
    description = "Checks positional string arguments against the default locale."
    val resources = fileTree("src/commonMain/composeResources") {
        include("values*/strings.xml")
    }
    inputs.files(resources)
    doLast {
        val argument = Regex("""%(\d+)\${'$'}([A-Za-z])""")
        val brokenArgument = Regex(
            """%\d+\s+[sd]\s+[sd]\b|\b\d+%\s*\${'$'}\s*[sd]\b|\b\d+%\s+[sd]\b""",
            RegexOption.IGNORE_CASE
        )
        val doubledPercent = Regex("""%\s*%(?!\d+\${'$'}[A-Za-z])""")
        val reversedArgument = Regex("""%\s*\d+%|\${'$'}\d+%""")
        fun readStrings(file: java.io.File): Map<String, String> {
            val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
            val nodes = factory.newDocumentBuilder().parse(file).getElementsByTagName("string")
            return buildMap {
                repeat(nodes.length) { index ->
                    val element = nodes.item(index) as org.w3c.dom.Element
                    val key = element.getAttribute("name")
                    check(put(key, element.textContent) == null) { "Duplicate string: $file:$key" }
                }
            }
        }
        fun arguments(text: String) = argument.findAll(text)
            .map { it.groupValues[1] + ":" + it.groupValues[2] }.sorted().toList()

        val defaults = readStrings(file("src/commonMain/composeResources/values/strings.xml"))
        val errors = mutableListOf<String>()
        var compared = 0
        resources.files.sortedBy { it.path }.forEach { file ->
            readStrings(file).forEach strings@{ (key, text) ->
                val expected = defaults[key] ?: return@strings
                compared++
                if (arguments(text) != arguments(expected) || brokenArgument.containsMatchIn(text) ||
                    doubledPercent.containsMatchIn(text) || reversedArgument.containsMatchIn(text)) {
                    errors += "${file.parentFile.name}/$key: expected ${arguments(expected)}, got ${arguments(text)}; $text"
                }
            }
        }
        if (errors.isNotEmpty()) {
            throw GradleException("${errors.size} invalid translation formats:\n" + errors.joinToString("\n"))
        }
        logger.lifecycle("Validated {} strings across {} locale files", compared, resources.files.size)
    }
}
tasks.matching { it.name == "check" }.configureEach {
    dependsOn(verifyTranslationFormats)
}

kotlin {
    androidLibrary {
        namespace = "com.kazemieh.designsystem"
        compileSdk {
            version = release(36)
        }
        minSdk = 24
    }

    val xcfName = "core:designsystemKit"

    iosX64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    iosSimulatorArm64 {
        binaries.framework {
            baseName = xcfName
        }
    }

    js {
        browser()
    }

    jvm()

    sourceSets {
        commonMain {
            dependencies {
//                implementation(libs.jalali.datepicker.compose)
//                implementation(libs.jalalicalendar)
                implementation(project(":core:common"))
                implementation(project(":core:storage"))
                implementation(project(":core:jalali"))
                implementation(project(":core:money"))
                implementation(libs.compose.runtime)
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.compose.ui)
                implementation(libs.compose.material.icons.core)
                implementation(libs.compose.material.icons.extended)
                implementation(libs.compose.components.resources)
                implementation(libs.compose.uiToolingPreview)
                implementation(libs.androidx.lifecycle.runtimeCompose)
                implementation(libs.kotlinx.serialization)
                implementation(libs.koin.compose)


                implementation(libs.kotlinx.datetime)
                implementation(libs.reorderable)
                implementation(libs.image.loader)
            }
        }


        androidMain {
            dependencies {
                implementation(libs.androidx.activity.compose)
                implementation(libs.androidx.core.ktx)
            }
        }

        iosMain {
            dependencies {
            }
        }

        jsMain {
            dependencies {
            }
        }

        jvmMain {
            dependencies {
            }
        }
    }

}
