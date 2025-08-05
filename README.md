# DevUnion 프로젝트 인수인계 문서

---

## 1. 프로젝트 개요

* **프로젝트명**: DevUnion (개발자 Union)
* **목적**: 컴퓨터정보공학과 선후배 간의 유대감 증진 및 코딩 학습 지원을 위한 커뮤니티 및 멘토링 서비스.
* **핵심 기능**:
    * 회원가입, 로그인, 프로필 관리
    * Q&A 게시판, 커뮤니티 게시판, 포스트 게시판
    * 1:1 멘토링 및 쪽지 기능
    * 스터디 그룹 생성 및 관리
    * 실시간 알림 서비스
    * 자동 인기글 선정 시스템

---

## 2. 시스템 아키텍처

### 2.1. 전체 아키텍처 개념도

(여기에 프로젝트의 전체 아키텍처 다이어그램 이미지를 첨부합니다. 이전에 논의했던 VPC, Subnet, ALB, ECS, RDS, S3/CloudFront 등의 구조를 시각화한 그림입니다.)

### 2.2. 기술 스택

| 구분             | 상세 기술                                                                                                         | 비고                                                    |
| :--------------- | :---------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------ |
| **Backend** | `Spring Boot 3.x` (Java 21), `Spring Data JPA`, `Spring Security`, `Gradle`                                       | RESTful API 개발                                        |
| **Frontend** | `React.js`                                                                                                      | UI/UX 구현                                              |
| **Database** | `PostgreSQL`                                                                                                      | 주력 RDB                                                |
| **Cache/Message Queue** | `Redis` (caching, session), `Kafka` (실시간 알림)                                                              | 선택 사항이지만 권장                                    |
| **DevOps** | `Docker`, `AWS ECS (Fargate)`, `AWS S3`, `AWS CloudFront`, `AWS RDS`, `GitHub Actions`, `CloudWatch`              | CI/CD, 컨테이너, 서버리스, 모니터링                     |
| **Development** | `IntelliJ IDEA`, `VS Code`, `Git`, `nvm`                                                                         | 개발 환경                                               |

---

## 3. 개발 환경 설정

### 3.1. 필수 도구

* `Java JDK 21`: 백엔드 개발용
* `Node.js LTS`: 프론트엔드 개발용 (nvm으로 설치 권장)
* `Docker Desktop`: 로컬 DB 및 컨테이너 개발용
* `Git`: 버전 관리용

### 3.2. 백엔드 개발 환경 실행

로컬에서 Spring Boot 백엔드와 Docker PostgreSQL을 실행하는 방법입니다.

1.  **PostgreSQL Docker 컨테이너 실행**:
    * 터미널에서 아래 명령어로 PostgreSQL 컨테이너를 백그라운드에 실행합니다.
    * `-v` 옵션의 경로(`~/docker_data/pgdata`)는 데이터를 영속적으로 저장할 로컬 폴더 경로입니다.
    ```bash
    docker run --name devunion-db \
    -e POSTGRES_DB=devunion \
    -e POSTGRES_USER=devu \
    -e POSTGRES_PASSWORD=password \
    -e TZ=Asia/Seoul \
    -p 5432:5432 \
    -v ~/docker_data/pgdata:/var/lib/postgresql/data \
    -d postgres:latest
    ```
2.  **Spring Boot 설정**:
    * `src/main/resources/application.properties` 파일에 위 Docker 컨테이너 연결 정보를 설정합니다.
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/devunion
    spring.datasource.username=devu
    spring.datasource.password=password
    spring.jpa.hibernate.ddl-auto=update
    ```
3.  **Spring Boot 실행**:
    * IntelliJ IDEA에서 애플리케이션을 실행하거나, `./gradlew bootRun` 명령어로 실행합니다.

### 3.3. 프론트엔드 개발 환경 실행

1.  **의존성 설치**:
    * 프론트엔드 저장소로 이동하여 `npm install` 명령어로 의존성을 설치합니다.
2.  **개발 서버 실행**:
    * `npm start` 또는 `npm run dev` 명령어로 로컬 개발 서버를 실행합니다.
    * `.env` 파일을 통해 백엔드 API 서버의 로컬 주소(예: `http://localhost:8080`)를 설정하여 연동 테스트를 진행합니다.

---

