package com.multimeleon.welcome.peter_john.nfcapp

/**
 * Created by kavithas on 11/29/17.
 */

class DataModel_Driver(part_number: String, image_name: String, output_current: String, dimming_control_type: String, dimensions: String, max_rated_power: String, min_voltage: String, max_voltage: String, raw_data: List<String>) {

    var partNumber: String
        internal set
    var imageName: String
        internal set
    var outputCurrent: String
        internal set
    var dimmingControlType: String
        internal set
    var dimensions: String
        internal set
    var maxRatedPower: String
        internal set
    var minVoltage: String
        internal set
    var maxVoltage: String
        internal set
    var rawData: List<String>
        internal set

    init {
        this.partNumber = part_number
        this.imageName = image_name
        this.outputCurrent = output_current
        this.dimmingControlType = dimming_control_type
        this.dimensions = dimensions
        this.maxRatedPower = max_rated_power
        this.minVoltage = min_voltage
        this.maxVoltage = max_voltage
        this.rawData = raw_data
    }
}