package com.rrawat.gitlabmrview.kotlin

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.module.ModuleUtil
import com.intellij.util.ThrowableRunnable
import git4idea.repo.GitRepository
import git4idea.repo.GitRepositoryChangeListener

internal fun guessToken(url: String): String {
    val parsedUrl = GitUrl(url)
    return SettingsService.instance.projectSettings.map {
        val projectUrl = if (it.url.isNotEmpty() and it.token.isNotEmpty()) GitUrl(it.url) else null
        if (projectUrl == null) Pair(0, it.token)
        else if (parsedUrl.fullUrl == projectUrl.fullUrl) Pair(3, it.token)
        else if (parsedUrl.hostUrl == projectUrl.hostUrl) Pair(2, it.token)
        else Pair(1, it.token)
    }.maxByOrNull { it.first }?.second ?: ""
}

fun registerRepository(repository: GitRepository) {
    val module = ModuleUtil.findModuleForFile(repository.root, repository.project)!!
    var project = SettingsService.instance.projectSettings.find { it.name == module.name }

    if (project == null) {
        project = ProjectSettings(name = module.name,
            url = repository.remotes.first()?.firstUrl?.let { GitUrl(it).fullUrl } ?: "",
            token = guessToken(repository.remotes.first()?.firstUrl ?: ""),
            path = repository.presentableUrl)
        SettingsService.instance.projectSettings.add(project)
    }
    println(repository.remotes)
    MainWindowFactory.projects.add(ProjectData(project))
}

class GitRepoListener : GitRepositoryChangeListener {
    override fun repositoryChanged(repository: GitRepository) {
        invokeLater { ReadAction.run(ThrowableRunnable { registerRepository(repository) }) }
    }
}