package com.example.marvelheroes.repository

import android.annotation.SuppressLint
import com.example.marvelheroes.MyApplication
import com.example.marvelheroes.model.repository.Favorites
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe

/** Favorites Manager
 * This is an Object which means it is a singleton by nature
 * It will always return the same instance
 *
 * It returns the database instance from the App Class while
 * invoking it's methods
 *
 * It allows for it to be injected into constructors with
 * a single instance which can be accessed from any class
 * that calls it
 **/


object FavoritesManager {
    private var database : AppDatabase? = null
    init {
         database = MyApplication.database
    }

    fun getFavorites(): Flowable<List<Favorites>>? {
        return database?.favoritesDao()?.getAll()
    }
    fun getFavoritesMaybe(): Maybe<List<Favorites>>? {
        return database?.favoritesDao()?.getAllMaybe()
    }

    fun getFavoritesByName(query: String): Maybe<List<Favorites>>? {
        return database?.favoritesDao()?.getHeroByName(query)
    }

    @SuppressLint("CheckResult")
    fun isFavorite(favorite: Favorites?): Maybe<Favorites>? {
        return favorite?.let {
            database?.favoritesDao()?.getHeroById(it.heroId)
        }
    }

    fun insertFavorite(favorite: Favorites?): Completable? {
        return favorite?.let { database?.favoritesDao()?.insertQuery(favorite.heroId, favorite.heroName,favorite.heroDescription.toString(), favorite.heroImage.toString()) }
    }
    fun deleteFavorite(favorite: Favorites?): Completable? {
        return favorite?.let { database?.favoritesDao()?.deleteQuery(favorite.heroId) }
    }

}