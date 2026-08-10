package com.innowise.orderservice.dto.orderitem;

import com.innowise.orderservice.dto.item.ResponseItemDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseOrderItemDto {
    
    private ResponseItemDto itemDto;
    
    private Long quantity;

}
