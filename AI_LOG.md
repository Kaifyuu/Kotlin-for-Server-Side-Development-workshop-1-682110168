# AI Log

## Workshop #1 — Unit Converter

### รายการที่ 1
- **Prompt ที่ใช้ (สรุป):** ให้ AI เติมโค้ดใน `Workshop1.kt` ตาม stub เดิม (when-menu, celsiusToFahrenheit, kilometersToMiles, null-safety ด้วย toDoubleOrNull)
- **AI ตอบผิด / น่าสงสัยตรงไหน:** ตอน verify ผลลัพธ์ด้วยการรัน console ผ่าน bash พบว่าข้อความภาษาไทยออกมาเป็น `?????` — เกือบเข้าใจผิดว่าโค้ดมีปัญหา encoding
- **เราตัดสินใจ / แก้อย่างไร:** เช็คแล้วพบว่าเป็น terminal codepage ของ bash เอง ไม่ใช่บั๊กในโค้ด (ค่าตัวเลขที่คำนวณถูกต้องทั้งหมด: 20°C→68.00°F, 1km→0.62mi) จึงเปลี่ยนไปรันผ่าน IntelliJ console แทนสำหรับแคปหน้าจอจริง
- **สิ่งที่ได้เรียนรู้:** อย่าเชื่อว่าเอาต์พุตที่ดูแปลกคือบั๊กในโค้ดทันที ต้องแยกให้ออกระหว่างปัญหา encoding ของ terminal กับ logic error จริง

### รายการที่ 2
- **Prompt ที่ใช้ (สรุป):** ให้ AI ตรวจ baseline โปรเจกต์ก่อนเริ่มเขียนโค้ด (compile, gradle config)
- **AI ตอบผิด / น่าสงสัยตรงไหน:** ไฟล์ `AGENTS.md` ของคอร์สระบุว่าใช้ Kotlin 2.4.x / JDK 21+ แต่ตรวจ `build.gradle.kts` จริงพบว่าเป็น Kotlin 2.1.21 กับ `jvmToolchain(18)`
- **เราตัดสินใจ / แก้อย่างไร:** ยึดตาม `build.gradle.kts` (source of truth) แทนคำอธิบายใน AGENTS.md เพราะ compiler ตัวจริงคือกรรมการ ไม่ใช่เอกสาร
- **สิ่งที่ได้เรียนรู้:** เอกสารประกอบคอร์สอาจไม่ sync กับ config จริงเสมอไป ต้อง verify กับไฟล์ config ตรงๆ ก่อนเชื่อ

## Workshop #2 — Data Analysis Pipeline

### รายการที่ 1
- **Prompt ที่ใช้ (สรุป):** ให้ AI เติม `Workshop2.kt` ตาม comment เดิม (List chaining และ .asSequence())
- **AI ตอบผิด / น่าสงสัยตรงไหน:** ถ้าเติมโค้ดแบบ inline ใน `main()` ตรงๆ ตาม stub เดิม จะไม่มีฟังก์ชันให้เทสต์เรียกใช้ได้เลย (WorkshopTest.kt ต้องการฟังก์ชันชื่อ `calculateTotalElectronicsPriceOver500` ที่รับ `List<Product>`)
- **เราตัดสินใจ / แก้อย่างไร:** ให้ AI แยกโค้ดออกเป็นฟังก์ชันที่มีชื่อและ signature ตรงกับที่ test คาดหวัง แล้วให้ `main()` เรียกใช้ฟังก์ชันเหล่านั้นแทนการเขียน logic ซ้ำ
- **สิ่งที่ได้เรียนรู้:** เวลาให้ AI เติม stub ต้องเช็คไฟล์เทสต์คู่กันเสมอ ไม่งั้นโค้ดที่ได้จะรันได้แต่เทสต์ไม่ได้เลย

### รายการที่ 2
- **Prompt ที่ใช้ (สรุป):** ให้ AI เขียนเทสต์ให้ฟังก์ชันของ Workshop #2 เพิ่มจาก stub เดิมในไฟล์ WorkshopTest.kt
- **AI ตอบผิด / น่าสงสัยตรงไหน:** พบว่า `WorkshopTest.kt` ไม่มี `package org.example` เลยตั้งแต่ต้น (มีแค่ import kotlin.test) ทำให้ต่อให้ implementation ถูกทุกอย่าง ก็ยัง compile ไม่ผ่านเพราะ resolve `Product`/ฟังก์ชันใน `org.example` ไม่ได้
- **เราตัดสินใจ / แก้อย่างไร:** เพิ่ม `package org.example` บรรทัดแรกของไฟล์เทสต์ แล้วรัน `./gradlew test` ยืนยันว่า compile และผ่านทุกเคสจริง (รวม edge case: empty list, List vs Sequence ให้ผลตรงกัน)
- **สิ่งที่ได้เรียนรู้:** อย่าเชื่อว่า test ที่ AI เขียนถูกต้องจนกว่าจะรันผ่านจริง โดยเฉพาะปัญหาเชิง config อย่าง package declaration ที่ compiler error ไม่ได้บอกตรงๆ ว่า "ลืม package"

## Workshop #3 — เทสต์ของเราคุม AI (validateCitizenId)

