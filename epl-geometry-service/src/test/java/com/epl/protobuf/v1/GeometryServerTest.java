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

import org.epl.geometry.*;
import com.esri.core.geometry.*;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.ClientResponseObserver;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;


/**
 * Unit tests for {@link GeometryServer}.
 * For demonstrating how to write gRPC unit test only.
 * Not intended to provide a high code coverage or to test every major usecase.
 */
@RunWith(JUnit4.class)
public class GeometryServerTest {
    private GeometryServer server;
    private ManagedChannel inProcessChannel;

    @Before
    public void setUp() throws Exception {
        String uniqueServerName = "in-process server for " + getClass();
        // use directExecutor for both InProcessServerBuilder and InProcessChannelBuilder can reduce the
        // usage timeouts and latches in test. But we still add timeout and latches where they would be
        // needed if no directExecutor were used, just for demo purpose.
        server = new GeometryServer(InProcessServerBuilder.forName(uniqueServerName).directExecutor(), 0);
        server.start();
        inProcessChannel = InProcessChannelBuilder.forName(uniqueServerName).directExecutor().build();
    }

    @After
    public void tearDown() throws Exception {
        inProcessChannel.shutdownNow();
        server.stop();
    }


    @Test
    public void getWKTGeometry() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);
        OperatorExportToWkt op = OperatorExportToWkt.local();
        String geom = op.execute(0, polyline, null);

        GeometryData geometryData = GeometryData.newBuilder()
                .setWkt(geom)
                .setGeometryId(42)
                .setFeatureId("Pancakes")
                .build();

        GeometryRequest requestOp = GeometryRequest.newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.EXPORT_TO_WKT)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(requestOp);

        assertEquals(operatorResult.getGeometry().getWkt(), geometryData.getWkt());
        assertEquals(operatorResult.getGeometry().getGeometryId(), 42);
        assertEquals(operatorResult.getGeometry().getFeatureId(), "Pancakes");
        assertEquals(operatorResult.getGeometry().getSimple(), SimpleState.STRONG_SIMPLE);
    }

    @Test
    public void getWKTGeometryDataLeft() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);
        OperatorExportToWkt op = OperatorExportToWkt.local();
        String geom = op.execute(0, polyline, null);

        GeometryData geometryData = GeometryData.newBuilder().setWkt(geom).setGeometryId(42).build();

        GeometryRequest requestOp = GeometryRequest.newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.EXPORT_TO_WKT)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(requestOp);

        assertEquals(operatorResult.getGeometry().getWkt(), geometryData.getWkt());
        assertEquals(operatorResult.getGeometry().getGeometryId(), 42);
    }

    @Test
    public void getGeoJsonGeometryDataLeft() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);
        OperatorFactoryLocal factory = OperatorFactoryLocal.getInstance();
        SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(polyline);
        OperatorExportToGeoJsonCursor exportToGeoJsonCursor = new OperatorExportToGeoJsonCursor(GeoJsonExportFlags.geoJsonExportSkipCRS, null, simpleGeometryCursor);
        String geom = exportToGeoJsonCursor.next();

        GeometryData geometryData = GeometryData.newBuilder().setGeometryId(42).setGeojson(geom).build();

        GeometryRequest requestOp = GeometryRequest.newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.EXPORT_TO_WKT)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(requestOp);

        OperatorExportToWkt op2 = OperatorExportToWkt.local();
        String geom2 = op2.execute(0, polyline, null);

        assertEquals(operatorResult.getGeometry().getWkt(), geom2);
        assertEquals(operatorResult.getGeometry().getGeometryId(), 42);
    }


    @Test
    public void getGeoJsonGeometryData() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);
        OperatorExportToGeoJson op = OperatorExportToGeoJson.local();
        String geom = op.execute(0, null, polyline);

        GeometryData geometryData = GeometryData.newBuilder().setGeometryId(42).setGeojson(geom).build();

        GeometryRequest requestOp = GeometryRequest.newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.EXPORT_TO_WKT)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(requestOp);

        OperatorExportToWkt op2 = OperatorExportToWkt.local();
        String geom2 = op2.execute(0, polyline, null);

        assertEquals(operatorResult.getGeometry().getWkt(), geom2);
        assertEquals(operatorResult.getGeometry().getGeometryId(), 42);
    }



    @Test
    public void getWKTGeometryFromWKB() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);
        OperatorExportToWkb op = OperatorExportToWkb.local();


        GeometryData geometryData = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        GeometryRequest requestOp = GeometryRequest.newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.EXPORT_TO_WKT)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(requestOp);

        OperatorExportToWkt op2 = OperatorExportToWkt.local();
        String geom = op2.execute(0, polyline, null);
        assertEquals(operatorResult.getGeometry().getWkt(), geom);
    }

    @Test
    public void getWKTGeometryFromWKBData() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);
        OperatorExportToWkb op = OperatorExportToWkb.local();


        GeometryData geometryData = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        GeometryRequest requestOp = GeometryRequest.newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.EXPORT_TO_WKT)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(requestOp);

        OperatorExportToWkt op2 = OperatorExportToWkt.local();
        String geom = op2.execute(0, polyline, null);
        assertEquals(operatorResult.getGeometry().getWkt(), geom);
    }

    @Test
    public void getCONVEX_HULLGeometryFromWKB() {
        Polyline polyline = new Polyline();
        polyline.startPath(-200, -90);
        polyline.lineTo(-180, -85);
        polyline.lineTo(-90, -70);
        polyline.lineTo(0, 0);
        polyline.lineTo(100, 25);
        polyline.lineTo(170, 45);
        polyline.lineTo(225, 65);
        OperatorExportToESRIShape op = OperatorExportToESRIShape.local();

        GeometryData geometryData = GeometryData.newBuilder()
                .setEsriShape(ByteString.copyFrom(op.execute(0, polyline)))
                .build();

        GeometryRequest serviceOp = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.CONVEX_HULL)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceOp);

        OperatorImportFromWkb op2 = OperatorImportFromWkb.local();
        Geometry result = op2.execute(0, Geometry.Type.Unknown, operatorResult.getGeometry().getWkb().asReadOnlyByteBuffer(), null);

        boolean bContains = OperatorContains.local().execute(result, polyline, SpatialReference.create(4326), null);

        assertTrue(bContains);
    }


    @Test
    public void getCONVEX_HULLGeometryFromWKBData() {
        Polyline polyline = new Polyline();
        polyline.startPath(-200, -90);
        polyline.lineTo(-180, -85);
        polyline.lineTo(-90, -70);
        polyline.lineTo(0, 0);
        polyline.lineTo(100, 25);
        polyline.lineTo(170, 45);
        polyline.lineTo(225, 65);
        OperatorExportToESRIShape op = OperatorExportToESRIShape.local();

        GeometryData geometryData = GeometryData.newBuilder()
                .setEsriShape(ByteString.copyFrom(op.execute(0, polyline)))
                .build();

        GeometryRequest serviceOp = GeometryRequest
                .newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.CONVEX_HULL)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceOp);

        OperatorImportFromWkb op2 = OperatorImportFromWkb.local();
        Geometry result = op2.execute(0, Geometry.Type.Unknown, operatorResult.getGeometry().getWkb().asReadOnlyByteBuffer(), null);

        boolean bContains = OperatorContains.local().execute(result, polyline, SpatialReference.create(4326), null);

        assertTrue(bContains);
    }


    @Test
    public void testProjection() {
        Polyline polyline = new Polyline();
        polyline.startPath(500000, 0);
        polyline.lineTo(400000, 100000);
        polyline.lineTo(600000, -100000);
        OperatorExportToWkb op = OperatorExportToWkb.local();

        ProjectionData inputSpatialReference = ProjectionData.newBuilder()
                .setEpsg(32632)
                .build();

        GeometryData geometryData = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .setGeometryId(44)
                .setProj(inputSpatialReference)
                .build();

        ProjectionData outputSpatialReference = ProjectionData.newBuilder()
                .setEpsg(4326)
                .build();


        GeometryRequest serviceProjectOp = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.PROJECT)
                .setResultProj(outputSpatialReference)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceProjectOp);

        OperatorImportFromWkb op2 = OperatorImportFromWkb.local();
        Polyline result = (Polyline) op2.execute(0, Geometry.Type.Unknown, operatorResult.getGeometry().getWkb().asReadOnlyByteBuffer(), null);
        TestCase.assertNotNull(result);

        assertEquals(operatorResult.getGeometry().getSimple(), SimpleState.STRONG_SIMPLE);

        TestCase.assertFalse(polyline.equals(result));
        assertEquals(polyline.getPointCount(), result.getPointCount());
        assertEquals(operatorResult.getGeometry().getGeometryId(), 44);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(32632), SpatialReferenceEx.create(4326));
        Polyline originalPolyline = (Polyline)OperatorProject.local().execute(polyline, projectionTransformation, null);

        for (int i = 0; i < polyline.getPointCount(); i++) {
            assertEquals(result.getPoint(i).getX(), originalPolyline.getPoint(i).getX(), 1e-10);
            assertEquals(result.getPoint(i).getY(), originalPolyline.getPoint(i).getY(), 1e-10);
        }
    }

    @Test
    public void testSpatialReferenceReturn() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);

        OperatorExportToWkb op = OperatorExportToWkb.local();
        GeometryData geometryData = GeometryData
                .newBuilder()
                .setProj(ProjectionData.newBuilder().setEpsg(4326).build())
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        GeometryRequest serviceConvexOp = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.CONVEX_HULL)
                .build();


        GeometryRequest.Builder serviceOp = GeometryRequest.newBuilder()
                .setLeftGeometryRequest(serviceConvexOp)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(1).build())
                .setOperator(OperatorType.BUFFER);

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceOp.build());
        assertEquals(operatorResult.getGeometry().getProj().getEpsg(), 4326);
    }

    @Test
    public void testSpatialReferenceReturn_2() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);

        OperatorExportToWkb op = OperatorExportToWkb.local();
        GeometryData geometryData = GeometryData
                .newBuilder()
                .setProj(ProjectionData.newBuilder().setEpsg(4326).build())
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        GeometryRequest serviceConvexOp = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.CONVEX_HULL)
                .build();


        GeometryRequest.Builder serviceOp = GeometryRequest.newBuilder()
                .setLeftGeometryRequest(serviceConvexOp)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(1).build())
                .setResultProj(ProjectionData.newBuilder().setEpsg(4326).build())
                .setOperator(OperatorType.BUFFER);

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceOp.build());
        assertEquals(operatorResult.getGeometry().getProj().getEpsg(), 4326);
    }

    @Test
    public void testSpatialReferenceReturn_3() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);

        OperatorExportToWkb op = OperatorExportToWkb.local();
        GeometryData geometryData = GeometryData
                .newBuilder()
                .setProj(ProjectionData.newBuilder().setEpsg(4326).build())
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        GeometryRequest serviceConvexOp = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryData)
                .setOperationProj(ProjectionData.newBuilder().setEpsg(3857).build())
                .setOperator(OperatorType.CONVEX_HULL)
                .build();


        GeometryRequest.Builder serviceOp = GeometryRequest.newBuilder()
                .setLeftGeometryRequest(serviceConvexOp)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(1).build())
                .setOperator(OperatorType.BUFFER);

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceOp.build());
        assertEquals(operatorResult.getGeometry().getProj().getEpsg(), 3857);
    }

    @Test
    public void testSpatialReferenceReturn_5() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);

        OperatorExportToWkb op = OperatorExportToWkb.local();
        GeometryData geometryData = GeometryData
                .newBuilder()
                .setProj(ProjectionData.newBuilder().setEpsg(4326).build())
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        GeometryRequest serviceConvexOp = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryData)
                .setOperationProj(ProjectionData.newBuilder().setProj4(SpatialReferenceEx.create(3857).getProj4()).build())
                .setOperator(OperatorType.CONVEX_HULL)
                .build();


        GeometryRequest.Builder serviceOp = GeometryRequest.newBuilder()
                .setLeftGeometryRequest(serviceConvexOp)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(1).build())
                .setOperator(OperatorType.BUFFER);

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceOp.build());
        assertEquals(operatorResult.getGeometry().getProj().getEpsg(), 3857);
    }

    @Test
    public void testSpatialReferenceReturn_6() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);

        OperatorExportToWkb op = OperatorExportToWkb.local();
        GeometryData geometryData = GeometryData
                .newBuilder()
                .setProj(ProjectionData.newBuilder().setEpsg(4326).build())
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        GeometryRequest serviceConvexOp = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.CONVEX_HULL)
                .build();


        GeometryRequest.Builder serviceOp = GeometryRequest.newBuilder()
                .setLeftGeometryRequest(serviceConvexOp)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(1).build())
                .setResultProj(ProjectionData.newBuilder().setEpsg(4326).build())
                .setOperator(OperatorType.BUFFER)
                .setResultEncoding(Encoding.WKT);

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceOp.build());
        assertEquals(operatorResult.getGeometry().getEnvelope().getProj().getEpsg(), 4326);
    }

    @Test
    public void testEnvelopeReturn() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);

        OperatorExportToWkb op = OperatorExportToWkb.local();
        GeometryData geometryData = GeometryData
                .newBuilder()
                .setProj(ProjectionData.newBuilder().setEpsg(4326).build())
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        GeometryRequest serviceConvexOp = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.CONVEX_HULL)
                .build();


        GeometryRequest.Builder serviceOp = GeometryRequest.newBuilder()
                .setLeftGeometryRequest(serviceConvexOp)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(1).build())
                .setOperator(OperatorType.BUFFER);


        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceOp.build());

        OperatorImportFromWkb op2 = OperatorImportFromWkb.local();
        Geometry result = op2.execute(0, Geometry.Type.Unknown, operatorResult.getGeometry().getWkb().asReadOnlyByteBuffer(), null);

        boolean bContains = OperatorContains.local().execute(result, polyline, SpatialReference.create(4326), null);

        assertTrue(bContains);

        serviceOp.setResultEncoding(Encoding.WKB);
        GeometryResponse operatorResult2 = stub.operate(serviceOp.build());

        assertEquals(-1, operatorResult2.getGeometry().getEnvelope().getXmin(), 0.0);
        assertEquals(-1, operatorResult2.getGeometry().getEnvelope().getYmin(), 0.0);
        assertEquals(4, operatorResult2.getGeometry().getEnvelope().getXmax(), 0.0);
        assertEquals(4, operatorResult2.getGeometry().getEnvelope().getYmax(), 0.0);

    }

