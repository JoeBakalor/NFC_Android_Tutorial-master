package com.multimeleon.welcome.peter_john.nfcapp

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import java.io.IOException
import kotlin.experimental.and

/**
 * Created by joebakalor on 11/7/17.
 */

var MIN_OUTPUT_CURRENT = 100 //mA
var MAX_OUTPUT_CURRENT = 1500 //mA

var MIN_DIM_CURRENT = 10  //mA
var MAX_DIM_CURRENT = 263 //mA

var MIN_DIM_CONTROL_VOLTAGE = 10 //VOLTS * 10
var MAX_DIM_CONTROL_VOLTAGE = 30 //VOLTS * 10

var MIN_FULL_BRIGHT_VOLTAGE = 60 //VOLTS * 10
var MAX_FULL_BRIGHT_VOLTAGE = 90 //VOLTS * 10

var MIN_DIM_TO_OFF_CONTROL_VOLTAGE = 0 //VOLTS * 10
var MAX_DIM_TO_OFF_CONTROL_VOLTAGE = 17 //VOLTS * 10

class ULTConfigurationManager()
{

    data class ULTConfiguration(var outputCurrent: Short,
                                var minDimCurrent: Short,
                                var dimmingCurve: Short,
                                var fullBrightControlVoltage: Short,
                                var minDimControlVoltage: Short,
                                var dimToOffControlVoltage: Short)


    //NOT USED CURRENTLY
    enum class configParamters{
        OUTPUT_CURRENT,
        MIN_DIM_CURRENT,
        DIMMING_CURVE,
        FULL_BRIGHT_CONTROL_VOLTAGE,
        MIN_DIM_CONTROL_VOLTAGE,
        DIM_TO_OFF_CONTROL_VOLTAGE
    }

    enum class DriverModels{

    }

    //STORE FORMATTED STATIC CONFIGURATION READ FROM DRIVER
    private var staticDataConfiguration: ArrayList<ByteArray> = arrayListOf(
            byteArrayOf(0x00, 0x00, 0x00, 0x00),//28
            byteArrayOf(0x00, 0x00, 0x00, 0x00),//29
            byteArrayOf(0x00, 0x00, 0x00, 0x00),//2A
            byteArrayOf(0x00, 0x00, 0x00, 0x00),//2B

            byteArrayOf(0x00, 0x00, 0x00, 0x00),//2C
            byteArrayOf(0x00, 0x00, 0x00, 0x00),//2D EMPTY
            byteArrayOf(0x00, 0x00, 0x00, 0x00),//2E EMPTY
            byteArrayOf(0x00, 0x00, 0x00, 0x00))//2F EMPTY

    //STORE PENDING MDC WRITE TUNING DATA COMMAND
    var MDCPendingCommand: ArrayList<ByteArray> = arrayListOf(
            byteArrayOf(0x27, 0x05, 0x00, 0x00),
            byteArrayOf(0x00, 0x00, 0x00, 0x00),
            byteArrayOf(0x00, 0x00, 0x00, 0x00),
            byteArrayOf(0x00, 0x00, 0x00, 0x00),

            byteArrayOf(0x00, 0x00, 0x00, 0x00),
            byteArrayOf(0x00, 0x00, 0x00, 0x00),
            byteArrayOf(0x00, 0x00, 0x00, 0x00),
            byteArrayOf(0x00, 0x00, 0x00, 0x00),

            byteArrayOf(0x00, 0x00, 0x00, 0x00),
            byteArrayOf(0x00, 0x00, 0x00, 0x00)
    )


    //CONFIGURATION READ FROM DRVIER
    var currentConfiguration = ULTConfiguration(0,0,0,0,0,0)
    //USER CONFIGURATION
    var pendingConfiguration = ULTConfiguration(0,0,0,0,0,0)


    //PRINT CURRENT USER CONFIGURATION TO CONSOLE
    fun printPendingConfig(){
        println("Pending Configuration = $pendingConfiguration")
        //ULTUpdateStaticConfiguration()
    }

    //GET STATIC CONFIGURATION MEMORY BLOCKS TO WRITE TO DRIVER
    fun ULTGetStaticConfigurationBlocks(): ArrayList<ByteArray>{
        return staticDataConfiguration
    }

    //PARSE STATIC CONFIGURATION BYTES READ FROM DRIVER ==> LOOKS LIKE THIS ISN'T NEEDED
    //BUT LEFT IN CASE NEEDED IN THE FUTURE
    fun ULTParseStaticConfigurationBytes(staticConfigBytes: ByteArray){
        var i = 0
        do {
            for (j in 0..(staticDataConfiguration.count() - 1)){
                for (k in 0..3){
                    staticDataConfiguration[j][k] = staticConfigBytes[i]
                    i++
                }
            }
        } while (i < staticConfigBytes.count())
    }

