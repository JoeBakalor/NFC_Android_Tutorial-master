package com.multimeleon.welcome.peter_john.nfcapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import android.R.raw
import com.opencsv.CSVReader
import java.io.InputStreamReader
import android.app.Activity
import android.content.Intent
import java.io.Serializable


class SearchActivity : AppCompatActivity() {

    var driverDictionary:MutableList<List<String>> = mutableListOf()
    var tempDictionary:MutableList<MutableList<String>> = mutableListOf()
    var divIndex:Int = 0
    var rdocIndex:Int = 0
    var rdopIndex:Int = 0
    var dcIndex1:Int = 0
    var dcIndex2:Int = 0

    var driverRanges:MutableList<List<String>> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        //get the spinner from the xml.
        val dropdown = findViewById<Spinner>(R.id.divSpinner) as Spinner
        //create an adapter to describe how the items are displayed, load the list of items from the string array resource
        val adapter = ArrayAdapter.createFromResource(this, R.array.div_Entries, R.layout.spinner_item)
        //set the spinners adapter to the previously created one.
        dropdown.adapter = adapter

        val dropdown2 = findViewById<Spinner>(R.id.dcSpinner) as Spinner
        //create an adapter to describe how the items are displayed, load the list of items from the string array resource
        val adapter2 = ArrayAdapter.createFromResource(this, R.array.dc_Entries, R.layout.spinner_item)
        //set the spinners adapter to the previously created one.
        dropdown2.adapter = adapter2

    }

    public fun searchClick(view: View) {

        try {
            // Get Driver Input Voltage Spinner value
            var mSpinnerDiv = findViewById<Spinner>(R.id.divSpinner) as Spinner

            var div = ""
            if(mSpinnerDiv.selectedItemPosition > 0)
                div = mSpinnerDiv.selectedItem.toString()

            // Get Rated Driver Output Current EditText value
            var mEditTextRdoc = findViewById<EditText>(R.id.rdocEditText) as EditText

            var rdoc = mEditTextRdoc.text.toString().toFloatOrNull()
            if(rdoc == null)
                rdoc = 0.0f

            if(rdoc == 0.0f) {  // This is a required field

                mEditTextRdoc.setError("Please enter the Output Current")
                return
            }
                // Get Rated Driver Output Power EditText value
            var mEditTextRdop = findViewById<EditText>(R.id.rdopEditText) as EditText

            var rdop = mEditTextRdop.text.toString().toFloatOrNull()
            if(rdop == null)
                rdop = 0.0f

            // Get Dimming Control Spinner value
            var mSpinnerDc = findViewById<Spinner>(R.id.dcSpinner) as Spinner

            var dc = ""
            if(mSpinnerDc.selectedItemPosition > 0)
                dc = mSpinnerDc.selectedItem.toString()

            readDriversCSV()

            var driverList = filterDrivers(div, rdoc, rdop, dc)

            val resultIntent = Intent()
            resultIntent.putExtra("DriverList", driverList as Serializable)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.ValidValueErrorMsg), Toast.LENGTH_SHORT).show()
        }
    }

    private fun readDriversCSV() {

        try {
            // If the file has already been read, nothing to do
            if(driverDictionary.count() > 0)
                return

            val reader = CSVReader(InputStreamReader(assets.open("drivers.csv")))

            var nextLine = reader.readNext()
            while (nextLine != null) {
                // nextLine[] is an array of values from the line
                if(nextLine.joinToString("").isNotBlank())  {
                    tempDictionary.add(nextLine.toMutableList())
                }
                nextLine = reader.readNext()
            }

            for(i in tempDictionary[0].indices) {
                if(tempDictionary[0][i].trim().toLowerCase() == "input_voltage")
                    divIndex = i
                if(tempDictionary[0][i].trim().toLowerCase() == "output_current")
                    rdocIndex = i
                if(tempDictionary[0][i].trim().toLowerCase() == "rated power")
                    rdopIndex = i
                if(tempDictionary[0][i].trim().toLowerCase() == "option_1")
                    dcIndex1 = i
                if(tempDictionary[0][i].trim().toLowerCase() == "option_2")
                    dcIndex2 = i
            }

            tempDictionary.removeAt(0)

            var driverMap = tempDictionary.groupBy { it[0] }

            for(driver in driverMap.keys) {
                var rows = driverMap.get(driver)

                var maxCurrent = rows!!.first()[rdocIndex]
                var minCurrent = rows!!.last()[rdocIndex]

                driverRanges.add(listOf(driver, maxCurrent, minCurrent))
            }

            for(driver in tempDictionary) {
                var catalogName = driver[0]

                var range = driverRanges.filter(fun(row) = row[0] == catalogName)

                driver += range[0][1]
                driver += range[0][2]

                driverDictionary.add(driver.toList())
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun filterDrivers(div: String, rdoc: Float, rdop: Float, dc: String): MutableList<List<String>> {
        var filteredDrivers:MutableList<List<String>> = driverDictionary

        try {


            // Filter by Driver Input Voltage
            if(div.isNotBlank()) {
                filteredDrivers = filteredDrivers.filter(fun(row) = row[divIndex].trim() == div ).toMutableList()
            }

            // Filter by Rated Driver Output Current
            if(rdoc > 0.0f) {
                filteredDrivers = filteredDrivers.filter(fun(row) = rdoc >= row[rdocIndex].trim().toFloat() && rdoc - row[rdocIndex].trim().toFloat() <= 19 ).toMutableList()
            }

            // Filter by Rated Driver Output Power
            if(rdop > 0.0f) {
                filteredDrivers = filteredDrivers.filter(fun(row) = row[rdopIndex].trim().toFloat() >= rdop ).toMutableList()
            }

            // Filter by Dimming Control
            if(dc.isNotBlank()) {
                filteredDrivers = filteredDrivers.filter(fun(row) = row[dcIndex1].trim() == dc || row[dcIndex2].trim() == dc).toMutableList()
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }

        filteredDrivers.sortBy { it[rdopIndex].trim().toFloat() }
        filteredDrivers = filteredDrivers.distinctBy { it[0] }.toMutableList()

        return filteredDrivers
    }
}
