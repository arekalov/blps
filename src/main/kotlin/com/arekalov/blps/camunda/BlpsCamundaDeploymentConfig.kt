package com.arekalov.blps.camunda

import org.camunda.bpm.engine.RepositoryService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.annotation.Order
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Component
@ConditionalOnProperty(name = ["blps.camunda.manual-deployment"], havingValue = "true", matchIfMissing = true)
class BlpsCamundaDeploymentConfig(
    private val repositoryService: RepositoryService,
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @EventListener(ApplicationReadyEvent::class)
    @Order(100)
    fun deployProcessesAndForms() {
        val resolver = PathMatchingResourcePatternResolver()
        val bpmn = resolver.getResources("classpath*:bpmn/*.bpmn")
        val forms = resolver.getResources("classpath*:forms/*.form")

        val bpmnList = bpmn.toList()
        val formsList = forms.toList()
        if (bpmnList.isEmpty()) {
            log.warn("No BPMN resources on classpath (classpath*:bpmn/*.bpmn)")
            return
        }

        val zipBytes = buildDeploymentZip(bpmnList, formsList)
        log.info("Deploying {} BPMN and {} form resources", bpmnList.size, formsList.size)

        val result = repositoryService.createDeployment()
            .name("blps")
            .enableDuplicateFiltering(false)
            .addZipInputStream(ZipInputStream(ByteArrayInputStream(zipBytes)))
            .deploy()

        val processCount = repositoryService.createProcessDefinitionQuery()
            .deploymentId(result.id)
            .count()

        log.info("Camunda deployment {}: {} process definitions", result.id, processCount)
    }

    private fun buildDeploymentZip(
        bpmn: List<org.springframework.core.io.Resource>,
        forms: List<org.springframework.core.io.Resource>,
    ): ByteArray {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            bpmn.forEach { resource ->
                val fileName = resource.filename ?: return@forEach
                addZipEntry(zip, fileName, resource)
            }
            forms.forEach { resource ->
                val fileName = resource.filename ?: return@forEach
                addZipEntry(zip, "forms/$fileName", resource)
            }
        }
        return output.toByteArray()
    }

    private fun addZipEntry(
        zip: ZipOutputStream,
        path: String,
        resource: org.springframework.core.io.Resource,
    ) {
        zip.putNextEntry(ZipEntry(path))
        resource.inputStream.use { input -> input.copyTo(zip) }
        zip.closeEntry()
    }
}
