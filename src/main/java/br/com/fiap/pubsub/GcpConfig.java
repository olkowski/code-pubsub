package br.com.fiap.pubsub;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GcpConfig {

    @ConfigProperty(name = "gcp.project-id")
    public String projectId;

    @ConfigProperty(name = "gcp.region")
    public String region;

    @ConfigProperty(name = "gcp.pubsub.topic")
    public String topic;

    @ConfigProperty(name = "gcp.pubsub.subscription")
    public String subscription;

    public String getProjectId() {
        return projectId;
    }

    public String getRegion() {
        return region;
    }

    public String getTopic() {
        return topic;
    }

    public String getSubscription() {
        return subscription;
    }

    @Override
    public String toString() {
        return "GcpConfig{" +
                "projectId='" + projectId + '\'' +
                ", region='" + region + '\'' +
                ", topic='" + topic + '\'' +
                ", subscription='" + subscription + '\'' +
                '}';
    }
}
