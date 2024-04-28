package org.bk.notification.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

class JsonMappingTest {

    @Test
    fun generalMapping() {
        val json = """
            {
              "name": "P. Sherman",
              "address": {
                "street": "42 Wallaby Way",
                "city": "Sydney"
              }
            }
        """.trimIndent()

        val mapper = jacksonObjectMapper()
        val person: Person = mapper.readValue(json)
        assertThat(person).isEqualTo(
            Person(
                name = "P. Sherman",
                address = Address(street = "42 Wallaby Way", city = "Sydney")

            )
        )
    }

    @Test
    fun testHdrHistogram() {
        val rand = Random()
        for (i in 0..999) {
            println(rand.nextGaussian(100.0, 50.0))
        }
    }

    @Test
    fun typeRefMapping() {
        val json = """
            {
                "a" : ["b", "c"],
                "b" : ["a", "c"],
                "c" : ["a", "b"]
            }
        """.trimIndent()

        val mapper = jacksonObjectMapper()
        val result: Map<String, List<String>> = mapper.readValue(json)
        println(result)
    }

    data class Address(val street: String, val city: String)
    data class Person(val name: String, val address: Address)
}