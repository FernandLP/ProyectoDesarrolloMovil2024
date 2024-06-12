package com.fei.apartados

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EmployeeMainListActivity : AppCompatActivity() {
    private lateinit var adapter: WidgetListAppartAdapter
    private val widgetsList = mutableListOf<WidgetListAppart>()
    private val filteredList = mutableListOf<WidgetListAppart>()
    private lateinit var lv_apparts: ListView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var searchView: SearchView

    private var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_main_list)

        lv_apparts = findViewById(R.id.lv_apparts)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        searchView = findViewById(R.id.searchView)

        adapter = WidgetListAppartAdapter(this, widgetsList)
        lv_apparts.adapter = adapter

        loadData()

        floatingActionButton.setOnClickListener{
            val intent = Intent(this, EmployeeNewAppartView::class.java)
            startActivityForResult(intent, EmployeeAppartView.REQUEST_CODE)
        }

        lv_apparts.setOnItemClickListener { parent, view, position, id ->
            val clickedItem = parent.getItemAtPosition(position) as WidgetListAppart
            val clickedItemId = clickedItem.id
            val intent = Intent(this, EmployeeAppartView::class.java)
            intent.putExtra("id", clickedItemId)
            startActivityForResult(intent, EmployeeAppartView.REQUEST_CODE)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // No se requiere ninguna acción específica en el envío de la consulta
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun loadData() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // Consulta para obtener todos los registros de la tabla Apartado y sus relaciones
        val query = "SELECT Apartado.id as id_apartado, Apartado.fecha_creacion, Apartado.estado, " +
                "Cliente.nombre, Cliente.telefono, " +
                "SUM(Articulo.precio_unitario * Articulo.cantidad) as precio_total, " +
                "COUNT(Articulo.id_apartado) as cantidad_articulos " +
                "FROM Apartado " +
                "INNER JOIN Cliente ON Apartado.id_cliente = Cliente.id " +
                "LEFT JOIN Articulo ON Apartado.id = Articulo.id_apartado " +
                "LEFT JOIN Abonos ON Apartado.id = Abonos.id_apartado " +
                "GROUP BY Apartado.id"

        // Ejecutar la consulta
        val cursor = db.rawQuery(query, null)

        // Limpiar la lista antes de cargar nuevos datos
        widgetsList.clear()

        // Iterar sobre todos los registros
        while (cursor.moveToNext()) {
            val idIndex = cursor.getColumnIndex("id_apartado")
            val clientIndex = cursor.getColumnIndex("nombre")
            val dateIndex = cursor.getColumnIndex("fecha_creacion")
            val priceIndex = cursor.getColumnIndex("precio_total")
            val itemsIndex = cursor.getColumnIndex("cantidad_articulos")
            val statusIndex = cursor.getColumnIndex("estado")

            val idAppart = cursor.getInt(idIndex)
            val client = cursor.getString(clientIndex)
            val date = cursor.getString(dateIndex)
            val price = cursor.getFloat(priceIndex).toString()
            val items = cursor.getInt(itemsIndex).toString() + " Articulos"
            val status = cursor.getString(statusIndex)

            val widget = WidgetListAppart(idAppart, client, date, price, items, status[0])
            widgetsList.add(widget)
        }

        // Cerrar el cursor y la base de datos
        cursor.close()
        db.close()

        // Limpiar la lista filtrada y añadir los nuevos elementos
        filteredList.clear()
        filteredList.addAll(widgetsList)

        // Notificar al adaptador que los datos han cambiado
        adapter.notifyDataSetChanged()
    }


    private fun filterList(query: String?) {
        filteredList.clear()
        if (query.isNullOrEmpty()) {
            filteredList.addAll(widgetsList)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (widget in widgetsList) {
                if (widget.client.lowercase().contains(lowerCaseQuery)) {
                    filteredList.add(widget)
                }
            }
        }
        adapter.notifyDataSetChanged()
    }

    fun popupMenuFilter(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.setOnMenuItemClickListener { item ->
            // Manejar la selección del usuario aquí
            when (item.itemId) {
                R.id.opc1 -> {
                    ordenarPorNumero()
                    true
                }
                R.id.opc2 -> {
                    ordenarPorNombre()
                    true
                }
                R.id.opc3 -> {
                    ordenarMasRecientes()
                    true
                }
                R.id.opc4 -> {
                    ordenarMasAntiguos()
                    true
                }
                // Agregar más casos según sea necesario
                else -> false
            }
        }
        popupMenu.inflate(R.menu.filter_menu)
        popupMenu.show()
    }

    private fun ordenarMasAntiguos() {
        filteredList.sortBy { it.date } // Ordenar por fecha en orden ascendente
        adapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
    }

    private fun ordenarMasRecientes() {
        filteredList.sortByDescending { it.date } // Ordenar por fecha en orden descendente
        adapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
    }

    private fun ordenarPorNumero() {
        filteredList.sortBy { it.id } // Ordenar por número
        adapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
    }

    private fun ordenarPorNombre() {
        filteredList.sortBy { it.client } // Ordenar por nombre
        adapter.notifyDataSetChanged() // Notificar al adaptador que los datos han cambiado
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Aquí llamamos a loadData para actualizar la información
            loadData()
        }
    }

    companion object {
        const val REQUEST_CODE = 1
    }

}
