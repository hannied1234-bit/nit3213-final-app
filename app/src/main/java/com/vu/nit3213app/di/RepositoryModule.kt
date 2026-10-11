package com.vu.nit3213app.di

import com.vu.nit3213app.data.repository.AuthRepositoryImpl
import com.vu.nit3213app.data.repository.DashboardRepositoryImpl
import com.vu.nit3213app.domain.repository.AuthRepository
import com.vu.nit3213app.domain.repository.DashboardRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module that connects each repository interface to its implementation.
 *
 * Interfaces cannot be constructor-injected, so Hilt needs to be told which class
 * to use. @Binds does this with less generated code than @Provides, because Hilt
 * already knows how to build the implementation from its @Inject constructor.
 * Swapping in a different implementation later means changing only this file.
 *
 * It's an abstract class because @Binds functions have no body. They're
 * declarations that Hilt reads at compile time, and Kotlin only allows bodiless
 * functions in an abstract class or an interface.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    // Read this as "when someone asks for an AuthRepository, give them an
    // AuthRepositoryImpl". @Singleton means the same instance is shared by every
    // ViewModel, which is fine because the repositories hold no per-screen state.
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
