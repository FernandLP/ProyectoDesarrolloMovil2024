package com.fei.apartados

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class EmployeeAppartView : AppCompatActivity() {
    private lateinit var buttonBack: ImageView
    private lateinit var floatingActionButton: FloatingActionButton

    private lateinit var idTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var toleranceTextView: TextView
    private lateinit var clientTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var adicionalTextView: TextView

    private lateinit var totalTextView: TextView
    private var total: Float = 0f
    private lateinit var abonadoTextView: TextView
    private var abonado: Float = 0f
    private lateinit var restanteTextView: TextView
    private var restante: Float = 0f

    private lateinit var llItemsContainer: LinearLayout

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

        totalTextView = findViewById(R.id.textViewTotal)
        abonadoTextView = findViewById(R.id.textViewAbonado)
        restanteTextView = findViewById(R.id.textViewRestante)

        llItemsContainer = findViewById(R.id.ll_items_container)

        id = intent.getIntExtra("id", -1)

        loadData()

        floatingActionButton.setOnClickListener{
            pupopActionMenu(it)
        }

        buttonBack.setOnClickListener {
            finish()
//            backAllert()
        }
    }

    @SuppressLint("Range", "MissingInflatedId")
    private fun loadData() {
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.readableDatabase

        // Inicializar las variables
        total = 0f
        abonado = 0f
        restante = 0f

        // Consulta para obtener información del Apartado y del Cliente
        val query = "SELECT Apartado.fecha_creacion, Apartado.fecha_tolerancia, Cliente.nombre, Cliente.telefono, Cliente.informacion_adicional " +
                "FROM Apartado " +
                "INNER JOIN Cliente ON Apartado.id_cliente = Cliente.id " +
                "WHERE Apartado.id = ?"
        val args = arrayOf(id.toString())
        val cursor = db.rawQuery(query, args)

        if (cursor.moveToFirst()) {
            val fechaCreacion = cursor.getString(cursor.getColumnIndex("fecha_creacion"))
            val fechaTolerancia = cursor.getString(cursor.getColumnIndex("fecha_tolerancia"))
            val nombreCliente = cursor.getString(cursor.getColumnIndex("nombre"))
            val telefonoCliente = cursor.getString(cursor.getColumnIndex("telefono"))
            val adicionalCliente = cursor.getString(cursor.getColumnIndex("informacion_adicional"))

            idTextView.text = id.toString()
            dateTextView.text = fechaCreacion
            toleranceTextView.text = fechaTolerancia
            clientTextView.text = nombreCliente
            phoneTextView.text = telefonoCliente
            adicionalTextView.text = adicionalCliente
        } else {
            Toast.makeText(this, "No se encontraron registros para el ID proporcionado", Toast.LENGTH_SHORT).show()
        }
        cursor.close()

        // Consulta para obtener los artículos del Apartado
        val queryArticulos = "SELECT cantidad, descripcion, precio_unitario " +
                "FROM Articulo " +
                "WHERE id_apartado = ?"
        val cursorArticulos = db.rawQuery(queryArticulos, args)

        // Obtener referencia al LinearLayout
        val llItemsContainer = findViewById<LinearLayout>(R.id.ll_items_container)
        llItemsContainer.removeAllViews() // Limpiar el contenedor antes de añadir nuevos elementos

        while (cursorArticulos.moveToNext()) {
            val cantidad = cursorArticulos.getInt(cursorArticulos.getColumnIndex("cantidad"))
            val nombreArticulo = cursorArticulos.getString(cursorArticulos.getColumnIndex("descripcion"))
            val precioArticulo = cursorArticulos.getFloat(cursorArticulos.getColumnIndex("precio_unitario"))

            // Inflar la vista de cada elemento desde el archivo de diseño
            val itemView = LayoutInflater.from(this).inflate(R.layout.widget_view_item, llItemsContainer, false)

            // Configurar los componentes de la vista con los datos del artículo
            val textViewCantidad = itemView.findViewById<TextView>(R.id.cant)
            val textViewDescripcion = itemView.findViewById<TextView>(R.id.desc)
            val textViewPrecio = itemView.findViewById<TextView>(R.id.price)
            val textViewTotal = itemView.findViewById<TextView>(R.id.total)

            textViewCantidad.text = cantidad.toString()
            textViewDescripcion.text = nombreArticulo
            textViewPrecio.text = precioArticulo.toString()
            textViewTotal.text = (cantidad * precioArticulo).toString()

            // Sumar en la variable total el resultado de multiplicar cantidad por precioArticulo
            total = total?.plus(cantidad * precioArticulo)!!

            // Añadir la vista configurada al contenedor
            llItemsContainer.addView(itemView)
        }

        cursorArticulos.close()

        val queryHistorial = "SELECT * " +
                "FROM Abonos " +
                "WHERE id_apartado = ?"
        val cursorHistorial = db.rawQuery(queryHistorial, args)

        val llHistoryContainer = findViewById<LinearLayout>(R.id.linearLayoutListAVH)
        llHistoryContainer.removeAllViews() // Limpiar el contenedor antes de añadir nuevos elementos

        while (cursorHistorial.moveToNext()) {
            val fechaAbono = cursorHistorial.getString(cursorHistorial.getColumnIndex("fecha"))
            val montoAbono = cursorHistorial.getFloat(cursorHistorial.getColumnIndex("cantidad"))

            val itemView = LayoutInflater.from(this).inflate(R.layout.widget_view_history, llHistoryContainer, false)

            val textViewFecha = itemView.findViewById<TextView>(R.id.date)
            val textViewMonto = itemView.findViewById<TextView>(R.id.amount)

            textViewFecha.text = fechaAbono
            textViewMonto.text = montoAbono.toString()

            // Sumar en la variable abonado las cantidades recuperadas
            abonado = abonado?.plus(montoAbono)!!

            // Añadir la vista configurada al contenedor
            llHistoryContainer.addView(itemView)
        }
        cursorHistorial.close()
        db.close()

        // Actualizar los TextViews
        totalTextView.text = total.toString()
        abonadoTextView.text = abonado.toString()
        restante = total?.minus(abonado!!)!!
        restanteTextView.text = restante.toString()
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
                    val intent = Intent(this, PayPayment::class.java)
                    intent.putExtra("id", id)
                    intent.putExtra("restante", restante)
                    startActivityForResult(intent, REQUEST_CODE)
                    true
                }
                R.id.opc2 -> {
                    val intent = Intent(this, EditArticles::class.java)
                    intent.putExtra("id", id)
                    startActivityForResult(intent, REQUEST_CODE)
                    true
                }
                R.id.opc3 -> {
                    // Crear un AlertDialog para confirmar la eliminación
                    AlertDialog.Builder(this)
                        .setTitle("Confirmar eliminación")
                        .setMessage("¿Estás seguro de que deseas eliminar este registro? Esta acción no se puede deshacer.")
                        .setPositiveButton("Eliminar") { dialog, which ->
                            // Eliminar el registro de la base de datos con el id
                            val dbHelper = DatabaseHelper(this)
                            val db = dbHelper.writableDatabase

                            // Comenzar una transacción
                            db.beginTransaction()
                            try {
                                // Eliminar filas en la tabla Abonos
                                val selectionAbonos = "id_apartado = ?"
                                val selectionArgsAbonos = arrayOf(id.toString())
                                db.delete("Abonos", selectionAbonos, selectionArgsAbonos)

                                // Eliminar filas en la tabla Articulo
                                val selectionArticulo = "id_apartado = ?"
                                val selectionArgsArticulo = arrayOf(id.toString())
                                db.delete("Articulo", selectionArticulo, selectionArgsArticulo)

                                // Eliminar el registro en la tabla Apartado
                                val selectionApartado = "id = ?"
                                val selectionArgsApartado = arrayOf(id.toString())
                                val deletedRows = db.delete("Apartado", selectionApartado, selectionArgsApartado)

                                // Marcar la transacción como exitosa
                                db.setTransactionSuccessful()

                                // Mostrar mensaje al usuario
                                if (deletedRows > 0) {
                                    Toast.makeText(this, "Registro eliminado con éxito", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(this, "No se encontró el registro para eliminar", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                // Mostrar mensaje de error
                                Toast.makeText(this, "Error al eliminar el registro", Toast.LENGTH_SHORT).show()
                            } finally {
                                // Finalizar la transacción
                                db.endTransaction()
                                db.close()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        }
                        .setNegativeButton("Cancelar", null) // No hacer nada si el usuario cancela
                        .show()
                    true
                }

                R.id.opc4 -> {
                    showDatePickerDialog()
                    true
                }

                // Agregar más casos según sea necesario
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showDatePickerDialog() {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                // Aquí se ejecuta cuando el usuario selecciona la fecha y presiona "OK"
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)

                // Formatear la fecha seleccionada al formato dd/MM/yyyy
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedDate.time)

                // Actualizar la base de datos con la fecha seleccionada
                updateFechaTolerancia(formattedDate)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000 // Restringir seleccionar fechas pasadas
        datePickerDialog.show()
    }

    private fun updateFechaTolerancia(fechaTolerancia: String) {
        // Aquí debes implementar la lógica para actualizar la base de datos
        val dbHelper = DatabaseHelper(this)
        val db = dbHelper.writableDatabase

        val values = ContentValues().apply {
            put("fecha_tolerancia", fechaTolerancia)
        }

        val selection = "id = ?"
        val selectionArgs = arrayOf(id.toString())
        val updatedRows = db.update("Apartado", values, selection, selectionArgs)

        db.close()

        if (updatedRows > 0) {
            Toast.makeText(this, "Fecha de tolerancia actualizada correctamente", Toast.LENGTH_SHORT).show()
            loadData()
        } else {
            Toast.makeText(this, "Error al actualizar la fecha de tolerancia", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Aquí llamamos a loadData para actualizar la información
            loadData()
        }
    }

    companion object {
        const val REQUEST_CODE = 1
    }

}