package works.hop.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import static works.hop.grpc.AppServiceGrpc.newBlockingStub;
import static works.hop.grpc.AppServiceGrpc.newStub;

public class Client {

    public static void main(String[] args) throws Exception {
        blockingStub();
        //nonBlockingStub();
    }

    public static void blockingStub() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8080")
                .usePlaintext()
                .build();

        AppServiceGrpc.AppServiceBlockingStub blockingStub = newBlockingStub(channel);
        Acceptor.HelloRequest request = Acceptor.HelloRequest.newBuilder()
                        .setName("Ray")
                        .build();

        Acceptor.HelloResponse response = blockingStub.greeting(request);

        System.out.println(response);
        channel.shutdownNow();
    }

    public static void nonBlockingStub() {
        final ManagedChannel channel = ManagedChannelBuilder.forTarget("localhost:8080")
                .usePlaintext()
                .build();

        AppServiceGrpc.AppServiceStub newStub = newStub(channel);
        Acceptor.HelloRequest request = Acceptor.HelloRequest.newBuilder()
                        .setName("Joy")
                        .build();

        newStub.greetings(request, new StreamObserver<Acceptor.HelloResponse>() {
            public void onNext(Acceptor.HelloResponse response) {
                System.out.println(response);
            }

            public void onError(Throwable t) {
            }

            public void onCompleted() {
                // Typically you'll shutdown the channel somewhere else.
                // But for the purpose of the lab, we are only making a single
                // request. We'll shutdown as soon as this request is done.
                channel.shutdownNow();
            }
        });
    }
}
