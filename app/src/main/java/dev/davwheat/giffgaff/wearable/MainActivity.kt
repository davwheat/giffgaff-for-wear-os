package dev.davwheat.giffgaff.wearable

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment


class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var preferences: SharedPreferences

    fun onInvalidToken() {
        navController.navigate(R.id.action_loadingFragment_to_loginNoticeFragment)
    }

    fun onValidToken(membername: String, password: String, token: String) {
        Toast.makeText(
            baseContext,
            getString(R.string.welcome_member, membername),
            Toast.LENGTH_LONG
        )
            .show()

        preferences.edit()
            .putString("membername", membername)
            .putString("password", password)
            .putString("token", token)
            .apply()

        navController.navigate(R.id.action_loadingFragment_to_accountDetailsFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        preferences = getPreferences(Context.MODE_PRIVATE)

        if (preferences.contains("membername") && preferences.contains("password")) {
            val membername = preferences.getString("membername", "").toString()
            val password = preferences.getString("password", "").toString()
            val token = preferences.getString("token", "").toString()

            if (token != "") {
                Log.d("Initial Load", "Checking validity of existing token...")
                IsTokenValid(token, baseContext) { isValid, _ ->
                    if (isValid) {
                        // Valid token
                        Log.d("Initial Load", "Token is valid. Continuing...")
                        onValidToken(membername, password, token)
                    } else {
                        FetchNewToken(membername, password)
                    }
                }
            } else {
                FetchNewToken(membername, password)
            }
        } else {
            onInvalidToken()
        }
    }

    fun FetchNewToken(membername: String, password: String) {
        Log.d("giffgaffWear", "Logging in as $membername with saved password")

        makeTokenRequest(membername, password, baseContext) { token ->
            // Callback when we get response
            if (token == null) {
                // Invalid/no token
                Toast.makeText(
                    baseContext,
                    getString(R.string.token_invalid_on_launch),
                    Toast.LENGTH_LONG
                )
                    .show()

                onInvalidToken()
            } else {
                // Valid token
                onValidToken(membername, password, token)
            }
        }
    }
}