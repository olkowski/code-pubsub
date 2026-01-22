@echo off
REM Script de setup do GCP Pub/Sub para o projeto (Windows)

REM Dados do projeto
set GCP_PROJECT_ID=elated-bison-474212-v4
set GCP_REGION=us-central1
set GCP_PUBSUB_TOPIC=cloud-scheduler
set GCP_PUBSUB_SUBSCRIPTION=cloud-scheduler-sub

echo.
echo ==========================================
echo GCP Pub/Sub Setup
echo ==========================================
echo Project ID: %GCP_PROJECT_ID%
echo Region: %GCP_REGION%
echo Topic: %GCP_PUBSUB_TOPIC%
echo Subscription: %GCP_PUBSUB_SUBSCRIPTION%
echo ==========================================
echo.

REM Configurar projeto
gcloud config set project %GCP_PROJECT_ID%
gcloud config set compute/region %GCP_REGION%

REM Criar tópico
echo 📝 Criando/Verificando tópico: %GCP_PUBSUB_TOPIC%
gcloud pubsub topics create %GCP_PUBSUB_TOPIC% --project=%GCP_PROJECT_ID% 2>nul

REM Criar subscription
echo 📝 Criando/Verificando subscription: %GCP_PUBSUB_SUBSCRIPTION%
gcloud pubsub subscriptions create %GCP_PUBSUB_SUBSCRIPTION% ^
    --topic=%GCP_PUBSUB_TOPIC% ^
    --project=%GCP_PROJECT_ID% 2>nul

echo.
echo ✅ Setup concluído!
echo.
echo Para usar as variáveis de ambiente no PowerShell:
echo $env:GCP_PROJECT_ID = "%GCP_PROJECT_ID%"
echo $env:GCP_REGION = "%GCP_REGION%"
echo $env:GCP_PUBSUB_TOPIC = "%GCP_PUBSUB_TOPIC%"
echo $env:GCP_PUBSUB_SUBSCRIPTION = "%GCP_PUBSUB_SUBSCRIPTION%"
echo.
