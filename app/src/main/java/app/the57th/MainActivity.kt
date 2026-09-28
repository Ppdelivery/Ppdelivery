package app.the57th

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.RadioGroup
import android.widget.Switch
import android.widget.TextView
import app.the57th.core.FiftySeventh
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var today: TextView
    private lateinit var monthPicker: NumberPicker
    private lateinit var yearPicker: NumberPicker
    private lateinit var style: RadioGroup
    private lateinit var timely: Switch
    private lateinit var timelyNote: TextView

    private val dayWatcher = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) = render()
    }

    private val phoneLocale: Locale get() = resources.configuration.locales[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        today = findViewById(R.id.today)
        monthPicker = findViewById(R.id.month)
        yearPicker = findViewById(R.id.year)
        style = findViewById(R.id.style)
        timely = findViewById(R.id.timely)
        timelyNote = findViewById(R.id.timely_note)

        val anchor = AnchorStore.get(this)

        monthPicker.minValue = 1
        monthPicker.maxValue = 12
        monthPicker.value = anchor.monthValue

        yearPicker.minValue = AnchorStore.MIN_YEAR
        yearPicker.maxValue = AnchorStore.MAX_YEAR
        yearPicker.wrapSelectorWheel = false
        yearPicker.value = anchor.year

        val onChange = NumberPicker.OnValueChangeListener { _, _, _ ->
            AnchorStore.set(this, YearMonth.of(yearPicker.value, monthPicker.value))
            changed()
        }
        monthPicker.setOnValueChangedListener(onChange)
        yearPicker.setOnValueChangedListener(onChange)

        style.check(
            if (AnchorStore.style(this) == FiftySeventh.Style.CLASSIC) R.id.style_classic
            else R.id.style_phone
        )
        style.setOnCheckedChangeListener { _, id ->
            AnchorStore.setStyle(
                this,
                if (id == R.id.style_classic) FiftySeventh.Style.CLASSIC else FiftySeventh.Style.PHONE,
            )
            changed()
        }

        timely.isChecked = AnchorStore.timely(this)
        timely.setOnCheckedChangeListener { _, on ->
            AnchorStore.setTimely(this, on)
            changed()
        }

        syncSettingsUi()
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

    private fun changed() {
        syncSettingsUi()
        render()
        CountWidget.refreshAll(this)
    }

    /** Month names follow the chosen wording; timely wording only exists in English. */
    private fun syncSettingsUi() {
        val classic = AnchorStore.style(this) == FiftySeventh.Style.CLASSIC
        val locale = if (classic) Locale.ENGLISH else phoneLocale
        monthPicker.displayedValues = Month.values()
            .map { it.getDisplayName(TextStyle.FULL_STANDALONE, locale) }
            .toTypedArray()

        val english = classic || phoneLocale.language == "en"
        timely.isEnabled = english
        timelyNote.setText(if (english) R.string.timely_note else R.string.timely_note_english_only)
    }

    private fun render() {
        today.text = AnchorStore.todayText(this)
    }
}
