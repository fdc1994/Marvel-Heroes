package com.example.marvelheroes.model.comic

import com.example.marvelheroes.model.Image

data class Comic(
    val id : Int, //Id of the comic
    val title : String, //Title of the comic
    val description : String, //Description of the comic
    val pageCount : Int, //Page count of the comic
    val thumbnail : Image, //thumbnail
    val textObjects: List<textObjects> //thumbnail
)
