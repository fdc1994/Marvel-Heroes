package com.example.marvelheroes.model.story

data class StoryDataWrapper(
    val code: Int, // (int, optional): The HTTP status code of the returned result.,
    val status: String, // (string, optional): A string description of the call status.,
    val data : StoryDataContainer? //  (CharacterDataContainer, optional): The results returned by the call.,
)