package com.example.marvelheroes.model.event

data class EventDataContainer(
    val limit: Int, //(int, optional): The requested result limit.,
    val total: Int, //(int, optional): The total number of resources available given the current filter set.,
    val count: Int, // (int, optional): The total number of results returned by this call.
    val results : List<Event> //The list of events returned by the call
)
