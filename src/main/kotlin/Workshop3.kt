package org.example

// Workshop #3: Testing - validateCitizenId

fun validateCitizenId(id: String): Boolean {
    if (id.length != 13) return false
    if (!id.all { it.isDigit() }) return false

    val digits = id.map { it.digitToInt() }
    val sum = digits.take(12)
        .mapIndexed { index, digit -> digit * (13 - index) }
        .sum()
    val checkDigit = (11 - (sum % 11)) % 10

    return checkDigit == digits[12]
}
