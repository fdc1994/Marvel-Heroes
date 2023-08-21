package com.example.marvelheroes.model.comic

data class textObjects (
    val type : String?, //(string, optional): The canonical type of the text object (e.g. solicit text, preview text, etc.).,
    val language: String?, //(string, optional): The IETF language tag denoting the language the text object is written in.,
    val text : String? //  (string, optional): The text.
        )