    //PARSE TUNING DATA BYTE ARRAY READ FROM DRIVER
    fun ULTParsTuningDataBytes(tuningDataBytes: ByteArray, preWrite: Boolean){

        //COPY DATA READ FROM TUNING DATA BLOCK TO SET AREAS,THE USER WONT
        //CONFIGURE
        var i = 0
        MDCPendingCommand[1][0] = tuningDataBytes[i]; i++
        MDCPendingCommand[1][1] = tuningDataBytes[i]; i++
        MDCPendingCommand[1][2] = tuningDataBytes[i]; i++
        MDCPendingCommand[1][3] = 160.toByte(); i++//PHONE IS READING OUT 0XA4 FOR SOME REASON.  MANUALLY SETTING 0XA0

        MDCPendingCommand[2][0] = tuningDataBytes[i]; i++
        MDCPendingCommand[2][1] = tuningDataBytes[i]; i++
        MDCPendingCommand[2][2] = tuningDataBytes[i]; i++
        MDCPendingCommand[2][3] = tuningDataBytes[i]; i++

        val outputCurrent = (Math.round((((((((MDCPendingCommand[2][1]).toInt()).and(255)).shl(8)).or((MDCPendingCommand[2][0]).toInt() and 255)).toDouble()/65535)* MAX_OUTPUT_CURRENT))).toInt()
        val minDimCurrent = (Math.round((((((((MDCPendingCommand[2][3]).toInt()).and(255)).shl(8)).or((MDCPendingCommand[2][2]).toInt() and 255)).toDouble()/65535)* MAX_DIM_CURRENT))).toInt()



        MDCPendingCommand[3][0] = tuningDataBytes[i]; i++
        MDCPendingCommand[3][1] = tuningDataBytes[i]; i++
        MDCPendingCommand[3][2] = tuningDataBytes[i]; i++
        MDCPendingCommand[3][3] = tuningDataBytes[i]; i++

        val brightVoltage = ((((((MDCPendingCommand[3][1]).toInt()).and(255)).shl(8)).or((MDCPendingCommand[3][0]).toInt() and 255)).toDouble()/100).toInt()//.toDouble()/65535)*1050))).toInt()
        val dimVoltage = ((((((MDCPendingCommand[3][3]).toInt()).and(255)).shl(8)).or((MDCPendingCommand[3][2]).toInt() and 255)).toDouble()/100).toInt()


        MDCPendingCommand[4][0] = tuningDataBytes[i]; i++
        MDCPendingCommand[4][1] = tuningDataBytes[i]; i++
        MDCPendingCommand[4][2] = tuningDataBytes[i]; i++
        MDCPendingCommand[4][3] = tuningDataBytes[i]; i++

        val dimToOffVoltage = ((((((MDCPendingCommand[4][1]).toInt()).and(255)).shl(8)).or((MDCPendingCommand[4][0]).toInt() and 255)).toDouble()/100).toInt()

        //println("Bright Voltage LSB = $")
        if (preWrite == false){

            pendingConfiguration.minDimCurrent = minDimCurrent.toShort()
            pendingConfiguration.outputCurrent = outputCurrent.toShort()
            pendingConfiguration.fullBrightControlVoltage = brightVoltage.toShort()
            pendingConfiguration.minDimControlVoltage = dimVoltage.toShort()
            pendingConfiguration.dimToOffControlVoltage = dimToOffVoltage.toShort()
            println("READ OUTPUT CURRENT = $outputCurrent, READ MIN DIM CURRENT = $minDimCurrent, READ BRIGHT VOLTAGE = $brightVoltage")
        }


        MDCPendingCommand[5][0] = tuningDataBytes[i]; i++
        MDCPendingCommand[5][1] = tuningDataBytes[i]; i++
        MDCPendingCommand[5][2] = tuningDataBytes[i]; i++
        MDCPendingCommand[5][3] = tuningDataBytes[i]; i++

        MDCPendingCommand[6][0] = tuningDataBytes[i]; i++
        MDCPendingCommand[6][1] = tuningDataBytes[i]; i++
        MDCPendingCommand[6][2] = tuningDataBytes[i]; i++
        MDCPendingCommand[6][3] = tuningDataBytes[i]; i++

        MDCPendingCommand[7][0] = tuningDataBytes[i]; i++
        MDCPendingCommand[7][1] = tuningDataBytes[i]; i++
        MDCPendingCommand[7][2] = tuningDataBytes[i]; i++
        MDCPendingCommand[7][3] = tuningDataBytes[i]; i++

        MDCPendingCommand[8][0] = tuningDataBytes[i]; i++
        MDCPendingCommand[8][1] = tuningDataBytes[i]; i++
        MDCPendingCommand[8][2] = tuningDataBytes[i]; i++
        MDCPendingCommand[8][3] = tuningDataBytes[i]; i++

        MDCPendingCommand[9][0] = tuningDataBytes[i]; i++
        MDCPendingCommand[9][1] = tuningDataBytes[i];


        //MDCPendingCommand[9][2] = tuningDataBytes[i]; i++
        for (i in 0..(MDCPendingCommand.count() - 1)){
            println("MDC Pending Command: ${MDCPendingCommand[i].toHex()}")
        }

    }

