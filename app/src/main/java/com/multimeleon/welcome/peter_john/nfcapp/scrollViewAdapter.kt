package com.multimeleon.welcome.peter_john.nfcapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import java.io.Serializable

class scrollViewAdapter(private val dataSet: ArrayList<DataModel_Driver>, internal var mContext: Context) : ArrayAdapter<DataModel_Driver>(mContext, R.layout.search_row, dataSet), View.OnClickListener {
    private var lastPosition = -1

    private class ViewHolder {
        internal var image: ImageView? = null
        internal var partNumber: TextView? = null
        internal var outputCurrent: TextView? = null
        internal var dimmingControlType: TextView? = null
        internal var dimensions: TextView? = null
        internal var maxRatedPower: TextView? = null
        internal var minVoltage: TextView? = null
        internal var maxVoltage: TextView? = null
        internal var selectButton: TextView? = null
    }

    override fun onClick(v: View) {
        val position = v.getTag() as Int
        val `object` = getItem(position)
        val dataModel = `object` as DataModel_Driver

        when (v.getId()) {
            R.id.select_button -> {
                var activity = v.context as Activity
                activity.run{

                        val resultIntent = Intent()
                        resultIntent.putExtra("DriverList", dataModel.rawData as Serializable)
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                }
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val dataModel = getItem(position)
        val viewHolder: ViewHolder // view lookup cache stored in tag
        val result: View
        if (convertView == null) {
            viewHolder = ViewHolder()
            val inflater = LayoutInflater.from(getContext())
            convertView = inflater.inflate(R.layout.search_row, parent, false)
            viewHolder.image = convertView!!.findViewById(R.id.driver_image)
            viewHolder.partNumber = convertView!!.findViewById<TextView>(R.id.partNumber)
            viewHolder.outputCurrent = convertView!!.findViewById<TextView>(R.id.outputCurrent)
            viewHolder.dimmingControlType = convertView!!.findViewById<TextView>(R.id.dimmingControlType)
            viewHolder.dimensions = convertView!!.findViewById<TextView>(R.id.dimensions)
            viewHolder.maxRatedPower = convertView!!.findViewById<TextView>(R.id.maxRatedPower)
            viewHolder.minVoltage = convertView!!.findViewById<TextView>(R.id.minVoltage)
            viewHolder.maxVoltage = convertView!!.findViewById<TextView>(R.id.maxVoltage)
            viewHolder.selectButton = convertView!!.findViewById<TextView>(R.id.select_button)
            result = convertView
            convertView!!.setTag(viewHolder)
        } else {
            viewHolder = convertView!!.getTag() as ViewHolder
            result = convertView
        }

        /*
        val animation = AnimationUtils.loadAnimation(mContext, if (position > lastPosition) R.anim.up_from_bottom else R.anim.down_from_top)
        result.startAnimation(animation)  */
        lastPosition = position
        if(dataModel.imageName.trim().toLowerCase() == "c")
            viewHolder.image!!.setImageResource(R.drawable.driver_image_c)
        else if(dataModel.imageName.trim().toLowerCase() == "d")
            viewHolder.image!!.setImageResource(R.drawable.driver_image_d)
        else if(dataModel.imageName.trim().toLowerCase() == "l")
            viewHolder.image!!.setImageResource(R.drawable.driver_image_l)
        viewHolder.image
        viewHolder.partNumber!!.setText(dataModel.partNumber)
        viewHolder.outputCurrent!!.setText(dataModel.outputCurrent)
        viewHolder.dimmingControlType!!.setText(dataModel.dimmingControlType)
        viewHolder.dimensions!!.setText(dataModel.dimensions)
        viewHolder.maxRatedPower!!.setText(dataModel.maxRatedPower)
        viewHolder.minVoltage!!.setText(dataModel.minVoltage)
        viewHolder.maxVoltage!!.setText(dataModel.maxVoltage)

        viewHolder.selectButton!!.setOnClickListener(this)
        viewHolder.selectButton!!.setTag(position)
        return convertView
    }// Get the data item for this position
    // Check if an existing view is being reused, otherwise inflate the view
    // Return the completed view to render on screen
}// View lookup cache