package io.zerobase.smarttracing

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.google.common.io.Resources
import io.zerobase.smarttracing.models.*
import io.zerobase.smarttracing.pdf.DocumentFactory
import io.zerobase.smarttracing.qr.QRCodeGenerator
import org.slf4j.LoggerFactory
import org.thymeleaf.TemplateEngine
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import org.w3c.tidy.Tidy
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import javax.ws.rs.core.UriBuilder

fun main() {
    (LoggerFactory.getLogger("org.thymeleaf") as Logger).level = Level.TRACE
    val qrCodeGenerator = QRCodeGenerator(
        baseLink = UriBuilder.fromUri(URI.create("https://zerobase.io/")),
        logo = Resources.getResource("qr/qr-code-logo.png")
    )
    val qrCodeId = ScannableId("qr01")
    val resolver = ClassLoaderTemplateResolver().apply {
        suffix = ".html"
        characterEncoding = StandardCharsets.UTF_8.displayName()
    }
    val templateEngine = TemplateEngine().apply {
        templateResolvers = setOf(resolver)
    }
    val documentFactory = DocumentFactory(templateEngine, Tidy().apply {
        inputEncoding = StandardCharsets.UTF_8.displayName()
        outputEncoding = StandardCharsets.UTF_8.displayName()
        xhtml = true
    })

    val fakeOrg = Organization(id = OrganizationId("fake"), name = "Fake Org",
        address = Address("", "", "Manchester", "New Hampshire", "", ""),
        contactName = "", contactInfo = ContactInfo("", ""))

    // qr pdf
     val result = qrCodeGenerator.generate(qrCodeId.value).let { documentFactory.siteOnboarding(fakeOrg, it) }.render()
    //     And finally, we create the PDF:
    Files.copy(result, Paths.get("pdfs", "zerobase-qr.pdf"))
}
