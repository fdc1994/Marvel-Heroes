package com.example.marvelheroes.model.event

import com.example.marvelheroes.model.Image

data class Event(
    val title : String, //Title of the event
    val description : String, //Description of the event
val thumbnail : Image // Thumbnail of Event
)