## 4. AWS 인프라 구축 가이드

DevUnion 프로젝트의 AWS 환경 구축은 다음 단계로 진행되었습니다. 상세한 내용은 별도의 문서 또는 스크린샷 가이드를 참조하세요.

### 4.1. 네트워크 (VPC) 구성

| 리소스              | 이름/ID                                  | 설정                                                                       |
| :------------------ | :--------------------------------------- | :------------------------------------------------------------------------- |
| **VPC** | `devunion-vpc`                           | `10.0.0.0/16`                                                              |
| **Public Subnet (AZ1)** | `devunion-public-az1`                    | `10.0.10.0/24`, `Enable auto-assign public IPv4 address` 활성화                    |
| **Public Subnet (AZ2)** | `devunion-public-az2`                    | `10.0.20.0/24`, `Enable auto-assign public IPv4 address` 활성화                    |
| **Private Subnet (AZ1)**| `devunion-private-az1`                   | `10.0.11.0/24`                                                             |
| **Private Subnet (AZ2)**| `devunion-private-az2`                   | `10.0.21.0/24`                                                             |
| **Internet Gateway**| `devunion-igw`                           | Public 서브넷 라우팅 테이블에 연결                                         |
| **NAT Gateway** | `devunion-nat-gw-az1` (or two)           | Private 서브넷 라우팅 테이블에 연결                                        |
| **Route Table** | `devunion-public-rt`, `devunion-private-rt` | Public/Private 서브넷 연결                                                 |

### 4.2. 주요 AWS 리소스 설정

* **EC2 (Bastion Host)**: `devunion-bastion-host`
    * **OS**: Ubuntu Server LTS
    * **위치**: Public Subnet
    * **역할**: Private 서브넷 자원(ECS 컨테이너, RDS 등)으로의 SSH 보안 접속 통로.
* **RDS (PostgreSQL)**:
    * **배포**: Multi-AZ 구성
    * **위치**: Private Subnet
    * **설정**: `devunion-db-sg` 보안 그룹으로 `devunion-app-sg`의 접근만 허용.
* **ECS (Spring Boot)**:
    * **배포**: Fargate 또는 EC2 런치 타입
    * **위치**: Private Subnet
    * **설정**: `devunion-app-sg` 보안 그룹으로 `ALB` 트래픽만 허용.
* **S3 & CloudFront**:
    * **S3**: `devunion-frontend-static` 버킷에 React 정적 파일 업로드.
    * **CloudFront**: S3를 원본으로 하는 CDN 설정.
* **ALB**:
    * **위치**: Public Subnet
    * **역할**: 외부 트래픽을 ECS 서비스로 분산.

---

## 5. CI/CD 및 배포 프로세스

`GitHub Actions`를 활용하여 다음과 같은 자동화된 파이프라인을 구축했습니다.

1.  **Code Push**: 백엔드 또는 프론트엔드 저장소에 코드를 푸시.
2.  **CI (통합)**: GitHub Actions가 워크플로우를 트리거.
    * 코드 빌드 및 테스트.
    * Docker 이미지 빌드 (백엔드) 또는 정적 파일 빌드 (프론트엔드).
3.  **CD (배포)**:
    * **백엔드**: Docker 이미지를 `ECR`에 푸시하고, `ECS` 서비스의 작업 정의(Task Definition)를 업데이트하여 무중단 배포.
    * **프론트엔드**: 빌드된 정적 파일을 `S3`에 업로드하고 `CloudFront` 캐시 무효화.

---

## 6. 운영 및 유지보수

* **모니터링**:
    * `AWS CloudWatch`: ECS, RDS, ALB의 주요 성능 지표를 실시간으로 모니터링하고 알람 설정.
* **로깅**:
    * `AWS CloudWatch Logs`: ECS 컨테이너의 로그를 중앙 집중식으로 수집 및 저장.
* **비용 관리**:
    * AWS `Billing` 대시보드를 주기적으로 확인하여 비용 모니터링.

---

## 7. 중요 정보

* **GitHub 저장소**:
    * 백엔드: `https://github.com/jisub-dev/devunion-backend`
    * 프론트엔드: (추가 예정)
* **API 명세서**: (추가 예정)
* **주요 담당자**:
    * **이름**: 김지섭
    * **이메일**: `kimjiseob1209@gmail.com`