package com.perevodchik.not_a_task_manager

data class NotAData(
    var id: Long,
    var title: String,
    var text: String,
    var tags: String,
    var isFavorite: Int,
    val createTime: Long,
    var lastUpdateTime: Long,
    var holder: MainActivity.NotATaskAdapter.ViewHolder? = null
)