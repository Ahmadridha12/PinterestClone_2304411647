package com.example.hands10_firebase

data class Notification(
    val userName: String? = null,
    val message: String? = null,
    val timestamp: Long? = null,
    val pinId: String? = null,
    val imageUrl: String? = null
) {
    // Constructor kosong wajib untuk Firebase
    constructor() : this(null, null, null, null, null)
}