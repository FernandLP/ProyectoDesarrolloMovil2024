package com.fei.apartados

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var loginButton: Button /*boton para iniciar sesion*/
    private lateinit var editTextUser: EditText /*texto para el usuario*/
    private lateinit var editTextPassword: EditText /*texto para la contraseña*/
    private lateinit var textView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        loginButton = findViewById(R.id.loginButton)
        editTextUser = findViewById(R.id.editTextUser)
        editTextPassword = findViewById(R.id.editTextPassword)
        textView = findViewById(R.id.textViewMessage)

        loginButton.setOnClickListener{
            login()
        }
    }

    private fun login() {

        val user = editTextUser.text.toString()
        val password = editTextPassword.text.toString()

        if (user == "Eduardo") {
            if (password == "1234") {
                val intent = Intent(this, EmployeeAppartView::class.java)
                intent.putExtra("user", user)

                startActivity(intent)
            }else {
                Toast.makeText(this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show()
            }
        }else {
            Toast.makeText(this, "Nombre incorrecto", Toast.LENGTH_SHORT).show()
        }
        //Implementar logica para el login recuperando informacion de una base de datos
        val intent = Intent(this, EmployeeAppartView::class.java)
        startActivity(intent)

        /* val intent = Intent(this, EmployeeMainListActivity::class.java)
        startActivity(intent) */
    }



}