    //CREATE MDC PROTOCOL PACKET TO CONFIGURE TUNING DATA
    fun ULTCreateMDCProtocolPacket(): ArrayList<ByteArray>{

        //SET OUTPUT CURRENT IN MDC COMMAND PACKET
        val mdcTrimBaseVal =  ((pendingConfiguration.outputCurrent.toDouble()/ MAX_OUTPUT_CURRENT) * 65535).toShort()
        MDCPendingCommand[2][0] = mdcTrimBaseVal.and(0xff).toByte()
        MDCPendingCommand[2][1] = ((((mdcTrimBaseVal).and(0xff00.toShort())).toInt()).shr(8)).toByte()

        //SET DIM CURRENT
        val mdcTrimFloor = ((pendingConfiguration.minDimCurrent.toDouble()/ MAX_DIM_CURRENT) * 65535).toShort()
        MDCPendingCommand[2][2] = mdcTrimFloor.and(0xff).toByte()
        MDCPendingCommand[2][3] = ((((mdcTrimFloor).and(0xff00.toShort())).toInt()).shr(8)).toByte()

        //SET BRIGHT VOLTAGE
        val mdcBrightVoltage = pendingConfiguration.fullBrightControlVoltage.toShort()
        MDCPendingCommand[3][0] = mdcBrightVoltage.and(0xff).toByte()
        MDCPendingCommand[3][1] = ((((mdcBrightVoltage).and(0xff00.toShort())).toInt()).shr(8)).toByte()

        //SET DIM VOLTAGE
        val mdcDimVoltage = pendingConfiguration.minDimControlVoltage.toShort()
        MDCPendingCommand[3][2] = mdcDimVoltage.and(0xff).toByte()
        MDCPendingCommand[3][3] = ((((mdcDimVoltage).and(0xff00.toShort())).toInt()).shr(8)).toByte()

        //SET DIM TO OFF VOLTAGE
        val mdcDimToOffVoltage = pendingConfiguration.dimToOffControlVoltage.toShort()
        MDCPendingCommand[4][0] = mdcDimToOffVoltage.and(0xff).toByte()
        MDCPendingCommand[4][1] = ((((mdcDimToOffVoltage).and(0xff00.toShort())).toInt()).shr(8)).toByte()

        //TO DO:  POPULATE ADDITIONAL CONFIGURATION PARAMETERS FOR DRIVERS THAT SUPPORT THEM,
        //RECOMMEND TO USE A CASE WITH FALL THROUGH TO REDUCE OVERLAP OF SETTING PARAMETERS THAT
        //ARE AVAILABLE ON MULTIPLE DRIVER MODELS

        //SET ADDITIONAL COMMAND BYTES DEPENDING ON WHICH DRIVER. DRIVER USED FOR WRITING APP ONLY HAD OUTPUT
        //CURRENT AND DIM CURRENT AVAILABLE TO BE CONFIGURED

        //POPULATE CHECKSUM INTO COMMAND PACKET
        MDCPendingCommand[9][2] = ULTCalculateCheckSum()

        //DEBUG: PRINT FINAL MDC COMMAND TO CONSOLE FOR DEBUGGING
        for (i in 0..(MDCPendingCommand.count() - 1)){
            println("MDC Pending Command: ${MDCPendingCommand[i].toHex()}")
        }

        return MDCPendingCommand
    }

    //CALCULATE CHECKSUM
    fun ULTCalculateCheckSum(): Byte{

        var checksum: Byte = 0x00

        //NEED TO FIRST CREATE SINGLE ARRAY OF BYTES FROM ARRAY OF BYTE ARRAYS.
        //WE STORE IT THIS WAY IN THE FIRST PLACE BECAUSE WHEN WE WRITE TO THE
        //DRIVER, WE DO IT IN BLOCKS OF 4 BYTES.  SO WE STORE OUR CONFIGURATION
        //IN AN ARRAY OF 4 BYTE BYTE ARRAYS
        var combinedByteArrays: ArrayList<Byte> = arrayListOf()
        for (i in 0..(MDCPendingCommand.count()) - 1){
            combinedByteArrays.addAll(MDCPendingCommand[i].toList())
        }
        //CONVERT ARRAYLIST TO BYTE ARRAY
        val bytes: ByteArray = combinedByteArrays.toByteArray()

        //COMPUTE CHECKSUM
        for (i in 0..(bytes.count() - 3)){
            checksum = checksum.plus(bytes[i]).toByte()
        }

        println("Check sum = ${byteArrayOf(checksum).toHex()}")
        return checksum

    }

    //PARSE LOG DATA BYTE ARRAY READ FROM DRIVER
    fun ULTParseLogDataBytes(logDataBytes: ByteArray){

        //NOT IMPLEMENTED, NEED TO ADD LOGIC TO PARSE
        //LOG DATA FROM BYTE ARRAY
    }

}
