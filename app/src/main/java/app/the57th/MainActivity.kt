package app.the57th

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.TextView
import app.the57th.core.FiftySeventh
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var today: TextView
    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker

    private val dayWatcher = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = render()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        today = findViewById(R.id.today)
        monthPicker = findViewById(R.id.month)
        yearPicker = findViewById(R.id.year)

        val anchor = AnchorStore.get(this)

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.displayedValues =
            Month.values().map { it.getDisplayName(TextStyle.FULL, Locale.ENGLISH) }.toTypedArray()
        monthPicker.value = anchor.monthValue

        yearPicker.minValue = AnchorStore.MIN_YEAR
        yearPicker.maxValue = AnchorStore.MAX_YEAR
        yearPicker.wrapSelectorWheel = false
        yearPicker.value = anchor.year

        val onChange = NumberPicker.OnValueChangeListener { _, _, _ ->
            AnchorStore.set(this, YearMonth.of(yearPicker.value, monthPicker.value))
            render()
            CountWidget.refreshAll(this)
        }
        monthPicker.setOnValueChangedListener(onChange)
        yearPicker.setOnValueChangedListener(onChange)
    }

    override fun onResume() {
        super.onResume()
        render()
        // TIME_TICK fires every minute while we're visible; cheap way to roll over at midnight.
        registerReceiver(dayWatcher, IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        })
    }

    override fun onPause() {
        unregisterReceiver(dayWatcher)
        super.onPause()
    }

    private fun render() {
        today.text = FiftySeventh.format(AnchorStore.get(this), LocalDate.now())
    }
}
