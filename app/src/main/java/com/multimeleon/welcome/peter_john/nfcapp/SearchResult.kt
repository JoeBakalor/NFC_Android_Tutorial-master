package com.multimeleon.welcome.peter_john.nfcapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView


class SearchResult : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_result)

        var driverList = intent.extras.get("DriverList") as MutableList<List<String>>

        var dataModels: ArrayList<DataModel_Driver> = arrayListOf()

        for(driver in driverList)
            dataModels.add(DataModel_Driver(driver[0], driver[8], driver[2], driver[7], driver[9], driver[3], driver[4], driver[5], driver))

        // Create the adapter to convert the array to views
        val adapter = scrollViewAdapter(dataModels, this)

        val listView = findViewById<ListView>(R.id.searchList) as ListView
        listView.setAdapter(adapter)
        listView.emptyView = findViewById(R.id.list_empty)
    }
}
