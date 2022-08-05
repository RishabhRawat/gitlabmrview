package com.rrawat.gitlabmrview.kotlin

import com.intellij.ui.table.JBTable
import com.intellij.util.ui.ColumnInfo
import java.awt.Component
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

class MainWindowColumnInfo(
    name: String,
    private val comparator: Comparator<PipelineRecord>? = null,
    private val value: (item: PipelineRecord) -> Any,
) : ColumnInfo<PipelineRecord, Any>(name) {
    override fun valueOf(item: PipelineRecord?): Any? {
        return item?.let { value(item) }
    }

    override fun getComparator(): Comparator<PipelineRecord> {
        return comparator ?: compareBy { valueOf(it) as Comparable<*>? }
    }
}

abstract class MainWindowCellObject : DefaultTableCellRenderer() {
    open fun view(item: Component, value: Any?, isSelected: Boolean, hasFocus: Boolean): Component {
        return item
    }

    open fun onPress(e: MouseEvent) {}
    open fun onRelease(e: MouseEvent) {}

    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int,
    ): Component {
        val r = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
        return view(r, value, isSelected, hasFocus)
    }
}

class MainWindowMouseListener(private val table: JBTable) : MouseListener {
    override fun mouseClicked(e: MouseEvent) {}

    override fun mousePressed(e: MouseEvent) {
        val renderer = table.columnModel.getColumn(table.columnAtPoint(e.point)).cellRenderer
        if (renderer is MainWindowCellObject) renderer.onPress(e)
    }

    override fun mouseReleased(e: MouseEvent) {
        val renderer = table.columnModel.getColumn(table.columnAtPoint(e.point)).cellRenderer
        if (renderer is MainWindowCellObject) renderer.onRelease(e)
    }

    override fun mouseEntered(e: MouseEvent?) {}

    override fun mouseExited(e: MouseEvent?) {}

}

