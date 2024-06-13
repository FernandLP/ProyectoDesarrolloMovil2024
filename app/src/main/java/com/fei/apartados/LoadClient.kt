package com.fei.apartados

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class LoadClient : AppCompatActivity() {
    private lateinit var adapterClients: WidgetListClientAdapter
    private val widgetsListClients = mutableListOf<WidgetListClient>()
    private val filteredListClients = mutableListOf<WidgetListClient>()
    private lateinit var lv_apparts: ListView
    private lateinit var searchView: SearchView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_load_client)

        lv_apparts = findViewById(R.id.lv_apparts)
        searchView = findViewById(R.id.searchView)
        adapterClients = WidgetListClientAdapter(this, filteredListClients)

        loadClients()

        lv_apparts.setOnItemClickListener { parent, view, position, id ->
            val selectedClient = parent.getItemAtPosition(position) as WidgetListClient
            val resultIntent = Intent().apply {
                putExtra("selectedClientId", selectedClient.id) // assuming WidgetListClient has an id property
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    @SuppressLint("Range")
    private fun loadClients() {
        // Cargar clientes desde la base de datos o donde corresponda
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val query = "SELECT * FROM Cliente"

        val cursor = db.rawQuery(query, null)
        widgetsListClients.clear()

        while (cursor.moveToNext()) {
            val client = WidgetListClient(
                cursor.getInt(cursor.getColumnIndex("id")),
                cursor.getString(cursor.getColumnIndex("nombre")),
                cursor.getString(cursor.getColumnIndex("telefono")),
                cursor.getString(cursor.getColumnIndex("informacion_adicional"))
            )
            widgetsListClients.add(client)
        }

        cursor.close()
        db.close()

        filteredListClients.clear()
        filteredListClients.addAll(widgetsListClients)

        lv_apparts.adapter = adapterClients
        adapterClients.notifyDataSetChanged()
    }

    fun popupMenuFilter(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.opc1 -> {
                    ordenarPorNombre()
                    true
                }
                R.id.opc2 -> {
                    ordenarPorNumeroTelefonico()
                    true
                }
                else -> false
            }
        }
        popupMenu.inflate(R.menu.filter_menu_clients)
        popupMenu.show()
    }

    private fun ordenarPorNumeroTelefonico() {
        filteredListClients.sortBy { it.phone }
        adapterClients.notifyDataSetChanged()
    }

    private fun ordenarPorNombre() {
        filteredListClients.sortBy { it.client }
        adapterClients.notifyDataSetChanged()
    }
}