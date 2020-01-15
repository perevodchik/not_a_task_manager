package com.perevodchik.not_a_task_manager

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexboxLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.not_a_tag_item.view.*
import kotlinx.android.synthetic.main.not_a_task_item_preview.view.*

class MainActivity : AppCompatActivity() {
    /** display */
    private val display by lazy { windowManager.defaultDisplay }
    private var dialog: Dialog? = null

    companion object {
        var db: SQLiteDatabase? = null
        var dbHelper: DbHelper? = null
    }

    private val list = mutableListOf<NotAData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = DbHelper(this)
        db = dbHelper?.readableDatabase


        list.addAll(getTaskFromDB())

        not_a_task_recycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        not_a_task_recycler.adapter = NotATaskAdapter(list, this)

        fab.setOnClickListener {
            val values = ContentValues()
            val time = Calendar.getInstance().timeInMillis
            values.apply {
                put("title", "")
                put("text", "")
                put("tags", "")
                put("isFavorite", 0)
                put("updateTime", time)
                put("createTime", time)
            }
            val id = DbHelper.create(values)

            list.add(0, NotAData(id, "", "", "", 0, time, time, null))
            not_a_task_recycler.adapter?.notifyDataSetChanged()

            dialog?.hide()
            dialog = NotATaskModal(0).show(list[0], this)
        }

        display.getSize(Values.size)
    }

    fun removeTask(id: Long, position: Int) {
        val iterator = list.listIterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.id == id) {
                iterator.remove()
            }
        }
        not_a_task_recycler.adapter?.notifyItemRemoved(position)
    }

    private fun getTaskFromDB(): MutableList<NotAData> {
        val list = mutableListOf<NotAData>()
        val c =
            db!!.query(Values.TABLE_NAME, null, null, null, null, null, "${Values.columnId} DESC")
        if (c.moveToFirst()) {
            do {
                val id = c.getColumnIndex("id")
                val title = c.getColumnIndex("title")
                val text = c.getColumnIndex("text")
                val tags = c.getColumnIndex("tags")
                val isFavorite = c.getColumnIndex("isFavorite")
                val createTime = c.getColumnIndex("createTime")
                val updateTime = c.getColumnIndex("updateTime")
                val entity = NotAData(
                    c.getLong(id),
                    c.getString(title),
                    c.getString(text),
                    c.getString(tags),
                    c.getInt(isFavorite),
                    c.getLong(createTime),
                    c.getLong(updateTime)
                )
                list.add(entity)
            } while (c.moveToNext())
        }
        if (!c.isClosed) c.close()

        return list
    }

    inner class NotATaskAdapter(_list: MutableList<NotAData>, _ctx: Context) :
        RecyclerView.Adapter<NotATaskAdapter.ViewHolder>() {
        private val notATaskList = _list
        private val context = _ctx
        private val inflater = (context as Activity).layoutInflater

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(inflater.inflate(R.layout.not_a_task_item_preview, parent, false))
        }

        override fun getItemCount(): Int {
            return notATaskList.size
        }

        @SuppressLint("InflateParams")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.notATitle.text = notATaskList[position].title

            holder.notAText.text = notATaskList[position].text.substring(
                0,
                when {
                    notATaskList[position].text.length >= 97 -> 97
                    notATaskList[position].text.isEmptyOrBlank() -> 0
                    else -> notATaskList[position].text.length
                }
            ).plus("...")

            val a = notATaskList[position].tags.split(",")
            holder.notATags.removeAllViews()
            for (item in a) {
                val textView = inflater.inflate(R.layout.not_a_tag_item, null, false)
                textView.not_a_tag_item_preview.text = item.trim()
                holder.notATags.addView(textView)
            }

            when (notATaskList[position].isFavorite) {
                0 -> holder.favoriteButton.background =
                    this@MainActivity.resources.getDrawable(R.drawable.ic_favorite_not, null)
                else -> holder.favoriteButton.background =
                    this@MainActivity.resources.getDrawable(R.drawable.ic_favorite_1, null)
            }

            holder.favoriteButton.setOnClickListener {
                val newValue = if (notATaskList[position].isFavorite == 0) 1 else 0
                val values = ContentValues().apply {
                    put(Values.columnIsFavorite, newValue)
                }
                DbHelper.update(notATaskList[position].id, values)
                holder.favoriteButton.background = this@MainActivity.resources.getDrawable(
                    if (newValue == 0) R.drawable.ic_favorite_not else R.drawable.ic_favorite_1,
                    null
                )
                notATaskList[position].isFavorite = newValue
            }

            holder.deleteButton.setOnClickListener {
                DeleteDialog(
                    list[position].id,
                    position,
                    this@MainActivity
                ).show(this@MainActivity.supportFragmentManager, "delete_dialog")
            }

            holder.notAPane.setOnClickListener {
                dialog?.hide()
                dialog = NotATaskModal(position).show(notATaskList[position], this@MainActivity)
            }
            holder.notAPane.setOnLongClickListener {
                true
            }
        }

        inner class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            val notAPane: LinearLayout = v.not_a_task_pane_preview
            val notATitle: TextView = v.not_a_title_preview
            val notAText: TextView = v.not_a_text_preview
            val notATags: FlexboxLayout = v.not_a_tags_preview
            val favoriteButton: ImageView = v.favorite_button
            val deleteButton: ImageView = v.delete_button
        }
    }

    override fun onDestroy() {
        db?.close()
        super.onDestroy()
    }
}
