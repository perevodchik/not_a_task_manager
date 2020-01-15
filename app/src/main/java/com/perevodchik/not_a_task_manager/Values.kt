package com.perevodchik.not_a_task_manager

import android.graphics.Point

object Values {
    val LOG_TAG = DbHelper::class.java.simpleName
    const val DATABASE_NAME = "not_a_task.db"
    const val TABLE_NAME = "not_a_task"
    const val dateFormat = "hh:mm dd MMMM yyyy"

    /** database column names START **/
    const val columnId = "id"
    const val columnTitle = "title"
    const val columnText = "text"
    const val columnTags = "tags"
    const val columnIsFavorite = "isFavorite"
    const val columnCreateTime = "createTime"
    const val columnLastUpdateTime = "updateTime"
    /** database column names END **/

    /** container for hold size screen */
    var size: Point = Point()
}
