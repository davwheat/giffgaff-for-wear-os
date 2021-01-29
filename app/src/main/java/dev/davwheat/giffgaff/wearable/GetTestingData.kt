package dev.davwheat.giffgaff.wearable

fun GetTestingData(): AccountInfo {
    val goodybag = Goodybag("BD043", 12, 3, true, 10.00, 9437184, 1048576, 8776581, 1048576)

    return AccountInfo(420, true, "28/02/2021", goodybag, "07000123456")
}