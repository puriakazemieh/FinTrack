package ext

import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependencyBundle
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.provider.Provider

internal fun DependencyHandler.implementationBundle(dependencyNotation: Provider<ExternalModuleDependencyBundle>): Dependency? =
    add("implementation", dependencyNotation)

internal fun DependencyHandler.implementation(dep: Provider<MinimalExternalModuleDependency>): Dependency? =
    add("implementation", dep)

internal fun DependencyHandler.debugImplementation(dependencyNotation: Provider<ExternalModuleDependencyBundle>): Dependency? =
    add("debugImplementation", dependencyNotation)

internal fun DependencyHandler.annotationProcessor(dependencyNotation: Provider<MinimalExternalModuleDependency>): Dependency? =
    add("annotationProcessor", dependencyNotation)

internal fun DependencyHandler.testImplementation(dep: Provider<MinimalExternalModuleDependency>): Dependency? =
    add("testImplementation", dep)

internal fun DependencyHandler.testRuntimeOnly(dep: Provider<MinimalExternalModuleDependency>): Dependency? =
    add("testRuntimeOnly", dep)

internal fun DependencyHandler.ksp(dep: Provider<MinimalExternalModuleDependency>): Dependency? =
    add("ksp", dep)

internal fun DependencyHandler.coreLibraryDesugaring(dep: Provider<MinimalExternalModuleDependency>): Dependency? =
    add("coreLibraryDesugaring", dep)

internal fun DependencyHandler.androidTestImplementation(dependencyNotation: Provider<MinimalExternalModuleDependency>): Dependency? =
    add("androidTestImplementation", dependencyNotation)

internal fun DependencyHandler.api(dependencyNotation: Provider<MinimalExternalModuleDependency>): Dependency? =
    add("api", dependencyNotation)