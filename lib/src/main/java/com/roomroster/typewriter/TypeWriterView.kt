package com.roomroster.typewriter

import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TypeWriterView : AppCompatTextView, LifecycleObserver, CoroutineScope {

    var typingDelay: Int = DEFAULT_TYPING_DELAY

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(
        context: Context,
        attrs: AttributeSet?,
        attributeSetId: Int
    ) : super(context, attrs, attributeSetId) {
        with(context.obtainStyledAttributes(attrs, R.styleable.TypeWriterView)) {
            typingDelay = getInt(R.styleable.TypeWriterView_typingDelay, DEFAULT_TYPING_DELAY)
        }
    }

    private var job = Job()

    override val coroutineContext = Dispatchers.Main + job

    private var isTyping: Boolean = false
    private var textToType: String = ""

    fun setLifecycleOwner(owner: LifecycleOwner) {
        owner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun cancel() {
        Log.e("TEST", "I AM STOPPING")
        job.cancel()
    }

    suspend fun type(text: String): String = withContext(coroutineContext) { startTyping(text) }

    fun start(text: String) = launch(coroutineContext) { startTyping(text) }

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

    private suspend fun startTyping(text: String): String = withContext(coroutineContext) {
        textToType = if (text == textToType && getCurrentText().isNotEmpty()) {
            text.replace(getCurrentText(), "")
        } else {
            clearText()
            text
        }

        // On start
        for (char in textToType) {
            append(char.toString())
            delay(typingDelay.toLong())
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

        const val DEFAULT_TYPING_DELAY = 150

        private const val BUNDLE_SUPER_STATE: String = "BUNDLE_SUPER_STATE"
        private const val BUNDLE_CURRENT_TEXT: String = "BUNDLE_CURRENT_TEXT"
        private const val BUNDLE_TOTAL_TEXT: String = "BUNDLE_TOTAL_TEXT"
    }
}