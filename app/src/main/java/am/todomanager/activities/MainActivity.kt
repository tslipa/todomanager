package am.todomanager.activities

import am.todomanager.recyclers.ListAdapter
import am.todomanager.recyclers.ListElement
import am.todomanager.R
import am.todomanager.Reminder
import am.todomanager.database.AppDatabase
import am.todomanager.database.EntityListElement
import am.todomanager.database.EntitySettings
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private var list : ArrayList<ListElement> = ArrayList()
    private var sortType : Int = 0
    private lateinit var db: AppDatabase
    private var maxId = 0
    private var notificationManager: NotificationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        db = AppDatabase.getDatabase(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
        recyclerView.adapter = ListAdapter(list, this@MainActivity)

        GlobalScope.launch {
            val settingsPopulated = (db.daoSettings().count() == 1)
            if (settingsPopulated) {
                val settings = db.daoSettings().get()
                runOnUiThread {
                    Log.d(null, "${settings.sort} ${settings.maxId}")
                    sortType = settings.sort
                    maxId = settings.maxId
                }
            }
            val dataFromDB = db.daoElements().getAll()
            insertData(dataFromDB, recyclerView)
        }

        registerForContextMenu(recyclerView)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && findViewById<FloatingActionButton>(R.id.fab_main_add).isShown) {
                    findViewById<FloatingActionButton>(R.id.fab_main_add).hide()
                }
                if (dy > 0 || dy < 0 && findViewById<FloatingActionButton>(R.id.fab_main_sort).isShown) {
                    findViewById<FloatingActionButton>(R.id.fab_main_sort).hide()
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    findViewById<FloatingActionButton>(R.id.fab_main_add).show()
                    findViewById<FloatingActionButton>(R.id.fab_main_sort).show()
                }
            }
        })

        createNotificationChannel(
            "am.todo.reminder",getString(R.string.reminders),
            getString(R.string.reminders_description))
    }

    //RESTORING THE STATE
    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putParcelableArrayList("list", list)
        savedInstanceState.putInt("sortType", sortType)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.run {
            list = savedInstanceState.getParcelableArrayList("list")!!
            findViewById<RecyclerView>(R.id.recycler_view).adapter = ListAdapter(
                list, this@MainActivity)
            sortType = savedInstanceState.getInt("sortType", 0)
        }
    }

    //LOADING DATA FROM DATABASE
    private fun insertData(dataFromDB: List<EntityListElement>, recyclerView: RecyclerView) {
        if (list.isEmpty()) {
            for (i in dataFromDB) {
                if (maxId < i.le_id) maxId = i.le_id
                list.add(
                    ListElement(
                        i.le_id,
                        i.title,
                        i.description,
                        i.date,
                        i.time,
                        i.icon,
                        i.rating,
                        i.edited,
                        i.notification))
            }
            runOnUiThread { doSort(sortType) }
            recyclerView.post(Runnable { recyclerView.adapter?.notifyDataSetChanged() })
        }
    }

    //NOTIFICATIONS
    private fun createNotificationChannel(id: String, name: String, description: String) {
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(id, name, importance)

        channel.description = description
        channel.enableLights(true)
        channel.lightColor = Color.RED
        channel.enableVibration(true)
        channel.vibrationPattern =
            longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.createNotificationChannel(channel)
    }

    private fun addNewNotification(le: ListElement) {
        if (le.notification != -1) {
            val c = Calendar.getInstance(Locale.ROOT)
            c.time = Date(le.date)
            val mYear = c.get(Calendar.YEAR)
            val mMonth = c.get(Calendar.MONTH)
            val mDay = c.get(Calendar.DAY_OF_MONTH)
            c.clear()
            c.time = Date(le.time)
            val mHour = c.get(Calendar.HOUR_OF_DAY)
            val mMinute = c.get(Calendar.MINUTE)
            c.clear()
            c.set(mYear, mMonth, mDay, mHour, mMinute)

            when (le.notification)
            {
                0 -> c.add(Calendar.HOUR_OF_DAY, -1)
                1 -> c.add(Calendar.MINUTE, -30)
                2 -> c.add(Calendar.MINUTE, -15)
                3 -> c.add(Calendar.MINUTE, -10)
                4 -> c.add(Calendar.MINUTE, -5)
            }

            val time = c.timeInMillis

            val calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(mYear, mMonth, mDay, mHour, mMinute)
            val deadline = Date(calendar.timeInMillis)
            val timeToDisplay = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.US).format(deadline)

            val intent = Intent(this, Reminder::class.java)
            intent.putExtra("id", le.id).putExtra("title", "${le.title} [${timeToDisplay}]")
            val pendingIntent = PendingIntent.getBroadcast(this, le.id, intent, 0)
            val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent)
        }
    }

    private fun deleteNotification(le: ListElement) {
        val intent = Intent(this, Reminder::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this.applicationContext, le.id, intent, 0)
        val am = applicationContext.getSystemService(ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    //CALLING OTHER ACTIVITIES
    fun callAdd(view: View) {
        maxId++;
        GlobalScope.launch {
            db.daoSettings().update(EntitySettings(0, sortType, maxId))
        }
        val switchActivityIntent = Intent(this, ActivityAdd::class.java)
        switchActivityIntent.putExtra("id", maxId)
        startActivityForResult(switchActivityIntent, 11)
    }

    fun callPreview(view: View) {
        val switchActivityIntent = Intent(this, ActivityPreview::class.java)
        val pos = view.tag as Int
        val le = list[pos]
        switchActivityIntent.putExtra("element", le)
            .putExtra("position", pos)
        startActivityForResult(switchActivityIntent, 12)
    }

    private fun callEdit(pos: Int) {
        val switchActivityIntent = Intent(this, ActivityEdit::class.java)
        val le = list[pos]
        switchActivityIntent.putExtra("element", le)
            .putExtra("position", pos)
        startActivityForResult(switchActivityIntent, 13)
    }

    private fun callDelete(pos: Int) {
        val id = list[pos].id
        GlobalScope.launch {
            db.daoElements().deleteElement(id)
        }
        deleteNotification(list[pos])
        list.removeAt(pos)
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }

    //RETURNING FROM OTHER ACTIVITIES
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 11 && resultCode == RESULT_OK) {
            val le = data?.getParcelableExtra<ListElement>("element")
            if (le != null) {
                list.add(le)
                GlobalScope.launch {
                    val dbElement = EntityListElement(
                        le.id,
                        le.title,
                        le.description,
                        le.date,
                        le.time,
                        le.icon,
                        le.rating,
                        le.edited,
                        le.notification)
                    db.daoElements().insert(dbElement)
                }
                addNewNotification(le)
            }
        } else if (requestCode == 12 && resultCode == RESULT_OK) {
            if (data?.getStringExtra("action")!! == "edit") {
                val le = data.getParcelableExtra<ListElement>("element")
                if (le != null) {
                    val position: Int = data.getIntExtra("position", 0)
                    list[position] = le

                    GlobalScope.launch {
                        val dbElement = EntityListElement(
                            le.id,
                            le.title,
                            le.description,
                            le.date,
                            le.time,
                            le.icon,
                            le.rating,
                            le.edited,
                            le.notification)
                        db.daoElements().update(dbElement)
                    }
                    deleteNotification(le)
                    addNewNotification(le)
                }
            } else if (data.getStringExtra("action")!! == "delete") {
                val position: Int = data.getIntExtra("position", 0)
                val id: Int = list[position].id
                GlobalScope.launch {
                    db.daoElements().deleteElement(id)
                }
                deleteNotification(list[position])
                list.removeAt(position)
            }
        } else if (requestCode == 13 && resultCode == RESULT_OK) {
            val le = data?.getParcelableExtra<ListElement>("element")
            if (le != null) {
                val position: Int = data.getIntExtra("position", 0)
                list[position] = le
                GlobalScope.launch {
                    val dbElement = EntityListElement(
                        le.id,
                        le.title,
                        le.description,
                        le.date,
                        le.time,
                        le.icon,
                        le.rating,
                        le.edited,
                        le.notification)
                    db.daoElements().update(dbElement)
                }
                deleteNotification(le)
                addNewNotification(le)
            }
        }

        doSort(sortType)
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }

    //DIALOG BOXES
    fun callEditDeleteDialog(view: View) {
        val pos = view.tag as Int
        val title = list[pos].title
        val choices = arrayOf(getString(R.string.edit), getString(R.string.delete))

        val dialog = AlertDialog.Builder(this, R.style.ToDoManagerAlertDialog)
                .setTitle(title)
                .setItems(choices) { _, which ->
                    if (which == 0) {
                        callEdit(pos)
                    } else if (which == 1) {
                        AlertDialog.Builder(this, R.style.ToDoManagerAlertDialog)
                            .setTitle(getString(R.string.warning)).setMessage(getString(R.string.question_delete_task))
                            .setPositiveButton(getString(R.string.yes)) { _, _ -> callDelete(pos) }
                            .setNegativeButton(getString(R.string.no), null).show()
                    }
                }.create()

        dialog.show()
    }

    fun callSortDialog(view: View) {
        val choices = resources.getStringArray(R.array.sort_possibilities)

        var newSortType: Int = sortType

        val dialog = AlertDialog.Builder(this, R.style.ToDoManagerAlertDialog)
            .setTitle(getString(R.string.choose_sort))
            .setSingleChoiceItems(choices, sortType) { _, which -> newSortType = which}
            .setPositiveButton("OK") { _, _ -> doSort(newSortType) }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    //SORTING
    private fun doSort(type: Int) {
        sortType = type
        GlobalScope.launch {
            db.daoSettings().update(EntitySettings(0, sortType, maxId))
        }
        when (type) {
            0 -> sortEditDateInc()
            1 -> sortEditDateDesc()
            2 -> sortDeadlineInc()
            3 -> sortDeadlineDesc()
            4 -> sortTitleInc()
            5 -> sortTitleDesc()
            6 -> sortPriorityInc()
            7 -> sortPriorityDesc()
            8 -> sortTypes()
        }
    }

    private fun sortEditDateInc() {
        list.sortWith { lhs, rhs ->
            if (lhs.edited > rhs.edited) 1 else if (lhs.edited < rhs.edited) -1 else 0
        }
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }

    private fun sortEditDateDesc() {
        list.sortWith { lhs, rhs ->
            if (lhs.edited > rhs.edited) -1 else if (lhs.edited < rhs.edited) 1 else 0
        }
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }

    private fun sortDeadlineInc() {
        list.sortWith { lhs, rhs ->
            if (lhs.date < rhs.date || (lhs.date == rhs.date && lhs.time < rhs.time)) -1
            else if (lhs.date > rhs.date || (lhs.date == rhs.date && lhs.time > rhs.time)) 1
            else 0
        }
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }

    private fun sortDeadlineDesc() {
        list.sortWith { lhs, rhs ->
            if (lhs.date > rhs.date || (lhs.date == rhs.date && lhs.time > rhs.time)) -1
            else if (lhs.date < rhs.date || (lhs.date == rhs.date && lhs.time < rhs.time)) 1
            else 0
        }
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }

    private fun sortTitleInc() {
        list.sortWith { lhs, rhs ->
            if (lhs.title!!.toLowerCase(Locale.ROOT) < rhs.title!!.toLowerCase(Locale.ROOT)) -1
            else if (lhs.title!! > rhs.title!!) 1 else 0
        }
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }

    private fun sortTitleDesc() {
        list.sortWith { lhs, rhs ->
            if (lhs.title!!.toLowerCase(Locale.ROOT) > rhs.title!!.toLowerCase(Locale.ROOT)) -1
            else if (lhs.title!! < rhs.title!!) 1 else 0
        }
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }

    private fun sortPriorityInc() {
        list.sortWith { lhs, rhs ->
            if (lhs.rating < rhs.rating) -1 else if (lhs.rating > rhs.rating) 1 else 0
        }
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }

    private fun sortPriorityDesc() {
        list.sortWith { lhs, rhs ->
            if (lhs.rating > rhs.rating) -1 else if (lhs.rating < rhs.rating) 1 else 0
        }
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }

    private fun sortTypes() {
        list.sortWith { lhs, rhs ->
            if (lhs.icon < rhs.icon) -1 else if (lhs.icon > rhs.icon) 1 else 0
        }
        findViewById<RecyclerView>(R.id.recycler_view).adapter?.notifyDataSetChanged()
    }
}