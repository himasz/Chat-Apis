package com.chat.web;

import com.chat.dto.*;
import com.chat.service.ContactsService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/api/v1/contacts")
public class ContactsController {
  @Inject ContactsService service;

  @POST
  @Path("/add")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> add(@Valid ContactRequestDTO contactRequestDTO) {
    return service.addContact(contactRequestDTO);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Multi<ContactResponseDTO> getAllContacts() {
    return service.getAllContacts();
  }

  @POST
  @Path("/send")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> sendMessage(@Valid MessageRequestDTO messageRequestDTO) {
    return service.sendMessage(messageRequestDTO);
  }

  @POST
  @Path("/webhook")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> sendMessageViaWebhook(@Valid WebhookRequestDTO webhookRequestDTO) {
    return service.sendMessageViaWebhook(webhookRequestDTO);
  }

  @POST
  @Path("/conversations")
  @Produces(MediaType.APPLICATION_JSON)
  public Uni<Response> listConversations(@Valid ConversationRequestDTO conversationRequestDTO) {
    return service.listConversations(conversationRequestDTO);
  }
}
