package com.epam.digital.data.platform.bpm.history.kafka;

import static org.apache.kafka.common.config.TopicConfig.RETENTION_MS_CONFIG;

import com.epam.digital.data.platform.bpm.history.kafka.KafkaProperties.TopicProperties;
import com.epam.digital.data.platform.bpm.history.kafka.exception.CreateKafkaTopicException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;

@Slf4j
@RequiredArgsConstructor
public class StartupHistoryProcessKafkaTopicCreator {

  private static final long DAYS_TO_MS = 24 * 60 * 60 * 1000L;
  private static final long TOPIC_CREATION_TIMEOUT = 60L;

  private final AdminClient kafkaAdminClient;
  private final KafkaProperties kafkaProperties;

  @PostConstruct
  public void createKafkaTopics() {
    var topicsToCreate = getNewTopics();

    log.info("Creating next topics {}", topicsToCreate);
    var createTopicsResult = kafkaAdminClient.createTopics(topicsToCreate);
    try {
      createTopicsResult.all().get(TOPIC_CREATION_TIMEOUT, TimeUnit.SECONDS);
      log.info("All required topics created.");
    } catch (Exception e) {
      throw new CreateKafkaTopicException(
          String.format("Failed to create kafka topics %s in %d sec", topicsToCreate,
              TOPIC_CREATION_TIMEOUT), e);
    }
  }

  private Stream<TopicProperties> getRequiredTopicsStream() {
    return Stream.of(
        kafkaProperties.getTopics().getHistoryProcessInstanceTopic(),
        kafkaProperties.getTopics().getHistoryTaskTopic());
  }

  private Set<String> getExistingTopics() {
    try {
      return kafkaAdminClient.listTopics().names()
          .get(TOPIC_CREATION_TIMEOUT, TimeUnit.SECONDS);
    } catch (Exception e) {
      throw new CreateKafkaTopicException(String.format(
          "Failed to retrieve existing kafka topics in %d sec", TOPIC_CREATION_TIMEOUT), e);
    }
  }

  private Set<NewTopic> getNewTopics() {
    log.info("Selecting existing topics...");
    var existingTopicNames = getExistingTopics();
    log.info("Found next kafka topics - {}", existingTopicNames);

    return getRequiredTopicsStream()
        .filter(requiredTopic -> !existingTopicNames.contains(requiredTopic.getName()))
        .map(this::getNewTopic)
        .collect(Collectors.toSet());
  }

  private NewTopic getNewTopic(TopicProperties topicProperties) {
    var newTopic = new NewTopic(topicProperties.getName(), topicProperties.getNumPartitions(),
        topicProperties.getReplicationFactor());

    var days = topicProperties.getRetentionPolicyInDays();
    newTopic.configs(Map.of(RETENTION_MS_CONFIG, Long.toString(days * DAYS_TO_MS)));
    return newTopic;
  }
}
