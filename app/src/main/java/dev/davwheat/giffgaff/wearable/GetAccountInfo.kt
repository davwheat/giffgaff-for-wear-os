package dev.davwheat.giffgaff.wearable

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

fun GetAccountInfo(
    token: String,
    context: Context,
    callback: (data: AccountInfo?) -> Unit
): Boolean {
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
                      __typename
                    }
                    balance {
                      data
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

                        myGoodybag = Goodybag(
                            currentGoodybag.getString("sku"),
                            currentGoodybag.getJSONObject("expiresIn").getInt("days"),
                            currentGoodybag.getJSONObject("expiresIn").getInt("hours"),
                            currentGoodybag.getBoolean("reserve"),
                            currentGoodybag.getInt("price").toDouble() / 100,
                            currentGoodybag.getJSONObject("allowance").getInt("data"),
                            currentGoodybag.getJSONObject("allowance").getInt("reserve"),
                            currentGoodybag.getJSONObject("balance").getInt("data"),
                            currentGoodybag.getJSONObject("balance").getInt("reserve")
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

    return true
}