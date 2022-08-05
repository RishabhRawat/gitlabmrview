package com.rrawat.gitlabmrview.kotlin

import com.intellij.openapi.components.*

@Service
@State(
    name = "SmartGitlabPipelines", storages = [Storage("smartgitlabpipelines.xml", roamingType = RoamingType.DISABLED)]
)
class SettingsService : PersistentStateComponent<SettingsService.SettingsState> {
    data class SettingsState(var projectSettings: MutableList<ProjectSettings> = mutableListOf())

    private var state = SettingsState()

    val projectSettings: MutableList<ProjectSettings>
        get() = state.projectSettings

    companion object {
        val instance: SettingsService
            get() = service()
    }

    override fun getState(): SettingsState {
        return state
    }

    override fun loadState(state: SettingsState) {
        this.state = state
    }

}