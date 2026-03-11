/**
 * Converted from: src/Basket.API/Grpc/BasketService.cs
 * .NET Class: eShop.Basket.API.Grpc.BasketService
 *
 * gRPC service implementation for basket operations.
 * Buyer identity is extracted from the JWT "sub" claim in gRPC metadata,
 * matching .NET's ServerCallContext.GetUserIdentity() pattern.
 */
package com.eshop.basket.grpc;

import com.eshop.basket.model.CustomerBasket;
import com.eshop.basket.repository.BasketRepository;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.stream.Collectors;

@GrpcService
public class BasketGrpcService extends BasketGrpc.BasketImplBase {

    private static final Logger log = LoggerFactory.getLogger(BasketGrpcService.class);
    private static final Metadata.Key<String> AUTHORIZATION_KEY =
            Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER);

    private final BasketRepository basketRepository;
    private final JwtDecoder jwtDecoder;

    public BasketGrpcService(BasketRepository basketRepository, JwtDecoder jwtDecoder) {
        this.basketRepository = basketRepository;
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public void getBasket(GetBasketRequest request, StreamObserver<CustomerBasketResponse> responseObserver) {
        String userId = getUserIdentity();
        if (userId == null || userId.isEmpty()) {
            responseObserver.onNext(CustomerBasketResponse.getDefaultInstance());
            responseObserver.onCompleted();
            return;
        }

        log.debug("GetBasket called for buyer: {}", userId);

        CustomerBasket basket = basketRepository.getBasket(userId)
                .orElse(new CustomerBasket(userId));

        responseObserver.onNext(toGrpcResponse(basket));
        responseObserver.onCompleted();
    }

    @Override
    public void updateBasket(UpdateBasketRequest request, StreamObserver<CustomerBasketResponse> responseObserver) {
        String userId = getUserIdentity();
        if (userId == null || userId.isEmpty()) {
            responseObserver.onError(Status.UNAUTHENTICATED
                    .withDescription("The caller is not authenticated.")
                    .asRuntimeException());
            return;
        }

        log.debug("UpdateBasket called for buyer: {}", userId);

        CustomerBasket basket = new CustomerBasket(userId);
        basket.setItems(request.getItemsList().stream()
                .map(this::toModelItem)
                .collect(Collectors.toList()));

        CustomerBasket updated = basketRepository.updateBasket(basket);
        responseObserver.onNext(toGrpcResponse(updated));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteBasket(DeleteBasketRequest request, StreamObserver<DeleteBasketResponse> responseObserver) {
        String userId = getUserIdentity();
        if (userId == null || userId.isEmpty()) {
            responseObserver.onError(Status.UNAUTHENTICATED
                    .withDescription("The caller is not authenticated.")
                    .asRuntimeException());
            return;
        }

        log.debug("DeleteBasket called for buyer: {}", userId);
        basketRepository.deleteBasket(userId);

        responseObserver.onNext(DeleteBasketResponse.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private String getUserIdentity() {
        try {
            Metadata metadata = GrpcMetadataContext.getMetadata();
            if (metadata == null) return null;
            String authHeader = metadata.get(AUTHORIZATION_KEY);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
            String token = authHeader.substring(7);
            Jwt jwt = jwtDecoder.decode(token);
            return jwt.getSubject();
        } catch (Exception e) {
            log.warn("Failed to extract user identity from gRPC metadata: {}", e.getMessage());
            return null;
        }
    }

    private CustomerBasketResponse toGrpcResponse(CustomerBasket basket) {
        CustomerBasketResponse.Builder builder = CustomerBasketResponse.newBuilder();
        for (com.eshop.basket.model.BasketItem item : basket.getItems()) {
            builder.addItems(BasketItem.newBuilder()
                    .setProductId(item.getProductId().intValue())
                    .setQuantity(item.getQuantity())
                    .build());
        }
        return builder.build();
    }

    private com.eshop.basket.model.BasketItem toModelItem(BasketItem grpcItem) {
        com.eshop.basket.model.BasketItem item = new com.eshop.basket.model.BasketItem();
        item.setProductId((long) grpcItem.getProductId());
        item.setQuantity(grpcItem.getQuantity());
        return item;
    }
}
