package com.perevodchik.not_a_task_manager

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class DeleteDialog(_id: Long, _position: Int, _context: MainActivity): DialogFragment() {
    private val taskId = _id
    private val position = _position
    private val context = _context

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context as Context)
        builder.setMessage(R.string.delete_task)
            .setPositiveButton(R.string.delete
            ) { dialog, _ ->
                DbHelper.delete(taskId)
                context.removeTask(taskId, position)
                dialog.cancel()
            }
            .setNegativeButton(R.string.cancel
            ) { dialog, _ ->
                dialog.cancel()
            }
        return builder.create()
    }
}