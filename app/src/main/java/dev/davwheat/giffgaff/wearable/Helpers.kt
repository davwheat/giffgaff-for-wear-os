package dev.davwheat.giffgaff.wearable

import android.content.Context
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import javax.net.ssl.SSLHandshakeException

class Helpers {
    companion object {
        fun create(): Helpers = Helpers()
    }

    fun formatPhoneNumber(number: String): String {
        if (number.startsWith("07")) {
            return "${number.substring(0, 5)} ${number.substring(5)}"
        }

        return number;
    }

    fun getAccountInfo(
        token: String,
        context: Context,
        callback: (data: AccountInfo?) -> Unit
    ) {
        val GIFFGAFF_GQL_API_URL = context.getString(R.string.api_graphql_endpoint)

        val QUERY =
            """query getAccountInfo {
              member {
                sim {
                  phoneNumber
                }
                credit {
                  amount
                  freeGiffgaff {
                    enabled
                    expiryDate
                    __typename
                  }
                  current {
                    sku
                    expiresIn {
                      days
                      hours
                      __typename
                    }
                    reserve
                    price
                    allowance {
                      data
                      reserve
                      unlimitedData
                      __typename
                    }
                    balance {
                      data
                      reserve
                      __typename
                    }
                    __typename
                  }
                  __typename
                }
                __typename
              }
            }""".trimIndent()


        val queue = Volley.newRequestQueue(context)
        queue.cache.clear();

        val body = JSONObject()
        body.put("query", QUERY)

        val accountInfo = object : JsonObjectRequest(
            Method.POST,
            GIFFGAFF_GQL_API_URL,
            body,
            Response.Listener { response: JSONObject ->
                if (response.has("data")) {
                    var myGoodybag: Goodybag? = null;

                    if (response.optString("error") != "") {
                        Log.d(
                            "giffgaff Wear",
                            "Error when fetching acct data:" + response.getString("error")
                        )
                        callback.invoke(null)

                    } else {
                        val data: JSONObject = response.getJSONObject("data")
                            .getJSONObject("member")
                            .getJSONObject("credit")

                        if (data.optJSONObject("current") !== null) {
                            var currentGoodybag = data.getJSONObject("current")

                            val hasReserve = currentGoodybag.getBoolean("reserve")

                            myGoodybag = Goodybag(
                                // SKU
                                currentGoodybag.getString("sku"),
                                // Expires in (days)
                                currentGoodybag.getJSONObject("expiresIn").getInt("days"),
                                // Expires in (hours)
                                currentGoodybag.getJSONObject("expiresIn").getInt("hours"),
                                // Has reservetank
                                hasReserve,
                                // Price (£)
                                currentGoodybag.getInt("price").toDouble() / 100,
                                // Data allowance
                                currentGoodybag.getJSONObject("allowance").getInt("data"),
                                // Reservetank allowance
                                if (hasReserve)
                                    currentGoodybag.getJSONObject("allowance")
                                        .getInt("reserve") else 0,
                                // Data remaining
                                currentGoodybag.getJSONObject("balance").getInt("data"),
                                // Data remaining
                                currentGoodybag.getJSONObject("balance").optInt("reserve", -1),
                                // Is unlimited data
                                currentGoodybag.getJSONObject("allowance")
                                    .getBoolean("unlimitedData"),
                            )
                        }

                        var acctInfo = AccountInfo(
                            data.getInt("amount"),
                            data.getJSONObject("freeGiffgaff").getBoolean("enabled"),
                            data.getJSONObject("freeGiffgaff").optString("expiryDate"),
                            myGoodybag,
                            phoneNumber = response.getJSONObject("data")
                                .getJSONObject("member")
                                .getJSONObject("sim")
                                .optString("phoneNumber")
                        )

                        callback.invoke(acctInfo)
                    }

                } else {
                    Log.d(
                        "giffgaff Wear",
                        "Error when fetching acct data: invalid response"
                    )
                    callback.invoke(null)
                }
            },
            Response.ErrorListener { error ->
                Log.d("ERR/acct data", error.toString())
                callback.invoke(null)
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
    }

    fun isTokenValid(
        token: String,
        context: Context,
        callback: (isValid: Boolean, token: String) -> Unit
    ) {
        // If testing token, it's valid!
        if (token == context.getString(R.string.testing_token)) {
            callback.invoke(true, token)
            return
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
            Method.POST,
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
                Log.d("ERR/token valid", error.toString())
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
    }

    fun makeTokenRequest(
        username: String,
        password: String,
        context: Context,
        callback: (success: Boolean, token: String, errorString: String) -> Unit
    ) {
        // Intercepted with a MITM proxy watching HTTP requests from the giffgaff app
        // This token is what permits us to generate Bearer tokens for users
        val GIFFGAFF_MASTER_AUTH_TOKEN = context.getString(R.string.api_oauth_master_token)

        // OAuth API
        val GIFFGAFF_OAUTH_URL = context.getString(R.string.api_oauth_endpoint)

        val queue = Volley.newRequestQueue(context)
        queue.cache.clear();

        val tokenRequest = object : StringRequest(
            Method.POST,
            GIFFGAFF_OAUTH_URL,
            Response.Listener { strResponse ->
                val response = JSONObject(strResponse)

                Log.d("giffgaff Wear", response.toString(2))
                if (response.has("access_token")) {
                    callback.invoke(true, response.getString("access_token"), "")
                } else {
                    callback.invoke(
                        false,
                        "",
                        context.getString(R.string.sign_in_invalid_details_message)
                    )
                }
            },
            Response.ErrorListener { error ->
                Log.d("giffgaff Wear", error.toString())

                if (error.cause is SSLHandshakeException) {
                    callback.invoke(false, "", context.getString(R.string.api_invalid_ssl))
                } else if (error.cause is AuthFailureError) {
                    callback.invoke(
                        false,
                        "",
                        context.getString(R.string.sign_in_invalid_details_message)
                    )
                } else {
                    callback.invoke(false, "", context.getString(R.string.api_unknown_error));
                }
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
    }
}