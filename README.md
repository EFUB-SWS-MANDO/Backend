<div align="center">

# <img width="417" height="199" alt="image" src="https://github.com/user-attachments/assets/bcdf328b-0f28-4161-9f83-d4dde3b0ccdd" />


### Start · Prepare · Reach OUT

AI 기반 커리어 아카이브 서비스

가볍게 흘려보낸 오늘의 기록이, 내일의 커리어 자산이 됩니다.

<br>

[![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=flat-square&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=flat-square&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square&logo=docker&logoColor=white)](https://www.docker.com/)
[![AWS](https://img.shields.io/badge/AWS-EC2%20%7C%20S3-FF9900?style=flat-square&logo=amazonaws&logoColor=white)](https://aws.amazon.com/)

[Service](https://sprout-efub.vercel.app/login) · [API](https://api.sprout-p-e.p-e.kr/) · [API Documentation](https://app.notion.com/p/API-38fe46b1b00c80e19f51f8a9adba3d87) · [Frontend Repository](https://github.com/EFUB-SWS-MANDO/Frontend)

</div>

---

## Overview

SPROUT는 사용자의 경험을 기록하고 AI를 활용해 커리어 자산으로 발전시키는 AI 기반 커리어 아카이브 서비스입니다.

프로젝트, 스터디, 세미나 등 다양한 경험을 기록하면 AI가 이를 분석하여 자기소개서 작성과 모의 면접까지 이어질 수 있도록 지원합니다.

---

## Project Overview

| Item | Details |
|------|---------|
| Development | 2026.07.01 ~ 2026.08.08 (6 Weeks) |
| Type | Club Project |
| Language | Java 17 |
| Framework | Spring Boot |
| Database | PostgreSQL, Redis |

---

## Features

| Feature | Description |
|---------|-------------|
| Experience Archive | 자유 기록, 템플릿 기록, 태그 및 카테고리 관리, 파일 첨부 |
| AI Career Assistant | AI 자기소개서 생성, AI 모의 면접, SSE 스트리밍 |
| Social | 게시글 공유, 팔로우, 댓글 |
| Statistics | 활동 통계, 기록 추이, 카테고리 분석 |

---

## ERD

<img width="500" height="500" alt="unnamed-2026-07-28T20_53_51" src="https://github.com/user-attachments/assets/182a56bd-37f2-4259-be23-40dd5534aa21" />

---

## Architecture

```mermaid
flowchart TB
    User[User]

    Frontend[React Frontend - Vercel]

    Backend[Spring Boot Backend - AWS EC2]

    Security[Authentication - Kakao OAuth2 JWT]

    Redis[(Redis)]

    PostgreSQL[(PostgreSQL)]

    S3[AWS S3]

    OpenAI[OpenAI API]

    User --> Frontend
    Frontend --> Backend

    Backend --> Security
    Security --> Redis

    Backend --> PostgreSQL

    Backend -->|Generate Presigned URL| S3
    Frontend -->|Direct Upload| S3

    Backend -->|WebClient| OpenAI
    OpenAI --> Backend

    Backend -->|SSE Streaming| Frontend
```

<br>

### Key Components

| Component | Responsibility |
| --- | --- |
| Redis | Refresh Token 저장, JWT 블랙리스트 관리, SSE 세션 관리 및 캐시 |
| S3 | Presigned URL 기반 파일 업로드 |
| OpenAI | AI 자기소개서 및 모의면접 생성 |
| JWT / OAuth2 | 사용자 인증 및 인가 처리 |


---

## Tech Stack

### Backend

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.1.0-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=flat-square)
![QueryDSL](https://img.shields.io/badge/QueryDSL-0769AD?style=flat-square)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)

### Database

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7-DC382D?style=flat-square&logo=redis&logoColor=white)

### Infrastructure

![AWS EC2](https://img.shields.io/badge/AWS_EC2-FF9900?style=flat-square&logo=amazonec2&logoColor=white)
![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazons3&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=flat-square&logo=githubactions&logoColor=white)

### AI & Communication

![OpenAI](https://img.shields.io/badge/OpenAI_API-412991?style=flat-square&logo=openai&logoColor=white)
![SSE](https://img.shields.io/badge/Server--Sent_Events-SSE-blue?style=flat-square)

### Testing

![JUnit 5](https://img.shields.io/badge/JUnit_5-25A162?style=flat-square&logo=junit5&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2496ED?style=flat-square)

---

## Project Structure

Domain-Driven Structure를 적용하여 도메인별 패키지를 분리하고, 공통 모듈은 global 패키지에서 관리합니다.

```
├── domain
│   ├── auth
│   ├── member
│   ├── post
│   ├── comment
│   ├── follow
│   ├── resume
│   ├── interview
│   └── motivation
│
└── global
    ├── ai
    ├── config
    ├── common
    └── error
```

---

## API Documentation

[API Documentation](https://app.notion.com/p/API-38fe46b1b00c80e19f51f8a9adba3d87)

---

## Team

| Name | GitHub | Contribution |
| --- | --- | --- |
| 전채연 | [@RockScissors](https://github.com/RockScissors) | 프로젝트 초기 설정, AI Interview/Follow/Template/Category 도메인 개발, Statistics API 구현 |
| 김남우 | [@namwoooo](https://github.com/namwoooo) | Resume/Comment 도메인 개발, AWS S3 Presigned URL 기반 파일 업로드, 배포 환경 구성 |
| 박서영 | [@sum-young](https://github.com/sum-young) | Spring Security 인증/인가, Member/Profile/Post 도메인 개발 |

---

## Getting Started

### Prerequisites

- Java 17
- Docker
- PostgreSQL 17
- Redis 7

### Clone

```bash
git clone https://github.com/EFUB-SWS-MANDO/Backend.git
cd Backend
```

### Configuration

애플리케이션 실행을 위해 아래 환경 변수를 설정해야 합니다.

| Variable | Description |
| --- | --- |
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL |
| `SPRING_DATASOURCE_USERNAME` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | Database password |
| `REDIS_HOST` | Redis host |
| `REDIS_PORT` | Redis port |
| `JWT_SECRET` | JWT signing key |
| `OPENAI_API_KEY` | OpenAI API key |
| `AWS_ACCESS_KEY_ID` | AWS access key |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key |
| `S3_BUCKET_NAME` | S3 bucket name |

### Run

Docker Compose로 의존성 서비스를 실행한 뒤 애플리케이션을 실행합니다.

```bash
docker compose up -d

./gradlew bootRun
```

---

## CI/CD  

GitHub Actions를 통해 `main` 브랜치 push 시 자동 빌드 및 배포가 수행됩니다.

```mermaid
flowchart LR
    A[GitHub Push]
    B[GitHub Actions]
    C[Gradle Build]
    D[Docker Image Build]
    E[Docker Hub]
    F[AWS EC2]

    A --> B
    B --> C
    C --> D
    D --> E
    E --> F

```
