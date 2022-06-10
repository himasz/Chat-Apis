package com.chat.service;

import com.superchat.BasicTest;
import com.chat.dto.*;
import com.chat.entity.Contact;
import com.chat.entity.Conversation;
import com.chat.response.ResponseEntityDecorator;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
class ContactsServiceTest extends BasicTest {

    @Inject
    ContactsService service;

    @BeforeEach
    void before() {
        PanacheMock.mock(Contact.class);
    }

    @Test
    void addContactTest() {
        //When
        Uni<Response> responseUni = service.addContact(ContactRequestDTO.builder().build());

        //Then
        Response response = responseUni.await().indefinitely();
        ResponseEntityDecorator entity = (ResponseEntityDecorator) response.getEntity();
        Assertions.assertEquals(ContactsService.USER_ADDED_SUCCESSFULLY, entity.getMessage());
    }

    @Test
    void getAllContacts() {
        //Given
        Mockito.when(Contact.<Contact>streamAll()).thenReturn(createContacts());

        //When
        Multi<ContactResponseDTO> allContactsMulti = service.getAllContacts();
        List<ContactResponseDTO> responses = allContactsMulti.collect().asList().await().indefinitely();

        //Then
        Assertions.assertNotNull(responses);
        Assertions.assertEquals(1, responses.size());
        Assertions.assertEquals(NAME, responses.get(0).getName());
        Assertions.assertEquals(EMAIL, responses.get(0).getEmail());
        Assertions.assertEquals(ADDRESS, responses.get(0).getAddress());
        Assertions.assertEquals(PHONE, responses.get(0).getPhoneNumber());
    }

    @Test
    void sendMessage() {
        //Given
        Contact contact = Mockito.mock(Contact.class);
        Mockito.when(contact.getName()).thenReturn(NAME);
        List<Conversation> conversations = new ArrayList<>();

        Mockito.when(contact.getConversations()).thenReturn(conversations);
        Mockito.when(contact.update()).thenReturn(Uni.createFrom().item(contact));
        Mockito.when(Contact.findByEmail(EMAIL)).thenReturn(Uni.createFrom().item(contact));

        //When
        Response response = service.sendMessage(crateMessageRequest()).await().indefinitely();

        //Then
        Assertions.assertNotNull(response);
        ResponseEntityDecorator entity = (ResponseEntityDecorator) response.getEntity();
        Assertions.assertEquals(ContactsService.MESSAGE_WAS_SENT, entity.getMessage());
        Assertions.assertEquals(1, conversations.size());
        Assertions.assertEquals(CONTENT, conversations.get(0).getContent());
        Assertions.assertEquals(FROM, conversations.get(0).getFrom());
    }

    @Test
    void sendMessageViaWebhook() {
        //Given
        Contact contact = Mockito.mock(Contact.class);
        Mockito.when(contact.getName()).thenReturn(NAME);
        List<Conversation> conversations = new ArrayList<>();

        Mockito.when(contact.getConversations()).thenReturn(conversations);
        Mockito.when(contact.update()).thenReturn(Uni.createFrom().item(contact));
        Mockito.when(Contact.findByEmail(EMAIL)).thenReturn(Uni.createFrom().item(contact));

        //When
        Response response = service.sendMessageViaWebhook(crateWebhookRequestDTO()).await().indefinitely();

        //Then
        Assertions.assertNotNull(response);
        ResponseEntityDecorator entity = (ResponseEntityDecorator) response.getEntity();
        Assertions.assertEquals(ContactsService.MESSAGE_WAS_SENT, entity.getMessage());
        Assertions.assertEquals(1, conversations.size());
        Assertions.assertEquals(CONTENT, conversations.get(0).getContent());
        Assertions.assertEquals(LOCAL_DATE, conversations.get(0).getDate());
        Assertions.assertEquals(FROM, conversations.get(0).getFrom());
    }

    @Test
    void listConversations() {
        //Given
        Mockito.when(Contact.findByEmail(EMAIL)).thenReturn(Uni.createFrom().item(createContact()));

        //When
        Uni<Response> responseUni = service.listConversations(ConversationRequestDTO.builder().email(EMAIL).build());
        Response response = responseUni.await().indefinitely();

        //Then
        Assertions.assertNotNull(response);
        ResponseEntityDecorator entity = (ResponseEntityDecorator) response.getEntity();
        Assertions.assertEquals(List.of(createConversation()).toString(), entity.getMessage());
    }
}