# 📋 Checklist de Configuração - GitHub Actions Deploy

## ✅ O que foi feito:

- [x] Arquivo `ci.yml` atualizado com pipeline CI/CD completo
- [x] Build com Maven
- [x] Testes unitários
- [x] Build de imagem nativa (opcional)
- [x] Upload de artifacts
- [x] Deploy no Cloud Run
- [x] Suporte para Workload Identity Federation
- [x] Fallback para Service Account JSON
- [x] Documentação de deployment

## 🔧 Próximas ações necessárias:

### 1. **Setup Workload Identity Federation no GCP** (Recomendado)

```bash
# Execute os comandos em ordem

# 1.1 Criar Workload Identity Pool
gcloud iam workload-identity-pools create "github" \
  --project="elated-bison-474212-v4" \
  --location="global" \
  --display-name="GitHub Actions"

# 1.2 Criar Provider OIDC
gcloud iam workload-identity-pools providers create-oidc "github-provider" \
  --project="elated-bison-474212-v4" \
  --location="global" \
  --workload-identity-pool="github" \
  --display-name="GitHub Provider" \
  --attribute-mapping="google.subject=assertion.sub,attribute.actor=assertion.actor,attribute.aud=assertion.aud" \
  --issuer-uri="https://token.actions.githubusercontent.com"

# 1.3 Criar Service Account
gcloud iam service-accounts create github-actions \
  --project="elated-bison-474212-v4"

# 1.4 Conceder permissões (Cloud Run Admin)
gcloud projects add-iam-policy-binding elated-bison-474212-v4 \
  --member="serviceAccount:github-actions@elated-bison-474212-v4.iam.gserviceaccount.com" \
  --role="roles/run.admin"

# 1.5 Conceder permissões (GCR - Container Registry)
gcloud projects add-iam-policy-binding elated-bison-474212-v4 \
  --member="serviceAccount:github-actions@elated-bison-474212-v4.iam.gserviceaccount.com" \
  --role="roles/storage.admin"

# 1.6 Conceder permissões (Pub/Sub)
gcloud projects add-iam-policy-binding elated-bison-474212-v4 \
  --member="serviceAccount:github-actions@elated-bison-474212-v4.iam.gserviceaccount.com" \
  --role="roles/pubsub.editor"

# 1.7 Configurar Workload Identity Federation
WORKLOAD_IDENTITY_POOL_RESOURCE_NAME=$(gcloud iam workload-identity-pools describe "github" \
  --project="elated-bison-474212-v4" \
  --location="global" \
  --format="value(name)")

gcloud iam service-accounts add-iam-policy-binding "github-actions@elated-bison-474212-v4.iam.gserviceaccount.com" \
  --project="elated-bison-474212-v4" \
  --role="roles/iam.workloadIdentityUser" \
  --subject="principalSet://goog/subject/${WORKLOAD_IDENTITY_POOL_RESOURCE_NAME}/subject/repo:SEU_USUARIO/code-pubsub:ref:refs/heads/main"
```

### 2. **Adicionar Secrets no GitHub**

No seu repositório GitHub:
1. Settings → Secrets and variables → Actions
2. Click "New repository secret" e adicione:

**Secret 1 - WIF_PROVIDER:**
```
Nome: WIF_PROVIDER
Valor: projects/NUMERO_DO_PROJECT/locations/global/workloadIdentityPools/github/providers/github-provider
```

Para obter o PROJECT_NUMBER:
```bash
gcloud projects describe elated-bison-474212-v4 --format='value(projectNumber)'
```

**Secret 2 - WIF_SERVICE_ACCOUNT:**
```
Nome: WIF_SERVICE_ACCOUNT
Valor: github-actions@elated-bison-474212-v4.iam.gserviceaccount.com
```

### 3. **Ativar Cloud Run API** (se não estiver ativada)

```bash
gcloud services enable run.googleapis.com --project=elated-bison-474212-v4
gcloud services enable cloudbuild.googleapis.com --project=elated-bison-474212-v4
gcloud services enable container.googleapis.com --project=elated-bison-474212-v4
```

### 4. **Push para main branch**

```bash
git add .
git commit -m "feat: setup GitHub Actions CI/CD com deployment GCP"
git push origin main
```

O pipeline será acionado automaticamente!

### 5. **Validar Pipeline**

- Vá para GitHub → Actions
- Veja a execução do workflow
- Clique em "Deploy" para ver logs detalhados

## 📌 Informações importantes:

- **Branch que faz deploy:** `main` (apenas pushes nesta branch)
- **PRs:** Build e testes, mas sem deploy
- **Branch `develop`:** Build e testes
- **Imagem Docker:** `gcr.io/elated-bison-474212-v4/code-pubsub`
- **Cloud Run Service:** `code-pubsub` na região `us-central1`

## 🧪 Testar localmente antes de fazer push:

```bash
# Compilar
./mvnw clean compile

# Testar
./mvnw test

# Build da imagem
docker build -f src/main/docker/Dockerfile.jvm -t code-pubsub:latest .

# Run da imagem
docker run -p 8080:8080 \
  -e GCP_PROJECT_ID=elated-bison-474212-v4 \
  -e GCP_PUBSUB_TOPIC=cloud-cheduler \
  -e GCP_PUBSUB_SUBSCRIPTION=cloud-scheduler-sub \
  code-pubsub:latest
```

## 🆘 Troubleshooting:

### Erro de autenticação no GitHub Actions
- Verificar se WIF_PROVIDER e WIF_SERVICE_ACCOUNT estão corretos
- Verificar se o service account tem as permissões necessárias

### Erro ao fazer push de imagem para GCR
- Executar: `gcloud auth configure-docker gcr.io`
- Verificar quota de storage no GCP

### Cloud Run não inicia
- Ver logs: `gcloud run logs read code-pubsub`
- Verificar variáveis de ambiente
- Verificar permissões de Pub/Sub

## 📚 Referências:

- [GitHub Actions Auth](https://github.com/google-github-actions/auth)
- [Workload Identity Federation](https://cloud.google.com/docs/authentication/workload-identity-federation)
- [Cloud Run Deploy](https://cloud.google.com/run/docs/quickstarts/deploy-container)
