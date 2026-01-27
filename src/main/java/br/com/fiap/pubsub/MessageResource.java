package br.com.fiap.pubsub;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/messages")
@Produces(MediaType.APPLICATION_JSON)
public class MessageResource {

    @Inject
    PubSubMessageProducer producer;

    @Inject
    GcpConfig gcpConfig;

    @POST
    @Path("/publish")
    public Response publishMessage(String message) {
        try {
            String messageId = producer.publishMessage(message);
            return Response.ok("{\"status\": \"OK\", \"messageId\": \"" + messageId + "\"}").build();
        } catch (Exception e) {
            Log.errorf(e, "Erro ao publicar mensagem");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("{\"status\": \"OK\", \"service\": \"Pub/Sub Consumer/Producer 2\"}").build();
    }

    @GET
    @Path("/config")
    public Response getConfig() {
        try {
            return Response.ok("{" +
                    "\"projectId\": \"" + gcpConfig.getProjectId() + "\"," +
                    "\"region\": \"" + gcpConfig.getRegion() + "\"," +
                    "\"topic\": \"" + gcpConfig.getTopic() + "\"," +
                    "\"subscription\": \"" + gcpConfig.getSubscription() + "\"" +
                    "}").build();
        } catch (Exception e) {
            Log.errorf(e, "Erro ao obter configuração");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/debug")
    public Response debug() {
        try {
            String projectId = gcpConfig.getProjectId();
            String topic = gcpConfig.getTopic();
            String subscription = gcpConfig.getSubscription();

            String debugInfo = "{" +
                    "\"status\": \"Configuração carregada\"," +
                    "\"projectId\": \"" + projectId + "\"," +
                    "\"topic\": \"" + topic + "\"," +
                    "\"subscription\": \"" + subscription + "\"," +
                    "\"fullTopicPath\": \"projects/" + projectId + "/topics/" + topic + "\"," +
                    "\"fullSubscriptionPath\": \"projects/" + projectId + "/subscriptions/" + subscription + "\"" +
                    "}";

            Log.infof("Debug Info: %s", debugInfo);
            return Response.ok(debugInfo).build();
        } catch (Exception e) {
            Log.errorf(e, "Erro no debug");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"" + e.getMessage() + "\"}").build();
        }
    }
}
