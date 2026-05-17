package com.arekalov.blps.camunda

import org.camunda.bpm.engine.RepositoryService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(value = ["verify"], inheritProfiles = false)
class CamundaDeploymentVerifyTest {

    @Autowired
    private lateinit var repositoryService: RepositoryService

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    @Test
    fun `engine deploys tariff-list with BPMNEdge and embedded deployment forms`() {
        val definition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey("tariff-list")
            .latestVersion()
            .singleResult()

        assertTrue(definition != null) { "Process definition tariff-list not deployed" }

        val xml = repositoryService.getResourceAsStream(definition.deploymentId, definition.resourceName)
            .use { it.readBytes().decodeToString() }

        assertTrue(xml.contains("BPMNEdge")) {
            "Deployed XML missing BPMNEdge (version=${definition.version})"
        }
        assertTrue(xml.contains("camunda-forms:deployment:forms/")) {
            "Deployed XML must use camunda-forms:deployment (version=${definition.version})"
        }
        assertTrue(!xml.contains("embedded:deployment:forms/")) {
            "Deployed XML must not use embedded:deployment for .form files"
        }
        assertTrue(!xml.contains("embedded:app:forms/")) {
            "Deployed XML must not use embedded:app:forms"
        }

        repositoryService.getResourceAsStream(definition.deploymentId, "forms/vacancy-list-query.form")
            .use { stream ->
                assertTrue(stream != null) {
                    "Deployment ${definition.deploymentId} missing forms/vacancy-list-query.form"
                }
            }
    }

    @Test
    fun `engine-rest returns tariff-list xml with diagram edges`() {
        val url = "http://localhost:$port/blps/engine-rest/process-definition/key/tariff-list/xml"
        val response = restTemplate.getForEntity(url, String::class.java)

        assertTrue(response.statusCode == HttpStatus.OK) {
            "engine-rest failed: ${response.statusCode} ${response.body}"
        }
        val body = response.body!!
        assertTrue(body.contains("BPMNEdge")) { "REST XML missing BPMNEdge" }
        assertTrue(body.contains("camunda-forms:deployment:forms/")) { "REST XML missing camunda-forms:deployment" }
    }
}
