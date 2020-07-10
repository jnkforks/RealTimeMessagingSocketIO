package com.polatechno.realtimemessagingsocetio.data.model


data class MessageItem(
    val type: String,
    val username: String,
    val userId: String,
    val message: String
)