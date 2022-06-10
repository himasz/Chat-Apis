package com.chat.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebhookResponseDTO {
  private Long time;
  private Double value;
  private String location;
}
