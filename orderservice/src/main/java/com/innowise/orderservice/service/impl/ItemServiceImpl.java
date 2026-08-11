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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;

    private final ItemMapper itemMapper;

    @Override
    public ResponseItemDto createItem(CreateItemDto createItemDto) {
        Item item = itemMapper.toEntity(createItemDto);

        item.setDeleted(false);

        return itemMapper.toDto(itemRepository.save(item));
    }

    @Transactional
    @Override
    public ResponseItemDto updateItem(Long id, UpdateItemDto updateItemDto) {
        Item item = itemRepository.findById(id).orElseThrow(ItemNotFoundException::new);

        item.setName(updateItemDto.getName());
        item.setPrice(updateItemDto.getPrice());

        return itemMapper.toDto(itemRepository.save(item));
    }

    @Override
    public ResponseItemDto findById(Long id) {
        return itemMapper.toDto(itemRepository.findById(id)
                .orElseThrow(ItemNotFoundException::new));
    }

    @Override
    public Page<ResponseItemDto> findAll(Pageable pageable) {
        return itemRepository.findAll(PageRequest.of(pageable.getPageNumber(),
                pageable.getPageSize())).map(itemMapper::toDto);
    }

    @Transactional
    @Override
    public void deleteById(Long id) {
        if (!itemRepository.existsById(id)) {
            throw new ItemNotFoundException();
        }
        itemRepository.deleteById(id);
    }

}
