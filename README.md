# 🚀 Flash Sale Service (WebFlux)

MSA 환경에서 대용량 트래픽이 몰리는 선착순 이벤트의 동시성 문제를 해결하고, 데이터 유실 없이 외부 시스템으로 이벤트를 전파하는 주문 처리 서비스

## ✨ Key Features

### 🚀 Non-Blocking & Reactive Routing

- Spring WebFlux와 Kotlin Coroutines(coRouter)를 활용하여 적은 리소스로 수많은 동시 접속 요청을 스레드 병목 없이 논블로킹으로 처리

### 🛡️ Atomic Stock Management (Redis Lua)

- Redis의 싱글 스레드 특성과 Lua Script를 결합하여, DB 락(Lock) 없이 원자적이고 초고속으로 선착순 재고 차감을 완벽하게 보장

### 📦 Transactional Outbox Pattern & CDC

- 주문 데이터와 카프카 발행용 이벤트 데이터를 단일 DB 트랜잭션으로 묶어 저장

- Debezium(CDC)이 DB Binlog를 실시간으로 캡처하여 카프카로 릴레이해서 메시지 발행(At-Least-Once) 보장

### ♻️ Idempotent Consumer (멱등성 보장)

- 네트워크 장애 등으로 인한 카프카 메시지 중복 수신 시, Redis를 활용한 상태 검증을 통해 외부 결제/주문 API의 중복 호출을 차단

### 🧯 Dead Letter Queue (DLQ) & Fault Tolerance

- 형식이 깨지거나 필수 값이 누락된 독약 데이터(Poison Pill) 인입 시, 무한 루프를 방지하고 별도의 DLQ 토픽으로 격리
- 외부 API 서버 일시 장애 시, No-Ack 정책을 통해 카프카 단에서 무한 재시도 수행

### 🏗 Architecture Overview

```
Client (POST /api/v1/flash-sales)
  ↓
┌─ Flash Sale Service ──────────────────────────────────────┐
│ 1. Reactive Router & Handler                              │
│ 2. Atomic Stock Deduction (Redis Lua Script)              │
│ 3. Save Order & OutboxEvent (DB Transaction)              │
└───────────────────────────────────────────────────────────┘
  ↓ (Binlog)
┌─ Kafka Connect (Debezium) ────────────────────────────────┐
│ 4. Capture DB Changes & Publish to order-created-topic    │
└───────────────────────────────────────────────────────────┘
  ↓
┌─ Order Event Consumer ────────────────────────────────────┐
│ 5. Validate Payload (Send to DLQ if Poison Pill)          │
│ 6. Check Idempotency via Redis (Skip if Processed)        │
│ 7. Call External API & Mark as DONE in Redis              │
└───────────────────────────────────────────────────────────┘
  ↓
External Order System
```

## 🛠 Tech Stack

| Category     | Stack                                       |
|--------------|---------------------------------------------|
| Framework    | Spring Boot 3.5.13, Spring WebFlux          |
| Language     | Kotlin 2.3.20, Java 24                      |
| Asynchronous | Kotlin Coroutines, Project Reactor          |
| Database     | MySQL, Spring Data R2DBC (r2dbc-mysql)      |
| Cache & Lock | Redis Reactive                              |
| Messaging    | Apache Kafka (Spring Kafka), Debezium (CDC) |

## 🚀 Getting Started

#### 1. 인프라 컨테이너 실행 (MySQL, Redis, Zookeeper, Kafka, Kafka Connect)

```
Bash

docker-compose up -d
```

#### 2. Database 스키마 초기화

```
Bash

docker exec -i mysql-container mysql -u root -p password < init-schema.sql
```

#### 3. Debezium Connector 등록 (CDC 설정)

```
Bash

chmod +x register-debezium.sh
./register-debezium.sh
```

#### 4. 애플리케이션 실행

```
Bash

./gradlew bootRun
```
