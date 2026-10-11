package com.vu.nit3213app

import android.app.Application
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
 *
 * The class body is empty on purpose. Hilt generates a parent class at compile
 * time (Hilt_NitApplication) that does the set-up work, so nothing needs to be
 * written by hand.
 */
@HiltAndroidApp
class NitApplication : Application()
