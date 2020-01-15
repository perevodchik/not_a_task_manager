package com.perevodchik.not_a_task_manager

fun String.isEmptyOrBlank(): Boolean {
    return this.isEmpty() || this.isBlank()
}

