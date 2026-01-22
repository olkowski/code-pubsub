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
        Log.info("Iniciando Pub/Sub Consumer...");
        try {
            startConsuming();
        } catch (Exception e) {
            Log.errorf(e, "Erro ao iniciar Pub/Sub Consumer");
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

        Log.infof("Conectando ao Pub/Sub - Projeto: %s, Subscription: %s", projectId, subscriptionId);

        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

        // Cria um subscriber
        Subscriber subscriber = Subscriber
                .newBuilder(subscriptionName, (com.google.cloud.pubsub.v1.MessageReceiver) (message, consumer) -> {
                    // Processa a mensagem
                    String messageData = message.getData().toStringUtf8();
                    Log.infof("📨 Mensagem recebida: %s", messageData);
                    // Acknowledge da mensagem
                    consumer.ack();
                })
                .setParallelPullCount(4)
                .build();

        // Armazena o subscriber para shutdown gracioso
        subscriberRef.set(subscriber);

        // Inicia o subscriber
        subscriber.startAsync().awaitRunning();
        Log.info("✅ Pub/Sub Consumer iniciado e aguardando mensagens");
    }

    public void stopConsuming() throws Exception {
        Subscriber subscriber = subscriberRef.get();
        if (subscriber != null) {
            subscriber.stopAsync().awaitTerminated(10, TimeUnit.SECONDS);
            Log.info("✅ Pub/Sub Consumer parado");
        }
    }
}
