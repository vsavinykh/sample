package com.lanehealth.payrollservice.config

import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GrpcConfig {
    @Bean
    fun grpcServerConfigurer() =
        GrpcServerConfigurer {
            when (it) {
                is NettyServerBuilder -> it.directExecutor()
                else -> throw RuntimeException("Invalid grpc server provided")
            }
        }
}