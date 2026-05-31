package com.challengehub.mobile.core.network

import com.challengehub.mobile.BuildConfig

fun resolveMediaUrl(url: String): String {
    if (url.startsWith("http://") || url.startsWith("https://")) return url
    val origin = BuildConfig.API_BASE_URL.substringBefore("/api/v1/")
    return "$origin/${url.trimStart('/')}"
}
