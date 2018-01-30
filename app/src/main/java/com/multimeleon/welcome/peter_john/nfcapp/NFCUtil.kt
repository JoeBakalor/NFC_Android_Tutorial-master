package com.multimeleon.welcome.peter_john.nfcapp

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.*
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.nfc.tech.MifareUltralight
import android.nfc.tech.NfcA
import android.os.Parcelable
import org.jetbrains.anko.toast
import java.io.IOException

/**
 * Created by joebakalor on 11/7/17.
 */

object NFCUtil {

    //USED FOR MANAGING DRIVER CONFIGURATION SETTINGS
    var ultConfigManager: ULTConfigurationManager = ULTConfigurationManager()


    //CONVENIENCE FUNCTIONS
    fun recieveStaticConfigBlocks(bytes: ByteArray){
        ultConfigManager.ULTParseStaticConfigurationBytes(bytes)
    }

    fun recieveTuningDataConfigBlocks(bytes: ByteArray, preWrite: Boolean){
        ultConfigManager.ULTParsTuningDataBytes(bytes, preWrite)
    }

    fun recieveLogDataConfigBlocks(bytes: ByteArray){
        ultConfigManager.ULTParseLogDataBytes(bytes)
    }

    //WRITE THE CURRENT CONFIGURATION TO THE TAG
    fun ULTWriteConfiguration(intent: Intent?): Boolean{

        intent?.let {
            val tag = it.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            return writeConfiguration( tag)
        }
        return false
    }

    //READ THE CURRENT CONFIGURATION SAVED TO THE DRIVER
    fun ULTReadConfiguration(intent: Intent?, preWrite: Boolean): Boolean{
        return ULTReadNFCData(intent, preWrite)
    }

    //RETRIEVE TAG DATA FROM NFC ADAPTER IF MESSAGE FOUND
    private fun ULTReadNFCData(intent: Intent?, preWrite: Boolean): Boolean {
        intent?.let {
            println("Intent action = ${intent.action}")

            var staticConfigBytes: ArrayList<Byte> = arrayListOf()
            var tuningData: ArrayList<Byte> = arrayListOf()
            var logData: ArrayList<Byte> = arrayListOf()

            if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action || NfcAdapter.ACTION_TECH_DISCOVERED == intent.action || NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {

                //GET MIFARE TAG INSTANCE AND READ DATA
                var tag = it.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
                var mifareData = MifareUltralight.get(tag)
                var mdcCommandFound = false

                mifareData?.let {

                    try {

                        it.connect()

                        //readPages() reads 4 blocks or 16 bytes starting from the offset index specified

                        //READ STATIC DATA FROM DRIVER 40 ==> 5 BLOCKS, NOT USED FOR ANYTHING CURRENTLY
                        var data = it.readPages(40)
                        staticConfigBytes.addAll(data.toList())

                        data = it.readPages(44)
                        staticConfigBytes.addAll(data.toList())

                        //PROCESS STATIC CONFIG DATA READ FROM DRIVER
                        recieveStaticConfigBlocks(staticConfigBytes.toByteArray())

                        //READ TUNING DATA FROM TAG 48 ==> 9 BLOCKS

                        //ADD CHECK TO SEE IF THERE IS A PENDING COMMAND IN THE
                        //MDC COMMAND BUFFER, IF THERE ISN'T THEN ASSUME THE TUNING
                        //DATA PRESENT IN THE STATIC TUNING TABLE IS VALID

                        data = it.readPages(96)

                        //SHOULD MAKE A MORE SPECIFIC CHECK OF THE COMMAND FOR PRODUCTION CODE,
                        //RIGHT NOW JUST CHECKING IF THE MESSAGE SIZE IS NOT 0
                        if (data[0] != 0x00.toByte()){
                            println("MDC Command present in buffer 96")
                            mdcCommandFound = true
                            data = it.readPages(97)
                            tuningData.addAll(data.toList())
                            data = it.readPages(101)
                            tuningData.addAll(data.toList())
                            data = it.readPages(105)
                            tuningData.addAll(data.toList())

                            //PROCESS TUNING DATA READ FROM DRIVER
                            recieveTuningDataConfigBlocks(tuningData.toByteArray(), preWrite)
                        }

                        data = it.readPages(112)

                        if (data[0] != 0x00.toByte()){
                            println("MDC Command present in buffer 112")
                        }

                        data = it.readPages(128)

                        if (data[0] != 0x00.toByte()){
                            println("MDC Command present in buffer 128")
                        }

                        data = it.readPages(144)

                        if (data[0] != 0x00.toByte()){
                            println("MDC Command present in buffer 144")
                        }

                        data = it.readPages(160)

                        if (data[0] != 0x00.toByte()){
                            println("MDC Command present in buffer 160")
                        }

                        data = it.readPages(176)

                        if (data[0] != 0x00.toByte()){
                            println("MDC Command present in buffer 176")
                        }

                        data = it.readPages(192)

                        if (data[0] != 0x00.toByte()){
                            println("MDC Command present in buffer 192")
                        }
                        data = it.readPages(208)

                        if (data[0] != 0x00.toByte()){
                            println("MDC Command present in buffer")
                        }

                        if (mdcCommandFound == false){
                            data = it.readPages(48)
                            tuningData.addAll(data.toList())

                            println("TUNING DATA FIRST BLOCK = ${data.toHex()}")

                            data = it.readPages(52)
                            tuningData.addAll(data.toList())

                            data = it.readPages(56)
                            tuningData.addAll(data.toList())

                            //PROCESS TUNING DATA READ FROM DRIVER
                            recieveTuningDataConfigBlocks(tuningData.toByteArray(), preWrite)
                        }

                        //READ LOG DATA FROM TAG 64 ==> 4 BLOCK, NOT USED FOR ANYTHING CURRENTLY
                        data = it.readPages(64)
                        logData.addAll(data.toList())

                        //PROCESS LOG DATA READ FROM DRIVER
                        recieveLogDataConfigBlocks(logData.toByteArray())

                        it.close()

                    } catch (e: TagLostException){

                        println("Tag lost during communication")
                        return false
                    }

                    return true
                }

            } else {
                return false
            }
        }
        return false
    }

