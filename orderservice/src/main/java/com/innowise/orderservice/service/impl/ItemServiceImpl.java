package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.dto.item.CreateItemDto;
import com.innowise.orderservice.dto.item.ResponseItemDto;
import com.innowise.orderservice.dto.item.UpdateItemDto;
import com.innowise.orderservice.entity.Item;
import com.innowise.orderservice.exception.ItemNotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper;

    @Override
    public ResponseItemDto createItem(CreateItemDto createItemDto) {
        log.info("Request to create item");
        Item item = itemMapper.toEntity(createItemDto);

        ResponseItemDto result = itemMapper.toDto(itemRepository.save(item));
        log.info("Item created successfully with id: {}", result.getId());
        return result;
    }

    @Transactional
    @Override
    public ResponseItemDto updateItem(Long id, UpdateItemDto updateItemDto) {
        log.info("Request to update item with id: {}", id);
        Item item = itemRepository.findById(id).orElseThrow(() -> {
            log.error("Item with id {} not found for update", id);
            return new ItemNotFoundException();
        });

        item.setName(updateItemDto.getName());
        item.setPrice(updateItemDto.getPrice());

        ResponseItemDto result = itemMapper.toDto(itemRepository.save(item));
        log.info("Item with id {} updated successfully", id);
        return result;
    }

    @Override
    public ResponseItemDto findById(Long id) {
        log.info("Request to find item by id: {}", id);
        return itemMapper.toDto(itemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Item with id {} not found", id);
                    return new ItemNotFoundException();
                }));
    }

    @Override
    public Page<ResponseItemDto> findAll(Pageable pageable) {
        log.info("Request to find all items. Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());
        return itemRepository.findAll(PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize())).map(itemMapper::toDto);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        log.info("Request to delete item by id: {}", id);
        if (!itemRepository.existsById(id)) {
            log.error("Item with id {} not found for deletion", id);
            throw new ItemNotFoundException();
        }
        itemRepository.deleteById(id);
        log.info("Item with id {} deleted successfully", id);
    }

}
