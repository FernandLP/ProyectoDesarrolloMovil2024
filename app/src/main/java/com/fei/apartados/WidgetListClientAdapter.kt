package com.fei.apartados

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class WidgetListClientAdapter (context: Context, widgetsList: List<WidgetListClient>) :
    ArrayAdapter<WidgetListClient>(context, R.layout.widget_list_client_item, widgetsList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.widget_list_client_item, parent, false)
        }

        val widget = getItem(position)

        val textViewClient = view?.findViewById<TextView>(R.id.textViewClient)
        val textViewPhone = view?.findViewById<TextView>(R.id.textViewPhone)
        val textViewAditional = view?.findViewById<TextView>(R.id.textViewAdicional)

        textViewClient?.text = widget?.client
        textViewPhone?.text = widget?.phone

        if (widget?.aditional != null){
            textViewAditional?.text = widget?.aditional
        } else {
            textViewAditional?.text = "Sin información adicional"
        }


//        val textViewClient = view.findViewById<TextView>(R.id.textViewClient)

        return view!!
    }
}