    //NOT USED HERE, NOT USING NDEF FORMATTED MESSAGES
    private fun getNDefMessages(intent: Intent): Array<NdefMessage> {
        val rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

        println("RAW INTENT = ${intent.extras}")
        rawMessage?.let {
            return rawMessage.map {
                it as NdefMessage
            }.toTypedArray()
        }

        // Unknown tag type
        val empty = byteArrayOf()
        val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty)
        val msg = NdefMessage(arrayOf(record))
        return arrayOf(msg)
    }

    //DISABLE NFC
    fun disableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity) {
        nfcAdapter.disableForegroundDispatch(activity)
    }

    //ENABLE NFC WITH INTENT
    fun <T> enableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity, classType: Class<T>) {
        val pendingIntent = PendingIntent.getActivity(activity, 0,
                Intent(activity, classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        val nfcIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val nfcIntentFilter2 = IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED)
        val nfcIntentFilter3 = IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED)
        val filters = arrayOf(nfcIntentFilter, nfcIntentFilter2, nfcIntentFilter3)

        val TechLists = arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, TechLists)
    }

    //WRITE NDEF MESSAGE TO PHYSICAL TAG, FINAL OPERATION
    private fun writeConfiguration(tag: Tag?): Boolean {

        try {
            val mifareData = MifareUltralight.get(tag)
            mifareData?.let {
                println("Valid Mifare Tag Found, connect and transfer")


                //GET CURRENT CONFIGURATION
                val MDCPacket = NFCUtil.ultConfigManager.ULTCreateMDCProtocolPacket()

                //EACH ITEM IN MDCPACKET IS 4 BYTES
                //WRITE ONE BLOCK AT A TIME OF MCD COMMAND STARTING AT
                //0x60 HEX or 96 Int
                try {
                    it.connect()

                    it.writePage(96, MDCPacket[0])
                    it.writePage(97, MDCPacket[1])
                    it.writePage(98, MDCPacket[2])
                    it.writePage(99, MDCPacket[3])

                    it.writePage(100, MDCPacket[4])
                    it.writePage(101, MDCPacket[5])
                    it.writePage(102, MDCPacket[6])
                    it.writePage(103, MDCPacket[7])

                    it.writePage(104, MDCPacket[8])
                    it.writePage(105, MDCPacket[9])

                    it.close()

                } catch(e: TagLostException) {

                    println("Error occured while reading tag")
                    return false
                }
                return  true
            }
        } catch (e: Exception) {
            //Write operation has failed
            return false
        }
        return false
    }
}

//ByteArray extension to display hex values as a string for debugging purposes
private val HEX_CHARS = "0123456789ABCDEF".toCharArray()

fun ByteArray.toHex() : String{
    val result = StringBuffer()

    forEach {
        val octet = it.toInt()
        val firstIndex = (octet and 0xF0).ushr(4)
        val secondIndex = octet and 0x0F
        result.append(HEX_CHARS[firstIndex])
        result.append(HEX_CHARS[secondIndex])
    }

    return result.toString()
}
