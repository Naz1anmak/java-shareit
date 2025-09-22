package ru.practicum.shareit.item.dao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dao.ItemRequestRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItemRepositoryIntegrationTest {
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private EntityManager entityManager;

    private User owner;
    private Item item1;
    private Item item2;
    private Item item3;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        itemRepository.deleteAll();
        userRepository.deleteAll();
        itemRequestRepository.deleteAll();

        owner = userRepository.save(new User("Owner", "owner@test.com"));

        item1 = itemRepository.save(new Item("Drill", "Powerful drill", true, owner));
        item2 = itemRepository.save(new Item("Hammer", "Old hammer", false, owner));
        item3 = itemRepository.save(new Item("Saw", "Hand saw for wood", true, owner));

        Comment comment = new Comment();
        comment.setText("Good tool");
        comment.setAuthorName(owner.getName());
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item1);
        comment.setAuthor(owner);
        commentRepository.save(comment);

        entityManager.flush();
        entityManager.refresh(item1);
    }

    @Test
    void searchByNameOrDescriptionAndAvailableIsTrue_shouldReturnOnlyAvailableItems() {
        List<Item> result = itemRepository.searchByNameOrDescriptionAndAvailableIsTrue("drill");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Drill");

        List<Item> result2 = itemRepository.searchByNameOrDescriptionAndAvailableIsTrue("saw");
        assertThat(result2).hasSize(1);
        assertThat(result2.getFirst().getName()).isEqualTo("Saw");

        List<Item> result3 = itemRepository.searchByNameOrDescriptionAndAvailableIsTrue("hammer");
        assertThat(result3).isEmpty();
    }

    @Test
    void findByIdWithComments_shouldFetchComments() {
        Optional<Item> resultOpt = itemRepository.findByIdWithComments(item1.getId());

        assertThat(resultOpt).isPresent();
        Item result = resultOpt.get();

        assertThat(result.getComments()).hasSize(1);
        assertThat(result.getComments().getFirst().getText()).isEqualTo("Good tool");
    }

    @Test
    void findByOwnerIdWithComments_shouldReturnItemsWithComments() {
        List<Item> result = itemRepository.findByOwnerIdWithComments(owner.getId());

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Item::getId).contains(item1.getId(), item2.getId(), item3.getId());

        Item fetchedItem1 = result.stream()
                .filter(i -> i.getId().equals(item1.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(fetchedItem1.getComments()).hasSize(1);
        assertThat(fetchedItem1.getComments().getFirst().getText()).isEqualTo("Good tool");
    }

    @Test
    void findByRequestIdIn_shouldReturnItemsByRequestIds() {
        ItemRequest req1 = itemRequestRepository.save(new ItemRequest("Need a drill", owner));
        ItemRequest req2 = itemRequestRepository.save(new ItemRequest("Need a hammer", owner));

        item1.setRequest(req1);
        item2.setRequest(req2);
        itemRepository.saveAll(List.of(item1, item2));

        List<Item> result = itemRepository.findByRequestIdIn(List.of(req1.getId(), req2.getId()));

        assertThat(result).hasSize(2);
        assertThat(result).extracting(i -> i.getRequest().getId())
                .containsExactlyInAnyOrder(req1.getId(), req2.getId());
    }
}
