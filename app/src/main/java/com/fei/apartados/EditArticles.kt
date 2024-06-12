package com.fei.apartados

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class EditArticles : AppCompatActivity() {

    private lateinit var addButton: Button
    private lateinit var removeButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private lateinit var linearLayout: LinearLayout

    private var id: Int? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_articles)

        addButton = findViewById(R.id.btnAdd)
        removeButton = findViewById(R.id.btnRemove)
        saveButton = findViewById(R.id.btnSave)
        cancelButton = findViewById(R.id.btnCancel)

        linearLayout = findViewById(R.id.linearLayoutList)

        addButton.setOnClickListener { addItem() }
        removeButton.setOnClickListener { removeItem() }
        saveButton.setOnClickListener { saveItems() }
        cancelButton.setOnClickListener { cancelEdit() }

        id = intent.getIntExtra("id", -1)

        loadData()
    }

    @SuppressLint("Range")
    private fun loadData() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase
        val query = "SELECT cantidad, descripcion, precio_unitario " +
                "FROM Articulo " +
                "WHERE id_apartado = ?"

        val args = arrayOf(id.toString())
        val cursor = db.rawQuery(query, args)

        while (cursor.moveToNext()) {
            val cantidad = cursor.getInt(cursor.getColumnIndex("cantidad"))
            val nombreArticulo = cursor.getString(cursor.getColumnIndex("descripcion"))
            val precioArticulo = cursor.getFloat(cursor.getColumnIndex("precio_unitario"))

            val customWidget = LayoutInflater.from(this).inflate(R.layout.widget_new_appart_item, null)

            val editTextCantidad = customWidget.findViewById<EditText>(R.id.cant)
            val editTextNombre = customWidget.findViewById<EditText>(R.id.desc)
            val editTextPrecio = customWidget.findViewById<EditText>(R.id.price)
            val textViewTotal = customWidget.findViewById<TextView>(R.id.total)

            editTextCantidad.setText(cantidad.toString())
            editTextNombre.setText(nombreArticulo)
            editTextPrecio.setText(precioArticulo.toString())
            calcularTotal(editTextCantidad, editTextPrecio, textViewTotal)

            linearLayout.addView(customWidget)

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
        }

        cursor.close()
        db.close()
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
    }

    private fun removeItem() {
        if (linearLayout.childCount > 0) {
            linearLayout.removeViewAt(linearLayout.childCount - 1)
        }
    }

    private fun calcularTotal(editTextCantidad: EditText, editTextPrecio: EditText, textViewTotal: TextView) {
        val cantidad = editTextCantidad.text.toString().toDoubleOrNull() ?: 0.0
        val precio = editTextPrecio.text.toString().toDoubleOrNull() ?: 0.0
        val total = cantidad * precio
        textViewTotal.text = total.toString()
    }

    private fun saveItems() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase

        // Eliminar los registros antiguos
        db.execSQL("DELETE FROM Articulo WHERE id_apartado = ?", arrayOf(id.toString()))

        // Insertar los registros actualizados
        for (i in 0 until linearLayout.childCount) {
            val widget = linearLayout.getChildAt(i)
            val editTextCantidad = widget.findViewById<EditText>(R.id.cant)
            val editTextNombre = widget.findViewById<EditText>(R.id.desc)
            val editTextPrecio = widget.findViewById<EditText>(R.id.price)

            val cantidad = editTextCantidad.text.toString().toInt()
            val nombre = editTextNombre.text.toString()
            val precio = editTextPrecio.text.toString().toFloat()

            val contentValues = ContentValues().apply {
                put("id_apartado", id)
                put("cantidad", cantidad)
                put("descripcion", nombre)
                put("precio_unitario", precio)
            }

            db.insert("Articulo", null, contentValues)
        }

        db.close()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun cancelEdit() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
