package com.example.spingiesegna

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform