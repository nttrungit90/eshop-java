package com.eshop.basket.grpc;

import io.grpc.Metadata;

/**
 * Thread-local holder for gRPC metadata (headers).
 * Used to pass authorization headers from the interceptor to service methods.
 */
public class GrpcMetadataContext {

    private static final ThreadLocal<Metadata> METADATA = new ThreadLocal<>();

    public static void setMetadata(Metadata metadata) {
        METADATA.set(metadata);
    }

    public static Metadata getMetadata() {
        return METADATA.get();
    }

    public static void clear() {
        METADATA.remove();
    }
}