//    @Test
//    public void testEnvelope() {
//        Polyline polyline = new Polyline();
//        polyline.startPath(0, 0);
//        polyline.lineTo(2, 3);
//        polyline.lineTo(3, 3);
//        GeometryRequest geometryRequest = GeometryRequest
//                .newBuilder()
//                .setResultEncoding(Encoding.ENVELOPE)
//                .setGeometry(GeometryData
//                        .newBuilder()
//                        .setWkb(ByteString
//                                .copyFrom(OperatorExportToWkb
//                                        .local()
//                                        .execute(0,polyline, null)))).build();
//
//        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
//        GeometryResponse response = stub.operate(geometryRequest);
//        assertEquals(0, response.getEnvelope().getXmin(), 0.0);
//        assertEquals(0, response.getEnvelope().getYmin(), 0.0);
//        assertEquals(3, response.getEnvelope().getXmax(), 0.0);
//        assertEquals(3, response.getEnvelope().getYmax(), 0.0);
//    }

    @Test
    public void testChainingBufferCONVEX_HULLLeft() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);
        // TODO inspect bug where it crosses dateline
//        polyline.startPath(-200, -90);
//        polyline.lineTo(-180, -85);
//        polyline.lineTo(-90, -70);
//        polyline.lineTo(0, 0);
//        polyline.lineTo(100, 25);
//        polyline.lineTo(170, 45);
//        polyline.lineTo(225, 64);

        OperatorExportToWkb op = OperatorExportToWkb.local();
        GeometryData geometryData = GeometryData
                .newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        GeometryRequest serviceConvexOp = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.CONVEX_HULL)
                .build();


        GeometryRequest serviceOp = GeometryRequest.newBuilder()
                .setLeftGeometryRequest(serviceConvexOp)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(1).build())
                .setOperator(OperatorType.BUFFER)
                .build();


        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceOp);

        OperatorImportFromWkb op2 = OperatorImportFromWkb.local();
        Geometry result = op2.execute(0, Geometry.Type.Unknown, operatorResult.getGeometry().getWkb().asReadOnlyByteBuffer(), null);

        boolean bContains = OperatorContains.local().execute(result, polyline, SpatialReference.create(4326), null);

        assertTrue(bContains);
    }

    @Test
    public void testChainingBufferCONVEX_HULLData() {
        Polyline polyline = new Polyline();
        polyline.startPath(0, 0);
        polyline.lineTo(2, 3);
        polyline.lineTo(3, 3);
        // TODO inspect bug where it crosses dateline
//        polyline.startPath(-200, -90);
//        polyline.lineTo(-180, -85);
//        polyline.lineTo(-90, -70);
//        polyline.lineTo(0, 0);
//        polyline.lineTo(100, 25);
//        polyline.lineTo(170, 45);
//        polyline.lineTo(225, 64);

        OperatorExportToWkb op = OperatorExportToWkb.local();
        GeometryData geometryData = GeometryData
                .newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .build();

        GeometryRequest serviceConvexOp = GeometryRequest
                .newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.CONVEX_HULL)
                .build();


        GeometryRequest serviceOp = GeometryRequest.newBuilder()
                .setGeometryRequest(serviceConvexOp)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(1).build())
                .setOperator(OperatorType.BUFFER)
                .build();


        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceOp);

        OperatorImportFromWkb op2 = OperatorImportFromWkb.local();
        Geometry result = op2.execute(0, Geometry.Type.Unknown, operatorResult.getGeometry().getWkb().asReadOnlyByteBuffer(), null);

        boolean bContains = OperatorContains.local().execute(result, polyline, SpatialReference.create(4326), null);

        assertTrue(bContains);
    }

    static double randomWithRange(double min, double max) {
        double range = Math.abs(max - min);
        return (Math.random() * range) + (min <= max ? min : max);
    }

