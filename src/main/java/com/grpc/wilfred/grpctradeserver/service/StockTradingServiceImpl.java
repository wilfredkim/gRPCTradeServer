package com.grpc.wilfred.grpctradeserver.service;

import com.grpc.wilfred.grpctradeserver.entity.Stock;
import com.grpc.wilfred.grpctradeserver.repository.StockRepository;
import com.wilfred.grpc.*;
import io.grpc.stub.StreamObserver;

import org.springframework.grpc.server.service.GrpcService;

import java.sql.Time;
import java.time.Instant;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@GrpcService
public class StockTradingServiceImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase {

    private final StockRepository stockRepository;

    public StockTradingServiceImpl(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Override
    public void getStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {

        //stockName -> DB -> map response -> return

        String stockSymbol = request.getStockSymbol();
        Stock stockEntity = stockRepository.findByStockSymbol(stockSymbol);

        StockResponse stockResponse = StockResponse.newBuilder()
                .setStockSymbol(stockEntity.getStockSymbol())
                .setPrice(stockEntity.getPrice())
                .setTimestamp(stockEntity.getLastUpdated().toString())
                .build();

        responseObserver.onNext(stockResponse);
        responseObserver.onCompleted();

    }

    @Override
    public void subscribeStockPrice(StockRequest request, StreamObserver<StockResponse> responseObserver) {
        String stockSymbol = request.getStockSymbol();
        try {
            for (int i = 0; i < 10; i++) {
                StockResponse stockResponse = StockResponse.newBuilder()
                        .setStockSymbol(stockSymbol)
                        .setPrice(new Random().nextDouble(100, 500))
                        .setTimestamp(Instant.now().toString())
                        .build();
                responseObserver.onNext(stockResponse);
                TimeUnit.SECONDS.sleep(1);
            }
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
            responseObserver.onError(e);
        }

    }

    @Override
    public StreamObserver<StockOrder> bulkStockOrder(StreamObserver<OrderSummary> responseObserver) {

        return new StreamObserver<>() {
            private int total_orders;
            private double total_amount;
            private double success_count;

            @Override
            public void onNext(StockOrder stockOrder) {
                total_orders++;
                total_amount += stockOrder.getPrice() * stockOrder.getQuantity();

                success_count++;
                System.out.println("Received order for " + stockOrder.getStockSymbol() +
                        " Quantity: " + stockOrder.getQuantity() +
                        " Price: " + stockOrder.getPrice());

            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error occurred: " + throwable.getMessage());

            }

            @Override
            public void onCompleted() {
                OrderSummary orderSummary = OrderSummary.newBuilder()
                        .setTotalOrders(total_orders)
                        .setTotalAmount(total_amount)
                        .setSuccessCount(success_count)
                        .build();
                responseObserver.onNext(orderSummary);
                responseObserver.onCompleted();

            }
        };
    }

    @Override
    public StreamObserver<StockOrder> liveStockTrading(StreamObserver<TradeStatus> responseObserver) {
        return new StreamObserver<>() {
            @Override
            public void onNext(StockOrder stockOrder) {
                System.out.println("Processing live order for " + stockOrder.getStockSymbol() +
                        " Quantity: " + stockOrder.getQuantity() +
                        " Price: " + stockOrder.getPrice());
                String status = "EXECUTED";
                String message = "Order executed successfully";
                if (stockOrder.getQuantity() < 0) {
                    status = "FAILED";
                    message = "Invalid quantity";
                }

                TradeStatus tradeStatus = TradeStatus.newBuilder()
                        .setOrderId(stockOrder.getOrderId())
                        .setStatus(status)
                        .setMessage(message)
                        .setTimestamp(Instant.now().toString())
                        .build();
                responseObserver.onNext(tradeStatus);
            }

            @Override
            public void onError(Throwable throwable) {
                System.out.println("Error occurred during live trading: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };


    }
}
