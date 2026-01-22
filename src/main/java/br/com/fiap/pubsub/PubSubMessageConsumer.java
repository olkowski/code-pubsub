package br.com.fiap.pubsub;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.TimeUnit;

@ApplicationScoped
public class PubSubMessageConsumer {

    private final String projectId = System.getenv("GCP_PROJECT_ID");
    private final String subscriptionId = System.getenv("GCP_PUBSUB_SUBSCRIPTION");

    public void startConsuming() throws Exception {
        ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(projectId, subscriptionId);

        // Cria um subscriber
        Subscriber subscriber = Subscriber
                .newBuilder(subscriptionName, (com.google.cloud.pubsub.v1.MessageReceiver) (message, consumer) -> {
                    // Processa a mensagem
                    String messageData = message.getData().toStringUtf8();
                    Log.infof("Mensagem recebida: %s", messageData);
                    // Acknowledge da mensagem
                    consumer.ack();
                }).build();

        // Inicia o subscriber
        subscriber.startAsync().awaitRunning();
        Log.info("Consumer de Pub/Sub iniciado");

        // Aguarda por 10 minutos
        subscriber.awaitTerminated(10, TimeUnit.MINUTES);
    }

    public void stopConsuming(Subscriber subscriber) throws Exception {
        subscriber.stopAsync().awaitTerminated();
        Log.info("Consumer de Pub/Sub parado");
    }
}
