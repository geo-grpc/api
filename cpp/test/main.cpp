//
// Created by David Raleigh on 3/7/18.
//

/*
 *
 * Copyright 2015 gRPC authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

#include <iostream>
#include <string>

#include <grpcpp/grpcpp.h>
#include <grpc/support/log.h>
#include <thread>

#include "epl/protobuf/v1/stac.pb.h"
#include "epl/protobuf/v1/geometry.pb.h"
#include "epl/protobuf/v1/geometry_service.pb.h"
#include "epl/protobuf/v1/geometry_service.grpc.pb.h"
#include "epl/protobuf/v1/stac_service.pb.h"
#include "epl/protobuf/v1/stac_service.grpc.pb.h"
#include "gtest/gtest.h"
#include "helper.h"
#include <grpc/grpc.h>
#include <grpcpp/grpcpp.h>
#include <grpc++/channel.h>
#include <grpc++/client_context.h>
#include <grpc++/create_channel.h>
#include <grpc++/security/credentials.h>
#include <cstdlib>
#include <memory>
//#include <regex>

using namespace epl::protobuf::v1;

using grpc::Channel;
using grpc::ClientReader;
using grpc::ClientAsyncResponseReader;
using grpc::ClientContext;
using grpc::CompletionQueue;
using grpc::Status;
using grpc::ServerBuilderOption;

namespace {
    class STACClientTests : public ::testing::Test {
    protected:
        STACClientTests() {


        }

        virtual ~STACClientTests() {
        }
    };

    TEST_F(STACClientTests, STAC_REQUEST_ID) {
        epl::protobuf::v1::StacRequest stacRequest;
        stacRequest.set_id("LC80330342017072LGN00");
        EXPECT_STRCASEEQ("LC80330342017072LGN00", stacRequest.id().c_str());
    }

    TEST_F(STACClientTests, STAC_REQUEST_ID_2) {
        epl::protobuf::v1::StacRequest stacRequest;
        stacRequest.set_id("LC80330342017072LGN00");
        EXPECT_STRCASEEQ("LC80330342017072LGN00", stacRequest.id().c_str());
    }
};

namespace {
    class GeometryClientTests : public ::testing::Test {
    protected:
        GeometryClientTests() {
            if (const char* env_p = std::getenv("GEOMETRY_SERVICE_HOST")) {
                m_channel = grpc::CreateChannel(env_p, grpc::InsecureChannelCredentials());
            } else {
                m_channel = grpc::CreateChannel("0.0.0.0:8980", grpc::InsecureChannelCredentials());
            }
        }

        virtual ~GeometryClientTests() {
        }

        std::shared_ptr<grpc::Channel> m_channel;
    };

    TEST_F(GeometryClientTests, TEST_CUT_1){
        // Fails in Linux and I have no idea why.
        std::unique_ptr<GeometryService::Stub> geometry_stub = GeometryService::NewStub(m_channel);

        GeometryData serviceGeometry;
        const char* wkt = "MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)), ((20 35, 45 20, 30 5, 10 10, 10 30, 20 35), (30 20, 20 25, 20 15, 30 20)))";
        serviceGeometry.set_wkt(wkt);

        GeometryData cutterGeometry;
        const char* wkt_cutter = "LINESTRING(0 0, 45 45)";
        cutterGeometry.set_wkt(wkt_cutter);

        GeometryRequest operatorRequest;
        operatorRequest.mutable_left_geometry()->CopyFrom(serviceGeometry);
        operatorRequest.mutable_right_geometry()->CopyFrom(cutterGeometry);
        operatorRequest.set_operator_(OperatorType::CUT);
        operatorRequest.set_result_encoding(Encoding::WKT);
        auto* clientContext = new grpc::ClientContext();
        auto* operatorResult = new GeometryResponse();

        std::cout << "Looking for features between 40, -75 and 42, -73" << std::endl;

        std::unique_ptr<ClientReader<GeometryResponse>> reader(geometry_stub->OperateServerStream(clientContext, operatorRequest));
        int count = 0;
        const char* expected1 = "MULTIPOLYGON (((35.625 35.625, 40 40, 20 45, 35.625 35.625)), ((10 10, 20 20, 20 25, 23.333333333333336 23.333333333333336, 29.375 29.375, 20 35, 10 30, 10 10)))";
        const char* expected2 = "MULTIPOLYGON (((40 40, 35.625 35.625, 45 30, 40 40)), ((30 5, 45 20, 29.375 29.375, 23.333333333333336 23.333333333333336, 30 20, 20 15, 20 20, 10 10, 30 5)))";
        reader->WaitForInitialMetadata();
        while (reader->Read(operatorResult)) {
            std::string result = operatorResult->geometry().wkt();
            if (count == 0) {
                EXPECT_STREQ(expected1 , result.c_str());
            } else {
                EXPECT_STREQ(expected2 , result.c_str());
            }

            count ++;
        }
        Status status = reader->Finish();
        if (status.ok()) {
            std::cout << "ListFeatures rpc succeeded." << std::endl;
        } else {
            EXPECT_NO_THROW(status.error_message());
            std::cout << status.error_message() << std::endl;
        }

        EXPECT_EQ(2, count);
    }

    TEST_F(GeometryClientTests, TEST_2) {
        std::unique_ptr<GeometryService::Stub> geometry_stub = GeometryService::NewStub(m_channel);

        ProjectionData spatialReferenceWGS84;
        spatialReferenceWGS84.set_epsg(4326);

        auto* spatialReferenceCalif = new ProjectionData();
        spatialReferenceCalif->set_epsg(32632);

        // allocating this here means it is not copied in the set_allocated method, but a strange rule of control is given to the
        // operator request message
        auto* serviceGeometry = new GeometryData();
        const char* wkt = "MULTILINESTRING ((500000       0, 400000  100000, 600000 -100000))";
        serviceGeometry->set_wkt(wkt);
        serviceGeometry->set_allocated_proj(spatialReferenceCalif);

        auto* operatorRequest = new GeometryRequest();
        operatorRequest->mutable_result_proj()->CopyFrom(spatialReferenceWGS84);
        operatorRequest->set_allocated_left_geometry(serviceGeometry);
        operatorRequest->set_allocated_operation_proj(spatialReferenceCalif);
        operatorRequest->set_operator_(OperatorType::PROJECT);
        operatorRequest->set_result_encoding(Encoding::WKT);

        auto* clientContext = new grpc::ClientContext();
        auto* operatorResult = new GeometryResponse();

        geometry_stub->Operate(clientContext, *operatorRequest, operatorResult);

        std::string result = operatorResult->geometry().wkt();
        std::string expected("MULTILINESTRING ((9 0, 8.101251062924646 0.904618578893133, 9.898748937075354 -0.904618578893133))");
        std::string expected_2("MULTILINESTRING ((9 0, 8.101251062924646 0.9046185788931331, 9.898748937075354 -0.9046185788931331))");
        if (strncmp(result.c_str(), expected.c_str(), expected.size()) != 0 &&
            strncmp(result.c_str(), expected_2.c_str(), expected_2.size()) != 0) {
            if (strncmp(result.c_str(), expected.c_str(), expected.size()) != 0)
                EXPECT_STREQ(expected.c_str() , result.c_str());
            else
                EXPECT_STREQ(expected_2.c_str() , result.c_str());
        }
    }

    TEST_F(GeometryClientTests, TEST_CRAZY_NESTING) {
        std::unique_ptr<GeometryService::Stub> geometry_stub = GeometryService::NewStub(m_channel);

        /*
         * Polyline polyline = new Polyline();
        polyline.startPath(-120, -45);
        polyline.lineTo(-100, -55);
        polyline.lineTo(-90, -63);
        polyline.lineTo(0, 0);
        polyline.lineTo(1, 1);
        polyline.lineTo(100, 25);
        polyline.lineTo(170, 45);
        polyline.lineTo(175, 65);
        */
        const char* polyline = "MULTILINESTRING ((-120 -45, -100 -55, -90 -63, 0 0, 1 1, 100 25, 170 45, 175 65))";

        /*OperatorExportToWkb op = OperatorExportToWkb.local();

       ProjectionData spatialReferenceNAD = ProjectionData.newBuilder().setWkid(4269).build();
       ProjectionData spatialReferenceMerc = ProjectionData.newBuilder().setWkid(3857).build();
       ProjectionData spatialReferenceWGS = ProjectionData.newBuilder().setWkid(4326).build();
       ProjectionData spatialReferenceGall = ProjectionData.newBuilder().setWkid(54016).build();
       //TODO why does esri shape fail
         */
        ProjectionData spatialReferenceNAD;
        spatialReferenceNAD.set_epsg(4269);
        ProjectionData spatialReferenceMerc;
        spatialReferenceMerc.set_epsg(3857);
        ProjectionData spatialReferenceWGS;
        spatialReferenceWGS.set_epsg(4326);
        ProjectionData spatialReferenceGall;
        spatialReferenceGall.set_epsg(4088);
