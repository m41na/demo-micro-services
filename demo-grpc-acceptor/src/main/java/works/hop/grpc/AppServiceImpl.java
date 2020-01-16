package works.hop.grpc;

import io.grpc.stub.StreamObserver;

public class AppServiceImpl extends AppServiceGrpc.AppServiceImplBase {

    @Override
    public void greeting(Acceptor.HelloRequest request,
                         StreamObserver<Acceptor.HelloResponse> responseObserver) {
        // HelloRequest has toString auto-generated.
        System.out.println(request);

        // You must use a builder to construct a new Protobuffer object
        Acceptor.HelloResponse response = Acceptor.HelloResponse.newBuilder()
                .setGreeting("Hello there, " + request.getName())
                .build();

        // Use responseObserver to send a single response back
        responseObserver.onNext(response);

        // When you are done, you must call onCompleted.
        responseObserver.onCompleted();
    }

    @Override
    public void greetings(Acceptor.HelloRequest request,
                          StreamObserver<Acceptor.HelloResponse> responseObserver) {

        // You must use a builder to construct a new Protobuffer object
        Acceptor.HelloResponse response = Acceptor.HelloResponse.newBuilder()
                .setGreeting("Hello there again, " + request.getName())
                .build();

        // Feel free to construct different responses if you'd like.
        responseObserver.onNext(response);
        responseObserver.onNext(response);
        responseObserver.onNext(response);

        // When you are done, you must call onCompleted.
        responseObserver.onCompleted();
    }
}
