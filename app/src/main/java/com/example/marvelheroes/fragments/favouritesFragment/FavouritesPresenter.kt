package com.example.marvelheroes.fragments.favouritesFragment

import com.example.marvelheroes.model.repository.Favorites
import com.example.marvelheroes.repository.FavoritesManager
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers


/**Favourites Presenter
 *
 * Favourites Manager is injected to constructor*/
class FavouritesPresenter(private val favouritesManager : FavoritesManager = FavoritesManager) {

    /**Favorites Manager response for general query*/
    fun getFavorites(): Flowable<List<Favorites>>? {
        return favouritesManager.getFavorites()
    }
    /**Favorites Manager response for specific favorite query*/
    fun getFavoritesMaybe(): Maybe<List<Favorites>>? {
        return favouritesManager.getFavoritesMaybe()
    }
    /**Favorites Manager query for inserting a new favorite*/
    fun getFavoritesSearch(query: String): Maybe<List<Favorites>>? {
        return favouritesManager.getFavoritesByName(query)
    }
    /**Favorites Manager query for deleting a current favorite*/
    fun checkFavorite(favorite : Favorites) : Maybe<Favorites>? {
        return favouritesManager.isFavorite(favorite)
    }
    fun insertFavorite(favorite : Favorites) : Completable? {
        return favouritesManager.insertFavorite(favorite)
    }
    fun deleteFavorite(favorite : Favorites) : Completable? {
        return favouritesManager.deleteFavorite(favorite)
    }



}