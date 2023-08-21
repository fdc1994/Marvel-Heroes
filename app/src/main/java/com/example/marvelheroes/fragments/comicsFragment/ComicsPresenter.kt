package com.example.marvelheroes.fragments.comicsFragment


import com.example.marvelheroes.model.comic.ComicDataWrapper
import com.example.marvelheroes.model.repository.Favorites
import com.example.marvelheroes.network.ServiceResponse
import com.example.marvelheroes.repository.FavoritesManager
import io.reactivex.*

/**Main Activity Presenter
 *
 * Favourites Manager is injected to constructor*/
class ComicsPresenter(favManager : FavoritesManager = FavoritesManager) {
    private var serviceResponse: ServiceResponse? = ServiceResponse()
    private val favoritesManager = favManager

    /**Retrofit Api Call*/
    fun getComicsObject(
        title: String? = null,
        offset: Int = 0,
    ): Observable<ComicDataWrapper>? {

        return serviceResponse?.getHeroComicsObject(title, offset)?.retry(1)
    }
/*
    /**Favorites Manager response for general query*/
    fun getFavorites(): Flowable<List<Favorites>>? {
        return favoritesManager.getFavorites()
    }
    /**Favorites Manager response for specific favorite query*/
    fun checkFavorite(favorite : Favorites) : Maybe<Favorites>? {
        return favoritesManager.isFavorite(favorite)
    }
    /**Favorites Manager query for inserting a new favorite*/
    fun insertFavorite(favorite : Favorites) : Completable? {
        return favoritesManager.insertFavorite(favorite)
    }
    /**Favorites Manager query for deleting a current favorite*/
    fun deleteFavorite(favorite : Favorites) : Completable? {
        return favoritesManager.deleteFavorite(favorite)
    }

*/
}