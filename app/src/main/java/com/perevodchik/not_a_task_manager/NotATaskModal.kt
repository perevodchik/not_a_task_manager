package com.perevodchik.not_a_task_manager

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.TextView
import com.jakewharton.rxbinding3.widget.textChanges
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.not_a_tag_item.view.*
import kotlinx.android.synthetic.main.not_a_task_modal.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt


class NotATaskModal(_position: Int) {
    private val position = _position

    @SuppressLint("InflateParams", "SetTextI18n")
    fun show(
        data: NotAData,
        activity: MainActivity
    ): Dialog {
        val dialog = Dialog(activity)
        val inflater = activity.layoutInflater
        var a = data.tags.split(",")

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.not_a_task_modal)

        // set width and height pane
        dialog.not_a_task_pane.layoutParams.height = (Values.size.y * 0.95).roundToInt()
        dialog.not_a_task_pane.layoutParams.width = (Values.size.x * 0.95).roundToInt()

        dialog.not_a_task_date_create.text = "Create at ".plus(
            SimpleDateFormat(
                Values.dateFormat,
                Locale.UK
            ).format(Date(data.createTime))
        )
        dialog.not_a_task_date_update.text = "Last update at ".plus(
            SimpleDateFormat(
                Values.dateFormat,
                Locale.UK
            ).format(Date(data.lastUpdateTime))
        )

        for (item in a) {
            val textView = inflater.inflate(R.layout.not_a_tag_item, null, false)
            textView.not_a_tag_item_preview.text = item.trim()
            dialog.not_a_task_tags.addView(textView)
        }

        // set title observer
        dialog.not_a_task_title.setText(data.title)
        dialog.not_a_task_title.textChanges()
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnNext {
                data.title = it.toString()
                updateLastUpdateTime(dialog.not_a_task_date_update, data)
                val values = ContentValues().apply {
                    put(Values.columnTitle, "$it")
                    put(Values.columnLastUpdateTime, data.lastUpdateTime)
                }

                activity.not_a_task_recycler?.adapter?.notifyItemChanged(position)
                DbHelper.update(data.id, values)
            }
            .subscribe()

        // set text observer
        dialog.not_a_task_text.setText(data.text)
        dialog.not_a_task_text.textChanges()
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnNext {
                data.text = it.toString()
                updateLastUpdateTime(dialog.not_a_task_date_update, data)

                val values = ContentValues().apply {
                    put(Values.columnText, "$it")
                    put(Values.columnLastUpdateTime, data.lastUpdateTime)
                }

                activity.not_a_task_recycler?.adapter?.notifyItemChanged(position)
                DbHelper.update(data.id, values)
            }
            .subscribe()

        // set tag observer
        dialog.not_a_task_tag_text.setText(data.tags)
        dialog.not_a_task_tag_text.textChanges()
            .debounce(500, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .doOnNext {
                data.tags = it.toString()
                a = data.tags.split(",")
                dialog.not_a_task_tags.removeAllViews()
                updateLastUpdateTime(dialog.not_a_task_date_update, data)

                val values = ContentValues().apply {
                    put(Values.columnTags, "$it")
                    put(Values.columnLastUpdateTime, data.lastUpdateTime)
                }

                activity.not_a_task_recycler?.adapter?.notifyItemChanged(position)
                DbHelper.update(data.id, values)

                for (item in a) {
                    if (item.isEmptyOrBlank())
                        continue
                    dialog.not_a_task_tags.addView(getTagView(inflater, item.trim()))
                }
            }
            .subscribe()

        dialog.favorite_button.setOnClickListener {
            val newValue = if (data.isFavorite == 0) 1 else 0
            val values = ContentValues().apply {
                put(Values.columnIsFavorite, newValue)
            }
            DbHelper.update(data.id, values)
            activity.not_a_task_recycler.adapter?.notifyItemChanged(position)
            dialog.favorite_button.background = activity.resources.getDrawable(
                if (newValue == 0) R.drawable.ic_favorite_not else R.drawable.ic_favorite_1,
                null
            )
            data.isFavorite = newValue
        }

        dialog.delete_button.setOnClickListener {
            DeleteDialog(
                data.id,
                position,
                activity
            ).show(activity.supportFragmentManager, "delete_dialog")
        }

        dialog.show()

        return dialog
    }

    @SuppressLint("SetTextI18n")
    private fun updateLastUpdateTime(v: View, data: NotAData) {
        data.lastUpdateTime = Calendar.getInstance().timeInMillis
        (v as TextView).text = "Last update at ".plus(
            SimpleDateFormat(
                Values.dateFormat,
                Locale.UK
            ).format(Date(data.lastUpdateTime))
        )
    }

    @SuppressLint("InflateParams")
    private fun getTagView(inflater: LayoutInflater, text: String): TextView {
        val v = inflater.inflate(R.layout.not_a_tag_item, null, false)
        v.not_a_tag_item_preview.text = text
        return v as TextView
    }

}