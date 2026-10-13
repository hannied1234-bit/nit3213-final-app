// Says which folder (package) this file belongs to.
package com.vu.nit3213app.di

// Qualifier: marks an annotation as a Hilt name tag.
import javax.inject.Qualifier

/**
 * Hilt qualifier for the campus part of the auth URL ("footscray").
 *
 * The app could have many String dependencies, so a qualifier labels this one.
 * It appears in two places: on provideCampusLocation() in NetworkModule, which
 * makes the value, and on the parameter in AuthRepositoryImpl, which receives
 * it. Hilt matches the two by type plus tag.
 */
// Makes this annotation a Hilt name tag.
@Qualifier
// Keeps the tag in the compiled code, where Hilt's code generator reads it.
@Retention(AnnotationRetention.BINARY)
// Creates the @CampusLocation tag itself.
annotation class CampusLocation
