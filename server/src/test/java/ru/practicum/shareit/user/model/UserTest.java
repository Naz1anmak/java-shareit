package ru.practicum.shareit.user.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testNoArgsConstructor() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    void testAllArgsConstructor() {
        User user = new User("John Doe", "john@test.com");

        assertEquals("John Doe", user.getName());
        assertEquals("john@test.com", user.getEmail());
    }

    @Test
    void testSettersAndGetters() {
        User user = new User();

        user.setId(1L);
        assertEquals(1L, user.getId());

        user.setName("Jane Doe");
        assertEquals("Jane Doe", user.getName());

        user.setEmail("jane@test.com");
        assertEquals("jane@test.com", user.getEmail());
    }

    @Test
    void testEqualsAndHashCode() {
        User user1 = new User();
        user1.setId(1L);

        User user2 = new User();
        user2.setId(1L);

        User user3 = new User();
        user3.setId(2L);

        assertEquals(user1, user2);
        assertEquals(user1.hashCode(), user2.hashCode());
        assertNotEquals(user1, user3);
        assertNotEquals(null, user1);
    }

    @Test
    void testToString() {
        User user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@test.com");

        String toString = user.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("User"));
        assertTrue(toString.contains("name=John"));
        assertTrue(toString.contains("email=john@test.com"));
    }
}
