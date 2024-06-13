package com.fei.apartados

import android.app.Activity
import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditClient : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var aditionalEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private var id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_client)

        nameEditText = findViewById(R.id.editTextName)
        phoneEditText = findViewById(R.id.editTextPhone)
        aditionalEditText = findViewById(R.id.editTextAdditionalInfo)
        saveButton = findViewById(R.id.buttonSave)
        cancelButton = findViewById(R.id.buttonCancel)

        id = intent.getIntExtra("id", -1)

        loadData()

        saveButton.setOnClickListener {
            saveData()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun loadData() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        val query = "SELECT * FROM Cliente WHERE id = ?"
        val args = arrayOf(id.toString())
        val cursor = db.rawQuery(query, args)

        if (cursor.moveToFirst()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow("nombre"))
            val phone = cursor.getString(cursor.getColumnIndexOrThrow("telefono"))
            val aditional = cursor.getString(cursor.getColumnIndexOrThrow("informacion_adicional"))

            nameEditText.setText(name)
            phoneEditText.setText(phone)
            aditionalEditText.setText(aditional)
        }

        cursor.close()
        db.close()
    }

    private fun saveData() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val name = nameEditText.text.toString()
        val phone = phoneEditText.text.toString()
        val aditional = aditionalEditText.text.toString()

        val values = ContentValues().apply {
            put("nombre", name)
            put("telefono", phone)
            put("informacion_adicional", aditional)
        }

        val rowsUpdated = db.update("Cliente", values, "id = ?", arrayOf(id.toString()))

        if (rowsUpdated > 0) {
            Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show()
        }

        db.close()
    }
}
