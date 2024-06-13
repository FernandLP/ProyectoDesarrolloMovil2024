package com.fei.apartados

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class EmployeeClientView : AppCompatActivity() {

    private lateinit var clientTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var adicionalInfoTextView: TextView
    private lateinit var cantApartadosTextView: TextView
    private lateinit var lv_apparts: ListView
    private lateinit var adapterApparts: WidgetListAppartAdapter
    private var widgetsListApparts = mutableListOf<WidgetListAppart>()
    private val filteredListApparts = mutableListOf<WidgetListAppart>()
    private lateinit var floatingActionButton: FloatingActionButton

    private var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_client_view)

        clientTextView = findViewById(R.id.textViewClient)
        phoneTextView = findViewById(R.id.textViewPhone)
        adicionalInfoTextView = findViewById(R.id.textViewAdicional)
        cantApartadosTextView = findViewById(R.id.amountApartados)
        lv_apparts = findViewById(R.id.lv_apparts)
        floatingActionButton = findViewById(R.id.floatingActionButtonCV)

        adapterApparts = WidgetListAppartAdapter(this, filteredListApparts)
        lv_apparts.adapter = adapterApparts

        id = intent.getIntExtra("id", -1)

        loadData()

        lv_apparts.setOnItemClickListener { parent, view, position, id ->
            val clickedItem = parent.getItemAtPosition(position) as WidgetListAppart
            val clickedItemId = clickedItem.id
            val intent = Intent(this, EmployeeAppartView::class.java)
            intent.putExtra("id", clickedItemId)
            startActivityForResult(intent, EmployeeAppartView.REQUEST_CODE)
        }

        floatingActionButton.setOnClickListener{
            pupopActionMenu(it)
        }
    }

    @SuppressLint("Range")
    private fun loadData() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // Obtener información básica del cliente
        val queryCliente = "SELECT * FROM Cliente WHERE id = ?"
        val argsCliente = arrayOf(id.toString())
        val cursorCliente = db.rawQuery(queryCliente, argsCliente)

        if (cursorCliente.moveToFirst()) {
            val clientName = cursorCliente.getString(cursorCliente.getColumnIndex("nombre"))
            val phone = cursorCliente.getString(cursorCliente.getColumnIndex("telefono"))
            val adicionalInfo = cursorCliente.getString(cursorCliente.getColumnIndex("informacion_adicional"))

            clientTextView.text = clientName
            phoneTextView.text = phone
            adicionalInfoTextView.text = adicionalInfo
        }

        cursorCliente.close()

        // Obtener la cantidad de apartados del cliente
        val queryApartados = "SELECT COUNT(*) FROM Apartado WHERE id_cliente = ?"
        val argsApartados = arrayOf(id.toString())
        val cursorApartados = db.rawQuery(queryApartados, argsApartados)

        if (cursorApartados.moveToFirst()) {
            val cantidadApartados = cursorApartados.getInt(0)
            cantApartadosTextView.text = cantidadApartados.toString()
        }

        cursorApartados.close()

        // Obtener la lista detallada de apartados del cliente
        val queryList = """
        SELECT Apartado.id as id_apartado, Apartado.fecha_creacion, Apartado.estado, 
               SUM(Articulo.precio_unitario * Articulo.cantidad) as precio_total, 
               COUNT(Articulo.id_apartado) as cantidad_articulos 
        FROM Apartado 
        LEFT JOIN Articulo ON Apartado.id = Articulo.id_apartado 
        WHERE Apartado.id_cliente = ?
        GROUP BY Apartado.id
    """

        val cursorList = db.rawQuery(queryList, argsCliente) // Utilizar argsCliente en lugar de args
        widgetsListApparts.clear()

        while (cursorList.moveToNext()) {
            val idIndex = cursorList.getColumnIndex("id_apartado")
            val dateIndex = cursorList.getColumnIndex("fecha_creacion")
            val priceIndex = cursorList.getColumnIndex("precio_total")
            val itemsIndex = cursorList.getColumnIndex("cantidad_articulos")
            val statusIndex = cursorList.getColumnIndex("estado")

            val idAppart = cursorList.getInt(idIndex)
            val date = cursorList.getString(dateIndex)
            val price = cursorList.getFloat(priceIndex).toString()
            val items = cursorList.getInt(itemsIndex).toString() + " Articulos"
            val status = cursorList.getString(statusIndex)

            val widget = WidgetListAppart(idAppart, clientTextView.text.toString(), date, price, items, status[0])
            widgetsListApparts.add(widget)
        }

        cursorList.close()
        db.close()

        filteredListApparts.clear()
        filteredListApparts.addAll(widgetsListApparts)

        adapterApparts.notifyDataSetChanged()
    }


    private fun pupopActionMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_cliente)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.opc1 -> {
                    val intent = Intent(this, EditClient::class.java)
                    intent.putExtra("id", id)
                    startActivityForResult(intent, EmployeeAppartView.REQUEST_CODE)
                    true
                }
                R.id.opc2 -> {
                    // Eliminar cliente
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Eliminar Cliente")
                    builder.setMessage("¿Estás seguro de eliminar este cliente?")

                    builder.setPositiveButton("Sí") { _, _ ->
                        val dbHelper = DatabaseHelper(this)
                        val db = dbHelper.readableDatabase

                        // Verificar si el cliente está asociado a algún apartado
                        val checkQuery = "SELECT COUNT(*) FROM Apartado WHERE id_cliente = ?"
                        val checkArgs = arrayOf(id.toString())
                        val cursor = db.rawQuery(checkQuery, checkArgs)

                        var hasApartados = false
                        if (cursor.moveToFirst()) {
                            val count = cursor.getInt(0)
                            hasApartados = count > 0
                        }

                        cursor.close()

                        if (hasApartados) {
                            // El cliente tiene apartados asociados
                            Toast.makeText(this, "No se puede eliminar el cliente porque tiene apartados asociados", Toast.LENGTH_SHORT).show()
                        } else {
                            // El cliente no tiene apartados asociados, proceder con la eliminación
                            val deleteDb = dbHelper.writableDatabase
                            val deleteQuery = "DELETE FROM Cliente WHERE id = ?"
                            val deleteArgs = arrayOf(id.toString())
                            deleteDb.execSQL(deleteQuery, deleteArgs)
                            deleteDb.close()

                            Toast.makeText(this, "Cliente eliminado", Toast.LENGTH_SHORT).show()
                            setResult(Activity.RESULT_OK)
                            finish()
                        }

                        db.close()
                    }

                    builder.setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                    true
                }

                else -> false
            }
        }
        popupMenu.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == EmployeeAppartView.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Aquí llamamos a loadData para actualizar la información
            loadData()
        }
    }

    companion object {
        const val REQUEST_CODE = 1
    }
}
