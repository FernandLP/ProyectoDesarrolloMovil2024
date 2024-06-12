package com.fei.apartados

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.text.SimpleDateFormat
import java.util.Date

class PayPayment : AppCompatActivity() {

    private lateinit var montoTextView: EditText
    private lateinit var restanteTextView: TextView
    private lateinit var allButton: Button
    private lateinit var saveButton: Button

    private var id: Int? = null
    private var restante: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_payment)

        montoTextView = findViewById(R.id.editTextMonto)
        restanteTextView = findViewById(R.id.textViewRestante)
        allButton = findViewById(R.id.buttonAll)
        saveButton = findViewById(R.id.buttonSave)

        id = intent.getIntExtra("id", -1)
        restante = intent.getFloatExtra("restante", 0f)
        restanteTextView.text = restante.toString()

        allButton.setOnClickListener {
            montoTextView.setText(restante.toString())
        }

        saveButton.setOnClickListener {
            savePay()
        }

    }

    @SuppressLint("SimpleDateFormat")
    private fun savePay() {
        val monto = montoTextView.text.toString().toFloatOrNull()

        if (monto != null && monto > 0 && monto <= restante) {
            // Actualizar el restante
            restante -= monto
            restanteTextView.text = restante.toString()

            val dbHelper = DatabaseHelper(this)
            val db = dbHelper.writableDatabase

            // Crear un nuevo registro en la base de datos en la tabla Abonos
            val contentValues = ContentValues()
            contentValues.put("id_apartado", id)
            contentValues.put("cantidad", monto)

            // Obtener la fecha actual en el formato "dd/MM/yyyy"
            val currentDate = SimpleDateFormat("dd/MM/yyyy").format(Date())
            contentValues.put("fecha", currentDate)

            db.insert("Abonos", null, contentValues)

            // Actualizar el campo restante de la tabla Apartado
            val updateValues = ContentValues()
            updateValues.put("restante", restante)

            val whereClause = "id = ?"
            val whereArgs = arrayOf(id.toString())

            db.update("Apartado", updateValues, whereClause, whereArgs)

            db.close()

            Toast.makeText(this, "Pago guardado correctamente", Toast.LENGTH_SHORT).show()

            setResult(Activity.RESULT_OK)
            finish()
        } else {
            // Mostrar mensaje de error
            montoTextView.error = "Monto inválido, debe ser mayor a 0 y menor o igual al restante"
        }
    }


}