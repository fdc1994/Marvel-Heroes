package com.example.marvelheroes.model.character

import com.example.marvelheroes.model.Image
import java.io.Serializable

data class Character (
    val id: Int,// (int, optional): The unique ID of the character resource.,
    val name: String, // (string, optional): The name of the character.,
    val description: String,// (string, optional): A short bio or description of the character.,
    val thumbnail : Image // The representative image for this character.,
    ) :Serializable