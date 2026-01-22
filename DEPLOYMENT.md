# GCP Pub/Sub Consumer com Quarkus

Aplicação Quarkus para consumir e produzir mensagens do Google Cloud Pub/Sub.

## 📋 Informações do Projeto

- **Projeto GCP:** `elated-bison-474212-v4`
- **Região:** `us-central1`
- **Tópico Pub/Sub:** `projects/elated-bison-474212-v4/topics/cloud-scheduler`
- **Subscription:** `cloud-scheduler-sub`

## 🚀 Quick Start

### 1. Setup Local

```bash
# Autenticar com GCP
gcloud auth application-default login

# Configurar variáveis de ambiente
export GCP_PROJECT_ID=elated-bison-474212-v4
export GCP_REGION=us-central1
export GCP_PUBSUB_TOPIC=cloud-scheduler
export GCP_PUBSUB_SUBSCRIPTION=cloud-scheduler-sub
```

### 2. Executar em Dev

```bash
./mvnw quarkus:dev
```

Acesse: `http://localhost:8080`

### 3. Endpoints

- **Health Check:** `GET /messages/health`
- **Config:** `GET /messages/config`
- **Publish Message:** `POST /messages/publish`

```bash
curl -X POST http://localhost:8080/messages/publish \
  -H "Content-Type: text/plain" \
  -d "Minha mensagem"
```

## 🔧 GitHub Actions Setup

### Com Workload Identity Federation (Recomendado)

1. **Configure no Google Cloud:**

```bash
# Criar Workload Identity Pool
gcloud iam workload-identity-pools create "github" \
  --project="elated-bison-474212-v4" \
  --location="global" \
  --display-name="GitHub Actions"

# Criar Provider
gcloud iam workload-identity-pools providers create-oidc "github-provider" \
  --project="elated-bison-474212-v4" \
  --location="global" \
  --workload-identity-pool="github" \
  --display-name="GitHub" \
  --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.aud=assertion.aud" \
  --issuer-uri="https://token.actions.githubusercontent.com"

# Criar Service Account
gcloud iam service-accounts create github-actions \
  --project="elated-bison-474212-v4"

# Adicionar permissões
gcloud projects add-iam-policy-binding elated-bison-474212-v4 \
  --member="serviceAccount:github-actions@elated-bison-474212-v4.iam.gserviceaccount.com" \
  --role="roles/run.admin"

gcloud projects add-iam-policy-binding elated-bison-474212-v4 \
  --member="serviceAccount:github-actions@elated-bison-474212-v4.iam.gserviceaccount.com" \
  --role="roles/storage.admin"

gcloud projects add-iam-policy-binding elated-bison-474212-v4 \
  --member="serviceAccount:github-actions@elated-bison-474212-v4.iam.gserviceaccount.com" \
  --role="roles/pubsub.editor"
```

2. **Configure WIF no Service Account:**

```bash
# Obter resource name do pool
WORKLOAD_IDENTITY_POOL_RESOURCE_NAME=$(gcloud iam workload-identity-pools describe "github" \
  --project="elated-bison-474212-v4" \
  --location="global" \
  --format="value(name)")

# Configurar WIF
gcloud iam service-accounts add-iam-policy-binding "github-actions@elated-bison-474212-v4.iam.gserviceaccount.com" \
  --project="elated-bison-474212-v4" \
  --role="roles/iam.workloadIdentityUser" \
  --subject="principalSet://goog/subject/${WORKLOAD_IDENTITY_POOL_RESOURCE_NAME}/subject/repo:SEU_USUARIO/code-pubsub:ref:refs/heads/main"
```

3. **Adicione secrets no GitHub:**

Repository → Settings → Secrets and variables → Actions

- `WIF_PROVIDER`: `projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/github/providers/github-provider`
- `WIF_SERVICE_ACCOUNT`: `github-actions@elated-bison-474212-v4.iam.gserviceaccount.com`

### Com Service Account JSON (Alternativa)

1. **Crie a service account:**

```bash
gcloud iam service-accounts keys create key.json \
  --iam-account=github-actions@elated-bison-474212-v4.iam.gserviceaccount.com
```

2. **Adicione como secret:**

Repository → Settings → Secrets → New repository secret
- Nome: `GCP_CREDENTIALS`
- Valor: Conteúdo de `key.json`

## 📦 Build Local

### JVM
```bash
./mvnw clean package -DskipTests
java -jar target/quarkus-app/quarkus-run.jar
```

### Nativo
```bash
./mvnw clean package -Dnative -DskipTests
./target/code-pubsub-1.0.0-SNAPSHOT-runner
```

## 🐳 Docker

### Build
```bash
docker build -f src/main/docker/Dockerfile.jvm -t code-pubsub:latest .
```

### Run
```bash
docker run -i --rm \
  -p 8080:8080 \
  -e GCP_PROJECT_ID=elated-bison-474212-v4 \
  -e GCP_PUBSUB_TOPIC=cloud-scheduler \
  -e GCP_PUBSUB_SUBSCRIPTION=cloud-scheduler-sub \
  code-pubsub:latest
```

## ☁️ Deploy no Cloud Run

```bash
gcloud run deploy code-pubsub \
  --source . \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --set-env-vars GCP_PROJECT_ID=elated-bison-474212-v4,GCP_PUBSUB_TOPIC=cloud-scheduler,GCP_PUBSUB_SUBSCRIPTION=cloud-scheduler-sub \
  --memory 512Mi \
  --cpu 1
```

## 📊 Estrutura

```
.
├── src/main/java/br/com/fiap/pubsub/
│   ├── GcpConfig.java              # Configurações GCP
│   ├── PubSubMessageProducer.java  # Produtor
│   ├── PubSubMessageConsumer.java  # Consumidor
│   └── MessageResource.java         # Endpoints REST
├── src/main/resources/
│   └── application.properties       # Configurações
├── .github/workflows/
│   └── ci.yml                       # CI/CD Pipeline
└── pom.xml
```

## 📝 Logs

```bash
# Ver logs da aplicação
gcloud run logs read code-pubsub --limit 50

# Streaming logs
gcloud run logs read code-pubsub --limit 0 --follow
```

## 🔍 Troubleshooting

### Conexão com Pub/Sub falha
```bash
# Verificar tópico
gcloud pubsub topics describe projects/elated-bison-474212-v4/topics/cloud-scheduler

# Verificar subscription
gcloud pubsub subscriptions describe cloud-scheduler-sub
```

### Permissões insuficientes
```bash
gcloud projects get-iam-policy elated-bison-474212-v4 \
  --flatten="bindings[].members" \
  --format="table(bindings.role)" \
  --filter="bindings.members:github-actions@elated-bison-474212-v4.iam.gserviceaccount.com"
```

## 📚 Referências

- [Quarkus Google Cloud Integration](https://quarkus.io/guides/gcp)
- [Google Cloud Pub/Sub Java Client](https://github.com/googleapis/java-pubsub)
- [GitHub Actions Workload Identity](https://github.com/google-github-actions/auth)
- [Cloud Run Documentation](https://cloud.google.com/run/docs)
