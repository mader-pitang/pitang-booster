package com.pitang.booster_c1m1.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PaginatedResponseDTO<T> {
  private List<T> content;
  private int page;
  private int size;
  private int totalPages;
  private long totalElements;

  public static <T> PaginatedResponseDTO<T> from(Page<T> page) {
    return new PaginatedResponseDTO<>(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalPages(),
        page.getTotalElements()
    );
  }
}
