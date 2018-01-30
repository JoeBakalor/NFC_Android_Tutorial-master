package com.multimeleon.welcome.peter_john.nfcapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.find
import org.jetbrains.anko.toast
import java.math.BigDecimal
import android.view.MenuItem
import com.multimeleon.welcome.peter_john.nfcapp.ULTConfigurationOptions.standardMaxCurrent
import com.multimeleon.welcome.peter_john.nfcapp.ULTConfigurationOptions.standardMinCurrent
import java.io.Serializable


/**
 * Created by joebakalor on 11/7/17.
 */

class MainActivity : AppCompatActivity() {

    var SEARCH: Int = 1
    var SEARCHRESULT: Int = 2
    private var mNfcAdapter: NfcAdapter? = null
    var read = true

    //CONVIENECE
    var currentConfig = NFCUtil.ultConfigManager.pendingConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        ULTConfigurationOptions.setupOptions()
        setupUI()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.app_menu, menu)
        return true
    }

    override fun onResume() {
        super.onResume()
        println("onResume")
        mNfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }
    }

    override fun onPause() {
        println("onPause")
        super.onPause()
        mNfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        println("onNewIntent")
        super.onNewIntent(intent)

        if (read == true) {//READ CURRENT CONFIGURATION

            //val messageWrittenSuccessfully = NFCUtil.ULTWriteConfiguration(intent)
            val messageReadSuccessfully = NFCUtil.ULTReadConfiguration(intent, false)
            //LET USER KNOW IF THE DRIVER WAS CONFIGURED SUCCESSFULLY
            toast(messageReadSuccessfully.ifElse("Successful Read from Tag", "Tag Communication Interrupted"))
            updateUI()

        } else {//WRITE USER CONFIGURATION

            //FIRST POPULATE VALUES NOT CONFIGURED BY USER SO WE DONT OVERWRITE WITH BAD DATA
            val configReadSuccessfully = NFCUtil.ULTReadConfiguration(intent, true)
            //NOW WRITE THE CONFIGURATION
            if (configReadSuccessfully){
                val messageWrittenSuccessfully = NFCUtil.ULTWriteConfiguration(intent)//NFCUtil.createNFCMessage("FAKE MESSAGE", intent)
                //LET USER KNOW IF THE DRIVER WAS CONFIGURED SUCCESSFULLY
                toast(messageWrittenSuccessfully.ifElse("Successful Written to Tag", "Tag Communication Interrupted"))
            } else {
                toast("Tag Communication Interrupted")
            }

        }

    }

    fun updateUI() {

        outputCurrentSpinner.setSelection((NFCUtil.ultConfigManager.pendingConfiguration.outputCurrent.toInt() - MIN_OUTPUT_CURRENT))
        minDimCurrentSpinner.setSelection((NFCUtil.ultConfigManager.pendingConfiguration.minDimCurrent.toInt()) - MIN_DIM_CURRENT)
        fullBrightVoltageSpinner.setSelection((NFCUtil.ultConfigManager.pendingConfiguration.fullBrightControlVoltage.toInt()) - MIN_FULL_BRIGHT_VOLTAGE)
        minDimVoltageSpinner.setSelection((NFCUtil.ultConfigManager.pendingConfiguration.minDimControlVoltage.toInt()) - MIN_DIM_CONTROL_VOLTAGE)
        dimToOffVoltageSpinner.setSelection(NFCUtil.ultConfigManager.pendingConfiguration.dimToOffControlVoltage.toInt() -  MIN_DIM_TO_OFF_CONTROL_VOLTAGE)
        //TO DO: SET OTHER CONFIGURABLE PARAMTERS
    }


    //SETUP UI OBJECTS AND EVENT HANDLERS
    fun setupUI() {

        //  TO DO:  NEED TO ADD ANOTHER SPINNER TO ALLOW USER TO SELECT CORRECT DRIVER.
        //
        //  DEPENDING ON THE CONFIGURATION PARAMETERS AVAIALABLE FOR
        //  THE SELECTED DRIVER, DISABLE/ENABLE THE APPRPRIATE UI COMPONENTS,
        //  FOR EXAMPLE TO DISABLE USER ACCESS TO SETTING THE FULL BRIGHT CONTROL VOLTAGE, YOU CAN USE THE FOLLOWING CODE
        //
        //  fullBrightVoltageSpinner.isEnabled = false
        //
        //  This will disable and gray out the control on the user interface

        //SETUP SPINNER VALUES
        val outputCurrentSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ULTConfigurationOptions.outputPowerList)
        this.outputCurrentSpinner.adapter = outputCurrentSpinnerAdapter

        val minDimCurrentSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ULTConfigurationOptions.minDimCurrentList)
        this.minDimCurrentSpinner.adapter = minDimCurrentSpinnerAdapter

        val fullBrightVoltageSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ULTConfigurationOptions.fullBrightControlVoltageList)
        this.fullBrightVoltageSpinner.adapter = fullBrightVoltageSpinnerAdapter

        val minDimVoltageSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ULTConfigurationOptions.minDimControlVoltageList)
        this.minDimVoltageSpinner.adapter = minDimVoltageSpinnerAdapter

        val dimToOffVoltageSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ULTConfigurationOptions.dimToOffControlVoltageList)
        this.dimToOffVoltageSpinner.adapter = dimToOffVoltageSpinnerAdapter

        val minDimCurrentPctSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ULTConfigurationOptions.minDimCurrentPctList)
        this.minDimCurrentPctSpinner.adapter = minDimCurrentPctSpinnerAdapter

        val dimCurveLogSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ULTConfigurationOptions.dimCurveLogarithmicList)
        this.dimCurveSpinner.adapter = dimCurveLogSpinnerAdapter


        //SET SLIDER MAX VALUES. MAX VALUE IS EQUAL TO THE MAX NUMBER IN THE RANGE
        outputCurrentSlider.max = MAX_OUTPUT_CURRENT - MIN_OUTPUT_CURRENT//standardMaxCurrent - standardMinCurrent
        minDimCurrentSlider.max = MAX_DIM_CURRENT - MIN_DIM_CURRENT//253
        fullBrightVoltageSlider.max = MAX_FULL_BRIGHT_VOLTAGE - MIN_FULL_BRIGHT_VOLTAGE//20//90-70
        minDimVoltageSlider.max = MAX_DIM_CONTROL_VOLTAGE - MIN_DIM_CONTROL_VOLTAGE//30
        dimToOffVoltageSlider.max = MAX_DIM_TO_OFF_CONTROL_VOLTAGE - MIN_DIM_TO_OFF_CONTROL_VOLTAGE//17

        //SET DEFAULT BUTTON STATE TO WRITE
        writeToggleButton.callOnClick()
        read = false

        //SET HANDLER FOR WRITE TOGGLE BUTTON
        this.writeToggleButton.setOnClickListener(View.OnClickListener()  { view ->
            println("write button is selected, de-select read")
            read = false
            view.background = getDrawable(R.color.colorPrimary)
            readToggleButton.background = getDrawable(R.color.button_material_light)
        })

        //SET HANDLER FOR READ TOGGLE BUTTON
        this.readToggleButton.setOnClickListener(View.OnClickListener()  { view ->
            println("write button not selected")
            read = true
            view.background = getDrawable(R.color.colorPrimary)
            writeToggleButton.background = getDrawable(R.color.button_material_light)
        })

        //LINEAR DIMMING CURVE RADIO BUTTON
        dimCurveLinearBtn.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                println("Dim Curve Linear Button Set")
                //RADIO BUTTONS ARE MUTUALLY EXCLUSIVE, SET APPROPRIATELY
                dimCurveSftStrtBtn.isChecked = false
                dimCurveLogBtn.isChecked = false
                dimCurveSpinner.isEnabled = false

            }
        }

        //SOFT START DIMMING CURVE RADIO BUTTON
        dimCurveSftStrtBtn.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                println("Dim Curve Soft Start Button Set")
                //RADIO BUTTONS ARE MUTUALLY EXCLUSIVE, SET APPROPRIATELY
                dimCurveLinearBtn.isChecked = false
                dimCurveLogBtn.isChecked = false
                dimCurveSpinner.isEnabled = false
            }
        }

        //LOGARITHMIC DIMMING CURVE RADIO BUTTON
        dimCurveLogBtn.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                println("Dim Curve Log Button Set")
                //RADIO BUTTONS ARE MUTUALLY EXCLUSIVE, SET APPROPRIATELY
                dimCurveSftStrtBtn.isChecked = false
                dimCurveLinearBtn.isChecked = false
                dimCurveSpinner.isEnabled = true
            }
        }

        //DEBUG PRINT CONFIG STATUS
        fun printConfig() {
            NFCUtil.ultConfigManager.printPendingConfig()
        }

        //==================  OUTPUT CURRENT: SLIDER AND SPINNER EVENT HANDLERS

        var leavingOutputCurrentSlider = false
        var leavingOutputCurrentSpinner = false

        //OUTPUT CURRENT SLIDER
        this.outputCurrentSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                leavingOutputCurrentSlider = true
                if (leavingOutputCurrentSpinner == false) {
                    outputCurrentSpinner.setSelection(progress)
                } else {
                    leavingOutputCurrentSpinner = false
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        //OUPUT CURRENT SPINNER
        this.outputCurrentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                leavingOutputCurrentSpinner = true
                outputCurrentSlider.setProgress(position, true)
                leavingOutputCurrentSpinner = false

                //SET VALUE IN TAG MEM MAP
                NFCUtil.ultConfigManager.pendingConfiguration.outputCurrent = (ULTConfigurationOptions.outputPowerOptionSet[position]).toShort()
                printConfig()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        //========================  MIN DIM CURRENT: SLIDER AND SPINNER EVENT HANDLERS

        var leavingMinDimCurrentSlider = false
        var leavingMinDimCurrentSpinner = false

        //MIN DIM CURRENT SLIDER
        this.minDimCurrentSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //GET PERCENT VALUE OF TOTAL
                var percentSpinner = ((((progress).toDouble()) / ((ULTConfigurationOptions.minDimCurrentOptionSet.last() - ULTConfigurationOptions.minDimCurrentOptionSet.first())).toDouble()) * 100).toInt()

                if (percentSpinner > 99) {
                    percentSpinner = 100
                }

                //SET APPROPRIATE SPINNER VALUE BASED ON SLIDER POSITION
                //NEW CONFIGURATION IS WRITTEN TO ULTPendingConfiguration IN SPINNER LISTENER,
                //WHICH IS CALLED AS A RESULT OF SETTING THE SPINNER SELECTION HERE
                leavingMinDimCurrentSlider = true
                minDimCurrentSpinner.setSelection(progress)
                minDimCurrentPctSpinner.setSelection(percentSpinner)
                leavingMinDimCurrentSpinner = false
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        //MIN DIM CURRENT SPINNER - mA SELECTION
        this.minDimCurrentSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                leavingMinDimCurrentSpinner = true
                if (leavingMinDimCurrentSlider == false) {
                    minDimCurrentSlider.setProgress(position, true)
                } else {
                    leavingMinDimCurrentSlider = false
                }

                NFCUtil.ultConfigManager.pendingConfiguration.minDimCurrent = (ULTConfigurationOptions.minDimCurrentOptionSet[position]).toShort()
                printConfig()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        //MIN DIM CURRENT SPINNER - PERCENT SELECTION
        this.minDimCurrentPctSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                leavingMinDimCurrentSpinner = true
                if (leavingMinDimCurrentSlider == false) {
                    minDimCurrentSlider.setProgress(position, true)
                } else {
                    leavingMinDimCurrentSlider = false
                }
                //SET VALUE IN TAG MEM MAP
                NFCUtil.ultConfigManager.pendingConfiguration.minDimCurrent = (position).toShort()//finalSetting.toShort()

                //PRINT CONFIG FOR DEBUGGING
                printConfig()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        //====================  FULL BRIGHT VOLTAGE: SLIDER AND SPINNER EVENT HANDLERS

        var leavingFullBrightVoltageSlider = false
        var leavingFullBrightVoltageSpinner = false

        //FULL BRIGHT VOLTAGE SLIDER
        this.fullBrightVoltageSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                leavingFullBrightVoltageSlider = true
                if (leavingFullBrightVoltageSpinner == false) {
                    fullBrightVoltageSpinner.setSelection(progress)
                } else {
                    leavingFullBrightVoltageSpinner = false
                }
                //SET APPROPRIATE SPINNER VALUE BASED ON SLIDER POSITION

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        //FULL BRIGHT VOLTAGE SPINNER
        this.fullBrightVoltageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                leavingFullBrightVoltageSpinner = true
                fullBrightVoltageSlider.setProgress(position, true)
                leavingFullBrightVoltageSlider = false

                //SET VALUE IN TAG MEM MAP
                NFCUtil.ultConfigManager.pendingConfiguration.fullBrightControlVoltage = (((ULTConfigurationOptions.fullBrightVoltageOptionSet[position]).toDouble() / 10) * 1000).toShort()

                //PRINT CONFIG FOR DEBUGGING
                printConfig()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        //============MIN DIM VOLTAGE
        //MIN DIM VOLTAGE SLIDER
        var leavingMinDimVoltageSlider = false
        var leavingMinDimVoltageSpinner = false

        this.minDimVoltageSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                //SET APPROPRIATE SPINNER VALUE BASED ON SLIDER POSITION
                leavingMinDimVoltageSlider = true
                if (leavingMinDimVoltageSpinner == false) {
                    minDimVoltageSpinner.setSelection(progress)
                } else {
                    leavingMinDimVoltageSpinner = false
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        //MIN DIM VOLTAGE SPINNER
        this.minDimVoltageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //SET VALUE IN TAG MEM MAP
                leavingMinDimVoltageSpinner = true
                minDimVoltageSlider.setProgress(position, true)
                leavingMinDimVoltageSlider = false

                NFCUtil.ultConfigManager.pendingConfiguration.minDimControlVoltage = ((ULTConfigurationOptions.minDimVoltageOptionSet[position].toDouble() / 10) * 1000).toShort()
                printConfig()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        //====================DIM TO OFF

        //DIM TO OFF VOLTAGE SLIDER

        var leavingDimToOffVoltageSlider = false
        var leavingDimToOffVoltageSpinner = false

        this.dimToOffVoltageSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                leavingDimToOffVoltageSlider = true
                if (leavingDimToOffVoltageSpinner == false) {
                    //SET APPROPRIATE SPINNER VALUE BASED ON SLIDER POSITION
                    dimToOffVoltageSpinner.setSelection(progress)
                } else {
                    leavingDimToOffVoltageSpinner = false
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        //DIM TO OFF VOLTAGE SPINNER
        this.dimToOffVoltageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                leavingDimToOffVoltageSpinner = true
                dimToOffVoltageSlider.setProgress(position, true)
                leavingDimToOffVoltageSlider = false

                //SET VALUE IN TAG MEM MAP
                NFCUtil.ultConfigManager.pendingConfiguration.dimToOffControlVoltage = ((ULTConfigurationOptions.dimToOffVoltageOptionset[position].toDouble() / 10) * 1000).toShort()
                printConfig()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> {

                val intent1 = Intent(this, SearchActivity::class.java)
                this.startActivityForResult(intent1, SEARCH)
                return true;
            }

            else -> {
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        try {
            super.onActivityResult(requestCode, resultCode, intent)
            if(resultCode == Activity.RESULT_OK) {

                when(requestCode)
                {
                    SEARCH -> {
                        val extras = intent!!.extras
                        if (extras != null) {
                            var driverList = extras.get("DriverList") as MutableList<List<String>>

                            val searchResultIntent = Intent(this, SearchResult::class.java)
                            searchResultIntent.putExtra("DriverList", driverList as Serializable)
                            this.startActivityForResult(searchResultIntent, SEARCHRESULT)
                        }
                    }
                    SEARCHRESULT -> {
                        val extras = intent!!.extras
                        if (extras != null) {
                            var selectedDriver = extras.get("DriverList") as List<String>
                            standardMaxCurrent = selectedDriver[selectedDriver.count() - 2].toInt()
                            standardMinCurrent = selectedDriver[selectedDriver.count() - 1].toInt()
                            var outputCurrent = selectedDriver[2].toInt()

                            ULTConfigurationOptions.outputPowerList.clear()
                            ULTConfigurationOptions.outputPowerOptionSet.clear()

                            var i: Int = standardMinCurrent
                            while (i <= standardMaxCurrent){
                                ULTConfigurationOptions.outputPowerList.add("$i mA")
                                ULTConfigurationOptions.outputPowerOptionSet.add(i)
                                i += 1
                            }

                            val outputCurrentSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, ULTConfigurationOptions.outputPowerList)
                            this.outputCurrentSpinner.adapter = outputCurrentSpinnerAdapter

                            outputCurrentSlider.max = standardMaxCurrent - standardMinCurrent
                            outputCurrentSpinner.setSelection(outputCurrent - standardMinCurrent)
                            outputCurrentSlider.setProgress(((outputCurrent - standardMinCurrent)*(standardMaxCurrent - standardMinCurrent)) / (standardMaxCurrent - standardMinCurrent))

                            NFCUtil.ultConfigManager.pendingConfiguration.outputCurrent = outputCurrent.toShort()

                            minDimCurrentSpinner.setSelection(0)
                            minDimCurrentSlider.setProgress(0)
                            NFCUtil.ultConfigManager.pendingConfiguration.minDimCurrent = (ULTConfigurationOptions.minDimCurrentOptionSet[0]).toShort()

                            dimCurveLinearBtn.isChecked = false
                            dimCurveSftStrtBtn.isChecked = false
                            dimCurveLogBtn.isChecked = false
                            dimCurveSpinner.setSelection(0)
                            dimCurveSpinner.isEnabled = false

                            fullBrightVoltageSpinner.setSelection(10)
                            fullBrightVoltageSlider.setProgress(10)
                            NFCUtil.ultConfigManager.pendingConfiguration.fullBrightControlVoltage = (((ULTConfigurationOptions.fullBrightVoltageOptionSet[10]).toDouble() / 10) * 1000).toShort()

                            minDimVoltageSpinner.setSelection(10)
                            minDimVoltageSlider.setProgress(10)
                            NFCUtil.ultConfigManager.pendingConfiguration.minDimControlVoltage = ((ULTConfigurationOptions.minDimVoltageOptionSet[10].toDouble() / 10) * 1000).toShort()

                            dimToOffVoltageSpinner.setSelection(0)
                            dimToOffVoltageSlider.setProgress(0)
                            NFCUtil.ultConfigManager.pendingConfiguration.dimToOffControlVoltage = ((ULTConfigurationOptions.dimToOffVoltageOptionset[0].toDouble() / 10) * 1000).toShort()
                        }
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    public fun advancedClick(view: View) {
        // Remove the Advanced button
        findViewById<TextView>(R.id.advancedButton).visibility = View.GONE

        // Show Dimming Curve
        findViewById<TextView>(R.id.dimmingCurveLabel).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.dimmingCurveLayout).visibility = View.VISIBLE

        // Show Full Bright Control Voltage
        findViewById<TextView>(R.id.fbcvLabel).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.fbcvLayout).visibility = View.VISIBLE

        // Show Min Dim Control Voltage
        findViewById<TextView>(R.id.mdcvLabel).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.mdcvLayout).visibility = View.VISIBLE

        // Show Dim-to-Off Control Voltage
        findViewById<TextView>(R.id.dtocvLabel).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.dtocvLayout).visibility = View.VISIBLE
    }
}

object ULTConfigurationOptions{

    //STRING LIST USED FOR DISPLAY
    //NUMERIC LIST USED FOR ACTUAL VALUE STORED
    val outputPowerList = ArrayList<String>()
    var outputPowerOptionSet = ArrayList<Int>()

    val minDimCurrentList = ArrayList<String>()
    var minDimCurrentOptionSet = ArrayList<Int>()

    val fullBrightControlVoltageList = ArrayList<String>()
    var fullBrightVoltageOptionSet = ArrayList<Int>()

    val minDimControlVoltageList = ArrayList<String>()
    var minDimVoltageOptionSet = ArrayList<Int>()

    val dimToOffControlVoltageList = ArrayList<String>()
    var dimToOffVoltageOptionset = ArrayList<Int>()

    val minDimCurrentPctList = ArrayList<String>()

    val dimCurveLogarithmicList = ArrayList<String>()
    var dimCurveLogarithmicOptionSet = ArrayList<Int>()

    var standardMinCurrent = 315
    var standardMaxCurrent = 1050

    //SET OPTION RANGES
    fun setupOptions(){

        //315mA <= OUTPUT CURRENT <=1050mA
        var i: Int = MIN_OUTPUT_CURRENT
        while (i <= MAX_OUTPUT_CURRENT){
            outputPowerList.add("$i mA")
            outputPowerOptionSet.add(i)
            i += 1
        }

        //10mA <= MIN DIM CURRENT <= 263mA
        i = MIN_DIM_CURRENT
        while (i <= MAX_DIM_CURRENT){
            minDimCurrentList.add("$i mA")
            minDimCurrentOptionSet.add(i)
            i += 1
        }

        //LOG CURVE OPTONS
        i = 0
        while (i <= 10){
            dimCurveLogarithmicList.add("$i")
            dimCurveLogarithmicOptionSet.add(i)
            i += 1
        }

        //7V <= FULL BRIGHT VOLTAGE <= 9V
        //USING INTEGER VALUES BECAUSE ANDROID SLIDER ONLY ALLOWS INT, DIVIDE BY 10 BEFORE SET TAG MEM
        i = MIN_FULL_BRIGHT_VOLTAGE
        while (i <= MAX_FULL_BRIGHT_VOLTAGE){
            fullBrightControlVoltageList.add("${Math.round((i.toDouble()/10) * 100.00)/100.00} V")
            fullBrightVoltageOptionSet.add(i)
            i += 1
        }

        //0V <= MIN DIM CONTROL VOLTAGE <= 3V
        //USING INTEGER VALUES BECAUSE ANDROID SLIDER ONLY ALLOWS INT, DIVIDE BY 10 BEFORE SET TAG MEM
        i = MIN_DIM_CONTROL_VOLTAGE
        while (i <= MAX_DIM_CONTROL_VOLTAGE){
            minDimControlVoltageList.add("${Math.round((i.toDouble()/10) * 100.00)/100.00} V")
            minDimVoltageOptionSet.add(i)
            i += 1
        }

        //0V <= DIM TO OFF CONTROL VOLTAGE <= 1.7V
        //USING INTEGER VALUES BECAUSE ANDROID SLIDER ONLY ALLOWS INT, DIVIDE BY 10 BEFORE SET TAG MEM
        i = MIN_DIM_TO_OFF_CONTROL_VOLTAGE
        while (i <= MAX_DIM_TO_OFF_CONTROL_VOLTAGE){
            dimToOffControlVoltageList.add("${Math.round((i.toDouble()/10) * 100.00)/100.00} V")
            dimToOffVoltageOptionset.add(i)
            i += 1
        }

        //0->100%
        i = 0
        while (i <= 100){
            minDimCurrentPctList.add("$i %")
            i += 1
        }
    }
}

fun <T> Boolean.ifElse(primaryResult: T, secondaryResult: T) = if (this) primaryResult else secondaryResult