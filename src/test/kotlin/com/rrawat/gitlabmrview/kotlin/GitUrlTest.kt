package com.rrawat.gitlabmrview.kotlin

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory

internal class GitUrlTest {
    @TestFactory
    fun createURL() = listOf(
        "random@gitlab.company.com:User/repo.git" to "https://gitlab.company.com",
        "git@hostname.org:user/repo.git" to "https://hostname.org",
        "git@hostname.org/user/repo.git" to "https://hostname.org",
        "git@hostname.org/user/repo" to "https://hostname.org",
        "https://hostname.org/user/repo" to "https://hostname.org",
        "http://hostname.org/user/repo" to "http://hostname.org",
        "http://username:password@hostname.org/user/repo" to "http://hostname.org",
        "http://username@hostname.org/user/repo" to "http://hostname.org"
    ).map { (clonePath, url) ->
        DynamicTest.dynamicTest("Test url $clonePath") {
            val result = GitUrl(clonePath)
            Assertions.assertEquals(result.hostUrl, url)
        }
    }
}