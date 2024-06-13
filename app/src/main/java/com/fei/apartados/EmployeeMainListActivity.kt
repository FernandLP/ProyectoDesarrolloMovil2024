package com.fei.apartados

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EmployeeMainListActivity : AppCompatActivity() {
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var adapterApparts: WidgetListAppartAdapter
    private lateinit var adapterClients: WidgetListClientAdapter
    private val widgetsListApparts = mutableListOf<WidgetListAppart>()
    private val widgetsListClients = mutableListOf<WidgetListClient>()
    private val filteredListApparts = mutableListOf<WidgetListAppart>()
    private val filteredListClients = mutableListOf<WidgetListClient>()
    private lateinit var lv_apparts: ListView
    private lateinit var floatingActionButton: FloatingActionButton
    private lateinit var searchView: SearchView
    private lateinit var menu: ImageView

    private var id: Int? = null
    private var clientsView: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_main_list)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        lv_apparts = findViewById(R.id.lv_apparts)
        floatingActionButton = findViewById(R.id.floatingActionButton)
        searchView = findViewById(R.id.searchView)
        menu = findViewById(R.id.menu_icon)

        setupDrawer()

        adapterApparts = WidgetListAppartAdapter(this, filteredListApparts)
        adapterClients = WidgetListClientAdapter(this, filteredListClients)
        lv_apparts.adapter = adapterApparts

        loadData()
        updateStatus()

        floatingActionButton.setOnClickListener {
            val intent = Intent(this, EmployeeNewAppartView::class.java)
            startActivityForResult(intent, EmployeeAppartView.REQUEST_CODE)
        }

        lv_apparts.setOnItemClickListener { parent, view, position, id ->
            if (!clientsView) {
                val clickedItem = parent.getItemAtPosition(position) as WidgetListAppart
                val clickedItemId = clickedItem.id
                val intent = Intent(this, EmployeeAppartView::class.java)
                intent.putExtra("id", clickedItemId)
                startActivityForResult(intent, EmployeeAppartView.REQUEST_CODE)
            } else {
                val clickedItem = parent.getItemAtPosition(position) as WidgetListClient
                val clickedItemId = clickedItem.id
                val intent = Intent(this, EmployeeClientView::class.java)
                intent.putExtra("id", clickedItemId)
                startActivityForResult(intent, EmployeeClientView.REQUEST_CODE)
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if (!clientsView){
                    filterListAppart(newText)
                } else {
                    filterListClient(newText)
                }

                return true
            }
        })

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_list_apartados -> {
                    loadData()
                    //cerrar menu lateral
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_list_clientes -> {
                    loadClients()
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }

        menu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun updateStatus() {
        val dbHelper = DatabaseHelper(this)
        val dbReader: SQLiteDatabase = dbHelper.readableDatabase
        val dbWriter: SQLiteDatabase = dbHelper.writableDatabase

        val query = "SELECT * FROM Apartado"
        val cursor = dbReader.rawQuery(query, null)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = Date()

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
                val creationDateStr = cursor.getString(cursor.getColumnIndexOrThrow("fecha_creacion"))
                val remaining = cursor.getFloat(cursor.getColumnIndexOrThrow("restante"))
                val toleranceDateStr = cursor.getString(cursor.getColumnIndexOrThrow("fecha_tolerancia"))

                val creationDate = dateFormat.parse(creationDateStr)
                val toleranceDate = if (toleranceDateStr != null) dateFormat.parse(toleranceDateStr) else null

                val calendar = Calendar.getInstance()
                calendar.time = creationDate
                calendar.add(Calendar.MONTH, 1)
                val paymentDeadline = toleranceDate ?: calendar.time

                val daysDifference = (paymentDeadline.time - currentDate.time) / (1000 * 60 * 60 * 24)

                var newStatus: String? = null
                if (daysDifference in 0..6 && remaining > 0) {
                    newStatus = "d"
                } else if (currentDate.after(paymentDeadline) && remaining > 0) {
                    newStatus = "e"
                }

                if (newStatus != null) {
                    val values = ContentValues().apply {
                        put("estado", newStatus)
                    }
                    dbWriter.update("Apartado", values, "id = ?", arrayOf(id.toString()))
                }
            } while (cursor.moveToNext())
        }

        cursor.close()
        dbReader.close()
        dbWriter.close()
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

        clientsView = true
    }

    private fun setupDrawer() {
        setSupportActionBar(findViewById(R.id.toolbar))
        actionBarDrawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            true
        } else super.onOptionsItemSelected(item)
    }

    @SuppressLint("Range")
    private fun loadData() {
        // Cargar apartados desde la base de datos
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val query = """
        SELECT Apartado.id as id_apartado, Apartado.fecha_creacion, Apartado.estado, 
               Cliente.nombre, Cliente.telefono, 
               Apartado.restante as precio_total,  -- Usar restante en lugar de SUM(Articulo.precio_unitario * Articulo.cantidad)
               COUNT(Articulo.id_apartado) as cantidad_articulos 
        FROM Apartado 
        INNER JOIN Cliente ON Apartado.id_cliente = Cliente.id 
        LEFT JOIN Articulo ON Apartado.id = Articulo.id_apartado 
        LEFT JOIN Abonos ON Apartado.id = Abonos.id_apartado 
        GROUP BY Apartado.id
    """

        val cursor = db.rawQuery(query, null)
        widgetsListApparts.clear()

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
            widgetsListApparts.add(widget)
        }

        cursor.close()
        db.close()

        filteredListApparts.clear()
        filteredListApparts.addAll(widgetsListApparts)

        lv_apparts.adapter = adapterApparts
        adapterApparts.notifyDataSetChanged()

        clientsView = false
    }


    private fun filterListAppart(query: String?) {
        filteredListApparts.clear()
        if (query.isNullOrEmpty()) {
            filteredListApparts.addAll(widgetsListApparts)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (widget in widgetsListApparts) {
                if (widget.client.lowercase().contains(lowerCaseQuery)) {
                    filteredListApparts.add(widget)
                }
            }
        }
        adapterApparts.notifyDataSetChanged()
    }
    private fun filterListClient(query: String?) {
        filteredListClients.clear()
        if (query.isNullOrEmpty()) {
            filteredListClients.addAll(widgetsListClients)
        } else {
            val lowerCaseQuery = query.lowercase()
            for (widget in widgetsListClients) {
                if (widget.client?.lowercase()?.contains(lowerCaseQuery) == true) {
                    filteredListClients.add(widget)
                }
            }
        }
        adapterClients.notifyDataSetChanged()
    }

    fun popupMenuFilter(view: View) {
        if (!clientsView) {
            val popupMenu = PopupMenu(this, view)
            popupMenu.setOnMenuItemClickListener { item ->
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
                    else -> false
                }
            }
            popupMenu.inflate(R.menu.filter_menu)
            popupMenu.show()
        } else {
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
    }

    private fun ordenarPorNumeroTelefonico() {
        filteredListClients.sortBy { it.phone }
        adapterClients.notifyDataSetChanged()
    }

    private fun ordenarMasAntiguos() {
        filteredListApparts.sortBy { it.date }
        adapterApparts.notifyDataSetChanged()
    }

    private fun ordenarMasRecientes() {
        filteredListApparts.sortByDescending { it.date }
        adapterApparts.notifyDataSetChanged()
    }

    private fun ordenarPorNumero() {
        filteredListApparts.sortBy { it.id }
        adapterApparts.notifyDataSetChanged()
    }

    private fun ordenarPorNombre() {
        if (!clientsView) {
            filteredListApparts.sortBy { it.client }
            adapterApparts.notifyDataSetChanged()
        } else {
            filteredListClients.sortBy { it.client }
            adapterClients.notifyDataSetChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (!clientsView){
                loadData()
            } else {
                loadClients()
            }
        }
    }

    companion object {
        const val REQUEST_CODE = 1
    }
}
