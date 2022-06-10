package com.chat.entity;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity;
import io.smallrye.mutiny.Uni;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contact extends ReactivePanacheMongoEntity {
  private String name;
  private String email;
  private String event;
  private String address;
  private String phoneNumber;
  private List<Conversation> conversations = new ArrayList<>();

  public static Uni<Contact> findByEmail(String email) {
    return find("email", email).firstResult();
  }

  public static Uni<Contact> findByEvent(String event) {
    return find("event", event).firstResult();
  }
}
