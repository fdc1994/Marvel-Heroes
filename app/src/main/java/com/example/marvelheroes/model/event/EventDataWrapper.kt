package com.example.marvelheroes.model.event

data class EventDataWrapper(
    val code: Int, // (int, optional): The HTTP status code of the returned result.,
    val status: String, // (string, optional): A string description of the call status.,
    val data : EventDataContainer? //  (CharacterDataContainer, optional): The results returned by the call.,
)