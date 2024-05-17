package com.fei.apartados

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


class EmployeeAppartView : AppCompatActivity() {
    private lateinit var buttonBack: ImageView
    private lateinit var floatingActionButton: FloatingActionButton

    private lateinit var idTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var toleranceTextView: TextView
    private lateinit var clientTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var adicionalTextView: TextView

    private var id: Int? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_appart_view)

        buttonBack = findViewById(R.id.imageViewBack)
        floatingActionButton = findViewById(R.id.floatingActionButtonAV)

        idTextView = findViewById(R.id.textViewID)
        dateTextView = findViewById(R.id.textViewDate)
        toleranceTextView = findViewById(R.id.textViewTolerance)
        clientTextView = findViewById(R.id.textViewClient)
        phoneTextView = findViewById(R.id.textViewPhone)
        adicionalTextView = findViewById(R.id.textViewAdicional)

        id = intent.getIntExtra("id", -1)
        Toast.makeText(this, "$id", Toast.LENGTH_SHORT).show()

        loadData()

        floatingActionButton.setOnClickListener{
            pupopActionMenu(it)
        }

        buttonBack.setOnClickListener {
            finish()
//            backAllert()
        }
    }

    private fun loadData() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val query = "SELECT * FROM Apartado " +
                "INNER JOIN Cliente ON Apartado.id_cliente = Cliente.id " +
                "LEFT JOIN Articulo ON Apartado.id = Articulo.id_apartado " +
                "LEFT JOIN Abonos ON Apartado.id = Abonos.id_apartado " +
                "WHERE Apartado.id = ?"
        val args = arrayOf(id.toString())
        val cursor = db.rawQuery(query, args)

        if (cursor.moveToFirst()) {
            // Obtener los índices de las columnas en el cursor
            val fechaCreacionIndex = cursor.getColumnIndex("fecha_creacion")
            val fechaToleranciaIndex = cursor.getColumnIndex("fecha_tolerancia")
            val nombreClienteIndex = cursor.getColumnIndex("nombre")
            val telefonoClienteIndex = cursor.getColumnIndex("telefono")
            val adicionalClienteIndex = cursor.getColumnIndex("informacion_adicional")

            // Obtener los valores de las columnas
            val fechaCreacion = cursor.getString(fechaCreacionIndex)
            val fechaTolerancia = cursor.getString(fechaToleranciaIndex)
            val nombreCliente = cursor.getString(nombreClienteIndex)
            val telefonoCliente = cursor.getString(telefonoClienteIndex)
            val adicionalCliente = cursor.getString(adicionalClienteIndex)

            // Mostrar la información en un Toast para depurar
            idTextView.text = id.toString()
            dateTextView.text = fechaCreacion
            toleranceTextView.text = fechaTolerancia
            clientTextView.text = nombreCliente
            phoneTextView.text = telefonoCliente
            adicionalTextView.text = adicionalCliente
        } else {
            // Mostrar un mensaje de error si no se encontraron resultados
            Toast.makeText(this, "No se encontraron registros para el ID proporcionado", Toast.LENGTH_SHORT).show()
        }

        if (cursor != null && cursor.moveToFirst()) {
            do {
                for (i in 0 until cursor.columnCount) {
                    Log.d("DB_PRINT", cursor.getColumnName(i) + ": " + cursor.getString(i))
                }
            } while (cursor.moveToNext())
            cursor.close()
        }

        // Cerrar el cursor y la base de datos
        cursor.close()
        db.close()
    }




    private fun backAllert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro de que quieres salir?")

        // Agrega los botones de confirmación y cancelación
        builder.setPositiveButton("Sí") { dialog, which ->
            // Si el usuario confirma, cierra la actividad actual y vuelve a la anterior
            finish()
        }
        builder.setNegativeButton("Cancelar", null)

        // Muestra el cuadro de diálogo
        val dialog = builder.create()
        dialog.show()
    }

    private fun pupopActionMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.menu_personalizado)
        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.opc1 -> {
                    // Acción para la opción 1
                    Toast.makeText(this, "Opción 1 seleccionada", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.opc2 -> {
                    // Acción para la opción 2
                    Toast.makeText(this, "Opción 2 seleccionada", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.opc3 -> {
                    // Acción para la opción 2
                    Toast.makeText(this, "Opción 3 seleccionada", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.opc4 -> {
                    // Acción para la opción 2
                    Toast.makeText(this, "Opción 4 seleccionada", Toast.LENGTH_SHORT).show()
                    true
                }

                // Agregar más casos según sea necesario
                else -> false
            }
        }
        popupMenu.show()
    }
}