/*
        GeometryBagData geometryBagLeft = GeometryBagData.newBuilder()
                .setGeometryEncodingType(GeometryEncodingType.wkb)
                .addGeometryBinaries(ByteString.copyFrom(op.execute(0, polyline, null)))
                .setSpatialReference(spatialReferenceNAD)
                .build();
                */
        auto* geometryBagLeft = new GeometryData();
        geometryBagLeft->set_allocated_proj(&spatialReferenceNAD);
        geometryBagLeft->set_wkt(polyline);

        Params::Buffer bufferParams;
        bufferParams.set_distance(.5);

        auto* serviceOpLeft = new GeometryRequest();
        serviceOpLeft->set_allocated_left_geometry(geometryBagLeft);
        serviceOpLeft->set_operator_(OperatorType::BUFFER);
        serviceOpLeft->set_allocated_buffer_params(&bufferParams);
        serviceOpLeft->mutable_result_proj()->CopyFrom(spatialReferenceWGS);

//        serviceOpLeft->set_result_encoding(Encoding::WKT);
//        auto* clientContext = new grpc::ClientContext();
//        auto* operatorResult = new GeometryResponse();
//        geometry_stub->Operate(clientContext, *serviceOpLeft, operatorResult);
//        auto val = operatorResult->geometry().wkt();
//        std::cout << val << std::endl;

