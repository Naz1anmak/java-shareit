package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    @Query("SELECT i FROM Item i " +
            "WHERE (LOWER(i.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :text, '%'))) " +
            "AND i.available = true")
    List<Item> searchByNameOrDescriptionAndAvailableIsTrue(String text);

    @Query("select i from Item i " +
            "left join fetch i.comments " +
            "where i.id = :itemId")
    Optional<Item> findByIdWithComments(@Param("itemId") Long itemId);

    @Query("select distinct i from Item i " +
            "left join fetch i.comments c " +
            "where i.owner.id = :ownerId")
    List<Item> findByOwnerIdWithComments(@Param("ownerId") Long ownerId);

    List<Item> findByRequestIdIn(List<Long> requestIds);
}
