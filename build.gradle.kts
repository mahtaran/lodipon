buildscript {
	dependencies {
		classpath(libs.google.secrets)
	}
}

plugins {
	alias(libs.plugins.spotless)

	alias(libs.plugins.android.application) apply false
	alias(libs.plugins.kotlin.android) apply false
	alias(libs.plugins.compose.compiler) apply false
}

spotless {
	ratchetFrom("origin/main")

	json {
		target("**/*.json")
		targetExclude("dependency-graph-reports/**/*.json")

		jackson()
	}

	yaml {
		target("**/*.yaml")

		jackson()
	}
}
