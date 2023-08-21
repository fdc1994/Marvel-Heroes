package com.example.marvelheroes

import com.example.marvelheroes.model.character.CharacterDataWrapper
import com.example.marvelheroes.model.comic.ComicDataWrapper
import com.example.marvelheroes.model.event.EventDataWrapper
import com.example.marvelheroes.model.series.SeriesDataWrapper
import com.example.marvelheroes.model.story.StoryDataWrapper
import com.example.marvelheroes.network.MarvelApi
import com.example.marvelheroes.network.ServiceResponse
import com.example.marvelheroes.utils.Constants
import com.example.marvelheroes.utils.Helper
import io.mockk.*
import io.reactivex.observers.TestObserver
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*

class ServiceResponseTests : BaseTest(){

    private lateinit var service: MarvelApi
    private lateinit var serviceResponse: ServiceResponse

    @Before
    fun setup() {
        val url = mockWebServer.url("/")

        val client = OkHttpClient
            .Builder()
            .build()

        service = Retrofit.Builder()
            .baseUrl(url)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(MarvelApi::class.java)

        serviceResponse = ServiceResponse(api = service)

    }

    @Test
    fun testGetMainHeroesObjectSubscribesAndReturnsCorrectly() {
        enqueue("characters.json")

        val charId = 1009552
        val testSize = 20

        val testSubscribe = TestObserver<CharacterDataWrapper>()

        serviceResponse.getMainHeroesObject().subscribe(testSubscribe)

        testSubscribe.assertNoErrors()
        testSubscribe.assertSubscribed()
        testSubscribe.assertComplete()
        testSubscribe.assertValue { it.data.count == testSize }
        testSubscribe.assertValue { it.data.results[0].id == charId }

    }

    @Test
    fun testGetHeroComicsObjectSubscribesAndReturnsCorrectly() {
        enqueue("comics.json")

        /**wolverine*/
        val heroId = 1009718
        val comicTitle = "X-Men: Days of Future Past (Trade Paperback)"

        val testSubscribe = TestObserver<ComicDataWrapper>()
        serviceResponse.getHeroComicsObjectById(heroId).subscribe(testSubscribe)

        testSubscribe.assertNoErrors()
        testSubscribe.assertSubscribed()
        testSubscribe.assertComplete()
        testSubscribe.assertValue { it.data?.results?.get(0)?.title  == comicTitle }

    }

   @Test
    fun testGetHeroEventsObjectSubscribesAndReturnsCorrectly() {
        enqueue("events.json")

        val heroId = 1009718
        val eventTitle = "Acts of Vengeance!"

        val testSubscribe = TestObserver<EventDataWrapper>()
        serviceResponse.getHeroEventsObject(heroId).subscribe(testSubscribe)

        testSubscribe.assertNoErrors()
        testSubscribe.assertSubscribed()
        testSubscribe.assertComplete()
        testSubscribe.assertValue { it.data?.results?.get(0)?.title == eventTitle }


    }

    @Test
    fun testGetHeroSeriesObjectSubscribesAndReturnsCorrectly() {
        enqueue("series.json")

        val heroId = 1009718
        val seriesTitle = "5 Ronin (2010)"

        val testSubscribe = TestObserver<SeriesDataWrapper>()
        serviceResponse.getHeroSeriesObject(heroId).subscribe(testSubscribe)

        testSubscribe.assertNoErrors()
        testSubscribe.assertSubscribed()
        testSubscribe.assertComplete()
        testSubscribe.assertValue { it.data?.results?.get(0)?.title == seriesTitle }

    }

    @Test
    fun testGetHeroStoriesObjectSubscribesAndReturnsCorrectly() {
        enqueue("stories.json")

        val heroId = 1009718
        val storyTitle = "Cover #477"

        val testSubscribe = TestObserver<StoryDataWrapper>()
        serviceResponse.getHeroStoriesObject(heroId).subscribe(testSubscribe)

        testSubscribe.assertNoErrors()
        testSubscribe.assertSubscribed()
        testSubscribe.assertComplete()
        testSubscribe.assertValue { it.data?.results?.get(0)?.title == storyTitle }

    }