### รายการที่ 1
- **Prompt ที่ใช้ (สรุป):** เขียนเทสต์ 3 ตัวนิยาม `validateCitizenId(id: String): Boolean` (เคสถูก, ความยาวผิด, มีตัวอักษรปน) แล้วสั่ง AI เขียน implementation ให้เทสต์ผ่านทั้งหมด
- **AI ตอบผิด / น่าสงสัยตรงไหน:** implementation แรกที่ผ่านทั้ง 3 เทสต์เช็คแค่ความยาว 13 หลักกับว่าเป็นตัวเลขล้วน ไม่ได้ตรวจ checksum ของเลขบัตรจริงเลย — เทสต์ 3 ตัวแรกไม่ครอบคลุมพอที่จะบังคับให้ AI ทำ logic ที่ถูกต้องสมบูรณ์
- **เราตัดสินใจ / แก้อย่างไร:** รับรู้ว่านี่คือ gap ที่ตั้งใจ (ตามโจทย์) ยังไม่รับ implementation นี้เป็นคำตอบสุดท้าย รอเพิ่มเทสต์ edge case ก่อน
- **สิ่งที่ได้เรียนรู้:** เทสต์ที่ "ผ่านหมด" ไม่ได้แปลว่า implementation ถูกต้องสมบูรณ์ ถ้าเทสต์ไม่ครอบคลุม edge case ที่สำคัญ (เช่น checksum) AI จะเขียนแค่พอผ่านเทสต์ที่มีเท่านั้น

### รายการที่ 2
- **Prompt ที่ใช้ (สรุป):** เพิ่มเทสต์ edge case เช็ค checksum หลักสุดท้ายของเลขบัตร (เลข 13 หลัก ผ่าน format แต่ checksum ผิด) แล้วให้ AI แก้ implementation เดิม
- **AI ตอบผิด / น่าสงสัยตรงไหน:** ตามคาด — implementation เดิมพังทันทีตอนรันเทสต์ใหม่ (`1103700230480` ควรเป็น invalid เพราะ checksum ที่ถูกต้องคือ 3 ไม่ใช่ 0) ยืนยันว่า gap จากรายการที่ 1 มีจริง ไม่ใช่แค่ทฤษฎี
- **เราตัดสินใจ / แก้อย่างไร:** ให้ AI เพิ่ม logic คำนวณ checksum ตามสูตรบัตรประชาชนไทย (ผลรวมถ่วงน้ำหนักหลักที่ 1-12, mod 11, แล้วเทียบกับหลักที่ 13) รันเทสต์ทั้งหมดอีกครั้งจนผ่านครบก่อนรับงาน
- **สิ่งที่ได้เรียนรู้:** วงจร "เทสต์คุม AI" ใช้ได้จริง — เขียนเทสต์ที่ยังไม่ผ่านก่อน (red) แล้วให้ AI ทำให้ผ่าน (green) เป็นวิธีบังคับให้ AI ทำ business logic ที่ถูกต้อง ไม่ใช่แค่เดาตาม pattern ผิวๆ ของเทสต์ที่มีอยู่

## Workshop #4 — Ktor REST API (Task)

### รายการที่ 1
- **Prompt ที่ใช้ (สรุป):** สร้าง endpoint ตาม spec: GET/POST/PUT/DELETE /tasks พร้อม build.gradle.kts เดิม ระบุ Ktor 3.x
- **AI ตอบผิด / น่าสงสัยตรงไหน:** โจทย์ระบุให้สร้าง `TaskRequest(content, isDone)` แยกจาก `Task(id, content, isDone)` เพื่อไม่ให้ client ส่ง id เอง แต่ตรงข้อ POST กลับเขียนว่า "ใช้ `call.receive<Task>()`" ซึ่งขัดกันเอง (ถ้า receive เป็น Task ตรงๆ จะต้องให้ client ส่ง id มาด้วย ผิด intent ของ TaskRequest)
- **เราตัดสินใจ / แก้อย่างไร:** เลือกใช้ `call.receive<TaskRequest>()` แทนคำสั่งตามตัวอักษร แล้วให้ server generate id เอง ตรงกับเจตนาของ data model ที่โจทย์กำหนดไว้ก่อนหน้า
- **สิ่งที่ได้เรียนรู้:** โจทย์/สเปคเองก็ขัดแย้งกันได้ ต้องอ่านทั้งก้อนแล้วเลือกยึด intent ที่สอดคล้องกันมากกว่า ไม่ใช่เชื่อประโยคเดียวตรงๆ

### รายการที่ 2
- **Prompt ที่ใช้ (สรุป):** เพิ่ม kotlinx.serialization และ Ktor version ลง build.gradle.kts โดยไม่เดา version จากความจำ ให้ compiler เป็นกรรมการ
- **AI ตอบผิด / น่าสงสัยตรงไหน:** ตอนแรกเกือบลืมใส่ `kotlin("plugin.serialization")` compiler plugin คู่กับ dependency `kotlinx-serialization-json` — ถ้าใส่แค่ dependency เฉยๆ `@Serializable` จะ compile ผ่านแต่ไม่ generate serializer จริง
- **เราตัดสินใจ / แก้อย่างไร:** เพิ่ม plugin `kotlin("plugin.serialization") version "2.1.21"` ให้ตรงกับ Kotlin plugin version แล้วรัน `./gradlew compileKotlin` ยืนยันว่า resolve และ compile ผ่านจริงก่อนเขียน route logic ต่อ
- **สิ่งที่ได้เรียนรู้:** kotlinx.serialization ต้องมีทั้ง dependency และ compiler plugin คู่กันเสมอ ขาดอันใดอันหนึ่งจะไม่ error ตอน compile แต่จะพังตอน runtime แทน — ต้องรันจริงถึงจะมั่นใจได้
