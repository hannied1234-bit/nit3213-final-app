package com.vu.nit3213app.di

import javax.inject.Qualifier

/**
 * Hilt qualifier for the campus part of the auth URL ("footscray").
 *
 * The app could have many String dependencies, so a qualifier labels this one.
 * Hilt then knows exactly which String to inject wherever @CampusLocation appears.
 *
 * It has to appear in two places to work. NetworkModule puts it on the
 * @Provides function that supplies the value, and AuthRepositoryImpl puts it
 * on the constructor parameter that receives it. Hilt matches the two by the
 * pair of type and qualifier.
 *
 * BINARY retention keeps the annotation in the compiled class files, where
 * Hilt's code generator reads it. It isn't needed at runtime, so RUNTIME
 * retention would only add reflection data nobody uses.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CampusLocation
