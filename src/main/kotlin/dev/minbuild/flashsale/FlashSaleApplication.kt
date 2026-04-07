package dev.minbuild.flashsale

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class FlashSaleApplication

fun main(args: Array<String>) {
	runApplication<FlashSaleApplication>(*args)
}
