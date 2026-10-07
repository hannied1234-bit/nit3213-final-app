package com.vu.nit3213app.di

import javax.inject.Qualifier

/**
 * Hilt qualifier for the campus part of the auth URL ("footscray").
 *
 * The app could have many String dependencies, so a qualifier labels this one.
 * Hilt then knows exactly which String to inject wherever @CampusLocation appears.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CampusLocation
