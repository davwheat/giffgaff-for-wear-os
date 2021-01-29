package dev.davwheat.giffgaff.wearable

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

fun makeTokenRequest(
    username: String,
    password: String,
    context: Context,
    callback: (token: String?) -> Unit
): Boolean {
    // Intercepted with a MITM proxy watching HTTP requests from the giffgaff app
    // This token is what permits us to generate Bearer tokens for users
    val GIFFGAFF_MASTER_AUTH_TOKEN = context.getString(R.string.api_oauth_master_token)

    // OAuth API
    val GIFFGAFF_OAUTH_URL = context.getString(R.string.api_oauth_endpoint)

    val queue = Volley.newRequestQueue(context)
    queue.cache.clear();

    val tokenRequest = object : StringRequest(Request.Method.POST,
        GIFFGAFF_OAUTH_URL,
        Response.Listener { strResponse ->
            val response = JSONObject(strResponse)

            Log.d("giffgaff Wear", response.toString(2))
            if (response.has("access_token")) {
                callback.invoke(response.getString("access_token"))
            } else {
                callback.invoke(null)
            }
        },
        Response.ErrorListener { error ->
            Log.d("giffgaff Wear", error.toString())
            callback.invoke(null)
        }) {
        override fun getHeaders(): Map<String, String> {
            val headers = HashMap<String, String>()

            headers["authorization"] = GIFFGAFF_MASTER_AUTH_TOKEN
            headers["accept"] = "application/json"

            headers["user-agent"] = context.getString(R.string.api_user_agent)
            headers["x-request-info"] = context.getString(R.string.api_x_request_info)
            headers["x-contact-info"] = context.getString(R.string.api_x_contact_info)

            return headers
        }

        override fun getParams(): Map<String, String> {
            val params = HashMap<String, String>()

            params["scope"] = "read"
            params["grant_type"] = "password"
            params["username"] = username
            params["password"] = password

            return params
        }
    }

    queue.add(tokenRequest)

    return true
}