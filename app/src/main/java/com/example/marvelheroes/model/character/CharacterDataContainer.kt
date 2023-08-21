package com.example.marvelheroes.model.character

import java.io.Serializable

data class CharacterDataContainer (
    val limit: Int, //(int, optional): The requested result limit.,
    val total: Int, //(int, optional): The total number of resources available given the current filter set.,
    val count: Int, // (int, optional): The total number of results returned by this call.
    val offset: Int,
    val results : List<Character> //
) : Serializable