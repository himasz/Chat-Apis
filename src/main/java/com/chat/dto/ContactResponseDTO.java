package com.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContactResponseDTO {
  private String name;
  private String email;
  private String address;
  private String phoneNumber;
}
