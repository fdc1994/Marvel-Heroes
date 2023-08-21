package com.example.marvelheroes.model.character

data class CharacterDataWrapper(
    val code: Int, // (int, optional): The HTTP status code of the returned result.,
    val count: Int, // (int,optional): The count of returned heroes
    val status: String, // (string, optional): A string description of the call status.,
    val data : CharacterDataContainer //  (CharacterDataContainer, optional): The results returned by the call.,
    )