//        MULTIPOLYGON (((-90 -63.50000000000001, -89.96729843538493 -63.498929461619305, -89.93473690388998 -63.4957224306869, -89.90245483899196 -63.490392640201605, -89.87059047744874 -63.482962913144554, -89.83928026734844 -63.473465064747565, -89.80865828381746 -63.461939766255654, -89.77885565489049 -63.44843637076632, -89.75 -63.43301270189221, -89.72221488349021 -63.41573480615128, -89.71326882781835 -63.409615960259515, 0.28673117218167476 -0.4096159602595151, 0.30438071450436155 -0.3966766701456237, 0.32967290755004797 -0.37591990373948647, 0.3535533905932766 -0.3535533905932766, 1.254263877312269 0.5471570961257157, 100.11780001281075 24.51407494715565, 100.12940952255127 24.517037086855467, 100.13736056394869 24.519238026179586, 170.13736056394868 44.51923802617959, 170.1607197326516 44.52653493525244, 170.19134171618254 44.53806023374436, 170.2211443451095 44.551563629233655, 170.25 44.566987298107776, 170.2777851165098 44.58426519384873, 170.30438071450436 44.60332332985439, 170.32967290755005 44.62408009626051, 170.35355339059328 44.64644660940673, 170.3759199037395 44.670327092449966, 170.3966766701456 44.69561928549564, 170.41573480615128 44.722214883490196, 170.4330127018922 44.75, 170.44843637076633 44.7788556548905, 170.46193976625565 44.80865828381745, 170.47346506474756 44.839280267348414, 170.48296291314455 44.870590477448744, 170.48507125007268 44.87873218748183, 175.48507125007268 64.87873218748184, 175.4903926402016 64.90245483899193, 175.4957224306869 64.93473690388997, 175.4989294616193 64.96729843538493, 175.5 65, 175.4989294616193 65.03270156461507, 175.4957224306869 65.06526309611003, 175.4903926402016 65.09754516100807, 175.48296291314455 65.12940952255126, 175.47346506474756 65.16071973265159, 175.46193976625565 65.19134171618255, 175.44843637076633 65.2211443451095, 175.4330127018922 65.25, 175.41573480615128 65.2777851165098, 175.3966766701456 65.30438071450436, 175.37591990373951 65.32967290755003, 175.35355339059328 65.35355339059328, 175.32967290755005 65.37591990373949, 175.30438071450436 65.39667667014562, 175.2777851165098 65.41573480615128, 175.25000000000003 65.43301270189222, 175.2211443451095 65.44843637076634, 175.19134171618254 65.46193976625564, 175.1607197326516 65.47346506474756, 175.12940952255127 65.48296291314453, 175.0975451610081 65.49039264020162, 175.06526309611002 65.49572243068691, 175.03270156461508 65.4989294616193, 175 65.5, 174.96729843538492 65.4989294616193, 174.93473690388998 65.49572243068691, 174.90245483899193 65.49039264020162, 174.87059047744873 65.48296291314453, 174.8392802673484 65.47346506474756, 174.80865828381746 65.46193976625564, 174.7788556548905 65.44843637076634, 174.75 65.43301270189222, 174.7222148834902 65.41573480615128, 174.69561928549564 65.39667667014562, 174.67032709244998 65.37591990373949, 174.64644660940675 65.35355339059328, 174.6240800962605 65.32967290755003, 174.6033233298544 65.30438071450436, 174.58426519384872 65.2777851165098, 174.5669872981078 65.25, 174.55156362923367 65.2211443451095, 174.53806023374435 65.19134171618255, 174.52653493525244 65.16071973265159, 174.51703708685545 65.12940952255126, 174.51492874992732 65.12126781251817, 169.58496866365303 45.4014274674209, 99.87236747513181 25.483541413557695, 0.8821999871892388 1.485925052844351, 0.8705904774487294 1.482962913144533, 0.8392802673484141 1.4734650647475505, 0.808658283817465 1.4619397662556468, 0.7788556548904921 1.4484363707663448, 0.7500000000000001 1.433012701892217, 0.72221488349021 1.4157348061512707, 0.6956192854956385 1.3966766701456166, 0.670327092449952 1.3759199037394865, 0.6464466094067234 1.3535533905932766, -0.3225966679995622 0.38451011318700523, -89.98001023802891 -62.375679385833585, -99.68765247622281 -54.60956559527848, -99.69561928549565 -54.603323329854376, -99.72221488349021 -54.58426519384872, -99.75 -54.566987298107776, -99.77639320225 -54.55278640450004, -119.77639320225002 -44.55278640450004, -119.77885565489049 -44.551563629233655, -119.80865828381748 -44.53806023374436, -119.83928026734844 -44.52653493525244, -119.87059047744873 -44.517037086855474, -119.90245483899196 -44.50960735979838, -119.93473690389 -44.50427756931309, -119.96729843538493 -44.5010705383807, -119.99999999999999 -44.5, -120.03270156461508 -44.5010705383807, -120.06526309611002 -44.50427756931309, -120.09754516100804 -44.50960735979838, -120.12940952255128 -44.517037086855474, -120.16071973265156 -44.52653493525244, -120.19134171618254 -44.53806023374436, -120.22114434510952 -44.551563629233655, -120.25 -44.566987298107776, -120.27778511650979 -44.58426519384872, -120.30438071450438 -44.603323329854376, -120.32967290755005 -44.62408009626051, -120.35355339059328 -44.64644660940673, -120.37591990373949 -44.670327092449966, -120.39667667014562 -44.69561928549564, -120.41573480615125 -44.722214883490196, -120.4330127018922 -44.75, -120.44843637076633 -44.778855654890506, -120.46193976625561 -44.80865828381745, -120.47346506474754 -44.839280267348414, -120.48296291314455 -44.870590477448744, -120.49039264020159 -44.90245483899193, -120.4957224306869 -44.93473690388997, -120.4989294616193 -44.96729843538493, -120.50000000000001 -45, -120.4989294616193 -45.03270156461507, -120.4957224306869 -45.06526309611003, -120.49039264020159 -45.09754516100807, -120.48296291314455 -45.129409522551256, -120.47346506474754 -45.160719732651586, -120.46193976625561 -45.19134171618255, -120.44843637076633 -45.221144345109494, -120.4330127018922 -45.25, -120.41573480615125 -45.27778511650981, -120.39667667014562 -45.30438071450436, -120.37591990373949 -45.32967290755004, -120.35355339059328 -45.35355339059328, -120.32967290755005 -45.37591990373949, -120.30438071450438 -45.396676670145624, -120.27778511650979 -45.41573480615128, -120.25 -45.433012701892224, -120.22360679775 -45.44721359549996, -100.27098476456104 -55.42352461209444, -90.31234752377719 -63.390434404721496, -90.30438071450436 -63.3966766701456, -90.27778511650979 -63.41573480615128, -90.25 -63.43301270189221, -90.22114434510951 -63.44843637076632, -90.19134171618254 -63.461939766255654, -90.16071973265156 -63.473465064747565, -90.12940952255127 -63.482962913144554, -90.09754516100804 -63.490392640201605, -90.06526309611002 -63.4957224306869, -90.03270156461508 -63.498929461619305, -90 -63.50000000000001)))
//        MULTIPOLYGON (((-90 -63.50000000000001, -89.96729843538493 -63.498929461619305, -89.93473690388998 -63.4957224306869, -89.90245483899196 -63.490392640201605, -89.87059047744874 -63.482962913144554, -89.83928026734844 -63.473465064747565, -89.80865828381746 -63.461939766255654, -89.77885565489049 -63.44843637076632, -89.75 -63.43301270189221, -89.72221488349021 -63.41573480615128, -89.71326882781835 -63.409615960259515, 0.28673117218167476 -0.4096159602595151, 0.30438071450436155 -0.3966766701456237, 0.32967290755004797 -0.37591990373948647, 0.3535533905932766 -0.3535533905932766, 1.254263877312269 0.5471570961257157, 100.11780001281075 24.51407494715565, 100.12940952255127 24.517037086855467, 100.13736056394869 24.519238026179586, 170.13736056394868 44.51923802617959, 170.1607197326516 44.52653493525244, 170.19134171618254 44.53806023374436, 170.2211443451095 44.551563629233655, 170.25 44.566987298107776, 170.2777851165098 44.58426519384873, 170.30438071450436 44.60332332985439, 170.32967290755005 44.62408009626051, 170.35355339059328 44.64644660940673, 170.3759199037395 44.670327092449966, 170.3966766701456 44.69561928549564, 170.41573480615128 44.722214883490196, 170.4330127018922 44.75, 170.44843637076633 44.7788556548905, 170.46193976625565 44.80865828381745, 170.47346506474756 44.839280267348414, 170.48296291314455 44.870590477448744, 170.48507125007268 44.87873218748183, 175.48507125007268 64.87873218748184, 175.4903926402016 64.90245483899193, 175.4957224306869 64.93473690388997, 175.4989294616193 64.96729843538493, 175.5 65, 175.4989294616193 65.03270156461507, 175.4957224306869 65.06526309611003, 175.4903926402016 65.09754516100807, 175.48296291314455 65.12940952255126, 175.47346506474756 65.16071973265159, 175.46193976625565 65.19134171618255, 175.44843637076633 65.2211443451095, 175.4330127018922 65.25, 175.41573480615128 65.2777851165098, 175.3966766701456 65.30438071450436, 175.37591990373951 65.32967290755003, 175.35355339059328 65.35355339059328, 175.32967290755005 65.37591990373949, 175.30438071450436 65.39667667014562, 175.2777851165098 65.41573480615128, 175.25000000000003 65.43301270189222, 175.2211443451095 65.44843637076634, 175.19134171618254 65.46193976625564, 175.1607197326516 65.47346506474756, 175.12940952255127 65.48296291314453, 175.0975451610081 65.49039264020162, 175.06526309611002 65.49572243068691, 175.03270156461508 65.4989294616193, 175 65.5, 174.96729843538492 65.4989294616193, 174.93473690388998 65.49572243068691, 174.90245483899193 65.49039264020162, 174.87059047744873 65.48296291314453, 174.8392802673484 65.47346506474756, 174.80865828381746 65.46193976625564, 174.7788556548905 65.44843637076634, 174.75 65.43301270189222, 174.7222148834902 65.41573480615128, 174.69561928549564 65.39667667014562, 174.67032709244998 65.37591990373949, 174.64644660940675 65.35355339059328, 174.6240800962605 65.32967290755003, 174.6033233298544 65.30438071450436, 174.58426519384872 65.2777851165098, 174.5669872981078 65.25, 174.55156362923367 65.2211443451095, 174.53806023374435 65.19134171618255, 174.52653493525244 65.16071973265159, 174.51703708685545 65.12940952255126, 174.51492874992732 65.12126781251817, 169.58496866365303 45.4014274674209, 99.87236747513181 25.483541413557695, 0.8821999871892388 1.485925052844351, 0.8705904774487294 1.482962913144533, 0.8392802673484141 1.4734650647475505, 0.808658283817465 1.4619397662556468, 0.7788556548904921 1.4484363707663448, 0.7500000000000001 1.433012701892217, 0.72221488349021 1.4157348061512707, 0.6956192854956385 1.3966766701456166, 0.670327092449952 1.3759199037394865, 0.6464466094067234 1.3535533905932766, -0.3225966679995622 0.38451011318700523, -89.98001023802891 -62.375679385833585, -99.68765247622281 -54.60956559527848, -99.69561928549565 -54.603323329854376, -99.72221488349021 -54.58426519384872, -99.75 -54.566987298107776, -99.77639320225 -54.55278640450004, -119.77639320225002 -44.55278640450004, -119.77885565489049 -44.551563629233655, -119.80865828381748 -44.53806023374436, -119.83928026734844 -44.52653493525244, -119.87059047744873 -44.517037086855474, -119.90245483899196 -44.50960735979838, -119.93473690389 -44.50427756931309, -119.96729843538493 -44.5010705383807, -119.99999999999999 -44.5, -120.03270156461508 -44.5010705383807, -120.06526309611002 -44.50427756931309, -120.09754516100804 -44.50960735979838, -120.12940952255128 -44.517037086855474, -120.16071973265156 -44.52653493525244, -120.19134171618254 -44.53806023374436, -120.22114434510952 -44.551563629233655, -120.25 -44.566987298107776, -120.27778511650979 -44.58426519384872, -120.30438071450438 -44.603323329854376, -120.32967290755005 -44.62408009626051, -120.35355339059328 -44.64644660940673, -120.37591990373949 -44.670327092449966, -120.39667667014562 -44.69561928549564, -120.41573480615125 -44.722214883490196, -120.4330127018922 -44.75, -120.44843637076633 -44.778855654890506, -120.46193976625561 -44.80865828381745, -120.47346506474754 -44.839280267348414, -120.48296291314455 -44.870590477448744, -120.49039264020159 -44.90245483899193, -120.4957224306869 -44.93473690388997, -120.4989294616193 -44.96729843538493, -120.50000000000001 -45, -120.4989294616193 -45.03270156461507, -120.4957224306869 -45.06526309611003, -120.49039264020159 -45.09754516100807, -120.48296291314455 -45.129409522551256, -120.47346506474754 -45.160719732651586, -120.46193976625561 -45.19134171618255, -120.44843637076633 -45.221144345109494, -120.4330127018922 -45.25, -120.41573480615125 -45.27778511650981, -120.39667667014562 -45.30438071450436, -120.37591990373949 -45.32967290755004, -120.35355339059328 -45.35355339059328, -120.32967290755005 -45.37591990373949, -120.30438071450438 -45.396676670145624, -120.27778511650979 -45.41573480615128, -120.25 -45.433012701892224, -120.22360679775 -45.44721359549996, -100.27098476456104 -55.42352461209444, -90.31234752377719 -63.390434404721496, -90.30438071450436 -63.3966766701456, -90.27778511650979 -63.41573480615128, -90.25 -63.43301270189221, -90.22114434510951 -63.44843637076632, -90.19134171618254 -63.461939766255654, -90.16071973265156 -63.473465064747565, -90.12940952255127 -63.482962913144554, -90.09754516100804 -63.490392640201605, -90.06526309611002 -63.4957224306869, -90.03270156461508 -63.498929461619305, -90 -63.50000000000001)))
        /*
 GeometryRequest nestedLeft = GeometryRequest
         .newBuilder()
         .setLeftNestedRequest(serviceOpLeft)
         .setOperatorType(ServiceOperatorType.ConvexHull)
         .setResultSpatialReference(spatialReferenceGall)
         .build();
         */
        auto* nestedLeft = new GeometryRequest();
        nestedLeft->set_allocated_left_geometry_request(serviceOpLeft);
        nestedLeft->set_operator_(OperatorType::CONVEX_HULL);
        nestedLeft->mutable_result_proj()->CopyFrom(spatialReferenceGall);

        /*

GeometryBagData geometryBagRight = GeometryBagData.newBuilder()
        .setGeometryEncodingType(GeometryEncodingType.wkb)
        .setSpatialReference(spatialReferenceNAD)
        .addGeometryBinaries(ByteString.copyFrom(op.execute(0, polyline, null)))
        .build();
         */
        auto* geometryBagRight = new GeometryData();
        geometryBagRight->set_wkt(polyline);
        geometryBagRight->set_allocated_proj(&spatialReferenceNAD);
