package dev.davwheat.giffgaff.wearable

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject


fun IsTokenValid(
    token: String, context: Context,
    callback: (isValid: Boolean, token: String) -> Unit
): Boolean {
    // If testing token, it's valid!
    if (token == context.getString(R.string.testing_token)) {
        callback.invoke(true, token)
        return true
    }

    val GIFFGAFF_GQL_API_URL = context.getString(R.string.api_graphql_endpoint)

    val QUERY =
        """query checkTokenValidity {
              member {
                  memberName
                  __typename
              }
          }""".trimIndent()

    val queue = Volley.newRequestQueue(context)
    queue.cache.clear();

    val body = JSONObject()
    body.put("query", QUERY)

    val accountInfo = object : JsonObjectRequest(
        Request.Method.POST,
        GIFFGAFF_GQL_API_URL,
        body,
        Response.Listener { response: JSONObject ->
            if (response.has("data")) {
                // Received response -- current token is still valid
                callback.invoke(true, token)
            } else {
                Log.d(
                    "giffgaff Wear",
                    "Existing token is no longer valid."
                )
                callback.invoke(false, token)
            }
        },
        Response.ErrorListener { error ->
            callback.invoke(false, token)
        }) {
        override fun getHeaders(): Map<String, String> {
            val headers = HashMap<String, String>()

            headers["Authorization"] = "Bearer $token"

            headers["user-agent"] = context.getString(R.string.api_user_agent)
            headers["x-request-info"] = context.getString(R.string.api_x_request_info)
            headers["x-contact-info"] = context.getString(R.string.api_x_contact_info)

            return headers
        }
    }

    queue.add(accountInfo)

    return true
}