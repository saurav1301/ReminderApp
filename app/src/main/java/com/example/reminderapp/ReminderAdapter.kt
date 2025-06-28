package com.example.reminderapp

import android.widget.ImageView
import Reminder
import android.content.Context
import android.graphics.*
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ReminderAdapter(
    private val reminders: MutableList<Reminder>,
    private val onItemCheckedChange: () -> Unit,
    var multiSelectMode: Boolean = false,
    private val onSwipeDelete: (Reminder, Int) -> Unit,
    private val onSwipeDone: (Int) -> Unit,
    private val onItemClick: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    private var recyclerView: RecyclerView? = null

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val alarmIcon: ImageView = itemView.findViewById(R.id.alarmIcon)
        val title: TextView = itemView.findViewById(R.id.taskTitle)
        val time: TextView = itemView.findViewById(R.id.taskTime)
        val status: TextView = itemView.findViewById(R.id.taskStatus)
        val innerLayout: View = itemView.findViewById(R.id.innerReminderLayout)
    }

    override fun onAttachedToRecyclerView(rv: RecyclerView) {
        super.onAttachedToRecyclerView(rv)
        recyclerView = rv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.title.text = reminder.title
        holder.time.text = reminder.time
        holder.status.text = reminder.status
        holder.alarmIcon.visibility = if (reminder.hasAlarm) View.VISIBLE else View.GONE


        // Status color background
        val statusBg = when (reminder.status) {
            "To-Do" -> Color.parseColor("#F44336")       // Red
            "In Progress" -> Color.parseColor("#FFC107") // Yellow
            "Done" -> Color.parseColor("#4CAF50")         // Green
            else -> Color.GRAY
        }
        holder.status.setBackgroundColor(statusBg)

        // Selected background & text color
        val background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(if (reminder.isSelected) Color.WHITE else Color.BLACK)
            if (reminder.isSelected) setStroke(5, Color.BLACK)
        }
        holder.innerLayout.background = background

        val textColor = if (reminder.isSelected) Color.BLACK else Color.WHITE
        holder.title.setTextColor(textColor)
        holder.time.setTextColor(textColor)
        holder.status.setTextColor(Color.WHITE) // Keep white text for chip

        holder.itemView.setOnClickListener {
            if (multiSelectMode) {
                reminder.isSelected = !reminder.isSelected
                notifyItemChanged(position)
                onItemCheckedChange()
            } else {
                onItemClick(reminder)
            }
        }

        holder.itemView.setOnLongClickListener {
            if (!multiSelectMode) {
                multiSelectMode = true
                reminder.isSelected = true
                notifyDataSetChanged()
                onItemCheckedChange()
            }
            true
        }
    }

    override fun getItemCount(): Int = reminders.size

    fun selectAll() {
        reminders.forEach { it.isSelected = true }
        notifyDataSetChanged()
        onItemCheckedChange()
    }

    fun clearSelection() {
        reminders.forEach { it.isSelected = false }
        multiSelectMode = false
        notifyDataSetChanged()
        onItemCheckedChange()
    }

    fun animateSwipeDelete(position: Int) {
        notifyItemChanged(position)
        Handler(Looper.getMainLooper()).post {
            val viewHolder = recyclerView?.findViewHolderForAdapterPosition(position)
            viewHolder?.itemView?.animate()?.translationX(-viewHolder.itemView.width.toFloat())
                ?.alpha(0f)
                ?.setDuration(300)
                ?.withEndAction {
                    // deletion handled in MainActivity
                }
                ?.start()
        }
    }

    fun getItemTouchHelper(context: Context): ItemTouchHelper {
        return ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                if (position == RecyclerView.NO_POSITION || position >= reminders.size) {
                    notifyDataSetChanged()
                    return
                }

                if (multiSelectMode) {
                    notifyItemChanged(position)
                    return
                }

                when (direction) {
                    ItemTouchHelper.LEFT -> onSwipeDelete(reminders[position], position)
                    ItemTouchHelper.RIGHT -> onSwipeDone(position)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint().apply { isAntiAlias = true }
                val textPaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 40f
                    typeface = Typeface.DEFAULT_BOLD
                    isAntiAlias = true
                }

                if (dX > 0) {
                    paint.color = Color.parseColor("#4CAF50")
                    c.drawRect(itemView.left.toFloat(), itemView.top.toFloat(), itemView.left + dX, itemView.bottom.toFloat(), paint)
                    c.drawText("Mark as Done", itemView.left + 50f, itemView.top + itemView.height / 2f + 15f, textPaint)
                } else if (dX < 0) {
                    paint.color = Color.parseColor("#F44336")
                    c.drawRect(itemView.right.toFloat() + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat(), paint)
                    val textWidth = textPaint.measureText("Delete")
                    c.drawText("Delete", itemView.right - textWidth - 50f, itemView.top + itemView.height / 2f + 15f, textPaint)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float = 0.3f
        })
    }
}
