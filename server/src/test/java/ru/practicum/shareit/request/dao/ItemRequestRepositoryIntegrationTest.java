package ru.practicum.shareit.request.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.model.User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class ItemRequestRepositoryIntegrationTest {
    @Autowired
    private ItemRequestRepository itemRequestRepository;
    @Autowired
    private UserRepository userRepository;

    private User requester;
    private ItemRequest request1;
    private ItemRequest request2;

    @BeforeEach
    void setUp() {
        itemRequestRepository.deleteAll();
        userRepository.deleteAll();

        requester = userRepository.save(new User("Requester", "requester@test.com"));

        request1 = itemRequestRepository.save(new ItemRequest("Need a drill", requester));
        request2 = itemRequestRepository.save(new ItemRequest("Need a hammer", requester));
    }

    @Test
    void existsByDescription_shouldReturnTrueIfExists() {
        boolean exists = itemRequestRepository.existsByDescription("Need a drill");
        assertThat(exists).isTrue();

        boolean notExists = itemRequestRepository.existsByDescription("Need a saw");
        assertThat(notExists).isFalse();
    }

    @Test
    void findAllByOrderByCreatedDesc_shouldReturnRequestsInDescOrder() {
        List<ItemRequest> requests = itemRequestRepository.findAllByOrderByCreatedDesc();

        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getId()).isEqualTo(request2.getId());
        assertThat(requests.get(1).getId()).isEqualTo(request1.getId());
    }
}