//    @Test
//    public void testUnion() {
//        int size = 1000;
//        List<String> points = new ArrayList<>(size);
//        List<Point> pointList = new ArrayList<>(size);
//        for (int i = 0; i < size; i++) {
//            double x = randomWithRange(-20, 20);
//            double y = randomWithRange(-20, 20);
//            points.add(String.format("Point(%f %f)", x, y));
//            pointList.add(new Point(x, y));
//        }
//        GeometryData geometryData = GeometryData.newBuilder()
//                .setAllWkt(points)
//                .build();
//
//        BufferParams bufferParams = BufferParams.newBuilder().setDistance(2.5).setUnionResult(true).build();
//
//        GeometryRequest serviceBufferOp = GeometryRequest.newBuilder()
//                .setLeftGeometry(geometryData)
//                .setOperator(OperatorType.BUFFER)
//                .setBufferParams(Params.(buffer)
//                .build();
//
//        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
//        GeometryResponse operatorResult = stub.operate(serviceBufferOp);
//
//        List<ByteBuffer> byteBufferList = operatorResult.getGeometry().getWkbList().stream().map(com.google.protobuf.ByteString::asReadOnlyByteBuffer).collect(Collectors.toList());
//        SimpleByteBufferCursor simpleByteBufferCursor = new SimpleByteBufferCursor(byteBufferList);
//        OperatorImportFromWkbCursor operatorImportFromWkbCursor = new OperatorImportFromWkbCursor(0, simpleByteBufferCursor);
//        Geometry result = OperatorImportFromWkb.local().execute(0, Geometry.Type.Unknown, operatorResult.getGeometry().getWkb().asReadOnlyByteBuffer(), null);
//        assertTrue(result.calculateArea2D() > (Math.PI * 2.5 * 2.5 * 2));
//
////    assertEquals(resultSR.calculateArea2D(), Math.PI * 2.5 * 2.5, 0.1);
////    shape_start = datetime.datetime.now()
////    spots = [p.buffer(2.5) for p in points]
////    patches = cascaded_union(spots)
////    shape_end = datetime.datetime.now()
////    shape_delta = shape_end - shape_start
////    shape_microseconds = int(shape_delta.total_seconds() * 1000)
////
////    stub = geometry_grpc.GeometryOperatorsStub(self.channel)
////    geometryData = GeometryData()
////
////    epl_start = datetime.datetime.now()
////    geometryData.geometry_binary.extend([s.wkb for s in spots])
////    geometryData.geometry_encoding_type = Encoding.Value('wkb')
////
////        # opRequestBuffer = GeometryRequest(left_geometry=geometryData,
////            #                                   operator_type=OperatorType.Value('BUFFER'),
////            #                                   buffer_distances=[2.5])
////
////    opRequestUnion = GeometryRequest(left_geometry=geometryData,
////            operator_type=OperatorType.Value('Union'))
////
////    response = stub.operate(opRequestUnion)
////    unioned_result = wkbloads(response.geometry.geometry_binary[0])
////    epl_end = datetime.datetime.now()
////    epl_delta = epl_end - epl_start
////    epl_microseconds = int(epl_delta.total_seconds() * 1000)
////    self.assertGreater(shape_microseconds, epl_microseconds)
////    self.assertGreater(shape_microseconds / 8, epl_microseconds)
////
////    self.assertAlmostEqual(patches.area, unioned_result.area, 4)
//    }
//
    @Test
    public void testCrazyNesting() {
        Polyline polyline = new Polyline();
        polyline.startPath(-120, -45);
        polyline.lineTo(-100, -55);
        polyline.lineTo(-90, -63);
        polyline.lineTo(0, 0);
        polyline.lineTo(1, 1);
        polyline.lineTo(100, 25);
        polyline.lineTo(170, 45);
        polyline.lineTo(175, 65);
        OperatorExportToWkb op = OperatorExportToWkb.local();

        ProjectionData spatialReferenceNAD = ProjectionData.newBuilder().setEpsg(4269).build();
        ProjectionData spatialReferenceMerc = ProjectionData.newBuilder().setEpsg(3857).build();
        ProjectionData spatialReferenceWGS = ProjectionData.newBuilder().setEpsg(4326).build();
        ProjectionData spatialReferenceGall = ProjectionData.newBuilder().setEpsg(4088).build();
        //TODO why does esri shape fail


        GeometryData geometryDataLeft = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .setProj(spatialReferenceNAD)
                .build();

        Params.Buffer bufferParams = Params.Buffer.newBuilder().setDistance(.5).build();

        GeometryRequest serviceOpLeft = GeometryRequest
                .newBuilder()
                .setGeometry(geometryDataLeft)
                .setOperator(OperatorType.BUFFER)
                .setBufferParams(bufferParams)
                .setResultProj(spatialReferenceWGS)
                .setResultEncoding(Encoding.WKT)
                .build();
        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse bufferResult = stub.operate(serviceOpLeft);
        GeometryData geometryData = bufferResult.getGeometry();
        Geometry geom = GeometryEngineEx.geometryFromWkt(geometryData.getWkt(), 0, Geometry.Type.Unknown);



        GeometryRequest nestedLeft = GeometryRequest
                .newBuilder()
                .setGeometryRequest(serviceOpLeft)
                .setOperator(OperatorType.CONVEX_HULL)
                .setResultProj(spatialReferenceGall)
                .build();

        GeometryData geometryDataRight = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .setProj(spatialReferenceNAD)
                .build();

        GeometryRequest serviceOpRight = GeometryRequest
                .newBuilder()
                .setGeometry(geometryDataRight)
                .setOperator(OperatorType.GEODESIC_BUFFER)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(1000).setUnionResult(false).build())
                .setOperationProj(spatialReferenceWGS)
                .build();

        GeometryRequest nestedRight = GeometryRequest
                .newBuilder()
                .setGeometryRequest(serviceOpRight)
                .setOperator(OperatorType.CONVEX_HULL)
                .setResultProj(spatialReferenceGall)
                .build();

        GeometryRequest operatorRequestContains = GeometryRequest
                .newBuilder()
                .setLeftGeometryRequest(nestedLeft)
                .setRightGeometryRequest(nestedRight)
                .setOperator(OperatorType.CONTAINS)
                .setOperationProj(spatialReferenceMerc)
                .build();

        stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(operatorRequestContains);
        Map<Long, Boolean> map = operatorResult.getRelateMapMap();

        assertTrue(map.get(0L));
    }


    @Test
    public void testCrazyNestingDataLeft() {
        Polyline polyline = new Polyline();
        polyline.startPath(-120, -45);
        polyline.lineTo(-100, -55);
        polyline.lineTo(-90, -63);
        polyline.lineTo(0, 0);
        polyline.lineTo(1, 1);
        polyline.lineTo(100, 25);
        polyline.lineTo(170, 45);
        polyline.lineTo(175, 65);
        OperatorExportToWkb op = OperatorExportToWkb.local();

        ProjectionData spatialReferenceNAD = ProjectionData.newBuilder().setEpsg(4269).build();
        ProjectionData spatialReferenceMerc = ProjectionData.newBuilder().setEpsg(3857).build();
        ProjectionData spatialReferenceWGS = ProjectionData.newBuilder().setEpsg(4326).build();
        ProjectionData spatialReferenceGall = ProjectionData.newBuilder().setEpsg(4088).build();
        //TODO why does esri shape fail


        GeometryData geometryLeft = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .setProj(spatialReferenceNAD)
                .build();

        Params.Buffer bufferParams = Params.Buffer.newBuilder().setDistance(.5).build();
        GeometryRequest serviceOpLeft = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryLeft)
                .setOperator(OperatorType.BUFFER)
                .setBufferParams(bufferParams)
                .setResultProj(spatialReferenceWGS)
                .build();

        GeometryRequest nestedLeft = GeometryRequest
                .newBuilder()
                .setLeftGeometryRequest(serviceOpLeft)
                .setOperator(OperatorType.CONVEX_HULL)
                .setResultProj(spatialReferenceGall)
                .build();

        GeometryData geometryDataRight = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .setProj(spatialReferenceNAD)
                .build();

        GeometryRequest serviceOpRight = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryDataRight)
                .setOperator(OperatorType.GEODESIC_BUFFER)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(1000).setUnionResult(false).build())
                .setOperationProj(spatialReferenceWGS)
                .build();

        GeometryRequest nestedRight = GeometryRequest
                .newBuilder()
                .setLeftGeometryRequest(serviceOpRight)
                .setOperator(OperatorType.CONVEX_HULL)
                .setResultProj(spatialReferenceGall)
                .build();

        GeometryRequest operatorRequestContains = GeometryRequest
                .newBuilder()
                .setLeftGeometryRequest(nestedLeft)
                .setRightGeometryRequest(nestedRight)
                .setOperator(OperatorType.CONTAINS)
                .setOperationProj(spatialReferenceMerc)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(operatorRequestContains);
        Map<Long, Boolean> map = operatorResult.getRelateMapMap();

        assertTrue(map.get(0L));
    }



    @Test
    public void testCrazyNesting2() {
        Polyline polyline = new Polyline();
        polyline.startPath(-120, -45);
        polyline.lineTo(-100, -55);
        polyline.lineTo(-91, -63);
        polyline.lineTo(0, 0);
        polyline.lineTo(1, 1);
        polyline.lineTo(100, 25);
        polyline.lineTo(170, 45);
        polyline.lineTo(175, 65);
        OperatorExportToWkb op = OperatorExportToWkb.local();
        OperatorImportFromWkb operatorImportFromWkb = OperatorImportFromWkb.local();

        ProjectionData spatialReferenceNAD = ProjectionData.newBuilder().setEpsg(4269).build();
        ProjectionData spatialReferenceMerc = ProjectionData.newBuilder().setEpsg(3857).build();
        ProjectionData spatialReferenceWGS = ProjectionData.newBuilder().setEpsg(4326).build();
        ProjectionData spatialReferenceGall = ProjectionData.newBuilder().setEpsg(4088).build();
        //TODO why does esri shape fail
        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);


        GeometryData geometryDataLeft = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .setProj(spatialReferenceNAD)
                .build();

        GeometryRequest serviceOpLeft = GeometryRequest
                .newBuilder()
                .setGeometry(geometryDataLeft)
                .setOperator(OperatorType.BUFFER)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(.5).build())
                .setResultProj(spatialReferenceWGS)
                .build();

        Geometry bufferedLeft = GeometryEngine.buffer(polyline, SpatialReference.create(4269), .5);
        Geometry projectedBuffered = GeometryEngineEx.project(bufferedLeft, SpatialReferenceEx.create(4269), SpatialReferenceEx.create(4326));
        GeometryResponse operatorResultLeft = stub.operate(serviceOpLeft);
        SimpleByteBufferCursor simpleByteBufferCursor = new SimpleByteBufferCursor(operatorResultLeft.getGeometry().getWkb().asReadOnlyByteBuffer());
        assertTrue(GeometryEngine.equals(projectedBuffered, operatorImportFromWkb.execute(0, simpleByteBufferCursor, null).next(), SpatialReference.create(4326)));


        GeometryRequest nestedLeft = GeometryRequest
                .newBuilder()
                .setGeometryRequest(serviceOpLeft)
                .setOperator(OperatorType.CONVEX_HULL)
                .setResultProj(spatialReferenceGall)
                .build();
        Geometry projectedBufferedConvex = GeometryEngine.convexHull(projectedBuffered);
        Geometry reProjectedBufferedCONVEX_HULL = GeometryEngineEx.project(projectedBufferedConvex, SpatialReferenceEx.create(4326), SpatialReferenceEx.create(4088));
        GeometryResponse operatorResultLeftNested = stub.operate(nestedLeft);
        simpleByteBufferCursor = new SimpleByteBufferCursor(operatorResultLeftNested.getGeometry().getWkb().asReadOnlyByteBuffer());
        assertTrue(GeometryEngine.equals(reProjectedBufferedCONVEX_HULL, operatorImportFromWkb.execute(0, simpleByteBufferCursor, null).next(), SpatialReference.create(4088)));

        GeometryData geometryDataRight = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .setProj(spatialReferenceNAD)
                .build();

        GeometryRequest serviceOpRight = GeometryRequest
                .newBuilder()
                .setGeometry(geometryDataRight)
                .setOperator(OperatorType.GEODESIC_BUFFER)
                .setBufferParams(Params.Buffer.newBuilder()
                        .setDistance(1000)
                        .setUnionResult(false)
                        .build())
                .setOperationProj(spatialReferenceWGS)
                .build();

        Geometry projectedRight = GeometryEngineEx.project(polyline, SpatialReferenceEx.create(4269), SpatialReferenceEx.create(4326));
        Geometry projectedBufferedRight = GeometryEngineEx.geodesicBuffer(projectedRight, SpatialReferenceEx.create(4326), 1000);
        GeometryResponse operatorResultRight = stub.operate(serviceOpRight);
        simpleByteBufferCursor = new SimpleByteBufferCursor(operatorResultRight.getGeometry().getWkb().asReadOnlyByteBuffer());
        assertTrue(GeometryEngine.equals(projectedBufferedRight, operatorImportFromWkb.execute(0, simpleByteBufferCursor, null).next(), SpatialReference.create(4326)));

        GeometryRequest nestedRight = GeometryRequest
                .newBuilder()
                .setGeometryRequest(serviceOpRight)
                .setOperator(OperatorType.CONVEX_HULL)
                .setResultProj(spatialReferenceGall)
                .build();

        Geometry projectedBufferedConvexRight = GeometryEngine.convexHull(projectedBufferedRight);
        Geometry reProjectedBufferedCONVEX_HULLRight = GeometryEngineEx.project(projectedBufferedConvexRight, SpatialReferenceEx.create(4326), SpatialReferenceEx.create(4088));
        GeometryResponse operatorResultRightNested = stub.operate(nestedRight);
        simpleByteBufferCursor = new SimpleByteBufferCursor(operatorResultRightNested.getGeometry().getWkb().asReadOnlyByteBuffer());
        assertTrue(GeometryEngine.equals(reProjectedBufferedCONVEX_HULLRight, operatorImportFromWkb.execute(0, simpleByteBufferCursor, null).next(), SpatialReference.create(4088)));

        GeometryRequest operatorRequestSymDifference = GeometryRequest
                .newBuilder()
                .setLeftGeometryRequest(nestedLeft)
                .setRightGeometryRequest(nestedRight)
                .setOperator(OperatorType.SYMMETRIC_DIFFERENCE)
                .setOperationProj(spatialReferenceMerc)
                .setResultProj(spatialReferenceNAD)
                .build();


        Geometry rightFinal = GeometryEngineEx.project(reProjectedBufferedCONVEX_HULLRight, SpatialReferenceEx.create(4088), SpatialReferenceEx.create(3857));
        Geometry leftFinal = GeometryEngineEx.project(reProjectedBufferedCONVEX_HULL, SpatialReferenceEx.create(4088), SpatialReferenceEx.create(3857));
        Geometry difference = GeometryEngine.symmetricDifference(leftFinal, rightFinal, SpatialReference.create(3857));
        Geometry differenceProjected = GeometryEngineEx.project(difference, SpatialReferenceEx.create(3857), SpatialReferenceEx.create(4269));

        GeometryResponse operatorResult = stub.operate(operatorRequestSymDifference);
        simpleByteBufferCursor = new SimpleByteBufferCursor(operatorResult.getGeometry().getWkb().asReadOnlyByteBuffer());
        assertTrue(GeometryEngine.equals(differenceProjected, operatorImportFromWkb.execute(0, simpleByteBufferCursor, null).next(), SpatialReference.create(4269)));

    }

    @Test
    public void testCrazyNesting2Left() {
        Polyline polyline = new Polyline();
        polyline.startPath(-120, -45);
        polyline.lineTo(-100, -55);
        polyline.lineTo(-91, -63);
        polyline.lineTo(0, 0);
        polyline.lineTo(1, 1);
        polyline.lineTo(100, 25);
        polyline.lineTo(170, 45);
        polyline.lineTo(175, 65);
        OperatorExportToWkb op = OperatorExportToWkb.local();
        OperatorImportFromWkb operatorImportFromWkb = OperatorImportFromWkb.local();

        ProjectionData spatialReferenceNAD = ProjectionData.newBuilder().setEpsg(4269).build();
        ProjectionData spatialReferenceMerc = ProjectionData.newBuilder().setEpsg(3857).build();
        ProjectionData spatialReferenceWGS = ProjectionData.newBuilder().setEpsg(4326).build();
        ProjectionData spatialReferenceGall = ProjectionData.newBuilder().setEpsg(4088).build();
        //TODO why does esri shape fail
        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);


        GeometryData geometryDataLeft = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .setProj(spatialReferenceNAD)
                .build();

        GeometryRequest serviceOpLeft = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryDataLeft)
                .setOperator(OperatorType.BUFFER)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(.5).build())
                .setResultProj(spatialReferenceWGS)
                .build();

        Geometry bufferedLeft = GeometryEngine.buffer(polyline, SpatialReference.create(4269), .5);
        Geometry projectedBuffered = GeometryEngineEx.project(bufferedLeft, SpatialReferenceEx.create(4269), SpatialReferenceEx.create(4326));
        GeometryResponse operatorResultLeft = stub.operate(serviceOpLeft);
        SimpleByteBufferCursor simpleByteBufferCursor = new SimpleByteBufferCursor(operatorResultLeft.getGeometry().getWkb().asReadOnlyByteBuffer());
        assertTrue(GeometryEngine.equals(projectedBuffered, operatorImportFromWkb.execute(0, simpleByteBufferCursor, null).next(), SpatialReference.create(4326)));


        GeometryRequest nestedLeft = GeometryRequest
                .newBuilder()
                .setLeftGeometryRequest(serviceOpLeft)
                .setOperator(OperatorType.CONVEX_HULL)
                .setResultProj(spatialReferenceGall)
                .build();
        Geometry projectedBufferedConvex = GeometryEngine.convexHull(projectedBuffered);
        Geometry reProjectedBufferedCONVEX_HULL = GeometryEngineEx.project(projectedBufferedConvex, SpatialReferenceEx.create(4326), SpatialReferenceEx.create(4088));
        GeometryResponse operatorResultLeftNested = stub.operate(nestedLeft);
        simpleByteBufferCursor = new SimpleByteBufferCursor(operatorResultLeftNested.getGeometry().getWkb().asReadOnlyByteBuffer());
        assertTrue(GeometryEngine.equals(reProjectedBufferedCONVEX_HULL, operatorImportFromWkb.execute(0, simpleByteBufferCursor, null).next(), SpatialReference.create(4088)));

        GeometryData geometryDataRight = GeometryData.newBuilder()
                .setWkb(ByteString.copyFrom(op.execute(0, polyline, null)))
                .setProj(spatialReferenceNAD)
                .build();

        GeometryRequest serviceOpRight = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryDataRight)
                .setOperator(OperatorType.GEODESIC_BUFFER)
                .setBufferParams(Params.Buffer.newBuilder()
                        .setDistance(1000)
                        .setUnionResult(false)
                        .build())
                .setOperationProj(spatialReferenceWGS)
                .build();

        Geometry projectedRight = GeometryEngineEx.project(polyline, SpatialReferenceEx.create(4269), SpatialReferenceEx.create(4326));
        Geometry projectedBufferedRight = GeometryEngineEx.geodesicBuffer(projectedRight, SpatialReferenceEx.create(4326), 1000);
        GeometryResponse operatorResultRight = stub.operate(serviceOpRight);
        simpleByteBufferCursor = new SimpleByteBufferCursor(operatorResultRight.getGeometry().getWkb().asReadOnlyByteBuffer());
        assertTrue(GeometryEngine.equals(projectedBufferedRight, operatorImportFromWkb.execute(0, simpleByteBufferCursor, null).next(), SpatialReference.create(4326)));

        GeometryRequest nestedRight = GeometryRequest
                .newBuilder()
                .setLeftGeometryRequest(serviceOpRight)
                .setOperator(OperatorType.CONVEX_HULL)
                .setResultProj(spatialReferenceGall)
                .build();

        Geometry projectedBufferedConvexRight = GeometryEngine.convexHull(projectedBufferedRight);
        Geometry reProjectedBufferedCONVEX_HULLRight = GeometryEngineEx.project(projectedBufferedConvexRight, SpatialReferenceEx.create(4326), SpatialReferenceEx.create(4088));
        GeometryResponse operatorResultRightNested = stub.operate(nestedRight);
        simpleByteBufferCursor = new SimpleByteBufferCursor(operatorResultRightNested.getGeometry().getWkb().asReadOnlyByteBuffer());
        assertTrue(GeometryEngine.equals(reProjectedBufferedCONVEX_HULLRight, operatorImportFromWkb.execute(0, simpleByteBufferCursor, null).next(), SpatialReference.create(4088)));

        GeometryRequest operatorRequestSymDifference = GeometryRequest
                .newBuilder()
                .setLeftGeometryRequest(nestedLeft)
                .setRightGeometryRequest(nestedRight)
                .setOperator(OperatorType.SYMMETRIC_DIFFERENCE)
                .setOperationProj(spatialReferenceMerc)
                .setResultProj(spatialReferenceNAD)
                .build();


        Geometry rightFinal = GeometryEngineEx.project(reProjectedBufferedCONVEX_HULLRight, SpatialReferenceEx.create(4088), SpatialReferenceEx.create(3857));
        Geometry leftFinal = GeometryEngineEx.project(reProjectedBufferedCONVEX_HULL, SpatialReferenceEx.create(4088), SpatialReferenceEx.create(3857));
        Geometry difference = GeometryEngine.symmetricDifference(leftFinal, rightFinal, SpatialReference.create(3857));
        Geometry differenceProjected = GeometryEngineEx.project(difference, SpatialReferenceEx.create(3857), SpatialReferenceEx.create(4269));

        GeometryResponse operatorResult = stub.operate(operatorRequestSymDifference);
        simpleByteBufferCursor = new SimpleByteBufferCursor(operatorResult.getGeometry().getWkb().asReadOnlyByteBuffer());
        assertTrue(GeometryEngine.equals(differenceProjected, operatorImportFromWkb.execute(0, simpleByteBufferCursor, null).next(), SpatialReference.create(4269)));
        assertEquals(operatorResult.getGeometry().getProj().getEpsg(), 4269);

    }

    @Test
    public void testMultipointRoundTrip() {
        MultiPoint multiPoint = new MultiPoint();
        for (double longitude = -180; longitude < 180; longitude+=10.0) {
            for (double latitude = -80; latitude < 80; latitude+=10.0) {
                multiPoint.add(longitude, latitude);
            }
        }

        ProjectionData spatialReferenceWGS = ProjectionData.newBuilder().setEpsg(4326).build();
        ProjectionData spatialReferenceGall = ProjectionData.newBuilder().setEpsg(32632).build();

        GeometryData geometryData = GeometryData.newBuilder()
                .setWkt(GeometryEngine.geometryToWkt(multiPoint, 0))
                .setProj(spatialReferenceWGS)
                .build();

        GeometryRequest serviceProjectOp = GeometryRequest.newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.PROJECT)
                .setOperationProj(spatialReferenceGall)
                .build();

        GeometryRequest.Builder serviceReProjectOp = GeometryRequest.newBuilder()
                .setGeometryRequest(serviceProjectOp)
                .setOperator(OperatorType.PROJECT)
                .setOperationProj(spatialReferenceWGS)
                .setResultEncoding(Encoding.WKT);

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        // TODO check the results of this test. the envelope appears to be maxed to inifinty
        GeometryResponse operatorResult = stub.operate(serviceReProjectOp.build());
    }


    @Test
    public void testMultipointRoundTripLeft() {
        MultiPoint multiPoint = new MultiPoint();
        for (double longitude = -180; longitude < 180; longitude+=10.0) {
            for (double latitude = -80; latitude < 80; latitude+=10.0) {
                multiPoint.add(longitude, latitude);
            }
        }

        ProjectionData spatialReferenceWGS = ProjectionData.newBuilder().setEpsg(4326).build();
        ProjectionData spatialReferenceGall = ProjectionData.newBuilder().setEpsg(32632).build();

        GeometryData geometryData = GeometryData.newBuilder()
                .setWkt(GeometryEngine.geometryToWkt(multiPoint, 0))
                .setProj(spatialReferenceWGS)
                .build();

        GeometryRequest serviceProjectOp = GeometryRequest.newBuilder()
                .setLeftGeometry(geometryData)
                .setOperator(OperatorType.PROJECT)
                .setOperationProj(spatialReferenceGall)
                .build();

        GeometryRequest serviceReProjectOp = GeometryRequest.newBuilder()
                .setLeftGeometryRequest(serviceProjectOp)
                .setOperator(OperatorType.PROJECT)
                .setOperationProj(spatialReferenceWGS)
                .setResultEncoding(Encoding.WKT)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(serviceReProjectOp);

    }


