# Gov-Portal Microservice Starter

A production-style reference application that mirrors the architecture I build in my day-to-day work: a secured Spring Boot backend, a VueJS front end, containerized with Docker, and deployed to Kubernetes with a CI/CD pipeline. Built as a self-contained showcase of the stack I use professionally on large-scale public-sector platforms.

## Why this project

At work I lead a team building Spring Boot + VueJS applications for a state government client — containerized with Docker, orchestrated on Kubernetes, deployed to AWS via Jenkins, and monitored with Splunk. That code isn't shareable, so this project recreates the same architecture end-to-end on a smaller scale: a citizen-services-style portal where users can submit and track service requests.

## Architecture

```
┌─────────────┐      HTTPS      ┌──────────────────┐      JDBC      ┌────────────┐
│  VueJS SPA  │ ───────────────▶│  Spring Boot API  │ ──────────────▶│ PostgreSQL │
│  (Nginx)    │◀─────────────── │  (REST, OAuth2)   │◀────────────── │            │
└─────────────┘     JSON        └──────────────────┘                └────────────┘
                                          │
                                          ▼
                                 ┌──────────────────┐
                                 │  Kafka (events)   │
                                 └──────────────────┘

  Docker containers  →  Kubernetes (Deployments, Services, Ingress)  →  AWS (EKS/EC2)
  Jenkins CI/CD  →  build, test, image push, rolling deploy
  Structured JSON logs  →  Splunk (or ELK locally)
```

## Tech stack

| Layer | Technology |
|---|---|
| Frontend | VueJS 3, Vue Router, Pinia, Axios |
| Backend | Java 17, Spring Boot, Spring Security (OAuth2/JWT), Spring Data JPA |
| Messaging | Apache Kafka |
| Database | PostgreSQL |
| Auth | OAuth2 password/client-credentials flow, JWT access tokens |
| Containerization | Docker, Docker Compose (local), Kubernetes (Deployments, Services, Ingress, ConfigMaps, Secrets) |
| CI/CD | Jenkins declarative pipeline (build → test → image → push → deploy) |
| Observability | Structured JSON logging (Logback), Spring Actuator health/metrics endpoints, dashboards designed for Splunk ingestion |
| Cloud | AWS (ECR for images, EKS/EC2 for compute, S3 for file uploads, CloudWatch for infra metrics) |

## Features

- User authentication and role-based access control (citizen vs. case-worker roles) via OAuth2/JWT
- Submit, update, and track service requests through a REST API
- Kafka event stream on request-status changes, consumed by a notification service
- Full test suite: JUnit 5 + Mockito for the backend, Vitest for the frontend
- Dockerized for local development with `docker-compose up`
- Kubernetes manifests for a production-style rollout (readiness/liveness probes, resource limits, HPA)
- Jenkinsfile implementing the same build → test → containerize → deploy pipeline used in production systems
- Actuator + structured logs so the app is ready to plug into Splunk or the ELK stack

## Getting started

### Prerequisites
- Java 17+, Maven
- Node 18+
- Docker & Docker Compose
- (Optional) `kubectl` + a local cluster (kind/minikube) to try the K8s manifests

### Run locally with Docker Compose
```bash
git clone https://github.com/thoomyashwanthreddy/gov-portal-microservice-starter.git
cd gov-portal-microservice-starter
docker-compose up --build
```
- API: http://localhost:8080
- Frontend: http://localhost:5173
- Kafka UI: http://localhost:8081

### Run the backend alone
```bash
cd backend
mvn spring-boot:run
```

### Run the frontend alone
```bash
cd frontend
npm install
npm run dev
```

### Run tests
```bash
# backend
cd backend && mvn test

# frontend
cd frontend && npm run test
```

## Deploying to Kubernetes

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secrets.yaml
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/ingress.yaml
```

## CI/CD

`Jenkinsfile` defines a four-stage pipeline:
1. **Build & unit test** — Maven for backend, npm for frontend
2. **Containerize** — build and tag Docker images
3. **Push** — push images to AWS ECR
4. **Deploy** — rolling update to the Kubernetes cluster via `kubectl apply`

## API overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/login` | Authenticate and receive a JWT |
| GET | `/api/requests` | List service requests (role-filtered) |
| POST | `/api/requests` | Submit a new service request |
| PATCH | `/api/requests/{id}` | Update request status (case-worker only) |
| GET | `/actuator/health` | Liveness/readiness probe endpoint |

## Roadmap

- [ ] Add PostGIS extension + a map-based request-location view
- [ ] Add rate limiting at the API gateway
- [ ] Add Terraform for the AWS infra instead of manual provisioning

## License

MIT
