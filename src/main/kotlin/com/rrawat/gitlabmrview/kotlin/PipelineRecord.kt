package com.rrawat.gitlabmrview.kotlin

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

internal fun toDateTimeString(format: String): (Instant) -> String {
    val formatter = DateTimeFormatter.ofPattern(format)
    return { x: Instant -> formatter.format(x.toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime()) }
}


data class PipelineRecord(
    val mrId: String,
    val projectName: String,
    val branch: String,
    val webUrl: String,
    val updatedAt: Instant?,
    val status: String,
) {
    companion object {
        val instantFormatter = toDateTimeString("yyyy-MM-dd hh:mm")
    }
}