package com.chat.web;

import com.superchat.BasicTest;
import com.chat.entity.Contact;
import com.chat.service.ContactsService;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class ContactsControllerTest extends BasicTest {
    @BeforeEach
    void before() {
        clearDB();
    }

    @Test
    public void addUserTest() throws JsonProcessingException {
        String response = given()
                .when()
                .body(MAPPER.writeValueAsString(crateContactRequest()))
                .contentType(ContentType.JSON)
                .post("/api/v1/contacts/add").asString();
        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(ContactsService.USER_ADDED_SUCCESSFULLY));

        Contact contact = Contact.findByEmail(EMAIL).await().indefinitely();
        Assertions.assertEquals(contact.getName(), NAME);
        Assertions.assertEquals(contact.getEmail(), EMAIL);
    }

    @Test
    public void getAllContactsTest() throws IOException {
        addContact();
        String response = given()
                .when()
                .get("/api/v1/contacts").asString();


        List<Contact> contacts = MAPPER.readValue(response, new TypeReference<List<Contact>>(){});
        Assertions.assertNotNull(contacts);
        Assertions.assertEquals(1, contacts.size());
        Assertions.assertEquals(NAME, contacts.get(0).getName());
        Assertions.assertEquals(EMAIL, contacts.get(0).getEmail());
    }

    @Test
    public void sendMessageTest() throws IOException {
        addContact();
        String response = given()
                .when()
                .body(MAPPER.writeValueAsString(crateMessageRequestWithPlaceholders()))
                .contentType(ContentType.JSON)
                .post("/api/v1/contacts/send").asString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(ContactsService.MESSAGE_WAS_SENT));

        Contact contact = Contact.findByEmail(EMAIL).await().indefinitely();
        Assertions.assertEquals( 1, contact.getConversations().size());
        Assertions.assertEquals( FROM, contact.getConversations().get(0).getFrom());
        Assertions.assertEquals( "Hello superchat, The price of bitcon is 37706.83", contact.getConversations().get(0).getContent());
    }

    @Test
    public void sendMessageViaWebhookTest() throws IOException {
        addContact();
        String response = given()
                .when()
                .body(MAPPER.writeValueAsString(crateWebhookRequest()))
                .contentType(ContentType.JSON)
                .post("/api/v1/contacts/webhook").asString();

        Assertions.assertNotNull(response);
        Assertions.assertTrue(response.contains(ContactsService.MESSAGE_WAS_SENT));

        Contact contact = Contact.findByEmail(EMAIL).await().indefinitely();
        Assertions.assertEquals( 1, contact.getConversations().size());
        Assertions.assertEquals( EVENT, contact.getConversations().get(0).getFrom());
        Assertions.assertEquals( CONTENT, contact.getConversations().get(0).getContent());
    }

    @Test
    public void listConversationsTest() throws IOException {
        addContact();
        sendMessage();
        String response = given()
                .when()
                .body(MAPPER.writeValueAsString(crateConversationRequest()))
                .contentType(ContentType.JSON)
                .post("/api/v1/contacts/conversations").asString();

        Assertions.assertNotNull(response);
        Contact contact = Contact.findByEmail(EMAIL).await().indefinitely();
        Assertions.assertEquals( 1, contact.getConversations().size());
        Assertions.assertTrue( response.contains(contact.getConversations().toString()));
    }

    private String sendMessage() throws JsonProcessingException {
        return given()
                .when()
                .body(MAPPER.writeValueAsString(crateMessageRequest()))
                .contentType(ContentType.JSON)
                .post("/api/v1/contacts/send").asString();
    }
    private void addContact() throws JsonProcessingException {
        given()
                .when()
                .body(MAPPER.writeValueAsString(crateContactRequest()))
                .contentType(ContentType.JSON)
                .post("/api/v1/contacts/add").asString();
    }

}
