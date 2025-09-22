package ru.practicum.shareit.item.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    @Test
    void testNoArgsConstructor() {
        Comment comment = new Comment();
        assertNotNull(comment);
    }

    @Test
    void testSettersAndGetters() {
        Comment comment = new Comment();

        comment.setId(1L);
        assertEquals(1L, comment.getId());

        comment.setText("Great item!");
        assertEquals("Great item!", comment.getText());

        comment.setAuthorName("John");
        assertEquals("John", comment.getAuthorName());

        LocalDateTime created = LocalDateTime.now();
        comment.setCreated(created);
        assertEquals(created, comment.getCreated());

        User author = new User("John", "john@test.com");
        comment.setAuthor(author);
        assertEquals(author, comment.getAuthor());

        Item item = new Item("Drill", "Desc", true, author);
        comment.setItem(item);
        assertEquals(item, comment.getItem());
    }

    @Test
    void testEqualsAndHashCode() {
        Comment comment1 = new Comment();
        comment1.setId(1L);

        Comment comment2 = new Comment();
        comment2.setId(1L);

        Comment comment3 = new Comment();
        comment3.setId(2L);

        assertEquals(comment1, comment2);
        assertEquals(comment1.hashCode(), comment2.hashCode());
        assertNotEquals(comment1, comment3);
        assertNotEquals(null, comment1);
    }

    @Test
    void testToString() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setText("Test comment");
        comment.setAuthorName("John");

        String toString = comment.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("Comment"));
        assertTrue(toString.contains("text=Test comment"));
        assertTrue(toString.contains("authorName=John"));
        assertFalse(toString.contains("item="));
        assertFalse(toString.contains("author="));
    }
}