//        geometryBagRight->set_geometry_encoding_type(GeometryEncodingType::wkt);
//        geometryBagRight->add_geometry_strings(polyline);

        /*
GeometryRequest serviceOpRight = GeometryRequest
       .newBuilder()
       .setLeftGeometryBag(geometryBagRight)
       .setOperatorType(GeometryRequest.GeodesicBuffer)
       .setBufferParams(GeometryRequest.BufferParams.newBuilder().addDistances(1000).setUnionResult(false).build())
       .setOperationSpatialReference(spatialReferenceWGS)
       .build();
         */
        auto* serviceOpRight = new GeometryRequest();
        serviceOpRight->set_allocated_left_geometry(geometryBagRight);
        serviceOpRight->set_operator_(OperatorType::GEODESIC_BUFFER);
        Params::Buffer geodesicBufferParams;
//        GeometryRequest_BufferParams geodesicBufferParams;
        geodesicBufferParams.set_distance(1000);
        geodesicBufferParams.set_union_result(false);
        serviceOpRight->set_allocated_buffer_params(&geodesicBufferParams);
        serviceOpRight->mutable_operation_proj()->CopyFrom(spatialReferenceWGS);

        /*
GeometryRequest nestedRight = GeometryRequest
      .newBuilder()
      .setLeftNestedRequest(serviceOpRight)
      .setOperatorType(GeometryRequest.ConvexHull)
      .setResultSpatialReference(spatialReferenceGall)
      .build();
         */
        auto* nestedRight = new GeometryRequest();
        nestedRight->set_allocated_geometry_request(serviceOpRight);
        nestedRight->set_operator_(OperatorType::CONVEX_HULL);
        nestedRight->mutable_result_proj()->CopyFrom(spatialReferenceGall);


        /*

GeometryRequest operatorRequestContains = GeometryRequest
     .newBuilder()
     .setLeftNestedRequest(nestedLeft)
     .setRightNestedRequest(nestedRight)
     .setOperatorType(GeometryRequest.Contains)
     .setOperationSpatialReference(spatialReferenceMerc)
     .build();
         */
        auto* operatorRequestContains = new GeometryRequest();
        operatorRequestContains->set_allocated_left_geometry_request(nestedLeft);
        operatorRequestContains->set_allocated_right_geometry_request(nestedRight);
        operatorRequestContains->set_operator_(OperatorType::CONTAINS);
        operatorRequestContains->mutable_operation_proj()->CopyFrom(spatialReferenceMerc);
        /*

GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
GeometryResponse operatorResult = stub.executeOperation(operatorRequestContains);
Map<Integer, Boolean> map = operatorResult.getRelateMapMap();
*/
        auto* clientContext = new grpc::ClientContext();
        auto* operatorResult = new GeometryResponse();
        geometry_stub->Operate(clientContext, *operatorRequestContains, operatorResult);


        ::google::protobuf::Map< ::google::protobuf::int64, bool > stuff = operatorResult->relate_map();
