package com.example.marvelheroes.model.series

data class SeriesDataContainer(
    val limit: Int, //(int, optional): The requested result limit.,
    val total: Int, //(int, optional): The total number of resources available given the current filter set.,
    val count: Int, // (int, optional): The total number of results returned by this call.
    val results : List<Series>? //The list of series returned by the call
)
