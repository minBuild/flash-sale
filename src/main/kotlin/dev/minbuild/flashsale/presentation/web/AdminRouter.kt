package dev.minbuild.flashsale.presentation.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class AdminRouter {

    @Bean
    fun adminRoutes(handler: AdminHandler) = coRouter {
        "/api/v1/admin/items".nest {
            POST("/{itemId}/stock", handler::setStock)
        }
    }

}
