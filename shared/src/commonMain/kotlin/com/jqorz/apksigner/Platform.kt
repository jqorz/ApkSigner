package com.jqorz.apksigner

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform