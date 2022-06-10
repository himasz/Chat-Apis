package com.superchat;

import com.chat.dto.ContactRequestDTO;
import com.chat.dto.ConversationRequestDTO;
import com.chat.dto.MessageRequestDTO;
import com.chat.dto.WebhookRequestDTO;
import com.chat.entity.Contact;
import com.chat.entity.Conversation;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import io.smallrye.mutiny.Multi;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@Testcontainers
@QuarkusTestResource(BasicTest.Initializer.class)
public class BasicTest {
    public static final String NAME = "superchat";
    public static final String PHONE = "phone";
    public static final String ADDRESS = "Address";
    public static final String EMAIL = "info@superchat.com";
    public static final String CONTENT = "This is a message!";

    public static final String CONTENT_WITH_PLACEHOLDERS = "Hello {{name}}, The price of bitcon is {{bitcoinPrice}}";
    public static final LocalDateTime LOCAL_DATE = LocalDateTime.now();
    public static final String FROM = "bla";
    public static final String EVENT = "webhook";

    @Container
    public static final MongoDBContainer mongoDBContainer = new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    public static final ObjectMapper MAPPER = new ObjectMapper();

    public static class Initializer implements QuarkusTestResourceLifecycleManager {

        @Override
        public Map<String, String> start() {
            BasicTest.mongoDBContainer.start();
            return Map.of("quarkus.mongodb.connection-string", "mongodb://" + mongoDBContainer.getContainerIpAddress() + ":" + mongoDBContainer.getFirstMappedPort() + "/contacts");
        }

        @Override
        public void stop() {
            BasicTest.mongoDBContainer.stop();
        }

    }

    public Contact createContact() {
        return new Contact(NAME, EMAIL, "EVENT", ADDRESS, PHONE, List.of(createConversation()));
    }

    public Conversation createConversation() {
        return new Conversation(FROM, CONTENT, LOCAL_DATE);
    }

    public Multi<Contact> createContacts() {
        List<Contact> contactList = new ArrayList<>();
        contactList.add(createContact());
        return Multi.createFrom().iterable(contactList);
    }

    public MessageRequestDTO crateMessageRequest() {
        return MessageRequestDTO.builder().to(EMAIL).from(FROM).content(CONTENT).build();
    }
    public MessageRequestDTO crateMessageRequestWithDate() {
        return MessageRequestDTO.builder().to(EMAIL).from(FROM).content(CONTENT).date(LOCAL_DATE).build();
    }
    public MessageRequestDTO crateMessageRequestWithPlaceholders() {
        return MessageRequestDTO.builder().to(EMAIL).from(FROM).content(CONTENT_WITH_PLACEHOLDERS).build();
    }

    public WebhookRequestDTO crateWebhookRequestDTO() {
        return WebhookRequestDTO.builder().event(FROM).to(EMAIL).publishedAt(LOCAL_DATE.toString()).data(CONTENT).build();
    }

    public void clearDB() {
        Contact.deleteAll().await().indefinitely();
    }


    public ContactRequestDTO crateContactRequest() {
        return ContactRequestDTO.builder().email(EMAIL).name(NAME).build();
    }

    public WebhookRequestDTO crateWebhookRequest() {
        return WebhookRequestDTO.builder().event(EVENT).data(CONTENT).to(EMAIL).publishedAt(LOCAL_DATE.toString()).build();
    }

    public ConversationRequestDTO crateConversationRequest() {
        return ConversationRequestDTO.builder().email(EMAIL).build();
    }

}
