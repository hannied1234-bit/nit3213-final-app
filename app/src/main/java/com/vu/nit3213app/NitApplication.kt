// Says which folder (package) this file belongs to.
package com.vu.nit3213app

// Application: the Android class that represents the whole running app.
import android.app.Application
// HiltAndroidApp: the Hilt label that switches Hilt on for the app.
import dagger.hilt.android.HiltAndroidApp

/**
 * The Application class for the app.
 *
 * @HiltAndroidApp starts Hilt's code generation and creates the app-wide
 * dependency container (the SingletonComponent). Every other Hilt annotation in
 * the project relies on this one being here. It is registered in
 * AndroidManifest.xml through android:name.
 *
 * Android creates the Application object before any Activity, and it lives for
 * as long as the process does. That's why the singleton container is attached
 * here. Objects marked @Singleton, such as the OkHttpClient and Retrofit, are
 * built once and then shared by every screen until the app is closed.
 */
// Switches Hilt on. Hilt creates its container of shared objects when the app starts.
// Why here: this class is created first and lives as long as the app does.
@HiltAndroidApp
// Creates the app's Application class. The body is empty because Hilt generates the set-up code.
class NitApplication : Application()
