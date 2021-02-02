package dev.davwheat.giffgaff.wearable

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

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

    fun GetAccountInfo(
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

    fun IsTokenValid(
        token: String,
        context: Context,
        callback: (isValid: Boolean, token: String) -> Unit
    ) {
        dev.davwheat.giffgaff.wearable.IsTokenValid(token, context, callback)
    }

    fun makeTokenRequest(
        username: String,
        password: String,
        context: Context,
        callback: (token: String?) -> Unit
    ) {
        dev.davwheat.giffgaff.wearable.makeTokenRequest(username, password, context, callback)
    }
}