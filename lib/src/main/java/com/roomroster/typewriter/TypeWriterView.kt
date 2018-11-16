package com.roomroster.typewriter

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class TypeWriterView : AppCompatTextView, LifecycleObserver {

    private var attrs = Attrs()

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        attributeSetId: Int
    ) : super(context, attrs, attributeSetId) {
        with(context.obtainStyledAttributes(attrs, R.styleable.TypeWriterView)) {
            Attrs(
                getInt(R.styleable.TypeWriterView_typingDelay, DEFAULT_TYPING_DELAY).toLong(),
                getInt(R.styleable.TypeWriterView_erasingDelay, DEFAULT_ERASE_DELAY).toLong()
            )
        }
    }

    private var job: Job? = null
    private var isTyping: Boolean = false
    private var textToType: String = ""

    fun setLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }

    fun setTypingDelay(delay: Long) {
        attrs = attrs.copy(typingDelay = delay)
    }

    fun setErasingDelay(delay: Long) {
        attrs = attrs.copy(eraseDelay = delay)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun cancel() {
        job?.cancel()
        job = null
    }

    private fun clearAndCancel() {
        isTyping = false
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
            delay(attrs.eraseDelay)
        }
    }

    private suspend fun startTyping(text: String, job: Job): String = withContext(job) {
        textToType = if (text == textToType && getCurrentText().isNotEmpty()) {
            text.replace(getCurrentText(), "")
        } else {
            clearText()
            text
        }

        // On start
        for (char in textToType) {
            append(char.toString())
            delay(attrs.typingDelay)
        }

        // On finished
        val result = this@TypeWriterView.text.toString()
        isTyping = false
        return@withContext result
    }

    private fun clearText() {
        text = ""
    }

    private fun getCurrentText() = this.text.toString()

    companion object {

        const val DEFAULT_TYPING_DELAY = 120
        const val DEFAULT_ERASE_DELAY = 30

        private const val BUNDLE_SUPER_STATE: String = "BUNDLE_SUPER_STATE"
        private const val BUNDLE_CURRENT_TEXT: String = "BUNDLE_CURRENT_TEXT"
        private const val BUNDLE_TOTAL_TEXT: String = "BUNDLE_TOTAL_TEXT"
    }

    data class Attrs(
        val typingDelay: Long = DEFAULT_TYPING_DELAY.toLong(),
        val eraseDelay: Long = DEFAULT_ERASE_DELAY.toLong()
    )
}