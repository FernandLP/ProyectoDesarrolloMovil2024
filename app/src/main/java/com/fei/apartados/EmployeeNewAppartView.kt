package com.fei.apartados

import android.annotation.SuppressLint
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
    private lateinit var firstAbEditText: EditText

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
        firstAbEditText =  findViewById(R.id.editTextFirstAb)

//        Esta funcion inserta datos para hacer pruebas
//        como no hay boton para esta funcion, quitar el comentario solo una ves y compilar
//        despues cerrar y volver a comentar la funcion
//        primero, compilar sin comentarios
//        segundo, cerrar aplicacion y comentar la funcion
//        tercero, volver a compilar
        //insertTestData()

        searchLastId()

        dateTextView.text = actual


        addButton.setOnClickListener {
            addItem()
        }


        saveButton.setOnClickListener {
            saveData()
        }


        backButton.setOnClickListener {
            backAlert()
        }

    }

    private fun searchLastId() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val query = "SELECT MAX(id) FROM Apartado"
        val cursor = db.rawQuery(query, null)

        if (cursor != null && cursor.moveToFirst()) {
            val lastId = cursor.getInt(0)
            var num = lastId.toInt() + 1;
            idEditText.setText(num.toString())
        }

        cursor?.close()
        db.close()
    }

    override fun onBackPressed() {
        // Llamar a tu función backAlert() aquí
        backAlert()
    }

    private fun insertTestData() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase

        // Insertar datos en la tabla Cliente
        db.execSQL("INSERT INTO Cliente (nombre, telefono) VALUES ('Eduardo', '2288123456')")
        db.execSQL("INSERT INTO Cliente (nombre, telefono, informacion_adicional) VALUES ('Miguel', '2281345678', 'avity@game.com')")
        db.execSQL("INSERT INTO Cliente (nombre, telefono) VALUES ('Daniel', '8884987654')")

        // Insertar datos en la tabla Apartado
        db.execSQL("INSERT INTO Apartado (fecha_creacion, cantidad_articulos, estado, fecha_tolerancia, restante, id_cliente) VALUES ('08/05/2024', 2, 'Activo', '08/05/2024', 0.0, 1)")
        db.execSQL("INSERT INTO Apartado (fecha_creacion, cantidad_articulos, estado, fecha_tolerancia, restante, id_cliente) VALUES ('08/05/2024', 2, 'Activo', '08/05/2024', 0.0, 2)")

        // Insertar datos en la tabla Articulo
        db.execSQL("INSERT INTO Articulo (id_apartado, descripcion, cantidad, precio_unitario) VALUES (1, 'Playera', 1, 80.0)")
        db.execSQL("INSERT INTO Articulo (id_apartado, descripcion, cantidad, precio_unitario) VALUES (1, 'Pantalon', 1, 120.0)")
        db.execSQL("INSERT INTO Articulo (id_apartado, descripcion, cantidad, precio_unitario) VALUES (1, 'Camisa', 1, 170.0)")
        db.execSQL("INSERT INTO Articulo (id_apartado, descripcion, cantidad, precio_unitario) VALUES (1, 'Pans', 1, 130.0)")
        db.execSQL("INSERT INTO Articulo (id_apartado, descripcion, cantidad, precio_unitario) VALUES (2, 'Chamarra', 1, 320.0)")
        db.execSQL("INSERT INTO Articulo (id_apartado, descripcion, cantidad, precio_unitario) VALUES (2, 'Playera', 1, 120.0)")

        // Insertar datos en la tabla Abonos
        db.execSQL("INSERT INTO Abonos (id_apartado, cantidad, fecha) VALUES (1, 50.0, '08/05/2024')")
        db.execSQL("INSERT INTO Abonos (id_apartado, cantidad, fecha) VALUES (2, 290.0, '08/05/2024')")

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

    private fun backAlert() {
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