//    @Test
//    public void testETRS() {
//        List<String> arrayDeque = new ArrayList<>();
//        for (double longitude = -180; longitude < 180; longitude+=15.0) {
//            for (double latitude = -90; latitude < 80; latitude+=15.0) {
//                Point point = new Point(longitude, latitude);
//                arrayDeque.add(OperatorExportToWkt.local().execute(0, point,null));
//            }
//        }
//
//        ProjectionData serviceSpatialReference = ProjectionData.newBuilder().setEpsg(4326).build();
//        ProjectionData outputSpatialReference = ProjectionData.newBuilder().setEpsg(3035).build();
//
//        GeometryData geometryData = GeometryData.newBuilder()
//                .setAllWkt(arrayDeque)
//                .setProj(serviceSpatialReference)
//                .build();
//
//        GeometryRequest serviceProjectOp = GeometryRequest.newBuilder()
//                .setLeftGeometry(geometryData)
//                .setOperator(OperatorType.PROJECT)
//                .setOperationProj(outputSpatialReference)
//                .build();
//
//        GeometryRequest serviceReProjectOp = GeometryRequest.newBuilder()
//                .setLeftGeometryRequest(serviceProjectOp)
//                .setOperator(OperatorType.PROJECT)
//                .setOperationProj(serviceSpatialReference)
//                .setResultEncoding(Encoding.wkt)
//                .build();
//
//        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
//        GeometryResponse operatorResult = stub.operate(serviceReProjectOp);
//        SimpleStringCursor simpleByteBufferCursor = new SimpleStringCursor(operatorResult.getGeometry().getWktList());
//        boolean bFoundEmpty = false;
//        while (simpleByteBufferCursor.hasNext()) {
//            String words = simpleByteBufferCursor.next();
//            if (words.equals("POINT EMPTY")) {
//                bFoundEmpty = true;
//            }
//        }
//        assertTrue(bFoundEmpty);
//    }

    @Test
    public void testProj4() {
        ProjectionData spatialReferenceData = ProjectionData
                .newBuilder()
                .setProj4("+init=epsg:4326")
                .build();
        ProjectionData spatialReferenceDataWKID = ProjectionData
                .newBuilder()
                .setEpsg(4326)
                .build();
        GeometryData geometryData = GeometryData
                .newBuilder()
                .setWkt("MULTILINESTRING ((-120 -45, -100 -55, -90 -63, 0 0, 1 1, 100 25, 170 45, 175 65))")
                .setProj(spatialReferenceData)
                .build();

        GeometryRequest operatorRequest = GeometryRequest
                .newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.PROJECT)
                .setResultEncoding(Encoding.WKB)
                .setResultProj(spatialReferenceDataWKID)
                .build();

        GeometryRequest operatorRequestEquals = GeometryRequest
                .newBuilder()
                .setLeftGeometryRequest(operatorRequest)
                .setRightGeometry(geometryData)
                .setOperator(OperatorType.CONTAINS)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(operatorRequestEquals);
        assertTrue(operatorResult.getSpatialRelationship());
    }

    @Test
    public void testCut() {
        GeometryData geometryDataPolygon = GeometryData.newBuilder()
                .setFeatureId("Barber")
                .setGeometryId(17)
                .setWkt("MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)), ((20 35, 45 20, 30 5, 10 10, 10 30, 20 35), (30 20, 20 25, 20 15, 30 20))) ")
                .build();
        GeometryData geometryDataCutter = GeometryData.newBuilder().setWkt("LINESTRING(0 0, 45 45)").build();
        GeometryRequest geometryRequest = GeometryRequest.newBuilder()
                .setLeftGeometry(geometryDataPolygon)
                .setRightGeometry(geometryDataCutter)
                .setOperator(OperatorType.CUT)
                .setResultEncoding(Encoding.WKT)
                .build();
        CountDownLatch done = new CountDownLatch(1);
        ClientResponseObserver<GeometryRequest, GeometryResponse> clientResponseObserver = new ClientResponseObserver<>() {
            int count = 0;

            @Override
            public void beforeStart(ClientCallStreamObserver<GeometryRequest> clientCallStreamObserver) {

            }

            @Override
            public void onNext(GeometryResponse geometryResponse) {
                assertEquals(geometryResponse.getGeometry().getGeometryId(), 18);
                assertEquals(geometryResponse.getGeometry().getFeatureId(), "Barber");
                count += 1;
            }

            @Override
            public void onError(Throwable throwable) {
                done.countDown();
            }

            @Override
            public void onCompleted() {
                assertEquals(2, count);
                done.countDown();
            }
        };

        GeometryServiceGrpc.GeometryServiceStub stub = GeometryServiceGrpc.newStub(inProcessChannel);
        stub.operateServerStream(geometryRequest, clientResponseObserver);


        try {
            done.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            fail("threw exception");
        }
    }

    @Test
    public void testGeodeticArea() {
        ProjectionData spatialReferenceData = ProjectionData.newBuilder().setEpsg(4326).build();
        Polygon polygon = new Polygon();
        polygon.startPath(-1, 1);
        polygon.lineTo(1, 1);
        polygon.lineTo(1, -1);
        polygon.lineTo(-1, -1);
        polygon.closeAllPaths();
        OperatorExportToWkb operatorExportToWkb = (OperatorExportToWkb)OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ExportToWkb);
        ByteBuffer buffer = operatorExportToWkb.execute(0, polygon,null);
        GeometryData geometryData = GeometryData
                .newBuilder()
                .setWkb(ByteString.copyFrom(buffer))
                .setProj(spatialReferenceData).build();
        GeometryRequest geometryRequest = GeometryRequest.newBuilder()
                .setOperator(OperatorType.GEODETIC_AREA)
                .setGeometry(geometryData)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse geometryResponse = stub.operate(geometryRequest);
