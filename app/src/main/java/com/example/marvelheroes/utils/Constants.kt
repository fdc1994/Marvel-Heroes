package com.example.marvelheroes.utils

import android.util.Log
import java.math.BigInteger
import java.security.MessageDigest

object Constants {
    const val API_KEY: String = "410a7f49479b3826c36500c6ff1d49a7"
    const val BASE_URL: String = "https://gateway.marvel.com:443/v1/public/"
    const val PRIVATE_API_KEY: String = "8d58d13095d70e482361abc3b84ce8952f784278"

    //Number of comics, events, series and stories we want to be returned
    const val CHAR_DETAILS_LIMIT : Int = 3

    //Number of Characters in a page
    const val CHAR_ITEMS_PER_PAGE : Int = 20
}