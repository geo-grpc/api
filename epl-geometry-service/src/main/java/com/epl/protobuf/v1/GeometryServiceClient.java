/*
Copyright 2017 Echo Park Labs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

For additional information, contact:

email: info@echoparklabs.io
*/

package com.epl.protobuf.v1;

import com.epl.protobuf.v1.GeometryServiceGrpc.GeometryServiceBlockingStub;
import com.epl.protobuf.v1.GeometryServiceGrpc.GeometryServiceStub;
import com.esri.core.geometry.*;
import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * Sample client code that makes gRPC calls to the server.
 */
public class GeometryServiceClient {
    private static final Logger logger = Logger.getLogger(GeometryServiceClient.class.getName());


    private final ManagedChannel channel;
    private final GeometryServiceBlockingStub blockingStub;
    private final GeometryServiceStub asyncStub;


    private Random random = new Random();
    private TestHelper testHelper;


    /**
     * Construct client for accessing GeometryService server at {@code host:port}.
     */
    public GeometryServiceClient(String host, int port) {
        this(ManagedChannelBuilder
                .forAddress(host, port).usePlaintext());
    }

    public GeometryServiceClient(String serviceTarget) {
        this(ManagedChannelBuilder
                .forTarget(serviceTarget)
                .nameResolverFactory(new KubernetesNameResolverProvider())
                .executor(Executors.newFixedThreadPool(4))
                .usePlaintext());
    }

    /**
     * Construct client for accessing GeometryService server using the existing channel.
     */
    public GeometryServiceClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = GeometryServiceGrpc.newBlockingStub(channel);
        asyncStub = GeometryServiceGrpc.newStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void testWRSShapefile(String pathFile) throws IOException, InterruptedException {
        File inFile = new File(pathFile);
        ProjectionData operatorSpatialReference = ProjectionData.newBuilder().setEpsg(54016).build();
        ProjectionData inputSpatialReference = ProjectionData.newBuilder().setEpsg(4326).build();
        ProjectionData outputSpatialReference = inputSpatialReference;

        GeometryData.Builder geometryBuilder = GeometryData.newBuilder()
                .setEsriShape(ByteString.copyFromUtf8(""))
                .setGeometryId(0)
                .setProj(inputSpatialReference);

        GeometryRequest.Builder operatorRequestBuilder = GeometryRequest.newBuilder()
                .setOperator(OperatorType.BUFFER)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(45))
                .getLeftGeometryRequestBuilder()
                .setResultEncoding(Encoding.GEOJSON)
                .setOperationProj(operatorSpatialReference)
                .setResultProj(outputSpatialReference);
//        GeometryRequest.Builder operatorRequestBuilder = GeometryRequest.newBuilder()
//                .setOperatorType(ServiceOperatorType.BUFFER)
//                .addBufferDistances(2.5)
//                .setMaxVerticesInFullCircle(66)
//                .setResultsEncodingType(GeometryEncodingType.wkb)
//                .setOperationProj(operatorSpatialReference)
//                .setResultProj(outputSpatialReference);

