package com.arekalov.blps.camunda

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.core.io.support.PathMatchingResourcePatternResolver

class ProcessDefinitionDiagramTest {

    private val resolver = PathMatchingResourcePatternResolver()

    @Test
    fun `all BPMN resources on classpath contain diagram interchange`() {
        val bpmnFiles = resolver.getResources("classpath*:bpmn/*.bpmn")
        assertTrue(bpmnFiles.size >= 20) {
            "Expected at least 20 BPMN files on classpath, found ${bpmnFiles.size}"
        }

        val broken = bpmnFiles.mapNotNull { resource ->
            val name = resource.filename ?: return@mapNotNull null
            val xml = resource.inputStream.use { it.readBytes().decodeToString() }
            when {
                !xml.contains("BPMNDiagram") -> "$name: missing BPMNDiagram"
                !xml.contains("BPMNShape") -> "$name: missing BPMNShape"
                !xml.contains("BPMNEdge") -> "$name: missing BPMNEdge"
                else -> null
            }
        }

        assertTrue(broken.isEmpty()) {
            "BPMN files without complete diagram markup:\n${broken.joinToString("\n")}"
        }
    }

    @Test
    fun `tariff-list BPMN is on classpath with edges`() {
        val resource = resolver.getResource("classpath:bpmn/tariff-list.bpmn")
        assertNotNull(resource)
        val xml = resource.inputStream.use { it.readBytes().decodeToString() }
        assertTrue(xml.contains("BPMNDiagram"))
        assertTrue(xml.contains("BPMNEdge id=\"flow-0_di\""))
    }
}
