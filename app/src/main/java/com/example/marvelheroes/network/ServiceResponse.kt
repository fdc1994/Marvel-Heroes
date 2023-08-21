package com.example.marvelheroes.network

import com.example.marvelheroes.model.character.CharacterDataWrapper
import com.example.marvelheroes.model.comic.ComicDataWrapper
import com.example.marvelheroes.model.event.EventDataWrapper
import com.example.marvelheroes.model.series.SeriesDataWrapper
import com.example.marvelheroes.model.story.StoryDataWrapper
import com.example.marvelheroes.utils.Constants
import com.example.marvelheroes.utils.Helper
import io.reactivex.Observable
import java.util.*

class ServiceResponse(private val api: MarvelApi = ServiceBuilder.buildService(),
                      private val helper: Helper = Helper
) {

    //get Current timeStamp
    private val timeStamp = (Calendar.getInstance().timeInMillis * 1000).toString()


    fun getMainHeroesObject(name: String? = null, offset : Int = 0): Observable<CharacterDataWrapper> {


        return api.getCharacters(
            timeStamp,
            Constants.API_KEY,
            helper.md5("$timeStamp${Constants.PRIVATE_API_KEY}${Constants.API_KEY}"),
            name,
            "modified",
            Constants.CHAR_ITEMS_PER_PAGE, offset
        )
    }


    fun getHeroComicsObjectById(heroId: Int): Observable<ComicDataWrapper> {

        return api.getCharacterComicsById(
            heroId,
            timeStamp,
            Constants.API_KEY,
            helper.md5("$timeStamp${Constants.PRIVATE_API_KEY}${Constants.API_KEY}"),
            Constants.CHAR_DETAILS_LIMIT
        )
    }

    fun getHeroComicsObject(title: String? = null, offset: Int = 0): Observable<ComicDataWrapper> {

        return api.getComics(
            timeStamp,
            Constants.API_KEY,
            helper.md5("$timeStamp${Constants.PRIVATE_API_KEY}${Constants.API_KEY}"),
            title,
            Constants.CHAR_ITEMS_PER_PAGE,
            offset
        )
    }

    fun getHeroEventsObject(heroId: Int): Observable<EventDataWrapper> {

        return api.getCharacterEvents(
            heroId,
            timeStamp,
            Constants.API_KEY,
            helper.md5("$timeStamp${Constants.PRIVATE_API_KEY}${Constants.API_KEY}"),
            Constants.CHAR_DETAILS_LIMIT
        )
    }

    fun getHeroSeriesObject(heroId: Int): Observable<SeriesDataWrapper> {

        return api.getCharacterSeries(
            heroId,
            timeStamp,
            Constants.API_KEY,
            helper.md5("$timeStamp${Constants.PRIVATE_API_KEY}${Constants.API_KEY}"),
            Constants.CHAR_DETAILS_LIMIT
        )
    }

    fun getHeroStoriesObject(heroId: Int): Observable<StoryDataWrapper> {

        return api.getCharacterStories(
            heroId,
            timeStamp,
            Constants.API_KEY,
            helper.md5("$timeStamp${Constants.PRIVATE_API_KEY}${Constants.API_KEY}"),
            Constants.CHAR_DETAILS_LIMIT
        )
    }


}