package com.fei.apartados

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
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
import kotlin.properties.Delegates

class EmployeeNewAppartView : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var addButton: Button
    private lateinit var removeButton: Button
    private lateinit var saveButton: Button
    private lateinit var backButton: ImageView

    private lateinit var clientEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var infoEditText: EditText
    private lateinit var firstAbEditText: EditText
    private lateinit var idEditText: EditText
    private lateinit var dateTextView: TextView
    private lateinit var linearLayout: LinearLayout
    private lateinit var loadContact: ImageView

    private lateinit var totalTextView: TextView
    private var loadClientBoolean: Boolean = false
    private var selectedClientId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_employee_new_appart_view)
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val actual = dateFormat.format(calendar.time)

        addButton = findViewById(R.id.btnAdd)
        removeButton = findViewById(R.id.btnRemove)
        saveButton = findViewById(R.id.btnSave)
        backButton = findViewById(R.id.imageViewBack)

        dateTextView = findViewById(R.id.textViewDate)
        linearLayout = findViewById(R.id.linearLayoutList)
        idEditText =  findViewById(R.id.editTextID)
        clientEditText = findViewById(R.id.editTextClient)
        phoneEditText = findViewById(R.id.editTextPhone)
        infoEditText = findViewById(R.id.editTextInfo)
        firstAbEditText =  findViewById(R.id.editTextFirstAb)
        loadContact = findViewById(R.id.imageViewLoadContact)

        totalTextView = findViewById(R.id.totalTotal)

//        insertTestData()

        idEditText.setText((searchLastId("Apartado")+1).toString())

        dateTextView.text = actual

        addItem()

        addButton.setOnClickListener {
            addItem()
        }

        removeButton.setOnClickListener {
            if (linearLayout.childCount > 0) {
                linearLayout.removeViewAt(linearLayout.childCount - 1)
            }
        }


        saveButton.setOnClickListener {
            saveData()
        }


        backButton.setOnClickListener {
            backAlert()
        }

        loadContact.setOnClickListener {
            loadClient()
        }

    }

    private fun loadClient() {
        val intent = Intent(this, LoadClient::class.java)
        startActivityForResult(intent, REQUEST_CODE_SELECT_CLIENT)
    }

    private fun searchLastId(tableName: String): Int {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val query = "SELECT MAX(id) FROM $tableName"
        val cursor = db.rawQuery(query, null)

        var lastId = -1 // Valor predeterminado si no se encuentra ningún ID

        if (cursor != null && cursor.moveToFirst()) {
            lastId = cursor.getInt(0)
        }

        cursor?.close()
        db.close()

        return lastId
    }


