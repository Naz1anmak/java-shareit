package ru.practicum.shareit.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.shareit.request.model.ItemRequest;

import java.util.List;

public interface ItemRequestRepository extends
        JpaRepository<ItemRequest, Long>,
        QuerydslPredicateExecutor<ItemRequest> {

    boolean existsByDescription(String description);

    List<ItemRequest> findAllByOrderByCreatedDesc();
}
