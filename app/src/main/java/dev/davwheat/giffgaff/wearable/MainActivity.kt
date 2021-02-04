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

    private fun onInvalidToken() {
        navController.navigate(R.id.action_loadingFragment_to_loginNoticeFragment)
    }

    private fun onValidToken(membername: String, password: String, token: String) {
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
                Helpers().isTokenValid(token, baseContext) { isValid, _ ->
                    if (isValid) {
                        // Valid token
                        Log.d("Initial Load", "Token is valid. Continuing...")
                        onValidToken(membername, password, token)
                    } else {
                        fetchNewToken(membername, password)
                    }
                }
            } else {
                fetchNewToken(membername, password)
            }
        } else {
            onInvalidToken()
        }
    }

    private fun fetchNewToken(membername: String, password: String) {
        Log.d("giffgaffWear", "Logging in as $membername with saved password")

        Helpers().makeTokenRequest(
            membername,
            password,
            baseContext
        ) { success, token, errorString ->
            // Callback when we get response
            if (!success) {
                // Invalid SSL is a user-fixable issue, so let's not invalidate
                // the login info, but instead inform the user of the issue.
                if (errorString == getString(R.string.api_invalid_ssl)) {
                    navController.navigate(R.id.action_loadingFragment_to_invalidSslErrorHelp)
                    return@makeTokenRequest;
                }

                // Invalid/no token
                Log.d("giffgaffWear", "Failed to log in with saved credentials")
                Toast.makeText(
                    baseContext,
                    if (errorString === getString(R.string.api_unknown_error)) getString(R.string.token_invalid_on_launch) else errorString,
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