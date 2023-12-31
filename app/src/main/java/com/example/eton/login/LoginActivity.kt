package com.example.eton.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.eton.R
import com.example.eton.noteList.NoteListActivity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.gotrue
import com.example.eton.supabase.Supabase
import io.github.jan.supabase.gotrue.providers.builtin.Email
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LoginActivity : AppCompatActivity() {

    private lateinit var client: SupabaseClient
    private lateinit var userId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        client = Supabase.getClient()
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            0
        )
    }

    // onLogin button clicked
    fun onLogin (view: View){
        val context = this
        val emailText: TextView = findViewById(R.id.emailText)
        val pass: TextView = findViewById(R.id.passwordText)
        var msg: String        // display toast message
        var auth = false    // check if authenticated

        // for async call to authenticate
        runBlocking {
            try {
                client.gotrue.loginWith(Email) {
                    email = emailText.text.toString()
                    password = pass.text.toString()
                }

                userId = client.gotrue.retrieveUserForCurrentSession(updateSession = true).id
                Log.i("userId", userId)

                msg = "User ${emailText.text} success!"
                auth = true
            } catch (e: Exception) {
                Log.e("Error", e.toString())
                msg = e.localizedMessage.toString().split("\n")[0] // dk if there's a better way to do this LOL
                auth = false
            }
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        lifecycleScope.launch(Dispatchers.IO) {
            // login and start new activity when authenticated
            if (auth) {
                val intent = Intent(context, NoteListActivity::class.java)
                intent.putExtra("userId", userId)
                emailText.text = ""
                pass.text = ""
                startActivity(intent)
            }
        }
    }

    fun onSignUp(view: View) {
        val emailText: TextView = findViewById(R.id.emailText)
        val pass: TextView = findViewById(R.id.passwordText)
        var msg: String

        runBlocking {
            try {
                client.gotrue.signUpWith(Email) {
                    email = emailText.text.toString()
                    password = pass.text.toString()
                }
                userId = client.gotrue.retrieveUserForCurrentSession(updateSession = true).id
                Log.i("userId", userId)

                msg = "User ${emailText.text} sign up success!"
            } catch (e: Exception) {
                msg = e.localizedMessage.toString().split("\n")[0] // dk if there's a better way to do this LOL
                Log.e("Error", e.toString())
            }
        }
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}