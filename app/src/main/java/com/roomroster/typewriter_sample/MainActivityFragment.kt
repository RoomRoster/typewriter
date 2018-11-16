package com.roomroster.typewriter_sample

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * A placeholder fragment containing a simple view.
 */
class MainActivityFragment : Fragment(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Main + job

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private fun setupViews() {
        typeWriter.setLifecycleOwner(this)

        btnDemo.setOnClickListener {
            launch(coroutineContext) { typeWriter.type("Hello darkness my old friend.") }
        }

        btnErase.setOnClickListener {
            launch(coroutineContext) { typeWriter.erase() }
        }

        btnEraseType.setOnClickListener {
            launch(coroutineContext) { typeWriter.eraseAndType("Brian give me back my pound.") }
        }

        btnType.setOnClickListener {
            if (txtDemo.text.isNotEmpty()) {
                launch(coroutineContext) { typeWriter.eraseAndType(txtDemo.text.toString()) }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
