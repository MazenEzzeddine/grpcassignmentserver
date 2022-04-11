import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {



    private final int port;
    private final Server server;

    private Main(int port) throws IOException {
        this(ServerBuilder.forPort(port), port);
    }

    private Main(ServerBuilder<?> serverBuilder, int port) {
        this.port = port;
        this.server = serverBuilder.addService(new AssignmentService()).build();
    }

    public void start() throws IOException {


        System.out.println("Server Started");
        server.start();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may has been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                Main.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        Main server = new Main(5002);
        server.start();
        server.blockUntilShutdown();


    }


    public static class AssignmentService extends AssignmentServiceGrpc.AssignmentServiceImplBase {
        @Override
        public void getAssignment(AssignmentRequest request, StreamObserver<AssignmentResponse> responseObserver) {
            System.out.println(request.getRequest());


            List<Partition> partitions = new ArrayList<>();
            Partition p1 = Partition.newBuilder().setArrivalRate(10).setId(1).setLag(500).build();

            Partition p2 = Partition.newBuilder().setArrivalRate(5.0).setId(2).setLag(250).build();

            Partition p3 = Partition.newBuilder().setArrivalRate(6.2).setId(3).setLag(490).build();

            partitions.add(p1);
            partitions.add(p2);
            partitions.add(p3);


            //Consumer c1= Consumer.newBuilder().setId(1).

            Consumer c1= Consumer.newBuilder().setId(1).addAssignedPartitions( p1).addAssignedPartitions(p2).build();

            Consumer c2= Consumer.newBuilder().setId(2).addAssignedPartitions( p3).build();

            responseObserver.onNext(AssignmentResponse.newBuilder().addConsumers(c1).addConsumers(c2).build());
            responseObserver.onCompleted();


            System.out.println("Sent Assignment to client");


        }



    }
}