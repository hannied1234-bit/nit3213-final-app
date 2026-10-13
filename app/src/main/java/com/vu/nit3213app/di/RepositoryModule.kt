// Says which folder (package) this file belongs to.
package com.vu.nit3213app.di

// AuthRepositoryImpl: the class that really does the login.
import com.vu.nit3213app.data.repository.AuthRepositoryImpl
// DashboardRepositoryImpl: the class that really fetches the dashboard.
import com.vu.nit3213app.data.repository.DashboardRepositoryImpl
// AuthRepository: the login interface the ViewModel asks for.
import com.vu.nit3213app.domain.repository.AuthRepository
// DashboardRepository: the dashboard interface the ViewModel asks for.
import com.vu.nit3213app.domain.repository.DashboardRepository
// Binds: marks a function that links an interface to the class that fills it in.
import dagger.Binds
// Module: marks a class as a set of Hilt instructions.
import dagger.Module
// InstallIn: says how long the things this module makes should live.
import dagger.hilt.InstallIn
// SingletonComponent: Hilt's container that lives as long as the whole app.
import dagger.hilt.components.SingletonComponent
// Singleton: the label meaning "make only one and share it".
import javax.inject.Singleton

/**
 * Hilt module that connects each repository interface to its implementation.
 *
 * Interfaces cannot be built directly, so Hilt needs to be told which class to
 * use. @Binds does this with less generated code than @Provides, because Hilt
 * already knows how to build the implementation from its @Inject constructor.
 * Swapping in a different implementation later means changing only this file.
 */
// Marks this as a set of Hilt instructions.
@Module
// Says everything made here lives as long as the app.
@InstallIn(SingletonComponent::class)
// Creates the module. It's "abstract" because @Binds functions have no body.
abstract class RepositoryModule {

    // Links an interface to the class that fills it in.
    @Binds
    // Makes only one and shares it.
    @Singleton
    // Reads as: "when someone asks for an AuthRepository, give them an AuthRepositoryImpl".
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    // Links an interface to the class that fills it in.
    @Binds
    // Makes only one and shares it.
    @Singleton
    // Reads as: "when someone asks for a DashboardRepository, give them a DashboardRepositoryImpl".
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
