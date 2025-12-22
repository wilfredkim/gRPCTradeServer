package com.grpc.wilfred.grpctradeserver.service;
import com.grpc.wilfred.grpctradeserver.entity.Stock;
import com.grpc.wilfred.grpctradeserver.repository.StockRepository;
import com.wilfred.grpc.StockRequest;
import com.wilfred.grpc.StockResponse;
import com.wilfred.grpc.StockTradingServiceGrpc;
import io.grpc.stub.StreamObserver;

import org.springframework.grpc.server.service.GrpcService;

@GrpcService
public class StockTradingServiceImpl extends StockTradingServiceGrpc.StockTradingServiceImplBase{

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
}
