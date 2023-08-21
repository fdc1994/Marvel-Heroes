package com.example.marvelheroes.model.repository

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
/** Data entity
 * The following code defines a User data entity.
 * Each instance of User represents a row in a user table in the app's database.
 * It will also be the returned object when querying the DB
 * */



@Entity
data class Favorites(
    @PrimaryKey (autoGenerate = true) val uid: Int?,
    @ColumnInfo(name = "hero_id") var heroId: Int,
    @ColumnInfo(name = "hero_name") val heroName: String,
    @ColumnInfo(name = "hero_description") val heroDescription: String?,
    @ColumnInfo(name = "thumbnail_url") val heroImage: String?,

    )
