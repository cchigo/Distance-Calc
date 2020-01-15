package com.chigo.distancecalc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //sets start and stop button visibility
        start.setOnClickListener{
            it.visibility = View.GONE
            stop.visibility = View.VISIBLE

        }

        stop.setOnClickListener {
            start.visibility = View.VISIBLE
            it.visibility = View.GONE

            //distance.append(calcDistance().toString())
        }
    }
}