    @Test
    fun testGetMainHeroesObjectUsesCorrectParameters() {
        //Arrange
        val timeStamp = "0"
        val name = "Paulo"
        val offset = 20
        val hash = "hash"

        val api = mockk<MarvelApi>(relaxed = true)
        val helper = mockk<Helper>()
        mockkStatic(Calendar::class)

        every { Calendar.getInstance().timeInMillis } returns 0
        every { helper.md5(any()) } returns "hash"

        val serviceResponse = ServiceResponse(api, helper)

        //Act
        serviceResponse.getMainHeroesObject(name, offset)

        //Assert
        verify {
            api.getCharacters(
                timeStamp,
                Constants.API_KEY,
                hash,
                name,
                "modified",
                Constants.CHAR_ITEMS_PER_PAGE,
                offset
            )
        }
    }

    @Test
    fun testGetHeroComicsObjectUsesCorrectParameters() {
        //Arrange
        val timeStamp = "0"
        val hash = "hash"
        val id = 1

        val api = mockk<MarvelApi>(relaxed = true)
        val helper = mockk<Helper>()
        mockkStatic(Calendar::class)

        every { Calendar.getInstance().timeInMillis } returns 0
        every { helper.md5(any()) } returns "hash"

        val serviceResponse = ServiceResponse(api, helper)

        //Act
        serviceResponse.getHeroComicsObjectById(id)

        //Assert
        verify {
            api.getCharacterComicsById(
                id,
                timeStamp,
                Constants.API_KEY,
                hash,
                Constants.CHAR_DETAILS_LIMIT
            )
        }
    }

    @Test
    fun testGetHeroEventsObjectUsesCorrectParameters() {
        //Arrange
        val timeStamp = "0"
        val hash = "hash"
        val id = 1

        val api = mockk<MarvelApi>(relaxed = true)
        val helper = mockk<Helper>()
        mockkStatic(Calendar::class)

        every { Calendar.getInstance().timeInMillis } returns 0
        every { helper.md5(any()) } returns "hash"

        val serviceResponse = ServiceResponse(api, helper)

        //Act
        serviceResponse.getHeroEventsObject(id)

        //Assert
        verify {
            api.getCharacterEvents(
                id,
                timeStamp,
                Constants.API_KEY,
                hash,
                Constants.CHAR_DETAILS_LIMIT
            )
        }
    }

    @Test
    fun testGetHeroSeriesObjectUsesCorrectParameters() {
        //Arrange
        val timeStamp = "0"
        val hash = "hash"
        val id = 1

        val api = mockk<MarvelApi>(relaxed = true)
        val helper = mockk<Helper>()
        mockkStatic(Calendar::class)

        every { Calendar.getInstance().timeInMillis } returns 0
        every { helper.md5(any()) } returns "hash"

        val serviceResponse = ServiceResponse(api, helper)

        //Act
        serviceResponse.getHeroSeriesObject(id)

        //Assert
        verify {
            api.getCharacterSeries(
                id,
                timeStamp,
                Constants.API_KEY,
                hash,
                Constants.CHAR_DETAILS_LIMIT
            )
        }
    }

    @Test
    fun testGetHeroStoriesObjectUsesCorrectParameters() {
        //Arrange
        val timeStamp = "0"
        val hash = "hash"
        val id = 1

        val api = mockk<MarvelApi>(relaxed = true)
        val helper = mockk<Helper>()
        mockkStatic(Calendar::class)

        every { Calendar.getInstance().timeInMillis } returns 0
        every { helper.md5(any()) } returns "hash"

        val serviceResponse = ServiceResponse(api, helper)

        //Act
        serviceResponse.getHeroStoriesObject(id)

        //Assert
        verify {
            api.getCharacterStories(
                id,
                timeStamp,
                Constants.API_KEY,
                hash,
                Constants.CHAR_DETAILS_LIMIT
            )
        }
    }

}