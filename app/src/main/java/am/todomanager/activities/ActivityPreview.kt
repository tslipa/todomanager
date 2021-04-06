package am.todomanager.activities

import am.todomanager.recyclers.ListElement
import am.todomanager.R
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.sql.Date
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

class ActivityPreview : AppCompatActivity() {
    private lateinit var iconNames: Array<String>
    private lateinit var le : ListElement
    private lateinit var date : String
    private lateinit var time : String
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        iconNames = resources.getStringArray(R.array.icon_names)

        val element = intent.getParcelableExtra<ListElement>("element")
        if (element != null) {
            le = element
        }

        date = SimpleDateFormat("dd.MM.yyyy", Locale.US).format(Date(le.date))
        time = SimpleDateFormat("HH:mm", Locale.US).format(Time(le.time))
        position = intent.getIntExtra( "position", 0)

        findViewById<TextView>(R.id.textView_preview_title).text = le.title
        if (le.description!!.isNotEmpty()) {
            findViewById<TextView>(R.id.textView_preview_description).text = le.description
        }
        findViewById<TextView>(R.id.textView_preview_deadline).text = "$date,   $time"
        findViewById<ImageView>(R.id.icon_preview).setImageResource(resources.getIdentifier(iconNames[le.icon] + "_w", "drawable", packageName))
        findViewById<RatingBar>(R.id.rating_preview).rating = le.rating
        if (le.notification != -1) {
            val choices = resources.getStringArray(R.array.notification_possibilities)
            findViewById<TextView>(R.id.textView_preview_notification).text = getString(R.string.notification) + " " + choices[le.notification].toLowerCase(Locale.ROOT)
        }
    }

    fun editTask(view: View) {
        val switchActivityIntent = Intent(this, ActivityEdit::class.java)
        switchActivityIntent.putExtra("element", le)
                .putExtra("position", position)
        startActivityForResult(switchActivityIntent, 21)
    }

    fun tryDelete(view: View) {
        AlertDialog.Builder(this, R.style.ToDoManagerAlertDialog)
                .setTitle(getString(R.string.warning)).setMessage(getString(R.string.question_delete_task))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> delete() }
                .setNegativeButton(getString(R.string.no), null).show()
    }

    private fun delete() {
        val intent = Intent()
        intent.putExtra("action", "delete")
                .putExtra("position", position)
        setResult(RESULT_OK, intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 21) {
            if (resultCode == RESULT_OK) {
                val intent = Intent()
                intent.putExtra("action", "edit")
                        .putExtra("element", data?.getParcelableExtra<ListElement>("element"))
                        .putExtra("position", position)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    fun back(view: View) {
        val intent = Intent()
        intent.putExtra("action", "none")
        setResult(RESULT_OK, intent)
        finish()
    }
}