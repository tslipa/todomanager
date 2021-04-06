package am.todomanager.activities

import am.todomanager.recyclers.ListElement
import am.todomanager.R
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.rm.rmswitch.RMSwitch
import java.lang.Integer.max
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*


class ActivityEdit : AppCompatActivity() {
    private lateinit var iconNames: Array<String>
    private lateinit var le : ListElement
    private lateinit var date: String
    private lateinit var time: String
    private lateinit var switch: RMSwitch
    private var position: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        iconNames = resources.getStringArray(R.array.icon_names)

        val element = intent.getParcelableExtra<ListElement>("element")
        if (element != null) {
            le = element
        }

        date = SimpleDateFormat("dd.MM.yyyy", Locale.US).format(java.sql.Date(le.date))
        time = SimpleDateFormat("HH:mm", Locale.US).format(Time(le.time))
        position = intent.getIntExtra( "position", 0)

        findViewById<EditText>(R.id.editText_edit_title).setText(le.title)
        findViewById<EditText>(R.id.editText_edit_description).setText(le.description)
        findViewById<Button>(R.id.button_edit_date).text = date
        findViewById<Button>(R.id.button_edit_time).text = time
        findViewById<ImageView>(R.id.icon_edit_old).setImageResource(resources.getIdentifier(iconNames[le.icon], "drawable", packageName))
        findViewById<RatingBar>(R.id.rating_edit).rating = le.rating

        switch = findViewById(R.id.switch_edit)
        val buttonNotification = findViewById<Button>(R.id.button_edit_notification)
        switch.addSwitchObserver { _, isChecked ->
            buttonNotification.isEnabled = isChecked
        }
        if (le.notification != -1) {
            switch.isChecked = true
            buttonNotification.isEnabled = true
            setNewNotification(le.notification)
        }
    }

    fun declineChanges(view: View) {
        val myIntent = Intent()
        setResult(RESULT_CANCELED, myIntent)
        finish()
    }

    fun acceptChanges(view: View) {
        var title = findViewById<TextView>(R.id.editText_edit_title).text.toString()

        val noTitle = title.isEmpty()
        if (noTitle) {
                title = resources.getString(R.string.title_automatic)
            AlertDialog.Builder(this, R.style.ToDoManagerAlertDialog)
                    .setTitle(getString(R.string.warning)).setMessage(R.string.message_title_removed)
                    .setPositiveButton(getString(R.string.yes)) { _, _ -> finishActivity(title) }
                    .setNegativeButton(getString(R.string.no), null).show()
        } else {
            finishActivity(title)
        }
    }

    private fun finishActivity(title: String) {
        val myIntent = Intent()
        le.title = title
        le.description = findViewById<TextView>(R.id.editText_edit_description).text.toString()
        le.rating = findViewById<RatingBar>(R.id.rating_edit).rating
        le.edited = System.currentTimeMillis()
        if (!switch.isChecked) {
            le.notification = -1
        }
        myIntent.putExtra("element", le)
                .putExtra("position", position)
        setResult(RESULT_OK, myIntent)
        finish()
    }

    fun chooseDate(view: View) {
        val c = Calendar.getInstance(Locale.ROOT)
        c.time = Date(le.date)
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, R.style.ToDoManagerAlertDialog, { _, year, monthOfYear, dayOfMonth ->
            val textView = findViewById<TextView>(R.id.button_edit_date)
            val calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(year, monthOfYear, dayOfMonth)
            le.date = calendar.timeInMillis
            val chosenDate = Date(le.date)
            val dateToDisplay = SimpleDateFormat("dd.MM.yyyy", Locale.US).format(chosenDate)
            textView.text = dateToDisplay
        }, mYear, mMonth, mDay)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    fun chooseTime(view: View) {
        val c = Calendar.getInstance(Locale.ROOT)
        c.time = Time(le.time)
        val mHour = c.get(Calendar.HOUR_OF_DAY)
        val mMinute = c.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, R.style.ToDoManagerAlertDialog, { _, hour, minute ->
            val textView = findViewById<TextView>(R.id.button_edit_time)
            val calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(0, 0, 0, hour, minute)
            le.time = calendar.timeInMillis
            val chosenTime = Time(le.time)
            val timeToDisplay = SimpleDateFormat("HH:mm", Locale.US).format(chosenTime)
            textView.text = timeToDisplay
        }, mHour, mMinute, true)
        timePickerDialog.show()
    }

    fun changeIcon(view: View) {
        val switchActivityIntent = Intent(this, ActivityIconChooser::class.java)
        switchActivityIntent.putExtra("icon", le.icon)
        startActivityForResult(switchActivityIntent, 31)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 31) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    le.icon = data.getIntExtra("icon", le.icon)
                    findViewById<ImageView>(R.id.icon_edit_old).setImageResource(resources.getIdentifier(iconNames[le.icon], "drawable", packageName))
                }
            }
        }
    }

    fun callNotificationSettings(view: View) {
        val choices = resources.getStringArray(R.array.notification_possibilities)
        var newNotification = max(le.notification, 0)

        val dialog = AlertDialog.Builder(this, R.style.ToDoManagerAlertDialog)
                .setTitle(getString(R.string.notification_settings))
                .setSingleChoiceItems(choices, max(le.notification, 0)) { _, which -> newNotification = which}
                .setPositiveButton(getString(R.string.OK)) { _, _ -> setNewNotification(newNotification)}
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
        dialog.show()
    }

    private fun setNewNotification(newNotification: Int) {
        val buttonNotification = findViewById<Button>(R.id.button_edit_notification)
        val choices = resources.getStringArray(R.array.notification_possibilities_short)
        le.notification = newNotification
        buttonNotification.text = choices[le.notification]
    }
}