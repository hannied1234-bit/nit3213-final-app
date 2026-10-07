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
 */
@HiltAndroidApp
class NitApplication : Application()
