/**
 * Converted from: src/Basket.API/Grpc/BasketService.cs
 * .NET Class: eShop.Basket.API.Grpc.BasketService
 *
 * gRPC service implementation for basket operations.
 */
package com.eshop.basket.grpc;

import com.eshop.basket.model.CustomerBasket;
import com.eshop.basket.repository.BasketRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@GrpcService
public class BasketGrpcService extends BasketGrpc.BasketImplBase {

    private static final Logger log = LoggerFactory.getLogger(BasketGrpcService.class);

    private final BasketRepository basketRepository;

    public BasketGrpcService(BasketRepository basketRepository) {
        this.basketRepository = basketRepository;
    }

    @Override
    public void getBasket(GetBasketRequest request, StreamObserver<CustomerBasketResponse> responseObserver) {
        log.info("gRPC GetBasket called for buyer: {}", request.getBuyerId());

        CustomerBasket basket = basketRepository.getBasket(request.getBuyerId())
                .orElse(new CustomerBasket(request.getBuyerId()));

        CustomerBasketResponse response = toGrpcResponse(basket);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void updateBasket(UpdateBasketRequest request, StreamObserver<CustomerBasketResponse> responseObserver) {
        log.info("gRPC UpdateBasket called for buyer: {}", request.getBuyerId());

        CustomerBasket basket = toModel(request);
        CustomerBasket updated = basketRepository.updateBasket(basket);

        CustomerBasketResponse response = toGrpcResponse(updated);
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBasket(DeleteBasketRequest request, StreamObserver<DeleteBasketResponse> responseObserver) {
        log.info("gRPC DeleteBasket called for buyer: {}", request.getBuyerId());

        boolean deleted = basketRepository.deleteBasket(request.getBuyerId());

        DeleteBasketResponse response = DeleteBasketResponse.newBuilder()
                .setSuccess(deleted)
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private CustomerBasketResponse toGrpcResponse(CustomerBasket basket) {
        return CustomerBasketResponse.newBuilder()
                .setBuyerId(basket.getBuyerId())
                .addAllItems(basket.getItems().stream()
                        .map(this::toGrpcItem)
                        .collect(Collectors.toList()))
                .build();
    }

    private BasketItem toGrpcItem(com.eshop.basket.model.BasketItem item) {
        BasketItem.Builder builder = BasketItem.newBuilder()
                .setProductId(item.getProductId())
                .setProductName(item.getProductName() != null ? item.getProductName() : "")
                .setQuantity(item.getQuantity())
                .setPictureUrl(item.getPictureUrl() != null ? item.getPictureUrl() : "");

        if (item.getId() != null) {
            builder.setId(item.getId());
        }
        if (item.getUnitPrice() != null) {
            builder.setUnitPrice(item.getUnitPrice().doubleValue());
        }
        if (item.getOldUnitPrice() != null) {
            builder.setOldUnitPrice(item.getOldUnitPrice().doubleValue());
        }

        return builder.build();
    }

    private CustomerBasket toModel(UpdateBasketRequest request) {
        CustomerBasket basket = new CustomerBasket(request.getBuyerId());
        basket.setItems(request.getItemsList().stream()
                .map(this::toModelItem)
                .collect(Collectors.toList()));
        return basket;
    }

    private com.eshop.basket.model.BasketItem toModelItem(BasketItem grpcItem) {
        com.eshop.basket.model.BasketItem item = new com.eshop.basket.model.BasketItem();
        item.setId(grpcItem.getId());
        item.setProductId(grpcItem.getProductId());
        item.setProductName(grpcItem.getProductName());
        item.setUnitPrice(BigDecimal.valueOf(grpcItem.getUnitPrice()));
        item.setOldUnitPrice(BigDecimal.valueOf(grpcItem.getOldUnitPrice()));
        item.setQuantity(grpcItem.getQuantity());
        item.setPictureUrl(grpcItem.getPictureUrl());
        return item;
    }
}
