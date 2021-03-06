package com.roomroster.typewriter

import android.content.Context
import android.content.res.TypedArray
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.roomroster.typewriter.util.randomPlusOrMinusBetween
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class TypeWriterView constructor(
    context: Context,
    attrs: AttributeSet?,
    attributeSetId: Int
) : AppCompatTextView(context, attrs, attributeSetId), LifecycleObserver {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    var options = Attrs()
        private set

    init {
        options = genOptions(context.obtainStyledAttributes(attrs, R.styleable.TypeWriterView))
    }

    private fun genOptions(typedArray: TypedArray) = with(typedArray) {
        Attrs(
            getInt(R.styleable.TypeWriterView_typingDelay, DEFAULT_TYPING_DELAY).toLong(),
            getInt(R.styleable.TypeWriterView_erasingDelay, DEFAULT_ERASE_DELAY).toLong(),
            getInt(R.styleable.TypeWriterView_randomWobbleBase, DEFAULT_RANDOM_WOBBLE),
            getBoolean(
                R.styleable.TypeWriterView_enableRandomWobble, DEFAULT_ENABLE_RANDOM_WOBBLE
            )
        )
    }

    private var job: Job? = null
    private var textToType: String = ""

    fun setLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }

    fun setTypingDelay(delay: Long) {
        options = options.copy(typingDelay = delay)
    }

    fun setErasingDelay(delay: Long) {
        options = options.copy(eraseDelay = delay)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun cancel() {
        job?.cancel()
        job = null
    }

    private fun clearAndCancel() {
        textToType = ""
        cancel()
    }

    suspend fun type(text: String): String {
        if (job != null) clearAndCancel()

        job = Job()
        return startTyping(text, job!!)
    }

    suspend fun erase() {
        if (job != null) clearAndCancel()

        job = Job()
        return eraseText(job!!)
    }

    suspend fun eraseAndType(text: String): String {
        erase()
        return startTyping(text, job!!)
    }

    override fun onSaveInstanceState(): Parcelable = bundleOf(
        BUNDLE_SUPER_STATE to super.onSaveInstanceState(),
        BUNDLE_CURRENT_TEXT to text.toString(),
        BUNDLE_TOTAL_TEXT to textToType
    )

    override fun onRestoreInstanceState(state: Parcelable) {
        (state as? Bundle)?.let { bundle ->
            text = bundle.getString(BUNDLE_CURRENT_TEXT) ?: ""
            textToType = bundle.getString(BUNDLE_TOTAL_TEXT) ?: ""
            super.onRestoreInstanceState(bundle.getParcelable(BUNDLE_SUPER_STATE))
        }
    }

    private suspend fun eraseText(job: Job) = withContext(job) {
        while (text.isNotEmpty()) {
            text = text.toString().dropLast(1)
            delay(getDelay(Action.ERASE))
        }
    }

    private suspend fun startTyping(text: String, job: Job): String = withContext(job) {
        textToType = if (text == textToType && getCurrentText().isNotEmpty()) {
            text.replace(getCurrentText(), "")
        } else {
            clearText()
            text
        }

        for (char in textToType) {
            append(char.toString())
            delay(getDelay(Action.TYPE))
        }

        return@withContext this@TypeWriterView.text.toString()
    }

    private fun clearText() {
        text = ""
    }

    private fun getCurrentText() = this.text.toString()

    private fun getDelay(action: Action): Long {
        val startingDelay = when (action) {
            Action.ERASE -> options.eraseDelay
            Action.TYPE -> options.typingDelay
        }

        return if (!options.enableRandomWobble) startingDelay else
            startingDelay + randomPlusOrMinusBetween(options.randomWobble).toLong()
    }

    companion object {

        const val DEFAULT_TYPING_DELAY = 120
        const val DEFAULT_ERASE_DELAY = 30
        private const val DEFAULT_RANDOM_WOBBLE = 100
        private const val DEFAULT_ENABLE_RANDOM_WOBBLE = false

        enum class Action {
            ERASE,
            TYPE
        }

        private const val BUNDLE_SUPER_STATE: String = "BUNDLE_SUPER_STATE"
        private const val BUNDLE_CURRENT_TEXT: String = "BUNDLE_CURRENT_TEXT"
        private const val BUNDLE_TOTAL_TEXT: String = "BUNDLE_TOTAL_TEXT"
    }

    data class Attrs(
        val typingDelay: Long = DEFAULT_TYPING_DELAY.toLong(),
        val eraseDelay: Long = DEFAULT_ERASE_DELAY.toLong(),
        val randomWobble: Int = DEFAULT_RANDOM_WOBBLE,
        val enableRandomWobble: Boolean = DEFAULT_ENABLE_RANDOM_WOBBLE
    )
}