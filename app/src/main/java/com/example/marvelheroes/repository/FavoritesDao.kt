package com.example.marvelheroes.repository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.marvelheroes.model.repository.Favorites
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

import androidx.room.Update

import androidx.room.OnConflictStrategy


/**Main Activity Presenter
 * Data access object (DAO)
 * The following code defines a DAO called UserDao.
 * UserDao provides the methods that the rest of the app uses to interact with data in the user table.
 **/

@Dao
interface FavoritesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
     fun insert(favorite : Favorites): Completable?
    @Delete
     fun delete(favorite : Favorites): Completable?

    @Query("SELECT * FROM favorites")
     fun getAll(): Flowable<List<Favorites>>?

    @Query("SELECT * FROM favorites")
    fun getAllMaybe(): Maybe<List<Favorites>>?

    @Query("SELECT * FROM favorites where hero_name LIKE  :name||'%'")
    fun getHeroByName(name: String?): Maybe<List<Favorites>>?

    @Query("SELECT * FROM favorites WHERE hero_id = :heroId")
    fun getHeroById(heroId: Int): Maybe<Favorites>?

    @Query("DELETE FROM favorites WHERE hero_id = :heroId")
    fun deleteQuery(heroId: Int): Completable?

    @Query("INSERT INTO favorites (hero_id, hero_name, hero_description, thumbnail_url) VALUES (:heroId,:hero_name, :hero_description,:thumbnail)")
    fun insertQuery(heroId: Int, hero_name: String, hero_description : String, thumbnail : String): Completable?

    @Query("DELETE FROM favorites")
     fun deleteAll(): Maybe<Int>?

}