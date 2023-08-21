package com.example.marvelheroes.repository

import android.content.Context
import android.os.Build.VERSION_CODES.Q
import org.junit.After
import androidx.room.Room
import org.junit.Before
import org.junit.Test
import java.lang.Exception
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.reactivex.rxkotlin.subscribeBy
import org.junit.Assert
import org.junit.runner.RunWith
import org.robolectric.annotation.Config


@RunWith(AndroidJUnit4::class)
@Config(sdk = [Q])
class AppDatabaseTest {

    private var context: Context = ApplicationProvider.getApplicationContext()
    private val favorite = Favorites.favorite1
    private var mDatabase: AppDatabase? = null

    /**
     * Setting up the DB with a context
     *
     * Context provided from mockk does not work
     */
    @Before
    fun initDb() {
        mDatabase = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ) // allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
    }

    /**
     * Closing DB after tests run
     */
    @After
    @Throws(Exception::class)
    fun closeDb() {
        mDatabase?.close()
    }


    /**
     * Inserting user in DB
     *
     * Checking after if it is present and if the id matches
     */
    @Test
    fun insertAndGetUserById() {
        // Insert user in database
        mDatabase?.favoritesDao()?.insert(favorite = favorite)
            ?.subscribeBy(
                onComplete = {
                    // Checking if user is in database
                    mDatabase?.favoritesDao()
                        ?.getHeroById(favorite.heroId)
                        ?.test()
                        ?.assertValue {
                            it.heroId == favorite.heroId
                        }
                }
            )
    }


    /**
     * Inserting user in DB
     *
     * Checking after if it is present and if the name matches
     */
    @Test
    fun insertAndGetUserByName() {
        // Insert user in database
        mDatabase?.favoritesDao()?.insert(favorite = favorite)
            ?.subscribeBy(
                onComplete = {
                    // Checking if user is in database
                    mDatabase?.favoritesDao()
                        ?.getHeroByName(favorite.heroName)
                        ?.test()
                        ?.assertValue {
                            it[0].heroName == favorite.heroName
                        }
                }
            )
    }


    /**
     * Inserting user in DB
     *
     * Checking after if it is present using the flowable observable
     */
    @Test
    fun getUsersFlowable() {
        // Insert user in database
        mDatabase?.favoritesDao()?.insert(favorite)
            ?.subscribe()
        // Given that we have a user in the data source
        val listFavorites = mDatabase?.favoritesDao()?.getAll()?.blockingFirst()
        Assert.assertEquals(listFavorites?.get(0)?.heroId, favorite.heroId)
    }


    /**
     * Inserting user in DB
     *
     * Checking after if it the list is empty with maybe observable
     */
    @Test
    fun getUsersMaybeNoResults() {
        mDatabase?.favoritesDao()?.getAllMaybe()?.test()?.assertValue {
            it.isEmpty()
        }
    }


    /**
     * Inserting user in DB
     *
     * Checking after if it is present and if the id matches with maybe observable
     */
    @Test
    fun getUsersMaybe() {
        // Insert user in database
        mDatabase?.favoritesDao()?.insertQuery(
            favorite.heroId, favorite.heroName,
            favorite.heroDescription!!, favorite.heroImage!!
        )
            ?.subscribeBy(
                onComplete = {
                    // Given that we have a user in the data source
                    mDatabase?.favoritesDao()?.getAllMaybe()?.test()?.assertValue {
                        it.isNotEmpty()
                    }
                }
            )


    }

    /**
     * Inserting and deleting user in DB
     *
     * Checking after if the list is null
     */
    @Test
    fun insertAndDeleteUser() {
        // Insert user in database
        mDatabase?.favoritesDao()?.insertQuery(
            favorite.heroId, favorite.heroName,
            favorite.heroDescription!!, favorite.heroImage!!
        )?.subscribeBy(
            onComplete = {
                // Delete user from database
                mDatabase?.favoritesDao()?.deleteQuery(favorite.heroId)?.subscribeBy(
                    onComplete = {
                        // Given that we have a user in the data source
                        mDatabase?.favoritesDao()?.getAllMaybe()?.test()?.assertValue {
                           it.isEmpty()
                        }
                    }
                )

            }
        )


    }

    /**
     * Inserting two users in DB
     * Deleting all with one query
     * Checking after if if the list is null
     */
    @Test
    fun insertAndDeleteAllUsers() {
        // Insert user in database
        mDatabase?.favoritesDao()?.insertQuery(
            favorite.heroId, favorite.heroName,
            favorite.heroDescription!!, favorite.heroImage!!
        )?.subscribeBy(
            onComplete = {
                mDatabase?.favoritesDao()?.insertQuery(
                    favorite.heroId, favorite.heroName,
                    favorite.heroDescription!!, favorite.heroImage!!
                )?.subscribeBy(
                    onComplete = {
                        mDatabase?.favoritesDao()?.deleteAll()?.subscribeBy(
                            onComplete = {
                                mDatabase?.favoritesDao()?.getAllMaybe()?.test()?.assertValue{
                                    it.isNullOrEmpty()
                                }
                            }
                        )
                    }
                )

            }
        )


    }


}