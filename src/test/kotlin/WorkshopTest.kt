package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class WorkshopTest {

    // --- Tests for Workshop #1: Unit Converter ---

    // celsius input: 20.0
    // expected output: 68.0
    @Test
    fun `test celsiusToFahrenheit with positive value`() {
        // Arrange: ตั้งค่า input และผลลัพธ์ที่คาดหวัง
        val celsiusInput = 20.0
        val expectedFahrenheit = 68.0

        // Act: เรียกใช้ฟังก์ชันที่ต้องการทดสอบ
        val actualFahrenheit = celsiusToFahrenheit(celsiusInput)

        // Assert: ตรวจสอบว่าผลลัพธ์ที่ได้ตรงกับที่คาดหวัง
        assertEquals(expectedFahrenheit, actualFahrenheit, 0.001, "20°C should be 68°F")
    }

    // celsius input: 0.0
    // expected output: 32.0
    @Test
    fun `test celsiusToFahrenheit with zero`() {
        val celsiusInput = 0.0
        val expectedFahrenheit = 32.0

        val actualFahrenheit = celsiusToFahrenheit(celsiusInput)

        assertEquals(expectedFahrenheit, actualFahrenheit, 0.001, "0°C should be 32°F")
    }

    // celsius input: -10.0
    // expected output: 14.0
    @Test
    fun `test celsiusToFahrenheit with negative value`() {
        val celsiusInput = -10.0
        val expectedFahrenheit = 14.0

        val actualFahrenheit = celsiusToFahrenheit(celsiusInput)

        assertEquals(expectedFahrenheit, actualFahrenheit, 0.001, "-10°C should be 14°F")
    }

    // test for kilometersToMiles function
    // kilometers input: 1.0
    // expected output: 0.621371
    @Test
    fun `test kilometersToMiles with one kilometer`() {
        val kilometersInput = 1.0
        val expectedMiles = 0.621371

        val actualMiles = kilometersToMiles(kilometersInput)

        assertEquals(expectedMiles, actualMiles, 0.000001, "1km should be 0.621371 miles")
    }

    // --- Tests for Workshop #1: Unit Converter End ---

    // --- Tests for Workshop #2: Data Analysis Pipeline ---

    private val sampleProducts = listOf(
        Product(name = "Laptop", price = 35000.0, category = "Electronics"),
        Product(name = "Smartphone", price = 25000.0, category = "Electronics"),
        Product(name = "T-shirt", price = 450.0, category = "Apparel"),
        Product(name = "Monitor", price = 7500.0, category = "Electronics"),
        Product(name = "Keyboard", price = 499.0, category = "Electronics"),
        Product(name = "Jeans", price = 1200.0, category = "Apparel"),
        Product(name = "Headphones", price = 1800.0, category = "Electronics")
    )

    @Test
    fun `test calculateTotalElectronicsPriceOver500 with sample products`() {
        val expectedTotal = 35000.0 + 25000.0 + 7500.0 + 1800.0

        val actualTotal = calculateTotalElectronicsPriceOver500(sampleProducts)

        assertEquals(expectedTotal, actualTotal, 0.001, "Should sum Electronics priced over 500")
    }

    @Test
    fun `test calculateTotalElectronicsPriceOver500 with empty list`() {
        val actualTotal = calculateTotalElectronicsPriceOver500(emptyList())

        assertEquals(0.0, actualTotal, 0.001, "Empty list should sum to 0.0")
    }

    @Test
    fun `test calculateTotalElectronicsPriceOver500Sequence matches List result`() {
        val listResult = calculateTotalElectronicsPriceOver500(sampleProducts)
        val sequenceResult = calculateTotalElectronicsPriceOver500Sequence(sampleProducts)

        assertEquals(listResult, sequenceResult, 0.001, "Sequence result should match List result")
    }

    // จงเขียน test cases เช็คจำนวนสินค้าที่อยู่ในหมวด 'Electronics' และมีราคามากกว่า 500 บาท
    @Test
    fun `test countElectronicsOver500 with sample products`() {
        val actualCount = countElectronicsOver500(sampleProducts)

        assertEquals(4, actualCount, "Should count Laptop, Smartphone, Monitor, Headphones")
    }

    @Test
    fun `test countElectronicsOver500 with empty list`() {
        val actualCount = countElectronicsOver500(emptyList())

        assertEquals(0, actualCount, "Empty list should count 0")
    }

    // --- Tests for Workshop #2: Data Analysis Pipeline End ---
}