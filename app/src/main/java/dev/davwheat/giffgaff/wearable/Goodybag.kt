package dev.davwheat.giffgaff.wearable

import kotlin.math.round

class Goodybag(
    val sku: String,
    val expiresInDays: Int,
    val expiresInHours: Int,
    val hasReserve: Boolean,
    val price: Double,

    val dataAllowance: Int,
    val reserveAllowance: Int?,

    val dataRemaining: Int,
    val reserveRemaining: Int?,
) {
    private fun ToGB(x: Double): Double {
        return round(x / 1024 / 1024 * 100) / 100
    }

    val dataAllowanceGB: Double
        get() {
            return if (hasReserve) {
                ToGB((dataAllowance + reserveAllowance!!).toDouble())
            } else {
                ToGB(dataAllowance.toDouble())
            }
        }

    val dataRemainingGB: Double
        get() {
            return if (hasReserve) {
                ToGB((dataRemaining + reserveRemaining!!).toDouble())
            } else {
                ToGB(dataRemaining.toDouble())
            }
        }

    val goodybagDrawableId: Int
        get() {

            if (hasReserve) {
                return when (sku) {
                    // £10 goodybag
                    "BD036" -> R.drawable.goodybag_10_reservetank

                    // £10 golden goodybag
                    "BD043" -> R.drawable.goodybag_10_golden_reservetank

                    // £12 goodybag
                    "BD037" -> R.drawable.goodybag_12_reservetank

                    // £15 goodybag
                    "BD038" -> R.drawable.goodybag_15_reservetank

                    // £15 golden goodybag
                    "BD044" -> R.drawable.goodybag_15_golden_reservetank

                    // £20 goodybag
                    "BD041" -> R.drawable.goodybag_20_reservetank

                    // £20 golden goodybag
                    // "BD045" -> R.drawable.goodybag_20_golden

                    // Unknown!
                    else -> R.drawable.goodybag_blank
                }
            }

            return when (sku) {
                // £6 goodybag
                "BD039" -> R.drawable.goodybag_6

                // £8 goodybag
                "BD040" -> R.drawable.goodybag_8

                // £10 goodybag
                "BD036" -> R.drawable.goodybag_10

                // £10 golden goodybag
                "BD043" -> R.drawable.goodybag_10_golden

                // £12 goodybag
                "BD037" -> R.drawable.goodybag_12

                // £15 goodybag
                "BD038" -> R.drawable.goodybag_15

                // £15 golden goodybag
                "BD044" -> R.drawable.goodybag_15_golden

                // £20 goodybag
                "BD041" -> R.drawable.goodybag_20

                // £20 golden goodybag
                "BD045" -> R.drawable.goodybag_20_golden

                // £25 goodybag
                "BD042" -> R.drawable.goodybag_25

                // £35 golden goodybag
                "BD046" -> R.drawable.goodybag_35_golden

                // Unknown!
                else -> R.drawable.goodybag_blank
            }
        }
}