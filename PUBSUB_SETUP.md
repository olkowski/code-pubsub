# Consumer de GCP Pub/Sub com Quarkus

Este projeto implementa um consumidor e produtor de mensagens para Google Cloud Pub/Sub usando Quarkus.

## 📋 Pré-requisitos

1. **Conta GCP** com projeto criado
2. **Google Cloud SDK** instalado
3. **Variáveis de ambiente** configuradas

## 🔧 Configuração

### 1. Autenticar com GCP

```bash
gcloud auth application-default login
```

Ou use um arquivo JSON de credenciais:
```bash
export GOOGLE_APPLICATION_CREDENTIALS=/caminho/para/credenciais.json
```

### 2. Criar Tópico e Subscription

```bash
# Criar tópico
gcloud pubsub topics create my-topic

# Criar subscription
gcloud pubsub subscriptions create my-subscription --topic=my-topic
```

### 3. Configurar Variáveis de Ambiente

```bash
export GCP_PROJECT_ID=seu-projeto-gcp
export GCP_PUBSUB_TOPIC=my-topic
export GCP_PUBSUB_SUBSCRIPTION=my-subscription
```

## 🚀 Executando a Aplicação

```bash
./mvnw quarkus:dev
```

A aplicação iniciará em `http://localhost:8080`

## 📡 Endpoints Disponíveis

### Health Check
```bash
curl http://localhost:8080/messages/health
```

### Publicar Mensagem
```bash
curl -X POST http://localhost:8080/messages/publish \
  -H "Content-Type: text/plain" \
  -d "Minha mensagem"
```

### Arquivo Completo

## 📁 Estrutura do Projeto

```
src/main/java/br/com/fiap/pubsub/
├── PubSubMessageProducer.java   # Publica mensagens
├── PubSubMessageConsumer.java   # Consome mensagens
└── MessageResource.java         # Endpoints REST
```

## 🔗 Classes Principais

### PubSubMessageProducer
- Publica mensagens em um tópico do Pub/Sub
- Usa `Publisher` da biblioteca Google Cloud

### PubSubMessageConsumer
- Consome mensagens de uma subscription
- Implementa processamento assíncrono com `Subscriber`
- Confirma (ACK) automaticamente

### MessageResource
- Expõe endpoints REST para publicar e verificar saúde
- Integra o produtor

## 🛠️ Build para Produção

```bash
# Build JVM
./mvnw clean package

# Build Nativo
./mvnw clean package -Dnative
```
