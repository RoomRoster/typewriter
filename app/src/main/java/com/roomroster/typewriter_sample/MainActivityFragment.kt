package com.roomroster.typewriter_sample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
        btnTest.setOnClickListener {
            Toast.makeText(requireContext(), "TEST ${Math.random()}", Toast.LENGTH_LONG).show()
        }

        typeWriter.typingDelay = 200
        typeWriter.setLifecycleOwner(this)

        launch(coroutineContext) {
            Log.e("MAIN", "START")
            typeWriter.type("Hello darkness my old friend.")
            Log.e("MAIN", "STOP")
        }

//        Runnable {
//            Log.e("MAIN", "CANCELLING")
//            typeWriter?.cancel()
//        }.let {
//            Handler(Looper.getMainLooper()).postDelayed(it, 5000)
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