        this.shapefileThrottled(inFile, operatorRequestBuilder, geometryBuilder);
    }

    public void testParcelsFile(String pathFile) throws IOException, InterruptedException {
        File inFile = new File(pathFile);
        String prfFile = inFile.getAbsolutePath().substring(0, inFile.getAbsolutePath().lastIndexOf('.')) + ".prj";
        String projectionWKT = new String(Files.readAllBytes(Paths.get(prfFile)));

        ProjectionData serviceSpatialReference = ProjectionData.newBuilder()
                .setWkt(projectionWKT).build();

        ProjectionData wgs84SpatiralReference = ProjectionData.newBuilder()
                .setEpsg(4326).build();

        GeometryData.Builder geometryBagBuilder = GeometryData.newBuilder()
                .setEsriShape(ByteString.copyFromUtf8(""))
                .setGeometryId(0)
                .setProj(serviceSpatialReference);

        Params.Buffer bufferParams = Params.Buffer.newBuilder().setDistance(2.5).build();

        GeometryRequest.Builder operatorRequestBuilder = GeometryRequest.newBuilder()
                .setOperator(OperatorType.BUFFER)
                .setBufferParams(bufferParams)
//                .addBufferDistances(2.5)
                .setResultEncoding(Encoding.WKT)
                .setResultProj(wgs84SpatiralReference);

        this.shapefileThrottled(inFile, operatorRequestBuilder, geometryBagBuilder);
    }

    public void shapefileChunked(File inFile) throws FileNotFoundException, InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        InputStream inputStream = new FileInputStream(inFile);
        int offsetSize = 262144;
        byte[] bytes = new byte[offsetSize];

        GeometryRequest project = GeometryRequest
                .newBuilder()
                .setOperationProj(ProjectionData.newBuilder().setEpsg(4326))
                .setResultProj(ProjectionData.newBuilder().setEpsg(54016))
                .setResultEncoding(Encoding.GEOJSON).build();

        GeometryRequest buffer = GeometryRequest
                .newBuilder()
                .setGeometryRequest(project)
                .setOperator(OperatorType.BUFFER)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(45))
                .setResultEncoding(Encoding.GEOJSON).build();

        FileRequestChunk.Builder fileChunkBuilder = FileRequestChunk
                .newBuilder()
                .setData(ByteString.copyFromUtf8(""))
                .setNestedRequest(buffer);

        GeometryServiceStub geometryServiceStub = asyncStub.withMaxInboundMessageSize(2147483647);

        ClientResponseObserver<FileRequestChunk, GeometryResponse> clientResponseObserver =
                new ClientResponseObserver<FileRequestChunk, GeometryResponse>() {
                    ClientCallStreamObserver<FileRequestChunk> requestStream;
                    @Override
                    public void beforeStart(ClientCallStreamObserver<FileRequestChunk> requestStream) {
                        this.requestStream = requestStream;
                        requestStream.disableAutoInboundFlowControl();
                        requestStream.setOnReadyHandler(() -> {
                            while (requestStream.isReady()) {
                                try {
                                    int resultSize = inputStream.read(bytes, 0, offsetSize);
                                    if (resultSize < offsetSize) {
                                        requestStream.onCompleted();
                                        inputStream.close();
                                        break;
                                    }
                                    fileChunkBuilder.setSize(resultSize).setData(ByteString.copyFrom(bytes, 0, resultSize));
                                    requestStream.onNext(fileChunkBuilder.build());
                                } catch (EOFException e2) {
                                    requestStream.onCompleted();
                                    try {
                                        // this is stupid
                                        inputStream.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                } catch (IOException eio) {
                                    logger.severe(eio.getLocalizedMessage());
                                    break;
                                }
                            }
                        });
                    }

                    @Override
                    public void onNext(GeometryResponse value) {
                        long id = value.getGeometry().getGeometryId();
                        if (id % 1000 == 0) {
                            logger.info("Geometry number " + id);
                        }
                        // Signal the sender to send one message.
                        requestStream.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        done.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("All Done");
                        done.countDown();
                    }
                };

        geometryServiceStub.fileOperateBiStreamFlow(clientResponseObserver);

        done.await();

//        channel.shutdown();
//        channel.awaitTermination(1, TimeUnit.SECONDS);
    }


    /**
     * https://github.com/ReactiveX/RxJava/wiki/Backpressure
     *
     * @param inFile
     * @throws IOException
     * @throws InterruptedException
     */
    public void shapefileThrottled(File inFile,
                                   GeometryRequest.Builder operatorRequestBuilder,
                                   GeometryData.Builder geometryBuilder) throws IOException, InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        ShapefileByteReader shapefileByteReader = new ShapefileByteReader(inFile);

        GeometryServiceStub geometryServiceStub = asyncStub
                .withMaxInboundMessageSize(2147483647)
                .withMaxOutboundMessageSize(2147483647);

        // When using manual flow-control and back-pressure on the client, the ClientResponseObserver handles both
        // request and response streams.
        ClientResponseObserver<GeometryRequest, GeometryResponse> clientResponseObserver =
                new ClientResponseObserver<GeometryRequest, GeometryResponse>() {
                    ClientCallStreamObserver<GeometryRequest> requestStream;

                    @Override
                    public void beforeStart(ClientCallStreamObserver<GeometryRequest> requestStream) {
                        this.requestStream = requestStream;
                        // Set up manual flow control for the response stream. It feels backwards to configure the response
                        // stream's flow control using the request stream's observer, but this is the way it is.
                        requestStream.disableAutoInboundFlowControl();

                        // Set up a back-pressure-aware producer for the request stream. The onReadyHandler will be invoked
                        // when the consuming side has enough buffer space to receive more messages.
                        //
                        // Messages are serialized into a transport-specific transmit buffer. Depending on the size of this buffer,
                        // MANY messages may be buffered, however, they haven't yet been sent to the server. The server must call
                        // request() to pull a buffered message from the client.
                        //
                        // Note: the onReadyHandler's invocation is serialized on the same thread pool as the incoming
                        // StreamObserver'sonNext(), onError(), and onComplete() handlers. Blocking the onReadyHandler will prevent
                        // additional messages from being processed by the incoming StreamObserver. The onReadyHandler must return
                        // in a timely manor or else message processing throughput will suffer.
                        requestStream.setOnReadyHandler(() -> {
                            while (requestStream.isReady()) {
                                if (shapefileByteReader.hasNext()) {
                                    byte[] data = shapefileByteReader.next();
                                    long id = shapefileByteReader.getGeometryID();
                                    ByteString byteString = ByteString.copyFrom(data);
//                                    logger.info("bytes length -->" + data.length);

                                    GeometryData geometryData = geometryBuilder
                                            .setEsriShape(byteString)
                                            .setGeometryId(id)
                                            .build();
                                    GeometryRequest operatorRequest = operatorRequestBuilder
                                            .setLeftGeometry(geometryData).build();
                                    requestStream.onNext(operatorRequest);
                                } else {
                                    requestStream.onCompleted();
                                    break;
                                }
                            }

                        });
                    }

                    @Override
                    public void onNext(GeometryResponse operatorResult) {
                        long id = operatorResult.getGeometry().getGeometryId();

                        if (id % 1000 == 0) {
                            logger.info("Geometry number " + id);
//                            logger.info(operatorResult.getGeometryBag().getGeojson(0));
                        }
                        // Signal the sender to send one message.
                        requestStream.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                        done.countDown();
                    }

                    @Override
                    public void onCompleted() {
                        logger.info("All Done");
                        done.countDown();
                    }
                };
        // Note: clientResponseObserver is handling both request and response stream processing.
        geometryServiceStub.operateBiStream(clientResponseObserver);


        done.await();

