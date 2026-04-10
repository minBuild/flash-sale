package dev.minbuild.flashsale.presentation.web

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.coRouter

@Configuration
class FlashSaleRouter {

    @Bean
    fun flashSaleRoutes(handler: FlashSaleHandler) = coRouter {
        "/api/v1/flash-sales".nest {
            POST("", handler::placeOrder)
        }
    }
    
}
