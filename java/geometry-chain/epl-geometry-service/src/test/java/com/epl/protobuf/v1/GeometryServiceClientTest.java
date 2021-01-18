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

import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.util.MutableHandlerRegistry;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Random;

import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link GeometryServiceClient}.
 * For demonstrating how to write gRPC unit test only.
 * Not intended to provide a high code coverage or to test every major usecase.
 */
@RunWith(JUnit4.class)
public class GeometryServiceClientTest {
    private final MutableHandlerRegistry serviceRegistry = new MutableHandlerRegistry();
    private final GeometryServiceClient.TestHelper testHelper = mock(GeometryServiceClient.TestHelper.class);
    private final Random noRandomness =
            new Random() {
                int index;
                boolean isForSleep;

                /**
                 * Returns a number deterministically. If the random number is for sleep time, then return
                 * -500 so that {@code Thread.sleep(random.nextInt(1000) + 500)} sleeps 0 ms. Otherwise, it
                 * is for list index, then return incrementally (and cyclically).
                 */
                @Override
                public int nextInt(int bound) {
                    int retVal = isForSleep ? -500 : (index++ % bound);
                    isForSleep = !isForSleep;
                    return retVal;
                }
            };
    private Server fakeServer;
    private GeometryServiceClient client;

    @Before
    public void setUp() throws Exception {
        String uniqueServerName = "fake server for " + getClass();

        // use a mutable service registry for later registering the service impl for each test case.
        fakeServer = InProcessServerBuilder
                .forName(uniqueServerName)
                .fallbackHandlerRegistry(serviceRegistry)
                .directExecutor()
                .build().start();
        client = new GeometryServiceClient(InProcessChannelBuilder.forName(uniqueServerName).directExecutor());
        client.setTestHelper(testHelper);
    }

    @After
    public void tearDown() throws Exception {
        client.shutdown();
        fakeServer.shutdownNow();
    }

    /**
     * Example for testing blocking unary call.
     */
    @Test
    public void testBlockingUnaryCall() {
    }

    /**
     * Example for testing blocking server-streaming.
     */
    @Test
    public void testBlockingServerStreaming() {
    }

    /**
     * Example for testing async client-streaming.
     */
    @Test
    public void testAsyncClientStreaming() throws Exception {
    }

    /**
     * Example for testing bi-directional call.
     */
    @Test
    public void testBidirectionalStreaming() throws Exception {
    }
}
