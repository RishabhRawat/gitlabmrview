package com.rrawat.gitlabmrview.kotlin

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

internal class GitlabAPITest {
    @Test
    fun fetchPipelines() {
        val url = GitUrl("git@gitlab.com:ase/ase.git")
        val client = GitlabAPI(url.hostUrl)
        client.setPersonalAccessToken("")
        val result = runBlocking { client.fetchPipelines(url.project) }
        result.forEach { println("$it") }
    }
}