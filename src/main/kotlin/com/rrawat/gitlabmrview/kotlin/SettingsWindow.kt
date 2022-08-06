package com.rrawat.gitlabmrview.kotlin

import com.intellij.icons.AllIcons
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.ColoredListCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.event.ListSelectionEvent
import javax.swing.event.ListSelectionListener


@Suppress("UnstableApiUsage")
class SettingsWindow : BoundConfigurable("Gitlab MR View", "Configuration for Gitlab MR View Plugin") {
    var currentProject = SettingsService.instance.projectSettings.getOrNull(0)

    class MyCellRenderer<T> : ColoredListCellRenderer<T>() {
        init {
            isOpaque = true
        }

        override fun customizeCellRenderer(
            list: JList<out T>,
            value: T,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean,
        ) {
            icon = AllIcons.Nodes.Folder
            append(value.toString())
        }
    }

    override fun createPanel(): DialogPanel {
        val projectListModel = DefaultListModel<String>()
        projectListModel.addAll(SettingsService.instance.projectSettings.map { it.name })
        var projectPanel: Placeholder? = null

        return panel {
            row {
                panel {
                    row {
                        val projectList = JBList(projectListModel)
                        projectList.cellRenderer = MyCellRenderer<String>()
                        projectList.addListSelectionListener(object : ListSelectionListener {
                            override fun valueChanged(e: ListSelectionEvent?) {
                                if (e == null || e.valueIsAdjusting) return
                                currentProject = SettingsService.instance.projectSettings[projectList.selectedIndex]
                                projectPanel?.component = createProjectPanel(currentProject!!)
                            }

                        })
                        projectList.selectedIndex = 0
                        scrollCell(projectList).resizableColumn().verticalAlign(VerticalAlign.FILL)
                            .horizontalAlign(HorizontalAlign.FILL)
                    }.resizableRow()
                }.resizableColumn().verticalAlign(VerticalAlign.FILL)
                projectPanel = placeholder()
                currentProject?.let { projectPanel!!.component = createProjectPanel(it) }
                projectPanel!!.resizableColumn().verticalAlign(VerticalAlign.TOP)
            }.resizableRow()
        }
    }

    private fun createProjectPanel(project: ProjectSettings) = panel {
        row("Project: ") {
            textField().bindText(project::name).enabled(false)
        }
        row("Gitlab URL: ") {
            textField().bindText(project::url).columns(COLUMNS_LARGE).resizableColumn()
        }
        row("Git root: ") {
            textField().bindText(project::path).columns(COLUMNS_LARGE).resizableColumn()
        }
        row("Access token: ") {
            textField().bindText(project::token).columns(COLUMNS_MEDIUM).resizableColumn()
        }
    }
}
