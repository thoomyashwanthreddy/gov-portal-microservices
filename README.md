# Gov-Portal Microservice Starter

A production-style reference application: a secured **Spring Boot** backend, a **VueJS** front end,
a standalone **Kafka-consuming notification service**, containerized with **Docker**, and deployed to
**Kubernetes** with a **Jenkins CI/CD** pipeline. Authentication and role-based access (citizen vs.
case-worker) are handled by **Keycloak** via OAuth2/JWT.

## Architecture

```
┌─────────────┐   HTTPS    ┌──────────────────┐   JDBC    ┌────────────┐
│  VueJS SPA  │──────────▶ │  Spring Boot API  │─────────▶ │ PostgreSQL │
│  (Nginx)    │◀────────── │  (REST, OAuth2)   │◀───────── │            │
└─────────────┘   JSON     └──────────────────┘           └────────────┘
                                     │        ▲
                                     ▼        │
                            ┌──────────────────┐        ┌────────────────────┐
                            │  Kafka (events)   │───────▶│ notification-service│
                            └──────────────────┘        └────────────────────┘
                                     ▲
                                     │
                            ┌──────────────────┐
                            │     Keycloak      │  (issues/validates JWTs)
                            └──────────────────┘

  Docker containers  →  Kubernetes (Deployments, Services, Ingress)  →  AWS (EKS/EC2)
  Jenkins CI/CD  →  build, test, image push, rolling deploy
  Structured JSON logs  →  Splunk (or ELK locally)
```

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | VueJS 3, Vue Router, Pinia, Axios, Vite |
| Backend | Java 17, Spring Boot 3, Spring Security (OAuth2 Resource Server), Spring Data JPA, Flyway |
| Auth | Keycloak (OAuth2/OIDC), JWT access tokens, realm roles → Spring authorities |
| Messaging | Apache Kafka (KRaft mode), consumed by a standalone `notification-service` |
| Database | PostgreSQL |
| Containerization | Docker, Docker Compose (local), Kubernetes (Deployments, Services, Ingress, ConfigMaps, Secrets, HPA) |
| CI/CD | Jenkins declarative pipeline (build → test → image → push → deploy) |
| Observability | Structured JSON logging (Logback + Logstash encoder), Spring Actuator health/metrics, Kafka UI |
| Cloud | AWS (ECR, EKS, CloudWatch) — referenced in the Jenkinsfile / K8s manifests |

## Project layout

```
gov-portal-microservice-starter/
├── backend/                  Spring Boot REST API
├── notification-service/     Kafka consumer microservice
├── frontend/                 VueJS 3 SPA
├── keycloak/realm-export.json  Seeded realm, client, and demo users
├── k8s/                      Kubernetes manifests
├── docker-compose.yml        Full local stack
├── Jenkinsfile                CI/CD pipeline
└── .env.example
```

## Getting started

### Prerequisites
- Java 17+, Maven
- Node 18+
- Docker & Docker Compose
- (Optional) `kubectl` + a local cluster (kind/minikube) to try the K8s manifests

### Run everything with Docker Compose

```bash
docker-compose up --build
```

This starts Postgres, Keycloak (with the `gov-portal` realm auto-imported), Kafka, Kafka UI, the
backend, the notification service, and the frontend.

| Service | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Keycloak admin console | http://localhost:8081 (`admin` / `admin`) |
| Kafka UI | http://localhost:8085 |

Two demo users are seeded by the realm import (see `keycloak/realm-export.json`):

| Username | Password | Role |
|---|---|---|
| `jane.citizen` | `citizen123` | `citizen` — submit & track own requests |
| `worker.bob` | `worker123` | `case-worker` — see & triage all requests |

> First boot can take ~30–60s while Keycloak imports the realm and the backend runs its Flyway
> migration — retry the login if you see a connection error immediately after `docker-compose up`.

### Run services individually

```bash
# Backend (needs Postgres, Kafka, Keycloak reachable at the hosts in application.yml)
cd backend && mvn spring-boot:run

# Notification service
cd notification-service && mvn spring-boot:run

# Frontend
cd frontend && npm install && npm run dev
```

### Run tests

```bash
cd backend && mvn test
cd notification-service && mvn test
cd frontend && npm run test
```

## Deploying to Kubernetes

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/kafka.yaml
kubectl apply -f k8s/keycloak.yaml
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/notification-service-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/ingress.yaml
kubectl apply -f k8s/hpa.yaml
```

Before applying, edit `k8s/secrets.yaml` with real credentials, and point `k8s/backend-deployment.yaml`
/ `k8s/frontend-deployment.yaml` / `k8s/notification-service-deployment.yaml` at your pushed ECR image
URIs (the Jenkins pipeline does the `kubectl set image` step for you on each deploy).

## CI/CD

`Jenkinsfile` defines the pipeline:

1. **Build & unit test** — Maven for backend & notification-service (parallel), npm for frontend
2. **Containerize** — build and tag Docker images for all three services
3. **Push** — push images to AWS ECR
4. **Deploy** — apply manifests and roll out the new images to the Kubernetes cluster via `kubectl`

## API overview

| Method | Endpoint | Description | Role |
|---|---|---|---|
| POST | `/api/auth/login` | Authenticate against Keycloak, receive a JWT | public |
| GET | `/api/requests` | List service requests (role-filtered) | citizen, case-worker |
| POST | `/api/requests` | Submit a new service request | citizen |
| PATCH | `/api/requests/{id}` | Update request status | case-worker |
| GET | `/actuator/health` | Liveness/readiness probe endpoint | public |

## Notes & known limitations

- The login endpoint uses the OAuth2 **Resource Owner Password Credentials** grant against Keycloak
  to keep the demo self-contained in a single SPA + API flow. It's deprecated under OAuth 2.1 — a
  production system should redirect to Keycloak directly with Authorization Code + PKCE instead.
- Kafka runs as a single-node KRaft broker (no replication) — fine for a demo, not for production.
- Keycloak runs in `start-dev` mode with its embedded database — see the comment in
  `k8s/keycloak.yaml` for what changes for a real production deployment.
- This project was generated as a full local reference implementation; it has not been deployed to
  a real AWS/EKS/Jenkins environment as part of generating it, so treat the Jenkinsfile and K8s
  manifests as a strong, working starting point to adapt to your actual cluster/registry naming.

## Roadmap

- [ ] Add PostGIS extension + a map-based request-location view
- [ ] Add rate limiting at the API gateway
- [ ] Add Terraform for the AWS infra instead of manual provisioning

## License

MIT
