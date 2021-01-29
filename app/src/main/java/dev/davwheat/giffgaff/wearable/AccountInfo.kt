package dev.davwheat.giffgaff.wearable

class AccountInfo(
    /**
     * Amount of credit in pence
     */
    val creditBalance: Int,
    /**
     * Whether free giffgaff to giffgaff calls/texts are available
     */
    val freeGiffgaffToGiffgaffEnabled: Boolean,
    /**
     * Date that free giffgaff to giffgaff calls/texts expire
     */
    val freeGiffgaffToGiffgaffExpiry: String?,
    /**
     * Active goodybag, or null
     */
    val goodybag: Goodybag?,
    /**
     * Member phone number
     */
    val phoneNumber: String?
) {
}