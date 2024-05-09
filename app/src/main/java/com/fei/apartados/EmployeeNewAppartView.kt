package com.fei.apartados

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import java.text.SimpleDateFormat
import java.util.Calendar

class EmployeeNewAppartView : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var addButton: Button
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView

    private lateinit var clientEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var infoEditText: EditText

    private lateinit var idEditText: EditText
    private lateinit var dateTextView: TextView
    private lateinit var linearLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_new_appart_view)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val actual = dateFormat.format(calendar.time)

        addButton = findViewById(R.id.btnAdd)
        saveButton = findViewById(R.id.btnSave)
        backButton = findViewById(R.id.imageViewBack)

        dateTextView = findViewById(R.id.textViewDate)
        linearLayout = findViewById(R.id.linearLayoutList)
        idEditText =  findViewById(R.id.editTextID)

        searchLastId()

        dateTextView.text = actual


        addButton.setOnClickListener {
            addItem()
        }


        saveButton.setOnClickListener {
            saveData()
        }


        backButton.setOnClickListener {
            backAllert()
        }

    }

    @SuppressLint("Range", "ResourceType")
    private fun searchLastId() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // Verificar si la tabla Articulo existe en la base de datos
        val cursor = db.rawQuery("SELECT * FROM sqlite_master WHERE type='table' AND name='Articulo'", null)
        if (cursor.count == 0) {
            // La tabla Articulo no existe, no hacer nada
            cursor.close()
            db.close()
            dbHelper.close()
            Toast.makeText(this, "No existe la base de datos", Toast.LENGTH_SHORT).show()
            idEditText.setText("null")
            return
        }
        cursor.close()

        // La tabla Articulo existe, realizar la consulta para obtener el último ID
        val query = "SELECT MAX(id_apartado) AS lastId FROM Articulo"
        val idCursor = db.rawQuery(query, null)
        if (idCursor.moveToFirst()) {
            val lastId = idCursor.getInt(idCursor.getColumnIndex("lastId"))
            // Imprimir el último ID
            Toast.makeText(this, "La base de datos existe y el ultimo valor es $lastId", Toast.LENGTH_SHORT).show()
            Log.d("Last ID", "El último ID de la tabla Articulo es: $lastId")
            val num = lastId + 1
            idEditText.setText(num.toString())
        }
        idCursor.close()
        db.close()
        dbHelper.close()
    }



    private fun addItem() {
        val customWidget = LayoutInflater.from(this).inflate(R.layout.widget_new_appart_item, null) // Crear instancia del widget personalizado
        linearLayout.addView(customWidget) // Agregar el widget personalizado al LinearLayout
        // Recorrer todos los elementos en el LinearLayout
        for (i in 0 until linearLayout.childCount) {
            val widgetPersonalizado = linearLayout.getChildAt(i)
            if (widgetPersonalizado is RelativeLayout) {
                val editTextCantidad = widgetPersonalizado.findViewById<EditText>(R.id.cant)
                val editTextPrecio = widgetPersonalizado.findViewById<EditText>(R.id.price)
                val textViewTotal = widgetPersonalizado.findViewById<TextView>(R.id.total)

                // Agregar listeners a los EditText de cantidad y precio
                editTextCantidad.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        calcularTotal(editTextCantidad, editTextPrecio, textViewTotal)
                    }
                })

                editTextPrecio.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        calcularTotal(editTextCantidad, editTextPrecio, textViewTotal)
                    }
                })
            }
        }
    }

    private fun saveData() {
        val cantidadList = mutableListOf<String>()
        val descripcionList = mutableListOf<String>()
        val precioList = mutableListOf<String>()
        val totalList = mutableListOf<String>()

        // Recorrer todos los elementos en el LinearLayout
        for (i in 0 until linearLayout.childCount) {
            val widgetPersonalizado = linearLayout.getChildAt(i)
            if (widgetPersonalizado is RelativeLayout) {
                val editTextCantidad = widgetPersonalizado.findViewById<EditText>(R.id.cant)
                val editTextDescripcion = widgetPersonalizado.findViewById<EditText>(R.id.desc)
                val editTextPrecio = widgetPersonalizado.findViewById<EditText>(R.id.price)
                val textViewTotal = widgetPersonalizado.findViewById<TextView>(R.id.total)

                val cantidad = editTextCantidad.text.toString()
                val descripcion = editTextDescripcion.text.toString()
                val precio = editTextPrecio.text.toString()
                val total = textViewTotal.text.toString()

                cantidadList.add(cantidad)
                descripcionList.add(descripcion)
                precioList.add(precio)
                totalList.add(total)

                Log.d("VALORES", "Cantidad: $cantidad, Descripción: $descripcion, Precio: $precio, Total: $total")
            }
        }

        // Aquí puedes hacer lo que necesites con las listas de datos
        // Por ejemplo, guardar en una base de datos o realizar alguna acción.
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

    private fun calcularTotal(editTextCantidad: EditText, editTextPrecio: EditText, textViewTotal: TextView) {
        val cantidad = editTextCantidad.text.toString().toDoubleOrNull() ?: 0.0
        val precio = editTextPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val total = cantidad * precio
        textViewTotal.text = total.toString()
    }


}