package com.pkndegwa.drawthisorthat

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import drawthisorthat.R
import drawthisorthat.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toolbar: Toolbar = binding.toolbar
        val upIcon: Drawable? = toolbar.navigationIcon
        upIcon?.setTint(resources.getColor(R.color.white, resources.newTheme()))

        val privacyPolicy = binding.privacyPolicyLink
        privacyPolicy.setOnClickListener {
            openPrivacyPolicyWebpage("https://www.privacypolicies.com/live/cdc3efd2-e3b8-4ecd-b7f4-a4a6f5bb6cf2")
        }
    }

    private fun openPrivacyPolicyWebpage(url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.application_not_found, Toast.LENGTH_SHORT).show()
        }
    }
}