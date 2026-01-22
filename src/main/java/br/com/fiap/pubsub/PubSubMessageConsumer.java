package br.com.fiap.pubsub;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.quarkus.logging.Log;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class PubSubMessageConsumer {

    @Inject
    GcpConfig gcpConfig;

    private final AtomicReference<Subscriber> subscriberRef = new AtomicReference<>();

    void onStart(@Observes StartupEvent ev) {
        Log.info("========================================");
        Log.info("Iniciando Pub/Sub Consumer...");
        Log.info("========================================");
        try {
            startConsuming();
        } catch (Exception e) {
            Log.errorf(e, "❌ Erro ao iniciar Pub/Sub Consumer");
            throw new RuntimeException("Falha na inicialização do Consumer", e);
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        Log.info("Parando Pub/Sub Consumer...");
        try {
            stopConsuming();
        } catch (Exception e) {
            Log.errorf(e, "Erro ao parar Pub/Sub Consumer");
        }
    }

    public void startConsuming() throws Exception {
        String projectId = gcpConfig.getProjectId();
        String subscriptionId = gcpConfig.getSubscription();

        Log.infof("========================================");
        Log.infof("Conectando ao Pub/Sub");
        Log.infof("  Projeto: %s", projectId);
        Log.infof("  Subscription: %s", subscriptionId);
        Log.infof("========================================");

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);
        Log.infof("Full Subscription Path: %s", subscriptionName.toString());

        // Cria um subscriber
        Subscriber subscriber = Subscriber
                .newBuilder(subscriptionName, (com.google.cloud.pubsub.v1.MessageReceiver) (message, consumer) -> {
                    try {
                        // Processa a mensagem
                        String messageData = message.getData().toStringUtf8();
                        String messageId = message.getMessageId();
                        Log.infof("========================================");
                        Log.infof("📨 Mensagem recebida!");
                        Log.infof("  ID: %s", messageId);
                        Log.infof("  Dados: %s", messageData);
                        Log.infof("========================================");

                        // Acknowledge da mensagem
                        consumer.ack();
                        Log.infof("✅ Mensagem %s reconhecida", messageId);
                    } catch (Exception e) {
                        Log.errorf(e, "❌ Erro ao processar mensagem");
                        consumer.nack();
                    }
                })
                .setParallelPullCount(4)
                .build();

        // Armazena o subscriber para shutdown gracioso
        subscriberRef.set(subscriber);

        // Inicia o subscriber
        subscriber.startAsync().awaitRunning();
        Log.info("========================================");
        Log.info("✅ Pub/Sub Consumer iniciado e aguardando mensagens");
        Log.info("========================================");
    }

    public void stopConsuming() throws Exception {
        Subscriber subscriber = subscriberRef.get();
        if (subscriber != null) {
            subscriber.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
            Log.info("✅ Pub/Sub Consumer parado");
        }
    }
}
