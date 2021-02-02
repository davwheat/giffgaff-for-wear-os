package dev.davwheat.giffgaff.wearable

fun GetTestingData(): AccountInfo {
    val testTenGolden = Goodybag("BD043", 12, 3, true, 10.00, 9437184, 1048576, 8776581, 1048576, false)
    val testThirtyGolden = Goodybag("BD046", 12, 3, false, 35.00, -1, 0, -1, -1, true)

    return AccountInfo(42069, true, "28/02/2021", testThirtyGolden, "07123456789")
}