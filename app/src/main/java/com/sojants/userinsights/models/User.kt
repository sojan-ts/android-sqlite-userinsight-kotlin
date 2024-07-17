package com.sojants.userinsights.models

import java.io.Serializable

data class User(
    var id: Int = 0,
    var name: String = "",
    var email: String = "",
    var gender: String = "",
    var age: Int = 0,
    var photo: ByteArray? = null
) : Serializable