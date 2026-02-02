/**
 * Converted from: src/Basket.API/Grpc/BasketService.cs
 * .NET Class: eShop.Basket.API.Grpc.BasketService
 *
 * REST API controller for basket operations.
 * This provides REST endpoints in addition to gRPC.
 */
package com.eshop.basket.api;

import com.eshop.basket.model.CustomerBasket;
import com.eshop.basket.repository.BasketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/basket")
@Tag(name = "Basket", description = "Shopping basket API")
public class BasketController {

    private final BasketRepository basketRepository;

    public BasketController(BasketRepository basketRepository) {
        this.basketRepository = basketRepository;
    }

    @GetMapping("/{buyerId}")
    @Operation(summary = "Get basket", description = "Get customer basket by buyer ID")
    public ResponseEntity<CustomerBasket> getBasket(@PathVariable String buyerId) {
        return basketRepository.getBasket(buyerId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(new CustomerBasket(buyerId)));
    }

    @PostMapping
    @Operation(summary = "Update basket", description = "Create or update customer basket")
    public ResponseEntity<CustomerBasket> updateBasket(@RequestBody CustomerBasket basket) {
        CustomerBasket updated = basketRepository.updateBasket(basket);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{buyerId}")
    @Operation(summary = "Delete basket", description = "Delete customer basket")
    public ResponseEntity<Void> deleteBasket(@PathVariable String buyerId) {
        basketRepository.deleteBasket(buyerId);
        return ResponseEntity.noContent().build();
    }
}
