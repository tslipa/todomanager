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


class ActivityAdd : AppCompatActivity() {
    private lateinit var iconNames: Array<String>
    private var id : Int = 0
    private var date : Long = System.currentTimeMillis()
    private var time : Long = System.currentTimeMillis()
    private var icon : Int = 6
    private var notification: Int = -1
    private lateinit var switch : RMSwitch
    private var defaultDate : Boolean = true
    private var defaultTime : Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        id = intent.getIntExtra("id", 0)

        findViewById<TextView>(R.id.textView_edit_header).text = getString(R.string.new_task)
        iconNames = resources.getStringArray(R.array.icon_names)
        findViewById<ImageView>(R.id.icon_edit_old).setImageResource(resources.getIdentifier(iconNames[icon], "drawable", packageName))

        switch = findViewById(R.id.switch_edit)
        val buttonNotification = findViewById<Button>(R.id.button_edit_notification)
        switch.addSwitchObserver { _, isChecked ->
            buttonNotification.isEnabled = isChecked
        }
    }

    private fun setDefaultDateValue() {
        val c = Calendar.getInstance(Locale.ROOT)
        c.time = Date(System.currentTimeMillis())
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH) + 1
        c.clear()
        c.set(mYear, mMonth, mDay)
        date = c.timeInMillis
    }

    private fun setDefaultTimeValue() {
        val c = Calendar.getInstance(Locale.ROOT)
        c.time = Date(System.currentTimeMillis())
        val mHour = c.get(Calendar.HOUR_OF_DAY)
        val mMinute = c.get(Calendar.MINUTE)
        c.clear()
        c.set(0, 0, 0, mHour, mMinute)
        time = c.timeInMillis
    }

    fun declineChanges(view: View) {
        val myIntent = Intent()
        setResult(RESULT_CANCELED, myIntent)
        finish()
    }

    fun acceptChanges(view: View) {
        var title = findViewById<TextView>(R.id.editText_edit_title).text.toString()

        val noTitle = title.isEmpty()
        val showDialog = noTitle || defaultDate || defaultTime
        if (showDialog) {
            var message = resources.getString(R.string.data_missing_start) + "\n"
            if (noTitle) {
                title = resources.getString(R.string.title_automatic)
                message += "- ${resources.getString(R.string.data_missing_title)}\n"
            }
            if (defaultDate) {
                setDefaultDateValue()
                message += "- ${resources.getString(R.string.data_missing_day)}\n"
            }
            if (defaultTime) {
                setDefaultTimeValue()
                message += "- ${resources.getString(R.string.data_missing_hour)}\n"
            }
            message += "\n${resources.getString(R.string.data_missing_end)}"
            AlertDialog.Builder(this, R.style.ToDoManagerAlertDialog)
                    .setTitle(getString(R.string.warning)).setMessage(message)
                    .setPositiveButton(getString(R.string.yes)) { _, _ -> finishActivity(title) }
                    .setNegativeButton(getString(R.string.no), null).show()
        } else {
            finishActivity(title)
        }
    }

    private fun finishActivity(title: String) {
        val myIntent = Intent()
        val description = findViewById<TextView>(R.id.editText_edit_description).text.toString()
        val rating = findViewById<RatingBar>(R.id.rating_edit).rating
        if (!switch.isChecked) {
            notification = -1
        }

        val le = ListElement(id, title, description, date, time, icon, rating, System.currentTimeMillis(), notification)
        myIntent.putExtra("element", le)
        setResult(RESULT_OK, myIntent)
        finish()
    }

    fun chooseDate(view: View) {
        val c = Calendar.getInstance(Locale.ROOT)
        c.time = Date(date)
        val mYear = c.get(Calendar.YEAR)
        val mMonth = c.get(Calendar.MONTH)
        val mDay = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, R.style.ToDoManagerAlertDialog, { _, year, monthOfYear, dayOfMonth ->
            val textView = findViewById<TextView>(R.id.button_edit_date)
            val calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(year, monthOfYear, dayOfMonth)
            date = calendar.timeInMillis
            val chosenDate = Date(date)
            val dateToDisplay = SimpleDateFormat("dd.MM.yyyy", Locale.US).format(chosenDate)
            textView.text = dateToDisplay
            defaultDate = false
        }, mYear, mMonth, mDay)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    fun chooseTime(view: View) {
        val c = Calendar.getInstance(Locale.ROOT)
        c.time = Time(time)
        val mHour = c.get(Calendar.HOUR_OF_DAY)
        val mMinute = c.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, R.style.ToDoManagerAlertDialog, { _, hour, minute ->
            val textView = findViewById<TextView>(R.id.button_edit_time)
            val calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(0, 0, 0, hour, minute)
            time = calendar.timeInMillis
            val chosenTime = Time(time)
            val timeToDisplay = SimpleDateFormat("HH:mm", Locale.US).format(chosenTime)
            textView.text = timeToDisplay
            defaultTime = false
        }, mHour, mMinute, true)
        timePickerDialog.show()
    }

    fun changeIcon(view: View) {
        val switchActivityIntent = Intent(this, ActivityIconChooser::class.java)
        switchActivityIntent.putExtra("icon", icon)
        startActivityForResult(switchActivityIntent, 31)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 31) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    icon = data.getIntExtra("icon", icon)
                    findViewById<ImageView>(R.id.icon_edit_old).setImageResource(resources.getIdentifier(iconNames[icon], "drawable", packageName))
                }
            }
        }
    }

    fun callNotificationSettings(view: View) {
        val choices = resources.getStringArray(R.array.notification_possibilities)
        var newNotification = max(notification, 0)

        val dialog = AlertDialog.Builder(this, R.style.ToDoManagerAlertDialog)
                .setTitle(getString(R.string.notification_settings))
                .setSingleChoiceItems(choices, max(notification, 0)) { _, which -> newNotification = which}
                .setPositiveButton(getString(R.string.OK)) { _, _ -> setNewNotification(newNotification)}
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
        dialog.show()
    }

    private fun setNewNotification(newNotification: Int) {
        val buttonNotification = findViewById<Button>(R.id.button_edit_notification)
        val choices = resources.getStringArray(R.array.notification_possibilities_short)
        notification = newNotification
        buttonNotification.text = choices[notification]
    }
}