//        std::string result = operatorResult->geometry_bag().geometry_strings(0);
        EXPECT_EQ(1, operatorResult->relate_map_size());
        EXPECT_TRUE(operatorResult->relate_map().at(0));
        EXPECT_TRUE(operatorResult->spatial_relationship());

        auto* clientContext2 = new grpc::ClientContext();
        auto* operatorResult2 = new GeometryResponse();

        auto* operatorRequestUnion = new GeometryRequest();
        operatorRequestUnion->set_allocated_left_geometry_request(serviceOpLeft);
        operatorRequestUnion->set_allocated_right_geometry_request(serviceOpRight);
        operatorRequestUnion->set_operator_(OperatorType::UNION);
        operatorRequestUnion->mutable_operation_proj()->CopyFrom(spatialReferenceMerc);
        operatorRequestUnion->set_result_encoding(Encoding::GEOJSON);

        geometry_stub->Operate(clientContext2, *operatorRequestUnion, operatorResult2);

//        fprintf(stderr, "results json %s\n", operatorResult2->geometry_bag().geojson(0).c_str());
    }
};

namespace {
    class STACTests : public ::testing::Test {
    protected:
        STACTests() {
            char* data = get_nsl_access_token();
            m_bearer_token.assign("Bearer ");
            m_bearer_token.append(data);
            if (const char* env_p = std::getenv("GEOMETRY_SERVICE_HOST")) {
                m_geometry_channel = grpc::CreateChannel(env_p, grpc::InsecureChannelCredentials());
            } else {
                m_geometry_channel = grpc::CreateChannel("0.0.0.0:8980", grpc::InsecureChannelCredentials());
            }

            if (const char* env_p = std::getenv("STAC_SERVICE")) {
                m_stac_channel = grpc::CreateChannel(env_p, grpc::InsecureChannelCredentials());
            } else {
                m_stac_channel = grpc::CreateChannel("0.0.0.0:10000", grpc::InsecureChannelCredentials());
            }
        }

