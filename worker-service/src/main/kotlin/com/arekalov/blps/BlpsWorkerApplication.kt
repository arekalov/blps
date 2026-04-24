package com.arekalov.blps

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BlpsWorkerApplication

fun main(args: Array<String>) {
	runApplication<BlpsWorkerApplication>(*args)
}
