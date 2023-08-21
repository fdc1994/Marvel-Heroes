package com.example.marvelheroes.network

import com.example.marvelheroes.model.character.CharacterDataWrapper
import com.example.marvelheroes.model.comic.ComicDataWrapper
import com.example.marvelheroes.model.event.EventDataWrapper
import com.example.marvelheroes.model.series.SeriesDataWrapper
import com.example.marvelheroes.model.story.StoryDataWrapper
import retrofit2.http.GET
import retrofit2.http.Query
import io.reactivex.Observable
import retrofit2.http.Path

/**Retrofit Api Call
 * This interface defines all the retrofit API calls
 * */

interface MarvelApi {

    @GET("characters")
    fun getCharacters (
        @Query("ts") timeStamp :String,
        @Query("apikey") apiKey : String,
        @Query("hash") hash : String,
        @Query("nameStartsWith") nameStartsWith : String?,
        @Query("orderBy") orderBy : String?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,

        ) : Observable<CharacterDataWrapper>

    @GET("characters/{characterId}/comics")
    fun getCharacterComicsById (
        @Path("characterId") charId : Int,
        @Query("ts") timeStamp :String,
        @Query("apikey") apiKey : String,
        @Query("hash") hash : String,
        @Query("limit") limit: Int
    ) : Observable<ComicDataWrapper>

    @GET("comics")
    fun getComics (
        @Query("ts") timeStamp:String,
        @Query("apikey") apiKey: String,
        @Query("hash") hash: String,
        @Query("titleStartsWith") titleStartsWith: String?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ) : Observable<ComicDataWrapper>

    @GET("characters/{characterId}/events")
    fun getCharacterEvents (
        @Path("characterId") charId : Int,
        @Query("ts") timeStamp :String,
        @Query("apikey") apiKey : String,
        @Query("hash") hash : String,
        @Query("limit") limit: Int
    ) : Observable<EventDataWrapper>

    @GET("characters/{characterId}/series")
    fun getCharacterSeries (
        @Path("characterId") charId : Int,
        @Query("ts") timeStamp :String,
        @Query("apikey") apiKey : String,
        @Query("hash") hash : String,
        @Query("limit") limit: Int
    ) : Observable<SeriesDataWrapper>

    @GET("characters/{characterId}/stories")
    fun getCharacterStories (
        @Path("characterId") charId : Int,
        @Query("ts") timeStamp :String,
        @Query("apikey") apiKey : String,
        @Query("hash") hash : String,
        @Query("limit") limit: Int
    ) : Observable<StoryDataWrapper>

}