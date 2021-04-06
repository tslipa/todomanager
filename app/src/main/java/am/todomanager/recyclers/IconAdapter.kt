package am.todomanager.recyclers

import am.todomanager.R
import am.todomanager.activities.ActivityIconChooser
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class IconAdapter(private val dataSet: ArrayList<IconElement>, private val mContext: Context) :
RecyclerView.Adapter<IconAdapter.ViewHolder>()  {

    class ViewHolder(view: View, context: Context) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView
        val textView: TextView
        val thisView: View

        init {
            imageView = view.findViewById(R.id.image_icon)
            textView = view.findViewById(R.id.textView_icon_type)
            thisView = view

            view.setOnClickListener {
                if (context is ActivityIconChooser) {
                    context.changeCurrent(view)
                }
            }
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.list_icon_item, viewGroup, false)
        return ViewHolder(view, mContext)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val le = dataSet[position]
        viewHolder.imageView.setImageResource(le.iconId)
        viewHolder.textView.text = le.iconDescription
        viewHolder.thisView.tag = position
        if (le.isCurrent) {
            viewHolder.thisView.setBackgroundColor(ContextCompat.getColor(mContext,
                R.color.dark_orange))
        } else {
            viewHolder.thisView.setBackgroundColor(ContextCompat.getColor(mContext,
                R.color.light_orange))
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}