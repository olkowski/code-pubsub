#!/bin/bash
# Script de setup do GCP Pub/Sub para o projeto

# Dados do projeto
export GCP_PROJECT_ID="elated-bison-474212-v4"
export GCP_REGION="us-central1"
export GCP_PUBSUB_TOPIC="cloud-scheduler"
export GCP_PUBSUB_SUBSCRIPTION="cloud-scheduler-sub"

echo "=========================================="
echo "GCP Pub/Sub Setup"
echo "=========================================="
echo "Project ID: $GCP_PROJECT_ID"
echo "Region: $GCP_REGION"
echo "Topic: $GCP_PUBSUB_TOPIC"
echo "Subscription: $GCP_PUBSUB_SUBSCRIPTION"
echo "=========================================="

# Configurar projeto
gcloud config set project $GCP_PROJECT_ID
gcloud config set compute/region $GCP_REGION

# Verificar se o tópico existe, se não criar
if ! gcloud pubsub topics describe projects/$GCP_PROJECT_ID/topics/$GCP_PUBSUB_TOPIC --project=$GCP_PROJECT_ID &>/dev/null; then
    echo "📝 Criando tópico: $GCP_PUBSUB_TOPIC"
    gcloud pubsub topics create $GCP_PUBSUB_TOPIC --project=$GCP_PROJECT_ID
else
    echo "✅ Tópico já existe: $GCP_PUBSUB_TOPIC"
fi

# Verificar se a subscription existe, se não criar
if ! gcloud pubsub subscriptions describe $GCP_PUBSUB_SUBSCRIPTION --project=$GCP_PROJECT_ID &>/dev/null; then
    echo "📝 Criando subscription: $GCP_PUBSUB_SUBSCRIPTION"
    gcloud pubsub subscriptions create $GCP_PUBSUB_SUBSCRIPTION \
        --topic=$GCP_PUBSUB_TOPIC \
        --project=$GCP_PROJECT_ID
else
    echo "✅ Subscription já existe: $GCP_PUBSUB_SUBSCRIPTION"
fi

echo ""
echo "✅ Setup concluído!"
echo ""
echo "Para usar as variáveis de ambiente:"
echo "export GCP_PROJECT_ID=$GCP_PROJECT_ID"
echo "export GCP_REGION=$GCP_REGION"
echo "export GCP_PUBSUB_TOPIC=$GCP_PUBSUB_TOPIC"
echo "export GCP_PUBSUB_SUBSCRIPTION=$GCP_PUBSUB_SUBSCRIPTION"
