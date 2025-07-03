package com.domcheung.fittrackpro.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val gender: String = "",
    val height: Float = 0f,  // cm
    val weight: Float = 0f   // kg
)