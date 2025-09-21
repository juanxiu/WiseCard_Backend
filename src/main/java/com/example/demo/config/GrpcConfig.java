package com.example.demo.config;

import com.example.demo.grpc.CardDataServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PreDestroy;
import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GrpcConfig {
    
    @Value("${grpc.server.port:9091}")
    private int grpcPort;
    
    private Server grpcServer;
    
    @Bean
    public Server grpcServer(CardDataServiceImpl cardDataService) throws IOException {
        grpcServer = ServerBuilder.forPort(grpcPort)
                .addService(cardDataService)
                .build()
                .start();
        
        log.info("gRPC 서버 시작됨 - 포트: {}", grpcPort);
        
        // 서버 종료 시 자동으로 정리
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("gRPC 서버 종료 중...");
            grpcServer.shutdown();
        }));
        
        return grpcServer;
    }
    
    @PreDestroy
    public void stopGrpcServer() {
        if (grpcServer != null && !grpcServer.isShutdown()) {
            log.info("gRPC 서버 종료 중...");
            grpcServer.shutdown();
        }
    }
}
