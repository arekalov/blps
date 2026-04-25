package com.arekalov.blps.eis.gateway.servlet

import com.arekalov.blps.eis.gateway.Bitrix24MappedRecord
import jakarta.resource.ResourceException
import jakarta.resource.cci.ConnectionFactory
import jakarta.resource.cci.Record
import jakarta.servlet.annotation.WebServlet
import jakarta.servlet.http.HttpServlet
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.nio.charset.StandardCharsets
import javax.naming.InitialContext

@WebServlet(name = "CrmDealAdd", value = ["/crm.deal.add"])
class CrmDealAddServlet : HttpServlet() {

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val body = req.reader.readText()
        logGateway("received /crm.deal.add, bodySize=${body.length}")
        if (body.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "empty body")
            return
        }
        val connectionFactory: ConnectionFactory = try {
            InitialContext().lookup("java:/eis/Bitrix24CF") as ConnectionFactory
        } catch (e: Exception) {
            logGateway("JNDI java:/eis/Bitrix24CF failed: ${e.message}")
            resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Bitrix JCA not available")
            return
        }
        var connection: jakarta.resource.cci.Connection? = null
        var interaction: jakarta.resource.cci.Interaction? = null
        try {
            connection = connectionFactory.connection
            interaction = connection.createInteraction()
            val inRecord = buildRequestRecord(body)
            val outRecord = Bitrix24MappedRecord("bitrixResponse")
            val ok = interaction.execute(null, inRecord, outRecord)
            if (!ok) {
                resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, "CCI execute returned false")
                return
            }
            val raw = extractResponseBody(outRecord)
            resp.contentType = "application/json"
            resp.characterEncoding = StandardCharsets.UTF_8.name()
            resp.status = HttpServletResponse.SC_OK
            resp.writer.write(raw)
        } catch (e: ResourceException) {
            logGateway("Bitrix JCA: ${e.message}")
            resp.sendError(HttpServletResponse.SC_BAD_GATEWAY, e.message ?: "ResourceException")
        } finally {
            runCatching { interaction?.close() }
            runCatching { connection?.close() }
        }
    }

    private fun buildRequestRecord(jsonBody: String): Bitrix24MappedRecord {
        val r = Bitrix24MappedRecord("bitrixRequest")
        r["operation"] = "crm.deal.add.json"
        r["path"] = "crm.deal.add.json"
        r["httpMethod"] = "POST"
        r["method"] = "POST"
        r["contentType"] = "application/json; charset=UTF-8"
        r["payload"] = jsonBody
        r["body"] = jsonBody
        return r
    }

    private fun extractResponseBody(responseRecord: Record): String {
        if (responseRecord is Bitrix24MappedRecord) {
            val v = responseRecord["body"] ?: responseRecord["payload"] ?: responseRecord["response"] ?: responseRecord["result"]
            if (v != null) {
                return if (v is String) v else v.toString()
            }
        }
        return responseRecord.toString()
    }

    private fun logGateway(msg: String) {
        println("[eis-gateway] $msg")
    }
}
