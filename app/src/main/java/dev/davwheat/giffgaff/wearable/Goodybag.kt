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

    val goodybagDrawableName: String
        get() {
            var drawableName = when (sku) {
                // £6 goodybag
                "BD039" -> "goodybag_6"

                // £8 goodybag
                "BD040" -> "goodybag_8"

                // £10 goodybag
                "BD036" -> "goodybag_10"

                // £10 golden goodybag
                "BD043" -> "goodybag_10_golden"

                // £12 goodybag
                "BD037" -> "goodybag_12"

                // £15 goodybag
                "BD038" -> "goodybag_15"

                // £15 golden goodybag
                "BD044" -> "goodybag_15_golden"

                // £20 goodybag
                "BD041" -> "goodybag_20"

                // £20 golden goodybag
                "BD045" -> "goodybag_15_golden"

                // £25 goodybag
                "BD042" -> "goodybag_25"

                // £35 golden goodybag
                "BD046" -> "goodybag_35_golden"

                // Unknown!
                else -> "goodybag_blank"
            }

            if (reserveAllowance != null) {
                drawableName += "_reservetank"
            }

            return "@drawable/$drawableName";
        }
}