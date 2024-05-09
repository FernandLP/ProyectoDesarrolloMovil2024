package com.fei.apartados

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class LoginActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private lateinit var loginButton: Button
    private lateinit var editTextUser: EditText
    private lateinit var editTextPassword: EditText
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
        //Implementar logica para el login recuperando informacion de una base de datos
        val intent = Intent(this, EmployeeMainListActivity::class.java)
        startActivity(intent)
    }
}