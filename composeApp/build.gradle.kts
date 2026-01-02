import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.composeHotReload)
}

repositories {
	google()
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

kotlin {
	jvm()

	js {
		browser()
		binaries.executable()
	}

	@OptIn(ExperimentalWasmDsl::class) wasmJs {
		browser()
		binaries.executable()
	}

	sourceSets {
		commonMain.dependencies {
			implementation(compose.runtime)
			implementation(compose.foundation)
			implementation(compose.material3)
			implementation(compose.ui)
			implementation(compose.components.resources)
			implementation(compose.components.uiToolingPreview)
			implementation(libs.androidx.lifecycle.viewmodelCompose)
			implementation(libs.androidx.lifecycle.runtimeCompose)
		}
		commonTest.dependencies {
			implementation(libs.kotlin.test)
		}
		jvmMain.dependencies {
			implementation(compose.desktop.currentOs)
			implementation(libs.kotlinx.coroutinesSwing)

			implementation("com.formdev:flatlaf:3.7")
			implementation("com.formdev:flatlaf-intellij-themes:3.7")

			implementation("io.github.aaronjyoder:fill:1.0.0-0.2206")
			implementation("com.github.aaronjyoder:polylabel-java-mirror:1.3.0")

			implementation("com.riskrieg:map:1.0.0-2.2206")
			implementation("com.riskrieg:palette:1.1.0-4.2206")
			implementation("com.riskrieg:codec:1.0.0-3.2206")

			implementation("tools.jackson.module:jackson-module-kotlin:3.0.3") // Needed for workaround at EditorModel.kt#343

			implementation("org.jgrapht:jgrapht-io:1.5.2")
		}
	}
}

fun getProjectProperty(name: String) = project.properties[name] as? String

compose.desktop {
	application {
		mainClass = "com.openkrieg.MainKt"

		nativeDistributions {
			targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Dmg)
			packageName = "Openkrieg Map Editor"
			packageVersion = "2.8.7"
			description = "The official Openkrieg map editor."
			vendor = "Openkrieg"

			val iconsRoot = project.file("launcher-icon")

			linux {
				iconFile.set(iconsRoot.resolve("linux.png"))

				packageName = "openkrieg-map-editor"
				debMaintainer = "Openkrieg <contact@adelheid.org>"
				menuGroup = "Openkrieg"
			}

			windows {
				iconFile.set(iconsRoot.resolve("windows.ico"))

				menuGroup = "Openkrieg"
				upgradeUuid = getProjectProperty("guid")
			}

			macOS {
				iconFile.set(iconsRoot.resolve("macos.icns"))

				bundleID = "com.openkrieg.mapeditor"
			}
		}
	}
}
