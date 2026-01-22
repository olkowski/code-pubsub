package br.com.fiap.pubsub;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.ProjectTopicName;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PubSubMessageProducer {

    private final String projectId = System.getenv("GCP_PROJECT_ID");
    private final String topicId = System.getenv("GCP_PUBSUB_TOPIC");

    public String publishMessage(String messageContent) throws Exception {
        ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
        Publisher publisher = Publisher.newBuilder(topicName).build();

        try {
            // Cria a mensagem
            ByteString data = ByteString.copyFromUtf8(messageContent);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            // Publica a mensagem
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            String messageId = messageIdFuture.get();

            Log.infof("Mensagem publicada com ID: %s", messageId);
            return messageId;
        } finally {
            if (publisher != null) {
                publisher.shutdown();
            }
        }
    }
}
