package org.example

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class Workshop3Test {

    // --- Tests for Workshop #3: validateCitizenId ---
    // 13-digit Thai citizen ID with a valid checksum digit

    @Test
    fun `test validateCitizenId with correct format and length`() {
        // Arrange
        val validId = "1103700230483"

        // Act
        val result = validateCitizenId(validId)

        // Assert
        assertTrue(result, "13-digit numeric ID should be valid")
    }

    @Test
    fun `test validateCitizenId with wrong length`() {
        // Arrange
        val shortId = "11037002304"

        // Act
        val result = validateCitizenId(shortId)

        // Assert
        assertFalse(result, "ID shorter than 13 digits should be invalid")
    }

    @Test
    fun `test validateCitizenId with letters mixed in`() {
        // Arrange
        val idWithLetters = "110370023048A"

        // Act
        val result = validateCitizenId(idWithLetters)

        // Assert
        assertFalse(result, "ID containing letters should be invalid")
    }

    // Edge case: correct length and all digits, but wrong checksum digit
    // (last digit should be 3 per the Thai citizen ID checksum formula, not 0)
    @Test
    fun `test validateCitizenId with wrong checksum digit`() {
        // Arrange
        val wrongChecksumId = "1103700230480"

        // Act
        val result = validateCitizenId(wrongChecksumId)

        // Assert
        assertFalse(result, "ID with invalid checksum digit should be invalid")
    }

    // --- Tests for Workshop #3: validateCitizenId End ---
}
