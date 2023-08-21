package com.example.marvelheroes.fragments.heroDetailsFragment

import com.example.marvelheroes.model.repository.Favorites
import com.example.marvelheroes.network.ServiceResponse
import com.example.marvelheroes.repository.FavoritesManager
import io.reactivex.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**Main Activity Presenter
 *
 * Favourites Manager is injected to constructor*/
class HeroDetailsPresenter(favManager: FavoritesManager = FavoritesManager) {
    private var serviceResponse: ServiceResponse = ServiceResponse()
    private val favouritesManager = favManager

    /**Retrofit Api Call
     * Zipping all answers together and retrying if there is any error
     * if after 5 tries some of the answers still return an error it will
     * zip only the responses
     *
     * If there is a full error (No response at all from all calls)
     * it will send a new object with a API ERROR tag which will be
     * red by the method that setups the UI and show the
     * appropriate No Connection Dialog
     * */
    fun getHeroEventsObject(heroId: Int): Observable<MutableList<Any>>? {
        return Observable
            .zip(
                serviceResponse.getHeroSeriesObject(heroId),
                serviceResponse.getHeroEventsObject(heroId),
                serviceResponse.getHeroComicsObjectById(heroId),
                serviceResponse.getHeroStoriesObject(heroId),
                { series, events, comics, stories ->
                    val list = mutableListOf<Any>()
                    comics.data?.results?.let { list.addAll(it) }
                    events.data?.results?.let { list.addAll(it) }
                    series.data?.results?.let { list.addAll(it) }
                    stories.data?.results?.let { list.addAll(it) }
                    if (list.isNullOrEmpty()) {
                        list.addAll(0, listOf("No Information"))
                    }
                    return@zip list
                }
            ).retry(5)
            .onErrorReturnItem(mutableListOf<Any>("API CALL ERROR"))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    /**Favorites Manager response for specific favorite query*/
    fun checkFavorite(favorite: Favorites): Maybe<Favorites>? {
        return favouritesManager.isFavorite(favorite)
    }

    /**Favorites Manager query for inserting a new favorite*/
    fun insertFavorite(favorite: Favorites): Completable? {
        return favouritesManager.insertFavorite(favorite)
    }

    /**Favorites Manager query for deleting a current favorite*/
    fun deleteFavorite(favorite: Favorites): Completable? {
        return favouritesManager.deleteFavorite(favorite)
    }

}