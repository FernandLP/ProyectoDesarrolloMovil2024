package com.fei.apartados

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlin.random.Random


class EmployeeMainListActivity : AppCompatActivity() {
    private lateinit var adapter: WidgetListAppartAdapter
    private val widgetsList = mutableListOf<WidgetListAppart>()
    private lateinit var lv_apparts: ListView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_main_list)

        lv_apparts = findViewById(R.id.lv_apparts)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        searchView = findViewById(R.id.searchView)

        loadData()

        floatingActionButton.setOnClickListener{
            val intent = Intent(this, EmployeeNewAppartView::class.java)
            startActivity(intent)
        }
//--------------------------------------------------------------------------------------------------
        lv_apparts.setOnItemClickListener { parent, view, position, id ->
            val clickedItem = parent.getItemAtPosition(position) as WidgetListAppart
//            val clickedItemId = clickedItem.id
            val clickedItemId = 2
            val intent = Intent(this, EmployeeAppartView::class.java)
            intent.putExtra("id", clickedItemId)
            startActivity(intent)
        }
//--------------------------------------------------------------------------------------------------
    }

    private fun loadData() {
        // Crear widgets de ejemplo
        val cantidadElementos = 15
        for (i in 1..cantidadElementos) {
            val numeroAleatorio = Random.nextInt(1000)

            val widget = WidgetListAppart(
                numeroAleatorio,
                "Eduardo Lozada Anastacio",
                "28/04/2024",
                "$450",
                "4 articulos",
                'n'
            )
            widgetsList.add(widget)
        }

        // Crear el adaptador y asignarlo al ListView
        adapter = WidgetListAppartAdapter(this, widgetsList)
        lv_apparts.adapter = adapter
    }

    fun pupopMenuFilter(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.setOnMenuItemClickListener { item ->
            // Manejar la selección del usuario aquí
            when (item.itemId) {
                R.id.opc1 -> {
                    ordenarPorNombre()
                    true
                }
                R.id.opc2 -> {
                    ordenarPorFecha()
                    true
                }
                // Agregar más casos según sea necesario
                else -> false
            }
        }
        popupMenu.inflate(R.menu.filter_menu)
        popupMenu.show()
    }

    private fun ordenarPorNombre() {
        widgetsList.sortBy { it.id } // Cambia "nombre" por el atributo real del widget que deseas ordenar
        adapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
    }

    private fun ordenarPorFecha() {
        widgetsList.sortBy { it.client } // Cambia "fecha" por el atributo real del widget que deseas ordenar
        adapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
    }
}
