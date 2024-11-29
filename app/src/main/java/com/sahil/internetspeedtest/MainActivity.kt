package com.sahil.internetspeedtest

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.sahil.internetspeedtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // Observing live data for speed updates
        viewModel.wifiSpeed.observe(this) { speed ->
            binding.tvWifiSpeed.text = getString(R.string.speed_format, speed)
        }
        viewModel.mobileSpeed.observe(this) { speed ->
            binding.tvMobileSpeed.text = getString(R.string.speed_format, speed)
        }

        // Start monitoring network speed
        viewModel.startMonitoring(this)

    }
}