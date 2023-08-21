package com.example.marvelheroes

import android.app.Application
import com.example.marvelheroes.repository.AppDatabase


/**Application class
 *
 * This will instantiate the database and return it's instance on Create
 * it will also store the searchview's queries since the fragment's views aren't destroyed
 * and onInstanceSaveState isn't called */

open class MyApplication : Application() {
    companion object {
        var database: AppDatabase? = null
        var searchMain : String? = null
        var searchFavourites : String? = null
        var searchComics : String? = null
    }


    override fun onCreate() {
        super.onCreate()
        //Room
      database = AppDatabase.getDatabase(applicationContext)

    }


}