//    override fun onBackPressed() {
//        super.onBackPressed()
//        // Llamar a tu función backAlert() aquí
//        backAlert()
//    }

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
        val customWidget = LayoutInflater.from(this).inflate(R.layout.widget_new_appart_item, null)
        linearLayout.addView(customWidget)

        val editTextCantidad = customWidget.findViewById<EditText>(R.id.cant)
        val editTextPrecio = customWidget.findViewById<EditText>(R.id.price)
        val textViewTotal = customWidget.findViewById<TextView>(R.id.total)

        // Agregar TextWatchers
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

        calcularTotal(editTextCantidad, editTextPrecio, textViewTotal)
    }


    private fun saveData() {
        // Obtener una referencia a la base de datos en modo escritura
        val dbHelper = DatabaseHelper(this)
        val db: SQLiteDatabase = dbHelper.writableDatabase

        val cantidadList = mutableListOf<Int>()
        val descripcionList = mutableListOf<String>()
        val precioList = mutableListOf<Float>()
        val totalList = mutableListOf<Float>()
        var allTablesInsertedSuccessfully = true // Variable para realizar un seguimiento del éxito de las inserciones

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

                cantidadList.add(cantidad.toInt())
                descripcionList.add(descripcion)
                precioList.add(precio.toFloat())
                totalList.add(total.toFloat())

                Log.d("VALORES", "Cantidad: $cantidad, Descripción: $descripcion, Precio: $precio, Total: $total")
            }
        }

        val clientId: Long
        if (loadClientBoolean) {
            // Usar el ID del cliente recuperado
            clientId = selectedClientId.toLong()
        } else {
            // Crear un nuevo cliente
            val client = clientEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val info = infoEditText.text.toString()
            val valuesClient = ContentValues().apply {
                put("nombre", client)
                put("telefono", phone)
                put("informacion_adicional", info)
            }
            // Guardar cliente
            clientId = db.insert("Cliente", null, valuesClient)
            if (clientId == -1L) {
                allTablesInsertedSuccessfully = false
            }
        }

        val creationDate = dateTextView.text.toString()
        val firstAb = firstAbEditText.text.toString().toInt()
        val numberItems = cantidadList.sum()
        val status = "n"
        val remaining = totalList.sum() - firstAb
        val valuesAppart = ContentValues().apply {
            put("fecha_creacion", creationDate)
            put("cantidad_articulos", numberItems)
            put("estado", status)
            put("restante", remaining)
            put("id_cliente", clientId)
        }
        // Guardar apartado
        val appartId = db.insert("Apartado", null, valuesAppart)
        if (appartId == -1L) {
            allTablesInsertedSuccessfully = false
        }

        for (i in cantidadList.indices) {
            val values = ContentValues().apply {
                put("id_apartado", appartId)
                put("descripcion", descripcionList[i])
                put("cantidad", cantidadList[i])
                put("precio_unitario", precioList[i])
            }
            val newRowId = db.insert("Articulo", null, values)
            if (newRowId == -1L) {
                allTablesInsertedSuccessfully = false
            }
        }

        val valuesPay = ContentValues().apply {
            put("id_apartado", appartId)
            put("cantidad", firstAb)
            put("fecha", creationDate)
        }
        // Guardar abono
        val abonoId = db.insert("Abonos", null, valuesPay)
        if (abonoId == -1L) {
            allTablesInsertedSuccessfully = false
        }

        // Cerrar la base de datos después de terminar de usarla
        db.close()

        // Verificar si todas las inserciones fueron exitosas y mostrar mensajes apropiados
        if (allTablesInsertedSuccessfully) {
            Toast.makeText(this, "Todas las tablas se guardaron exitosamente", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Hubo errores al guardar las tablas", Toast.LENGTH_SHORT).show()
        }
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

        actualizarTotalAcumulado()
    }

    private fun actualizarTotalAcumulado() {
        var totalAcumulado = 0.0
        for (i in 0 until linearLayout.childCount) {
            val widgetPersonalizado = linearLayout.getChildAt(i)
            if (widgetPersonalizado is RelativeLayout) {
                val textViewTotal = widgetPersonalizado.findViewById<TextView>(R.id.total)
                val total = textViewTotal.text.toString().toDoubleOrNull() ?: 0.0
                totalAcumulado += total
            }
        }
        totalTextView.text = totalAcumulado.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_CLIENT && resultCode == Activity.RESULT_OK) {
            data?.let {
                selectedClientId = it.getIntExtra("selectedClientId", -1)
                //imprimir lo que contenga selectedClientId
                loadClientBoolean = true
                Toast.makeText(this, "Cliente seleccionado: $selectedClientId", Toast.LENGTH_SHORT).show()
                // Aquí puedes actualizar la UI con la información del cliente seleccionado
                cargarCliente()
            }
        }
    }

    @SuppressLint("Range")
    private fun cargarCliente() {
        val dbHelper = DatabaseHelper(this)
        val db: SQLiteDatabase = dbHelper.readableDatabase
        val query = "SELECT * FROM Cliente WHERE id = ?"
        val cursor = db.rawQuery(query, arrayOf(selectedClientId.toString()))
        if (cursor.moveToFirst()) {
            val clientName = cursor.getString(cursor.getColumnIndex("nombre"))
            val clientPhone = cursor.getString(cursor.getColumnIndex("telefono"))
            val clientInfo = cursor.getString(cursor.getColumnIndex("informacion_adicional"))

            clientEditText.setText(clientName)
            phoneEditText.setText(clientPhone)
            infoEditText.setText(clientInfo)

            clientEditText.isEnabled = false
            phoneEditText.isEnabled = false
            infoEditText.isEnabled = false
        }
        cursor.close()
        db.close()
    }

    companion object {
        const val REQUEST_CODE_SELECT_CLIENT = 2
    }



}