//        channel.shutdown();
        channel.awaitTermination(1, TimeUnit.SECONDS);
    }

    public void getProjected() {
        Polyline polyline = new Polyline();
        polyline.startPath(500000, 0);
        polyline.lineTo(400000, 100000);
        polyline.lineTo(600000, -100000);
        OperatorExportToWkb op = OperatorExportToWkb.local();

        ProjectionData inputSpatialReference = ProjectionData.newBuilder()
                .setEpsg(32632)
                .build();

        GeometryData geometryData = GeometryData.newBuilder()
                .setProj(inputSpatialReference)
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        ProjectionData outputSpatialReference = ProjectionData.newBuilder()
                .setEpsg(4326)
                .build();


        GeometryRequest serviceProjectOp = GeometryRequest
                .newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.PROJECT)
                .setOperationProj(outputSpatialReference)
                .build();

        System.out.println("executing request");
        GeometryResponse operatorResult = blockingStub.operate(serviceProjectOp);
        System.out.println("finished request");

        OperatorImportFromWkb op2 = OperatorImportFromWkb.local();

        Polyline result = (Polyline) op2.execute(
                0,
                Geometry.Type.Unknown,
                operatorResult
                        .getGeometry()
                        .getWkb()
                        .asReadOnlyByteBuffer(),
                null);
        System.out.println(GeometryEngine.geometryToWkt(result, 0));
    }

    /**
     * Issues several different requests and then exits.
     */
    public static void main(String[] args) throws InterruptedException {
        GeometryServiceClient geometryServiceClient = null;
        String target = System.getenv("GEOMETRY_SERVICE_TARGET");
        if (target != null)
            geometryServiceClient = new GeometryServiceClient(target);
        else
            geometryServiceClient = new GeometryServiceClient(args[0], 8980);

        System.out.println("Starting main");
        try {
            String filePath = null;
            if (args.length >= 2) {
                filePath = args[1];
            } else {
                filePath = "/data/Parcels/PARCELS.shp";
            }
            long startTime = System.nanoTime();


            geometryServiceClient.shapefileChunked(new File(filePath));
            geometryServiceClient.testWRSShapefile(filePath);


//            geometryServiceClient.shapefileThrottled(filePath);
            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;
            System.out.println("Test duration");
            System.out.println(duration);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            geometryServiceClient.shutdown();
        }
    }

    private void info(String msg, Object... params) {
        logger.log(Level.INFO, msg, params);
    }

    private void warning(String msg, Object... params) {
        logger.log(Level.WARNING, msg, params);
    }


    /**
     * Only used for unit test, as we do not want to introduce randomness in unit test.
     */
    @VisibleForTesting
    void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Only used for helping unit test.
     */
    @VisibleForTesting
    interface TestHelper {
        /**
         * Used for verify/inspect message received from server.
         */
        void onMessage(Message message);

        /**
         * Used for verify/inspect error received from server.
         */
        void onRpcError(Throwable exception);
    }

    @VisibleForTesting
    void setTestHelper(TestHelper testHelper) {
        this.testHelper = testHelper;
    }
}
