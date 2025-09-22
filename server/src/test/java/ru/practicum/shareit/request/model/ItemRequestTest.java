package ru.practicum.shareit.request.model;

import org.junit.jupiter.api.Test;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ItemRequestTest {

    @Test
    void testNoArgsConstructor() {
        ItemRequest request = new ItemRequest();
        assertNotNull(request);
    }

    @Test
    void testAllArgsConstructor() {
        User requester = new User("John", "john@test.com");
        ItemRequest request = new ItemRequest("Need a drill", requester);

        assertEquals("Need a drill", request.getDescription());
        assertEquals(requester, request.getRequester());
    }

    @Test
    void testSettersAndGetters() {
        ItemRequest request = new ItemRequest();

        request.setId(1L);
        assertEquals(1L, request.getId());

        request.setDescription("Need a hammer");
        assertEquals("Need a hammer", request.getDescription());

        User requester = new User("John", "john@test.com");
        request.setRequester(requester);
        assertEquals(requester, request.getRequester());

        LocalDateTime created = LocalDateTime.now();
        request.setCreated(created);
        assertEquals(created, request.getCreated());
    }

    @Test
    void testEqualsAndHashCode() {
        ItemRequest request1 = new ItemRequest();
        request1.setId(1L);

        ItemRequest request2 = new ItemRequest();
        request2.setId(1L);

        ItemRequest request3 = new ItemRequest();
        request3.setId(2L);

        assertEquals(request1, request2);
        assertEquals(request1.hashCode(), request2.hashCode());
        assertNotEquals(request1, request3);
        assertNotEquals(null, request1);
    }

    @Test
    void testToString() {
        ItemRequest request = new ItemRequest();
        request.setId(1L);
        request.setDescription("Need tool");

        String toString = request.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("ItemRequest"));
        assertTrue(toString.contains("description=Need tool"));
        assertFalse(toString.contains("requester="));
    }
}
