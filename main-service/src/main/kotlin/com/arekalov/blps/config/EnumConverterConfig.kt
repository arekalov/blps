package com.arekalov.blps.config

import com.arekalov.blps.model.enum.VacancyStatus
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class EnumConverterConfig : WebMvcConfigurer {

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(StringToVacancyStatusConverter())
    }
}

class StringToVacancyStatusConverter : Converter<String, VacancyStatus> {
    override fun convert(source: String): VacancyStatus {
        return VacancyStatus.valueOf(source.uppercase())
    }
}
