package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ItemControllerTest {
    private MockMvc mockMvc;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

    private ObjectMapper objectMapper;
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private NewItemRequest newItemRequest;
    private ItemDto itemDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(itemController).build();
        objectMapper = new ObjectMapper();

        newItemRequest = new NewItemRequest("Drill", "Simple drill", true, null);
        itemDto = new ItemDto(1L, "Drill", "Simple drill", true);
    }

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        when(itemService.createItem(any(NewItemRequest.class), eq(1L)))
                .thenReturn(itemDto);

        mockMvc.perform(post("/items")
                        .header(USER_ID_HEADER, String.valueOf(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void updateItem_shouldReturnUpdatedItem() throws Exception {
        when(itemService.updateItem(eq(1L), any(UpdateItemRequest.class), eq(1L)))
                .thenReturn(itemDto);

        UpdateItemRequest updateItemRequest =
                new UpdateItemRequest("Updated drill", "Better drill", false);

        mockMvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateItemRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getItemById_shouldReturnItem() throws Exception {
        when(itemService.getItemById(1L, 1L))
                .thenReturn(new ItemWithCommentDto(
                        1L, "Drill", "Simple drill",
                        true, null, null, List.of()
                ));

        mockMvc.perform(get("/items/1")
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Drill"));
    }

    @Test
    void getPersonalItems_shouldReturnList() throws Exception {
        when(itemService.getPersonalItems(1L))
                .thenReturn(List.of(new ItemWithCommentDto(
                        1L, "Drill", "Simple drill",
                        true, null, null, List.of()
                )));

        mockMvc.perform(get("/items")
                        .header(USER_ID_HEADER, "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Drill"));
    }

    @Test
    void searchItems_shouldReturnFoundItems() throws Exception {
        when(itemService.searchItems("drill"))
                .thenReturn(List.of(itemDto));

        mockMvc.perform(get("/items/search")
                        .param("text", "drill"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    void addComment_shouldReturnCreatedComment() throws Exception {
        NewCommentRequest newCommentRequest = new NewCommentRequest("Good drill");
        CommentDto commentDto = new CommentDto(1L, "Good drill", "Alice", LocalDateTime.now());

        when(itemService.addComment(1L, 1L, newCommentRequest))
                .thenReturn(commentDto);

        mockMvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.text").value("Good drill"))
                .andExpect(jsonPath("$.authorName").value("Alice"));
    }
}
