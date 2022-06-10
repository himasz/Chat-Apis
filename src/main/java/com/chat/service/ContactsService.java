package com.chat.service;

import com.chat.dto.*;
import com.chat.entity.Contact;
import com.chat.entity.Conversation;
import com.chat.mapper.Mapper;
import com.chat.response.ResponseEntityDecorator;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import java.net.URI;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.core.Response;

@ApplicationScoped
public class ContactsService {

  private static final Mapper MAPPER = Mapper.INSTANCE;
  public static final String USER_ADDED_SUCCESSFULLY = "User added Successfully!";
  public static final String MESSAGE_WAS_SENT = "Message was sent!";
  // Assume that we get from an external Api
  public static final Double BITCOIN_PRICE = 37706.83;

  public Uni<Response> addContact(ContactRequestDTO contactRequestDTO) {
    Contact contact = MAPPER.mapToContact(contactRequestDTO);
    return contact
        .persist()
        .map(
            m ->
                responseBuilder("/add/")
                    .entity(getResponseEntityDecorator(USER_ADDED_SUCCESSFULLY))
                    .build());
  }

  public Multi<ContactResponseDTO> getAllContacts() {
    return Contact.<Contact>streamAll().map(contact -> MAPPER.mapToContactInfoDTO(contact));
  }

  public Uni<Response> sendMessage(MessageRequestDTO messageRequestDTO) {
    Conversation conversation = MAPPER.mapToConversation(messageRequestDTO);
    return Contact.findByEmail(messageRequestDTO.getTo())
        .onItem()
        .transform(contact -> addConversation(conversation, contact))
        .call(updatedContact -> updatedContact.update())
        .map(
            contact ->
                responseBuilder("/send/")
                    .entity(getResponseEntityDecorator(MESSAGE_WAS_SENT))
                    .build())
        .onFailure()
        .recoverWithNull();
  }

  public Uni<Response> sendMessageViaWebhook(WebhookRequestDTO webhookRequestDTO) {
    Conversation conversation = MAPPER.mapToConversation(webhookRequestDTO);
    return Contact.findByEmail(webhookRequestDTO.getTo())
        .onItem()
        .transform(contact -> addConversation(conversation, contact))
        .call(updatedContact -> updatedContact.update())
        .map(
            contact ->
                responseBuilder("/send/")
                    .entity(getResponseEntityDecorator(MESSAGE_WAS_SENT))
                    .build())
        .onFailure()
        .recoverWithNull();
  }

  public Uni<Response> listConversations(ConversationRequestDTO conversationRequestDTO) {
    return Contact.findByEmail(conversationRequestDTO.getEmail())
        .map(contact -> contact.getConversations())
        .map(
            conversations ->
                responseBuilder("/conversations/")
                    .entity(getResponseEntityDecorator(conversations.toString()))
                    .build())
        .onFailure()
        .recoverWithNull();
  }

  private Contact addConversation(Conversation conversation, Contact contact) {
    conversation.setContent(replacePlaceholders(contact, conversation.getContent()));
    contact.getConversations().add(conversation);
    return contact;
  }

  String replacePlaceholders(Contact contact, String content) {
    return content
        .replace("{{name}}", contact.getName())
        .replace("{{bitcoinPrice}}", String.valueOf(BITCOIN_PRICE));
  }

  private Response.ResponseBuilder responseBuilder(String url) {
    return Response.created(URI.create(url));
  }

  private ResponseEntityDecorator getResponseEntityDecorator(String message) {
    return ResponseEntityDecorator.builder().message(message).build();
  }
}
