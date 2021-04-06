package am.todomanager.activities

import am.todomanager.recyclers.IconAdapter
import am.todomanager.recyclers.IconElement
import am.todomanager.R
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ActivityIconChooser : AppCompatActivity() {
    private lateinit var iconNames: Array<String>
    private lateinit var iconDescriptions: Array<String>
    private val list: ArrayList<IconElement> = ArrayList()
    private var currentIcon : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon)

        iconNames = resources.getStringArray(R.array.icon_names)
        iconDescriptions = resources.getStringArray(R.array.icon_descriptions)
        currentIcon = intent.getIntExtra("icon", 0)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view_icon)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = IconAdapter(getData(), this)
    }

    private fun getData() : ArrayList<IconElement> {
        for (i in iconNames.indices) {
            val id = resources.getIdentifier(iconNames[i] + "_w", "drawable", packageName)
            val isCurrent = (currentIcon == i)

            list.add(IconElement(id, iconDescriptions[i], i, isCurrent))
        }
        return list
    }

    fun changeCurrent(view: View) {
        list[currentIcon].isCurrent = false
        currentIcon = view.tag as Int
        list[currentIcon].isCurrent = true
        findViewById<RecyclerView>(R.id.recycler_view_icon).adapter?.notifyDataSetChanged()
    }

    fun back(view: View) {
        val intent = Intent()
        setResult(RESULT_CANCELED, intent)
        finish()
    }

    fun accept(view: View) {
        val intent = Intent()
        intent.putExtra("icon", currentIcon)
        setResult(RESULT_OK, intent)
        finish()
    }
}