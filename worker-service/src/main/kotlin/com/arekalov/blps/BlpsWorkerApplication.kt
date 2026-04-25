package com.arekalov.blps

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.kafka.annotation.EnableKafka

@EnableKafka
@SpringBootApplication
class BlpsWorkerApplication

fun main(args: Array<String>) {
	runApplication<BlpsWorkerApplication>(*args)
}