        virtual ~STACTests() {
        }

        std::string m_bearer_token;
        std::shared_ptr<grpc::Channel> m_geometry_channel;
        std::shared_ptr<grpc::Channel> m_stac_channel;
    };

    TEST_F(STACTests, TEST_STAC_1){
        auto gp = grpc::StubOptions();
        std::unique_ptr<StacService::Stub> stac_stub = StacService::NewStub(m_stac_channel, gp);

        epl::protobuf::v1::StacRequest stacRequest;
        std::string id("LC80330342017072LGN00");

        stacRequest.set_id(id.c_str());
        auto* stacItem = new StacItem();
        auto* clientContext = new grpc::ClientContext();
        clientContext->AddMetadata("authorization", m_bearer_token);
        grpc::Status status = stac_stub->SearchOneItem(clientContext, stacRequest, stacItem);
        EXPECT_EQ(status.error_code(), grpc::StatusCode::OK);
        EXPECT_STRCASEEQ(status.error_message().c_str(), "");

        EXPECT_STRCASEEQ(stacItem->id().c_str(), id.c_str());
        delete stacItem;
        delete clientContext;
    }
};


int main(int argc, char **argv) {
    ::testing::InitGoogleTest(&argc, argv);
    int result = RUN_ALL_TESTS();
    return result;
}