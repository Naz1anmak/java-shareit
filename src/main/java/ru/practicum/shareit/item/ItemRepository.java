package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();

    public Item create(Item item) {
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(long itemId) {
        return Optional.ofNullable(items.get(itemId));
    }

    public List<Item> getItems() {
        return new ArrayList<>(items.values());
    }

    public List<Item> getItems(long ownerId) {
        List<Item> personalItems = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwnerId() == ownerId) {
                personalItems.add(item);
            }
        }
        return personalItems;
    }

    public Item update(Item item) {
        items.put(item.getId(), item);
        return item;
    }
}
