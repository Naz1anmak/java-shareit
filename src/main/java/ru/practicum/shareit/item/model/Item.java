package ru.practicum.shareit.item.model;

import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Item {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long ownerId;

    private final Map<List<String>, Boolean> reviews = new HashMap<>();
}
