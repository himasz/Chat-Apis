package com.chat.mapper;

import com.chat.dto.ContactRequestDTO;
import com.chat.dto.ContactResponseDTO;
import com.chat.dto.MessageRequestDTO;
import com.chat.dto.WebhookRequestDTO;
import com.chat.entity.Contact;
import com.chat.entity.Conversation;
import java.time.LocalDateTime;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@org.mapstruct.Mapper(componentModel = "cdi")
public interface Mapper {

  Mapper INSTANCE = Mappers.getMapper(Mapper.class);

  Contact mapToContact(ContactRequestDTO contactRequestDTO);

  ContactResponseDTO mapToContactInfoDTO(Contact contact);

  Conversation mapToConversation(MessageRequestDTO messageRequestDTO);

  @Mapping(target = "from", source = "webhookRequestDTO.event")
  @Mapping(target = "content", source = "webhookRequestDTO.data")
  @Mapping(target = "date", source = "webhookRequestDTO.publishedAt", qualifiedByName = "mapDate")
  Conversation mapToConversation(WebhookRequestDTO webhookRequestDTO);

  @Named("mapDate")
  default LocalDateTime mapDate(String publishedAt) {
    return LocalDateTime.parse(publishedAt);
  }
}
