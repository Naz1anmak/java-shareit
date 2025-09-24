package ru.practicum.shareit.item.dao;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class CommentRepositoryIntegrationTest {
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void findByItemId() {
        User owner = userRepository.save(new User("Owner", "owner@test.com"));
        User booker = userRepository.save(new User("Booker", "booker@test.com"));

        Item item = itemRepository.save(new Item("Drill", "Powerful drill", true, owner));

        Comment comment = new Comment();
        comment.setText("Great item!");
        comment.setAuthorName(booker.getName());
        comment.setCreated(LocalDateTime.now());
        comment.setItem(item);
        comment.setAuthor(booker);

        commentRepository.save(comment);

        entityManager.flush();
        entityManager.clear();

        List<Comment> comments = commentRepository.findByItemId(item.getId());

        assertThat(comments).hasSize(1);
        assertThat(comments.getFirst().getText()).isEqualTo("Great item!");
    }

    @Test
    void findByItemId_shouldReturnEmptyListWhenNoComments() {
        User owner = userRepository.save(new User("Owner", "owner@test.com"));
        Item item = itemRepository.save(new Item("Drill", "Powerful drill", true, owner));

        List<Comment> comments = commentRepository.findByItemId(item.getId());

        assertThat(comments).isEmpty();
    }

    @Test
    void findByItemId_shouldReturnMultipleComments() {
        User owner = userRepository.save(new User("Owner", "owner@test.com"));
        User booker1 = userRepository.save(new User("Booker1", "booker1@test.com"));
        User booker2 = userRepository.save(new User("Booker2", "booker2@test.com"));
        Item item = itemRepository.save(new Item("Drill", "Powerful drill", true, owner));

        Comment comment1 = new Comment();
        comment1.setText("Great item!");
        comment1.setAuthorName(booker1.getName());
        comment1.setCreated(LocalDateTime.now());
        comment1.setItem(item);
        comment1.setAuthor(booker1);

        Comment comment2 = new Comment();
        comment2.setText("Very useful");
        comment2.setAuthorName(booker2.getName());
        comment2.setCreated(LocalDateTime.now().minusDays(1));
        comment2.setItem(item);
        comment2.setAuthor(booker2);

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        entityManager.flush();
        entityManager.clear();

        List<Comment> comments = commentRepository.findByItemId(item.getId());

        assertThat(comments).hasSize(2);
        assertThat(comments).extracting(Comment::getText)
                .containsExactlyInAnyOrder("Great item!", "Very useful");
    }
}
