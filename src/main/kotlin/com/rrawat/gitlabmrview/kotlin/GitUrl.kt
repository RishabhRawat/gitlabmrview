package com.rrawat.gitlabmrview.kotlin

class ParseException(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {
    constructor(cause: Throwable) : this(null, cause)
}

// Test cases
// git@hostname.org:user/repo.git
// git@hostname.org/user/repo.git
// git@hostname.org/user/repo
// https://hostname.org/user/repo
// http://hostname.org/user/repo
// http://username:password@hostname.org/user/repo
// http://username@hostname.org/user/repo
// git@username:password@hostname.org:user/repo.git
class GitUrl(remoteUrl: String) {
    private var location = 0

    private var _protocol: String
    val protocol: String
        get() = _protocol

    init {
        val item = listOf("://", "@").map {
            val loc = remoteUrl.indexOf(it)
            if (loc > -1) Pair(remoteUrl.substring(location, loc), loc + it.length)
            else Pair("", remoteUrl.length)
        }.minByOrNull { it.second } ?: throw ParseException("Invalid git remote url -> $remoteUrl")

        _protocol = item.first
        location = item.second
    }

    private var username: String = ""
    private var password: String = ""

    init {
        val loc = remoteUrl.indexOf("@", location)
        if (loc > -1) {
            val portions = remoteUrl.substring(location, loc).split(":", limit = 2)
            username = portions[0]
            password = portions.getOrElse(1) { "" }
            location = loc + 1
        }
    }

    private var hostname: String

    init {
        val item = listOf(":", "/").map {
            val loc = remoteUrl.indexOf(it, location)
            if (loc > -1) Pair(remoteUrl.substring(location, loc), loc + it.length)
            else Pair("", remoteUrl.length)
        }.minByOrNull { it.second } ?: throw ParseException("Incomplete git remote url -> $remoteUrl")

        hostname = item.first
        location = item.second
        if (hostname.isEmpty()) throw ParseException("Cannot find hostname in $remoteUrl")
    }

    var project = remoteUrl.substring(location).removeSuffix("/").removeSuffix(".git")

    val hostUrl: String
        get() = "${if (protocol == "http") "http" else "https"}://$hostname"

    val fullUrl: String
        get() = "$hostUrl/$project"

}