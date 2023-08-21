package com.example.marvelheroes.model.comic

data class ComicDataContainer(
    val limit: Int, //(int, optional): The requested result limit.,
    val total: Int, //(int, optional): The total number of resources available given the current filter set.,
    val count: Int, // (int, optional): The total number of results returned by this call.
    val results : List<Comic>? // The list of comics returned by the call
)
