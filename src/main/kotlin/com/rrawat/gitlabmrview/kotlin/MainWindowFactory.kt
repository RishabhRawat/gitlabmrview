package com.rrawat.gitlabmrview.kotlin

import com.intellij.ide.BrowserUtil
import com.intellij.ide.ui.UITheme
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.ListTableModel
import git4idea.repo.GitRepositoryManager
import java.awt.Color
import java.awt.Component
import java.awt.event.MouseEvent
import java.awt.font.TextAttribute
import javax.swing.BorderFactory
import javax.swing.JLabel


class MainWindowFactory : ToolWindowFactory, DumbAware {

    class UrlTableCell : MainWindowCellObject() {
        private var visited = false
        private var url: String? = null
        override fun view(item: Component, value: Any?, isSelected: Boolean, hasFocus: Boolean): Component {
            if (value is Pair<*, *> && item is JLabel) {
                item.text = value.first as String
                url = value.second as String
                if (url!!.isNotEmpty()) {
                    if (item.hasFocus()) {
                        item.foreground = JBUI.CurrentTheme.Link.Foreground.HOVERED
                    } else if (visited) {
                        item.foreground = JBUI.CurrentTheme.Link.Foreground.VISITED
                    } else {
                        item.foreground = JBUI.CurrentTheme.Link.Foreground.ENABLED
                    }
                    val attrs = item.font.attributes as MutableMap<TextAttribute, Int>
                    attrs[TextAttribute.UNDERLINE] = TextAttribute.UNDERLINE_ON
                    item.font = item.font.deriveFont(attrs)
                }
            }
            return item
        }

        override fun onPress(e: MouseEvent) {
            if (!visited) {
                visited = true
                firePropertyChange("foreground", false, true)
            }
        }

        override fun onRelease(e: MouseEvent) {
            url?.let { if (url!!.isNotEmpty()) BrowserUtil.browse(url!!) }
        }
    }

    companion object {
        val tableModel = ListTableModel<PipelineRecord>(
            MainWindowColumnInfo("Project") { it.projectName },
            MainWindowColumnInfo("Branch") { it.branch },
            MainWindowColumnInfo("MR", compareBy { it.mrId }) { Pair(it.mrId, it.webUrl) },
            MainWindowColumnInfo("Status") { it.status },
            MainWindowColumnInfo("Update Time", compareBy { it.updatedAt }) {
                it.updatedAt?.let { instant -> PipelineRecord.instantFormatter(instant) } ?: ""
            },
        )
        val projects = mutableListOf<ProjectData>()

        private fun createDialogPanel(): JBScrollPane {
            val columnRendererMap = mapOf("Status" to object : MainWindowCellObject() {
                override fun view(item: Component, value: Any?, isSelected: Boolean, hasFocus: Boolean): Component {
                    if (value !is String) return item

                    item.foreground = when (value) {
                        "SUCCESS" -> Color.decode(UITheme.getColorPalette()["Actions.Green"])
                        "FAILED" -> Color.decode(UITheme.getColorPalette()["Actions.Red"])
                        "CANCELED" -> Color.decode(UITheme.getColorPalette()["Actions.Grey"])
                        else -> Color.decode(UITheme.getColorPalette()["Actions.Yellow"])
                    }

                    return item
                }
            }, "MR" to UrlTableCell())
            val table = JBTable(tableModel)
            for ((index, columnInfo) in tableModel.columnInfos.withIndex()) {
                if (columnInfo.name in columnRendererMap) {
                    table.columnModel.getColumn(index).cellRenderer = columnRendererMap[columnInfo.name]
                }
            }

            table.addMouseListener(MainWindowMouseListener(table))
            val pane = JBScrollPane(table)
            pane.border = BorderFactory.createEmptyBorder()
            return pane
        }
    }

    init {
        ProjectManager.getInstance().openProjects.forEach { project ->
            GitRepositoryManager.getInstance(project).repositories.forEach { registerRepository(it) }
        }
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(createDialogPanel(), null, false)
        contentManager.addContent(content)
    }
}