//        assertEquals(geometryResponse.getMeasure(), 90000.0);

        GeometryRequest projectThenArea = GeometryRequest.newBuilder()
                .setOperator(OperatorType.GEODETIC_AREA)
                .setGeometryRequest(GeometryRequest.newBuilder()
                        .setOperator(OperatorType.PROJECT)
                        .setGeometry(geometryData)
                        .setResultProj(ProjectionData.newBuilder().setEpsg(32632)))
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub2 = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse geometryResponse2 = stub2.operate(projectThenArea);
        assertEquals(geometryResponse.getMeasure(), geometryResponse2.getMeasure(), 0.000001);
    }

    @Test
    public void testPointGeodetic() {
        // POINT (4322181.519435114 3212199.338618969) proj4: "+proj=laea +lat_0=31.593750 +lon_0=-94.718750 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"
        ProjectionData spatialReferenceData = ProjectionData.newBuilder().setProj4("+proj=laea +lat_0=31.593750 +lon_0=-94.718750 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs").build();
        GeometryData geometryData = GeometryData.newBuilder().setWkt("POINT (4322181.519435114 3212199.338618969)").setProj(spatialReferenceData).build();
        GeometryRequest geometryRequest = GeometryRequest
                .newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.GEODESIC_BUFFER)
                .setBufferParams(Params.Buffer.newBuilder().setDistance(200).build())
                .setResultEncoding(Encoding.WKT)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse geometryResponse = stub.operate(geometryRequest);

        GeometryRequest geometryRequest1 = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryResponse.getGeometry())
                .setRightGeometry(geometryData)
                .setOperator(OperatorType.INTERSECTS)
                .build();

        GeometryResponse geometryResponse1 = stub.operate(geometryRequest1);
        assertTrue(geometryResponse1.getSpatialRelationship());
    }

    @Test
    public void testAffineTransform() {
        double x = -116;
        double y = 46;
        Point pt = new Point(x, y);
        String wkt = pt.toString();
        GeometryRequest geometryRequest = GeometryRequest
                .newBuilder()
                .setGeometry(
                        GeometryData
                                .newBuilder()
                                .setWkt(wkt)
                                .build())
                .setAffineTransformParams(
                        Params.AffineTransform.newBuilder()
                                .setXOffset(1)
                                .setYOffset(2).build())
                .setOperator(OperatorType.AFFINE_TRANSFORM)
                .setResultEncoding(Encoding.WKT)
                .build();
        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse geometryResponse = stub.operate(geometryRequest);
        assertEquals("POINT (-115 48)", geometryResponse.getGeometry().getWkt());
    }

    @Test
    public void testIntersection() {
        String wkt = "POLYGON ((-116.25 46.37499999905793, -116.1875 46.37499999905793, -116.1875 46.31249999905781, -116.25 46.31249999905781, -116.25 46.37499999905793))";
        double x = -116.21874999999999;
        double y = 46.34374999905787;
        ProjectionData wgs84 = ProjectionData.newBuilder().setEpsg(4326).build();
        GeometryData geometryData = GeometryData.newBuilder().setWkt(wkt).setProj(wgs84).build();

        ProjectionData spatialReferenceDataLocal = ProjectionData.newBuilder().setCustom(ProjectionData.Custom.newBuilder().setLon0(x).setLat0(y).build()).build();
        GeometryRequest geometryRequest = GeometryRequest.newBuilder().setGeometry(geometryData).setResultEncoding(Encoding.WKT).setOperator(OperatorType.PROJECT).setResultProj(spatialReferenceDataLocal).build();
        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse operatorResult = stub.operate(geometryRequest);
        GeometryData localGeometry = operatorResult.getGeometry();

        SpatialReferenceEx spatialReference = SpatialReferenceEx.createUTM(x, y);
        ProjectionData spatialReferenceDataUtm = ProjectionData.newBuilder().setProj4(spatialReference.getProj4()).build();

        GeometryRequest geometryRequestIntersection = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryData)
                .setRightGeometry(localGeometry)
                .setOperator(OperatorType.INTERSECTION)
                .setResultProj(spatialReferenceDataUtm)
                .setResultEncoding(Encoding.WKT)
                .build();
        GeometryResponse operatorResult2 = stub.operate(geometryRequestIntersection);

        GeometryRequest geometryRequest1 = GeometryRequest.newBuilder()
                .setLeftGeometry(operatorResult2.getGeometry())
                .setRightGeometry(geometryData)
                .setOperationProj(wgs84)
                .setOperator(OperatorType.INTERSECTS)
                .build();
        GeometryResponse operatorResult3 = stub.operate(geometryRequest1);
        assertTrue(operatorResult3.getSpatialRelationship());
    }

    @Test
    public void testLength() {
        String wkt = "LINESTRING (0 0, 1 0)";
        ProjectionData wgs84 = ProjectionData.newBuilder().setEpsg(4326).build();
        GeometryData geometryData = GeometryData.newBuilder().setWkt(wkt).setProj(wgs84).build();
        GeometryRequest geometryRequest = GeometryRequest.newBuilder().setGeometry(geometryData).setOperator(OperatorType.GEODETIC_LENGTH).build();
        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse geometryResponse = stub.operate(geometryRequest);
        assertEquals(111319.4907932264, geometryResponse.getMeasure(), 8);

    }

    @Test
    public void testGeneralize95() {
        String wktInput = "MULTIPOLYGON (((5 2, 5.1308062584602858 2.0042821535227944, 5.2610523844401031 2.0171102772523808, 5.3901806440322559 2.0384294391935405, 5.5176380902050415 2.0681483474218645, 5.6428789306063223 2.1061397410097897, 5.7653668647301792 2.1522409349774274, 5.8845773804380022 2.2062545169346244, 5.9999999999999991 2.2679491924311233, 6.1111404660392035 2.3370607753949102, 6.2175228580174409 2.4132933194175301, 6.3186916302001368 2.4963203850420457, 6.414213562373094 2.5857864376269051, 6.5036796149579548 2.6813083697998623, 6.5867066805824699 2.7824771419825591, 6.6629392246050898 2.8888595339607956, 6.7320508075688767 3, 6.7937454830653765 3.1154226195619974, 6.847759065022573 3.2346331352698208, 6.8938602589902107 3.3571210693936768, 6.9318516525781364 3.4823619097949585, 6.9615705608064609 3.6098193559677436, 6.982889722747621 3.7389476155598969, 6.9957178464772074 3.8691937415397137, 7 4, 6.9992288413054364 4.0555341344807063, 6.6947736951561883 15.015919395853626, 11.905851560026523 31.393592685446105, 11.931851652578136 31.48236190979496, 11.961570560806461 31.609819355967744, 11.982889722747622 31.738947615559898, 11.995717846477207 31.869193741539714, 12 32, 11.995717846477206 32.130806258460282, 11.982889722747618 32.261052384440106, 11.961570560806459 32.390180644032256, 11.931851652578136 32.51763809020504, 11.893860258990211 32.642878930606322, 11.847759065022572 32.765366864730183, 11.793745483065376 32.884577380438003, 11.732050807568877 33, 11.66293922460509 33.111140466039203, 11.6 33.200000000000003, 5.5999999999999996 41.200000000000003, 5.5867066805824699 41.217522858017439, 5.5036796149579548 41.318691630200135, 5.4142135623730949 41.414213562373092, 5.3186916302001377 41.503679614957953, 5.2175228580174409 41.586706680582466, 5.1111404660392044 41.66293922460509, 5 41.732050807568875, 4.8845773804380022 41.793745483065379, 4.7653668647301792 41.847759065022572, 4.6428789306063232 41.893860258990209, 4.5176380902050415 41.931851652578139, 4.3901806440322568 41.961570560806464, 4.2610523844401031 41.982889722747622, 4.1308062584602858 41.995717846477206, 4 42, 3.8691937415397142 41.995717846477206, 3.7389476155598973 41.982889722747622, 3.6098193559677441 41.961570560806457, 3.482361909794959 41.931851652578132, 3.3571210693936773 41.893860258990209, 3.2346331352698212 41.847759065022572, 3.1154226195619983 41.793745483065379, 3.0000000000000009 41.732050807568875, 2.8888595339607965 41.66293922460509, 2.7824771419825591 41.586706680582466, 2.6813083697998632 41.503679614957953, 2.5857864376269055 41.414213562373092, 2.4963203850420457 41.318691630200135, 2.4132933194175301 41.217522858017439, 2.3370607753949102 41.111140466039203, 2.2679491924311233 41, 2.2062545169346235 40.884577380438003, 2.152240934977427 40.765366864730183, 2.1061397410097888 40.642878930606322, 2.0681483474218636 40.51763809020504, 2.0384294391935391 40.390180644032256, 2.017110277252379 40.261052384440106, 2.004282153522793 40.130806258460289, 2 40, 2.0007711586945636 39.944465865519291, 2.6774890785664391 15.582620750131777, 1.094148439973476 10.606407314553895, 1.0681483474218636 10.517638090205041, 1.0384294391935391 10.390180644032256, 1.017110277252379 10.261052384440102, 1.004282153522793 10.130806258460286, 1 10, 1.0042821535227944 9.8691937415397142, 1.0171102772523808 9.7389476155598977, 1.0384294391935405 9.6098193559677441, 1.0681483474218645 9.4823619097949585, 1.1061397410097897 9.3571210693936777, 1.1522409349774274 9.2346331352698208, 1.2062545169346244 9.115422619561997, 1.2679491924311233 9, 1.3370607753949102 8.8888595339607974, 1.4132933194175301 8.7824771419825591, 1.4963203850420457 8.6813083697998632, 1.5857864376269051 8.585786437626906, 1.6813083697998623 8.4963203850420452, 1.7824771419825591 8.4132933194175301, 1.8888595339607956 8.3370607753949102, 2 8.2679491924311233, 2.1154226195619974 8.2062545169346244, 2.2346331352698208 8.1522409349774279, 2.3571210693936768 8.1061397410097893, 2.4823619097949585 8.0681483474218645, 2.6098193559677436 8.0384294391935391, 2.7389476155598969 8.0171102772523781, 2.8691937415397137 8.0042821535227926, 2.888015599689953 8.0036659896852811, 3.0007711586945636 3.9444658655192932, 3.0042821535227944 3.8691937415397142, 3.0171102772523808 3.7389476155598973, 3.0384294391935405 3.6098193559677441, 3.0681483474218645 3.482361909794959, 3.1061397410097897 3.3571210693936773, 3.1522409349774274 3.2346331352698212, 3.2062545169346244 3.1154226195619983, 3.2679491924311233 3.0000000000000009, 3.3370607753949102 2.8888595339607965, 3.4132933194175301 2.7824771419825591, 3.4963203850420457 2.6813083697998632, 3.5857864376269051 2.5857864376269055, 3.6813083697998623 2.4963203850420457, 3.7824771419825591 2.4132933194175301, 3.8888595339607956 2.3370607753949102, 4 2.2679491924311233, 4.1154226195619978 2.2062545169346235, 4.2346331352698208 2.152240934977427, 4.3571210693936768 2.1061397410097888, 4.4823619097949585 2.0681483474218636, 4.6098193559677432 2.0384294391935391, 4.7389476155598969 2.017110277252379, 4.8691937415397142 2.004282153522793, 5 2), (6.3577402241893219 27.149124350660838, 6.1738780506195123 33.768162599173976, 7.7816940790703999 31.624407894572798, 6.3577402241893219 27.149124350660838)))\n" +
                "MULTIPOLYGON (((5 -2, 5.1308062584602858 -1.9957178464772056, 5.2610523844401031 -1.9828897227476183, 5.3901806440322559 -1.9615705608064609, 5.5176380902050415 -1.9318516525781355, 5.6428789306063223 -1.8938602589902089, 5.7427813527082074 -1.8569533817705199, 30.742781352708207 8.1430466182294818, 30.765366864730179 8.1522409349774279, 30.884577380438003 8.2062545169346244, 31 8.2679491924311233, 31.111140466039203 8.3370607753949102, 31.217522858017439 8.4132933194175301, 31.318691630200139 8.4963203850420452, 31.414213562373096 8.585786437626906, 31.503679614957953 8.6813083697998632, 31.58670668058247 8.7824771419825591, 31.66293922460509 8.8888595339607956, 31.732050807568879 9, 31.788854381999833 9.1055728090000834, 36.788854381999833 19.105572809000083, 36.793745483065379 19.115422619561997, 36.847759065022572 19.234633135269821, 36.893860258990209 19.357121069393678, 36.931851652578139 19.48236190979496, 36.961570560806464 19.609819355967744, 36.982889722747622 19.738947615559898, 36.995717846477206 19.869193741539714, 37 20, 36.995717846477206 20.130806258460286, 36.982889722747622 20.261052384440102, 36.961570560806457 20.390180644032256, 36.931851652578132 20.51763809020504, 36.893860258990209 20.642878930606322, 36.847759065022572 20.765366864730179, 36.793745483065379 20.884577380438003, 36.788854381999833 20.894427190999917, 31.788854381999833 30.894427190999917, 31.732050807568875 31, 31.66293922460509 31.111140466039203, 31.58670668058247 31.217522858017439, 31.503679614957953 31.318691630200135, 31.414213562373096 31.414213562373092, 31.318691630200139 31.503679614957953, 31.217522858017443 31.58670668058247, 31.111140466039203 31.66293922460509, 31 31.732050807568875, 30.884577380438003 31.793745483065376, 30.765366864730179 31.847759065022572, 30.642878930606322 31.893860258990209, 30.568176659382747 31.917596225416773, 3.5681766593827478 39.917596225416773, 3.5176380902050415 39.931851652578132, 3.3901806440322564 39.961570560806464, 3.2610523844401031 39.982889722747622, 3.1308062584602863 39.995717846477206, 3 40, 2.8691937415397142 39.995717846477206, 2.7389476155598973 39.982889722747615, 2.6098193559677441 39.961570560806464, 2.482361909794959 39.931851652578132, 2.3571210693936773 39.893860258990209, 2.2346331352698212 39.847759065022572, 2.1154226195619983 39.793745483065379, 2.0000000000000009 39.732050807568875, 1.8888595339607963 39.66293922460509, 1.7824771419825594 39.586706680582466, 1.681308369799863 39.503679614957953, 1.5857864376269055 39.414213562373092, 1.4963203850420457 39.318691630200135, 1.4132933194175301 39.217522858017446, 1.33706077539491 39.111140466039203, 1.267949192431123 39, 1.2062545169346237 38.884577380438003, 1.1522409349774267 38.765366864730183, 1.1061397410097888 38.642878930606322, 1.0681483474218634 38.51763809020504, 1.0384294391935391 38.390180644032256, 1.0171102772523792 38.261052384440106, 1.004282153522793 38.130806258460282, 1 38, 1.0042821535227944 37.869193741539718, 1.0171102772523806 37.738947615559894, 1.0384294391935405 37.609819355967744, 1.0681483474218647 37.48236190979496, 1.1061397410097897 37.357121069393678, 1.1522409349774274 37.234633135269817, 1.2062545169346244 37.115422619561997, 1.2679491924311235 37, 1.3370607753949102 36.888859533960797, 1.4132933194175301 36.782477141982561, 1.4963203850420457 36.681308369799865, 1.5857864376269053 36.585786437626908, 1.6813083697998625 36.496320385042047, 1.7824771419825589 36.413293319417534, 1.8888595339607956 36.33706077539491, 2 36.267949192431118, 2.1154226195619974 36.206254516934621, 2.2346331352698208 36.152240934977428, 2.3571210693936768 36.106139741009784, 2.4318233406172522 36.082403774583227, 28.599409577746219 28.329044889507976, 32.763932022500207 20, 28.551206229908889 11.574548414817356, 6.3845784837230717 2.7078973163430327, 6.3268887828044376 2.9386561200175683, 19.26118525018893 13.447771999767468, 19.318691630200139 13.496320385042045, 19.414213562373096 13.585786437626906, 19.503679614957953 13.681308369799863, 19.58670668058247 13.782477141982559, 19.66293922460509 13.888859533960796, 19.732050807568879 14, 19.793745483065376 14.115422619561997, 19.847759065022572 14.234633135269821, 19.893860258990212 14.357121069393678, 19.931851652578136 14.482361909794959, 19.961570560806461 14.609819355967744, 19.982889722747622 14.738947615559898, 19.995717846477206 14.869193741539714, 20 15, 19.995717846477206 15.130806258460286, 19.982889722747618 15.261052384440102, 19.961570560806461 15.390180644032256, 19.931851652578136 15.517638090205041, 19.893860258990209 15.642878930606322, 19.847759065022572 15.765366864730179, 19.793745483065376 15.884577380438001, 19.732050807568875 16, 19.66293922460509 16.111140466039203, 19.58670668058247 16.217522858017439, 19.503679614957953 16.318691630200135, 19.414213562373096 16.414213562373092, 19.318691630200139 16.503679614957953, 19.217522858017443 16.58670668058247, 19.111140466039203 16.66293922460509, 19 16.732050807568875, 18.884577380438003 16.793745483065376, 18.765366864730179 16.847759065022572, 18.642878930606322 16.893860258990209, 18.51763809020504 16.931851652578139, 18.390180644032256 16.961570560806461, 18.261052384440102 16.982889722747622, 18.130806258460286 16.995717846477206, 18 17, 17.869193741539714 16.995717846477206, 17.738947615559898 16.982889722747622, 17.609819355967744 16.961570560806457, 17.48236190979496 16.931851652578136, 17.357121069393678 16.893860258990209, 17.234633135269821 16.847759065022572, 17.115422619561997 16.793745483065376, 17 16.732050807568875, 16.888859533960797 16.66293922460509, 16.782477141982561 16.58670668058247, 16.73881474981107 16.552228000232532, 5.2559522566699819 7.2224022245553954, 1.9402850002906638 20.485071250072664, 1.9318516525781353 20.51763809020504, 1.8938602589902103 20.642878930606322, 1.8477590650225726 20.765366864730179, 1.7937454830653756 20.884577380438003, 1.7320508075688765 21, 1.6629392246050898 21.111140466039203, 1.5867066805824699 21.217522858017439, 1.5036796149579543 21.318691630200139, 1.4142135623730947 21.414213562373096, 1.3186916302001375 21.503679614957953, 1.2175228580174411 21.58670668058247, 1.1111404660392044 21.66293922460509, 0.99999999999999989 21.732050807568879, 0.88457738043800249 21.793745483065376, 0.76536686473017945 21.847759065022572, 0.64287893060632306 21.893860258990212, 0.51763809020504148 21.931851652578136, 0.3901806440322565 21.961570560806461, 0.26105238444010315 21.982889722747622, 0.13080625846028612 21.995717846477206, 0 22, -0.13080625846028585 21.995717846477206, -0.26105238444010281 21.982889722747618, -0.39018064403225611 21.961570560806461, -0.51763809020504103 21.931851652578136, -0.64287893060632262 21.893860258990209, -0.7653668647301789 21.847759065022572, -0.88457738043800183 21.793745483065376, -0.99999999999999922 21.732050807568875, -1.1111404660392037 21.66293922460509, -1.2175228580174406 21.58670668058247, -1.318691630200137 21.503679614957953, -1.4142135623730945 21.414213562373096, -1.5036796149579543 21.318691630200139, -1.5867066805824699 21.217522858017443, -1.66293922460509 21.111140466039203, -1.732050807568877 21, -1.7937454830653763 20.884577380438003, -1.8477590650225733 20.765366864730179, -1.8938602589902112 20.642878930606322, -1.9318516525781366 20.51763809020504, -1.9615705608064609 20.390180644032256, -1.9828897227476208 20.261052384440102, -1.995717846477207 20.130806258460286, -2 20, -1.9957178464772056 19.869193741539714, -1.9828897227476194 19.738947615559898, -1.9615705608064595 19.609819355967744, -1.9402850002906638 19.514928749927336, 1.4516369470040571 5.947240960748454, 1.3901806440322564 5.9615705608064609, 1.2610523844401031 5.982889722747621, 1.130806258460286 5.9957178464772074, 1 6, 0.86919374153971418 5.9957178464772056, 0.73894761555989719 5.9828897227476192, 0.60981935596774384 5.9615705608064591, 0.48236190979495897 5.9318516525781355, 0.35712106939367738 5.8938602589902107, 0.2346331352698211 5.8477590650225721, 0.11542261956199817 5.7937454830653756, 7.7715611723760958E-16 5.7320508075688767, -0.11114046603920369 5.6629392246050898, -0.21752285801744065 5.5867066805824699, -0.31869163020013702 5.5036796149579548, -0.41421356237309448 5.4142135623730949, -0.50367961495795432 5.3186916302001377, -0.58670668058246989 5.2175228580174409, -0.66293922460509003 5.1111404660392044, -0.73205080756887697 5, -0.79374548306537629 4.8845773804380022, -0.84775906502257325 4.7653668647301792, -0.89386025899021115 4.6428789306063232, -0.93185165257813662 4.5176380902050415, -0.96157056080646086 4.3901806440322568, -0.98288972274762076 4.2610523844401031, -0.99571784647720696 4.1308062584602858, -1 4, -0.99571784647720563 3.8691937415397142, -0.98288972274761943 3.7389476155598973, -0.96157056080645953 3.6098193559677441, -0.93185165257813529 3.482361909794959, -0.89386025899021027 3.3571210693936773, -0.84775906502257259 3.2346331352698212, -0.79374548306537562 3.1154226195619983, -0.78885438199983171 3.1055728090000843, 0.21114561800016829 1.1055728090000843, 0.26794919243112347 1.0000000000000009, 0.33706077539491019 0.88885953396079653, 0.41329331941753011 0.78247714198255913, 0.49632038504204568 0.6813083697998632, 0.5857864376269053 0.58578643762690552, 0.68130836979986253 0.49632038504204568, 0.78247714198255891 0.41329331941753011, 0.88885953396079564 0.33706077539491019, 1 0.26794919243112325, 1.1154226195619974 0.20625451693462349, 1.2346331352698205 0.15224093497742697, 1.3571210693936768 0.10613974100978885, 1.4823619097949585 0.068148347421863598, 1.6098193559677436 0.038429439193539139, 1.7389476155598969 0.017110277252379014, 1.8691937415397137 0.0042821535227930418, 2 0, 2.1308062584602858 0.0042821535227943741, 2.2610523844401027 0.01711027725238079, 2.3901806440322559 0.038429439193540471, 2.517638090205041 0.068148347421864486, 2.6428789306063227 0.10613974100978973, 2.7653668647301788 0.15224093497742741, 2.8845773804380017 0.20625451693462438, 2.8866117144161265 0.20734189110017509, 3.0597149997093362 -0.48507125007266438, 3.0681483474218645 -0.5176380902050397, 3.1061397410097897 -0.64287893060632229, 3.1522409349774274 -0.76536686473017923, 3.2062545169346244 -0.88457738043800305, 3.2679491924311233 -1, 3.3370607753949102 -1.1111404660392026, 3.4132933194175301 -1.2175228580174391, 3.4963203850420457 -1.3186916302001386, 3.5857864376269051 -1.4142135623730958, 3.6813083697998623 -1.503679614957953, 3.7824771419825591 -1.5867066805824699, 3.8888595339607956 -1.6629392246050898, 4 -1.7320508075688785, 4.1154226195619978 -1.7937454830653756, 4.2346331352698208 -1.8477590650225721, 4.3571210693936768 -1.8938602589902125, 4.4823619097949585 -1.9318516525781355, 4.6098193559677432 -1.9615705608064609, 4.7389476155598969 -1.9828897227476219, 4.8691937415397142 -1.9957178464772056, 5 -2)))";
        Geometry poly = GeometryEngine.geometryFromWkt(wktInput, 0, Geometry.Type.Unknown);
        OperatorFactoryLocalEx engine = OperatorFactoryLocalEx.getInstance();
        OperatorGeneralizeByArea op = (OperatorGeneralizeByArea) engine.getOperator(OperatorEx.Type.GeneralizeByArea);
        Geometry geom = op.execute(poly, 5, true, GeneralizeType.Neither, SpatialReferenceEx.create(4326),null);
        int res = ((MultiVertexGeometry)geom).getPointCount();
        int original = ((MultiVertexGeometry)poly).getPointCount();
        assertTrue(res/(double)original < 0.95);

        String wkt = "MULTIPOLYGON (((1 1.003617493421433, 1 1.003617493421433, 0.9997649519467132 1.003609747854938, 0.999530910495817 1.003586544337746, 0.9992988779321902 1.003547982273469, 0.9990698479248393 1.003494226857979, 0.9988448012666893 1.003425508366702, 0.9986247016712343 1.003342121161815, 0.9984104916443466 1.003244422424226, 0.9982030884490086 1.003132830616434, 0.9980033801801327 1.00300782368356, 0.9978122219659593 1.002869937000829, 0.9976304323118294 1.0027197610768, 0.9974587896014011 1.002557939022455, 0.9972980287696582 1.002385163797099, 0.9971488381613458 1.002202175242747, 0.9970118565877482 1.002009756919397, 0.9968876705940221 1.001808732754287, 0.9967768119485581 1.001599963518935, 0.9966797553651014 1.00138434314843, 0.9965969164675547 1.001162794918189, 0.9965286500065195 1.000936267494034, 0.996475248335696 1.000705730872183, 0.9964369401552167 1.000472172226337, 0.9964138895278762 1.000236591679669, 0.9964061951730092 0.9999999980200158, 0.9964138900413748 0.9997634043769587, 0.9964369411734271 0.9995278238797968, 0.9964752498411954 0.9992942653155188, 0.996528651973547 0.9990637288059012, 0.9965969188624519 0.9988372015227254, 0.9966797581468895 0.9986156534597946, 0.9967768150696379 0.9984000332800704, 0.9968876740009895 0.998191264255702, 0.9970118602223078 0.9979902403181697, 0.9971488419613082 0.9977978222350962, 0.9972980326700053 0.9976148339296087, 0.9974587935353963 0.997442058957447, 0.9976304362121615 0.9972802371562934, 0.9978122257658933 0.9971300614811209, 0.9980033838146521 0.996992175038654, 0.9982030918559268 0.9968671683333408, 0.9984104947653715 0.996755576736514, 0.9986247044529655 0.9966578781896839, 0.9988448036615318 0.9965744911520876, 0.9990698498918178 0.9965057728017697, 0.9992988794376493 0.9964520174984963, 0.9995309115139988 0.996413455515775, 0.999764952460197 0.9963902520480808, 1 0.9963825064981803, 1.000235047539803 0.9963902520480808, 1.000469088486001 0.9964134555157748, 1.000701120562351 0.9964520174984965, 1.000930150108182 0.9965057728017697, 1.001155196338468 0.9965744911520876, 1.001375295547035 0.9966578781896839, 1.001589505234629 0.996755576736514, 1.001796908144073 0.9968671683333408, 1.001996616185348 0.996992175038654, 1.002187774234107 0.9971300614811212, 1.002369563787838 0.9972802371562934, 1.002541206464604 0.997442058957447, 1.002701967329995 0.9976148339296087, 1.002851158038692 0.9977978222350962, 1.002988139777692 0.99799024031817, 1.00311232599901 0.998191264255702, 1.003223184930362 0.9984000332800704, 1.00332024185311 0.9986156534597949, 1.003403081137548 0.9988372015227254, 1.003471348026453 0.9990637288059014, 1.003524750158804 0.9992942653155188, 1.003563058826573 0.9995278238797968, 1.003586109958625 0.9997634043769587, 1.003593804826991 0.9999999980200158, 1.003586110472124 1.00023659167967, 1.003563059844783 1.000472172226337, 1.003524751664304 1.000705730872183, 1.003471349993481 1.000936267494034, 1.003403083532445 1.001162794918189, 1.003320244634899 1.00138434314843, 1.003223188051442 1.001599963518935, 1.003112329405978 1.001808732754287, 1.002988143412252 1.002009756919397, 1.002851161838654 1.002202175242747, 1.002701971230342 1.002385163797099, 1.002541210398599 1.002557939022455, 1.002369567688171 1.0027197610768, 1.002187778034041 1.002869937000829, 1.001996619819867 1.00300782368356, 1.001796911550991 1.003132830616434, 1.001589508355653 1.003244422424226, 1.001375298328766 1.003342121161815, 1.001155198733311 1.003425508366702, 1.000930152075161 1.003494226857979, 1.00070112206781 1.003547982273469, 1.000469089504183 1.003586544337747, 1.000235048053287 1.003609747854938, 1 1.003617493421433)))";
        ProjectionData wgs84 = ProjectionData.newBuilder().setEpsg(4326).build();
        GeometryData geometryData = GeometryData.newBuilder().setWkt(wkt).setProj(wgs84).build();
        GeometryRequest geometryRequest = GeometryRequest.newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.GENERALIZE_BY_AREA)
                .setResultEncoding(Encoding.WKT)
                .setGeneralizeByAreaParams(
                        Params.GeneralizeByArea
                                .newBuilder()
                                .setPercentReduction(95)).build();
        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse geometryResponse = stub.operate(geometryRequest);
        Polygon polyResult = (Polygon) GeometryEngine.geometryFromWkt(geometryResponse.getGeometry().getWkt(), 0, Geometry.Type.Unknown);
        assertEquals(polyResult.getPointCount(), 4);
    }

    @Test
    public void testInverse() {
        SpatialReference sr = SpatialReference.create(4326);
        ProjectionData spatialReferenceData = ProjectionData.newBuilder().setEpsg(4326).build();
        GeometryData geometryData1 = GeometryData.newBuilder().setWkt("POINT (0 0)").setProj(spatialReferenceData).build();
        GeometryData geometryData2 = GeometryData.newBuilder().setWkt("POINT (-1 0)").setProj(spatialReferenceData).build();
        GeometryRequest geometryRequest = GeometryRequest.newBuilder()
                .setLeftGeometry(geometryData1)
                .setRightGeometry(geometryData2)
                .setOperator(OperatorType.GEODETIC_INVERSE)
                .build();
        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse geometryResponse = stub.operate(geometryRequest);

        assertEquals(111319.4907932264, geometryResponse.getGeodeticInverse().getDistance(), 0);
        assertEquals(-Math.PI / 2, geometryResponse.getGeodeticInverse().getAz12(), 0);
        assertEquals(Math.PI / 2, geometryResponse.getGeodeticInverse().getAz21(), 0);
    }
}
