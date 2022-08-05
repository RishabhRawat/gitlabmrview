package com.rrawat.gitlabmrview.kotlin

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.application.invokeLater
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.util.ThrowableRunnable
import com.intellij.util.concurrency.AppExecutorUtil
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class ProjectData(private val settings: ProjectSettings) {
    var records: List<PipelineRecord> = listOf()
    val name: String
        get() = settings.name

    private var settingsCopy = settings.copy()
    private var enabled = true
    private val gitUrl = GitUrl(settings.url)

    init {
        fetchInitialData()
    }

    private fun update() {
        if (enabled || settings != settingsCopy) {
            enabled = true
            settingsCopy = settings.copy()

            val gitlabAPI = GitlabAPI(gitUrl.hostUrl)
            gitlabAPI.setPersonalAccessToken(settings.token)


            records = runBlocking {
                try {
                    gitlabAPI.fetchPipelines(gitUrl.project)
                } catch (e: GitlabAPIException) {
                    enabled = false
                    listOf()
                }
            }

            invokeLater {
                WriteAction.run(ThrowableRunnable {
                    MainWindowFactory.tableModel.items =
                        MainWindowFactory.projects.fold(mutableListOf<PipelineRecord>()) { list, item ->
                            list.addAll(item.records)
                            list
                        }
                })
            }

        }
    }

    private fun fetchInitialData() {
        ProgressManager.getInstance()
            .run(object : Task.Backgroundable(null, "Fetching gitlab pipelines for ${settings.name}") {
                override fun run(indicator: ProgressIndicator) {
                    update()
                    AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
                        update()
                    }, 60, 60, TimeUnit.SECONDS)
                }
            })
    }

}