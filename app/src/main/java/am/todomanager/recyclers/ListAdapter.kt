package am.todomanager.recyclers

import am.todomanager.R
import am.todomanager.activities.MainActivity
import android.annotation.SuppressLint
import android.content.Context
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.sql.Time
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class ListAdapter(private val dataSet: ArrayList<ListElement>, private val mContext: Context) :
RecyclerView.Adapter<ListAdapter.ViewHolder>()  {
    private lateinit var iconNames: Array<String>

    class ViewHolder(view: View, context: Context) : RecyclerView.ViewHolder(view)  {
        val textViewTitle: TextView
        val textViewDate: TextView
        val icon: ImageView
        val priority: TextView

        init {
            textViewTitle = view.findViewById(R.id.textViewTitle)
            textViewDate = view.findViewById(R.id.textViewDate)
            icon = view.findViewById(R.id.imageView)
            priority = view.findViewById(R.id.textView_rating)

            view.setOnClickListener {
                if (context is MainActivity) {
                    context.callPreview(view)
                }
            }

            view.setOnLongClickListener {
                if (context is MainActivity) {

                    context.callEditDeleteDialog(view)
                }
                return@setOnLongClickListener true
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_tasks_item, viewGroup, false)
        iconNames = mContext.resources.getStringArray(R.array.icon_names)
        return ViewHolder(view, mContext)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val le = dataSet[position]
        viewHolder.textViewTitle.text = le.title
        val dfDate: DateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)
        val date: String = dfDate.format(Date(le.date))
        val dfTime: DateFormat = SimpleDateFormat("HH:mm", Locale.GERMANY)
        val time: String = dfTime.format(Time(le.time))
        val priorityValue: Int = le.rating.toInt()

        viewHolder.textViewDate.text = "$date, $time"
        viewHolder.itemView.tag = position
        viewHolder.icon.setImageResource(mContext.resources.getIdentifier(iconNames[le.icon] + "_w", "drawable", mContext.packageName))
        viewHolder.priority.text = "$priorityValue*"
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}