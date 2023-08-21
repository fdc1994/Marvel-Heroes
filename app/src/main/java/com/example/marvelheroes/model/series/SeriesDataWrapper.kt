package com.example.marvelheroes.model.series

data class SeriesDataWrapper(
    val code: Int, // (int, optional): The HTTP status code of the returned result.,
    val status: String, // (string, optional): A string description of the call status.,
    val data : SeriesDataContainer? //  (CharacterDataContainer, optional): The results returned by the call.,
)
