package com.atlas.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Event published when inventory is updated (reserved, released, or adjusted).
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class InventoryUpdatedEvent extends BaseEvent {

    private UUID productId;
    private String productSku;
    private Integer previousQuantity;
    private Integer newQuantity;
    private Integer quantityChanged;
    private InventoryAction action;
    private UUID orderId;

    public enum InventoryAction {
        RESERVED,
        RELEASED,
        ADJUSTED,
        SOLD,
        RESTOCKED
    }

    public static InventoryUpdatedEvent reserved(UUID productId, String sku, int previousQty,
            int newQty, UUID orderId) {
        InventoryUpdatedEvent event = InventoryUpdatedEvent.builder()
                .productId(productId)
                .productSku(sku)
                .previousQuantity(previousQty)
                .newQuantity(newQty)
                .quantityChanged(previousQty - newQty)
                .action(InventoryAction.RESERVED)
                .orderId(orderId)
                .build();
        event.initializeEvent("INVENTORY_RESERVED", productId, "product-service");
        return event;
    }

    public static InventoryUpdatedEvent released(UUID productId, String sku, int previousQty,
            int newQty, UUID orderId) {
        InventoryUpdatedEvent event = InventoryUpdatedEvent.builder()
                .productId(productId)
                .productSku(sku)
                .previousQuantity(previousQty)
                .newQuantity(newQty)
                .quantityChanged(newQty - previousQty)
                .action(InventoryAction.RELEASED)
                .orderId(orderId)
                .build();
        event.initializeEvent("INVENTORY_RELEASED", productId, "product-service");
        return event;
    }
}
