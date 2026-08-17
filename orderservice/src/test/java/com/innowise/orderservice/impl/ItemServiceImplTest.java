package com.innowise.orderservice.impl;

import com.innowise.orderservice.dto.item.ResponseItemDto;
import com.innowise.orderservice.dto.item.CreateItemDto;
import com.innowise.orderservice.dto.item.UpdateItemDto;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ItemServiceImplTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    private Item item;

    private Item updatedItem;

    private CreateItemDto createDto;

    private ResponseItemDto itemDto;

    private UpdateItemDto updateItemDto;

    @BeforeEach
    void setUp() {
        item = new Item();
        item.setId(1L);
        item.setName("Book");
        item.setPrice(BigDecimal.valueOf(10));

        createDto = new CreateItemDto();
        createDto.setName("Book");
        createDto.setPrice(BigDecimal.valueOf(10));

        itemDto = new ResponseItemDto();
        itemDto.setId(item.getId());
        itemDto.setName(item.getName());
        itemDto.setPrice(item.getPrice());

        updateItemDto = new UpdateItemDto();
        updateItemDto.setName("Book1");
        updateItemDto.setPrice(BigDecimal.valueOf(9.99));

        updatedItem = new Item();
        updatedItem.setId(item.getId());
        updatedItem.setName(updateItemDto.getName());
        updatedItem.setPrice(updateItemDto.getPrice());
    }

    @Test
    void createItem_ShouldReturnItemDto_WhenSuccessful() {
        when(itemMapper.toEntity(createDto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(item);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        ResponseItemDto result = itemService.createItem(createDto);

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());

        verify(itemMapper).toEntity(createDto);
        verify(itemRepository).save(item);
        verify(itemMapper).toDto(item);
    }

    @Test
    void updateItem_ShouldReturnItemDto_WhenSuccessful() {
        itemDto.setName(updateItemDto.getName());
        itemDto.setPrice(updateItemDto.getPrice());

        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);
        when(itemMapper.toDto(updatedItem)).thenReturn(itemDto);

        ResponseItemDto result = itemService.updateItem(item.getId(), updateItemDto);

        assertNotNull(result);
        assertEquals(updateItemDto.getName(), result.getName());

        verify(itemRepository).findById(item.getId());
        verify(itemRepository).save(any(Item.class));
        verify(itemMapper).toDto(updatedItem);
    }

    @Test
    void updateItem_ShouldThrowException_WhenItemNotFound() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.updateItem(99L, updateItemDto));

        verify(itemRepository).findById(99L);
    }

    @Test
    void findById_ShouldReturnItemDto_WhenSuccessful() {
        when(itemRepository.findById(item.getId())).thenReturn(Optional.of(item));
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        ResponseItemDto result = itemService.findById(item.getId());

        assertNotNull(result);
        assertEquals(item.getName(), result.getName());

        verify(itemRepository).findById(item.getId());
        verify(itemMapper).toDto(item);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        item.setId(99L);
        when(itemRepository.findById(item.getId())).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class, () -> itemService.findById(item.getId()));

        verify(itemRepository).findById(item.getId());
    }

    @Test
    void findAll_ShouldReturnPageOfItemDto() {
        int page = 0;
        int size = 10;

        Page<Item> itemPage = new PageImpl<>(List.of(item),
                PageRequest.of(page, size), 1);

        when(itemRepository.findAll(any(PageRequest.class))).thenReturn(itemPage);
        when(itemMapper.toDto(item)).thenReturn(itemDto);

        Page<ResponseItemDto> result = itemService.findAll(PageRequest.of(page, size));

        assertNotNull(result);
        assertEquals(item.getName(), result.getContent().getFirst().getName());
        assertEquals(1, result.getTotalElements());

        verify(itemRepository).findAll(any(PageRequest.class));
        verify(itemMapper).toDto(item);
    }

    @Test
    void deleteById_ShouldReturnNothing_WhenSuccessful() {
        when(itemRepository.existsById(item.getId())).thenReturn(true);

        itemService.deleteById(item.getId());

        verify(itemRepository).existsById(item.getId());
        verify(itemRepository).deleteById(item.getId());
    }

    @Test
    void deleteById_ShouldThrowException_WhenNotFound() {
        item.setId(99L);
        when(itemRepository.existsById(item.getId())).thenReturn(false);

        assertThrows(ItemNotFoundException.class,
                () -> itemService.deleteById(item.getId()));

        verify(itemRepository).existsById(item.getId());
        verify(itemRepository, never()).deleteById(anyLong());
    }
}
