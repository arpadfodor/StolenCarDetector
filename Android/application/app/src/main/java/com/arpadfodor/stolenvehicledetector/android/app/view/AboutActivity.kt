package com.arpadfodor.stolenvehicledetector.android.app.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.arpadfodor.stolenvehicledetector.android.app.R
import com.arpadfodor.stolenvehicledetector.android.app.view.utils.AppActivity
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.content_about.*

class AboutActivity : AppActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_about)
        val drawer = findViewById<DrawerLayout>(R.id.aboutActivityDrawerLayout)
        val navigation = findViewById<NavigationView>(R.id.about_navigation)
        initUi(drawer, navigation)

        fabMoreFromDeveloper.setOnClickListener {
            val developerPageUri = Uri.parse(getString(R.string.developer_page))
            val browserIntent = Intent(Intent.ACTION_VIEW, developerPageUri)
            startActivity(browserIntent)
        }

        fabReview.setOnClickListener {
            val storePageUri = Uri.parse(getString(R.string.store_page, packageName))
            val storeIntent = Intent(Intent.ACTION_VIEW, storePageUri)
            startActivity(storeIntent)
        }

        fabBugReport.setOnClickListener {
            val reportIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(
            "mailto",getString(R.string.maintenance_contact), null))
            reportIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.maintenance_message_title, packageName))
            startActivity(reportIntent)
        }

    }

    override fun subscribeToViewModel(){}
    override fun subscribeListeners(){}
    override fun unsubscribeListeners(){}

    override fun onBackPressed() {
        if(activityDrawerLayout.isDrawerOpen(GravityCompat.START)){
            activityDrawerLayout.closeDrawer(GravityCompat.START)
        }
        else{
            this.finish()
        }
    }

}