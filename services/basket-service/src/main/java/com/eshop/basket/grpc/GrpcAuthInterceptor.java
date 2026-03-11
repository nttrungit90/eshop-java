package com.eshop.basket.grpc;

import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

/**
 * gRPC server interceptor that captures request metadata (headers)
 * and stores them in GrpcMetadataContext for use by service methods.
 * This enables extracting JWT tokens from the "authorization" header.
 */
@GrpcGlobalServerInterceptor
public class GrpcAuthInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {
        GrpcMetadataContext.setMetadata(headers);
        try {
            return next.startCall(call, headers);
        } finally {
            // Note: don't clear here — the listener callbacks run asynchronously
        }
    }
}
