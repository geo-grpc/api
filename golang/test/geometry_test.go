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

package test

import (
	geomOps "github.com/geo-grpc/api/golang/epl/geometry"
	pb "github.com/geo-grpc/api/golang/epl/protobuf/v1"
	"github.com/twpayne/go-geom"
	"testing"
)


func TestGeometryRequests(t *testing.T) {
	spatialReferenceNAD27 := pb.ProjectionData{}
	spatialReferenceNAD27.SetEpsg(4267)
	geometryString := "MULTILINESTRING ((-120 -45, -100 -55, -90 -63, 0 0, 1 1, 100 25, 170 45, 175 65))"

	// define a geometry from an array of wkt strings (in this case only one geometry) with spatial reference nad83
	lefGeometryBag := &pb.GeometryData{Proj: &spatialReferenceNAD27}
	lefGeometryBag.SetWkt(geometryString)

	err, geometry1 := geomOps.GeomPbToGeom(lefGeometryBag)
	if err != nil {
		t.Errorf(err.Error())
	}
	// define a buffer opertor on geometry then buffered with distance size .5 degrees (I know horrible), and then
	// the result is transformed to WGS84
	chain := geomOps.InitChain(geometry1)
	chain = chain.Simplify(true).
		Buffer(.5).
		Project(4087).
		ConvexHull().
		Project(4326)

	err, result1 := chain.Execute()
	if err != nil {
		t.Errorf(err.Error())
	}

	if result1 == nil {
		t.Errorf("shouldn't be empty")
	}

	err, geometry2 := geomOps.GeomPbToGeom(lefGeometryBag)
	err, geomS := geomOps.Simplify(geometry2, true)
	err, geom2Buff := geomOps.Buffer(geomS, .5)
	err, geomProjected := geomOps.Project(geom2Buff, 4087)
	err, geomHulled := geomOps.ConvexHull(geomProjected)
	err, geomReProjected := geomOps.Project(geomHulled, 4326)

	if err != nil {
		t.Errorf(err.Error())
	}

	err, e := geomOps.Equals(result1, geomReProjected)
	if err != nil {
		t.Errorf(err.Error())
	}
	if result1 == nil || !e {
		t.Errorf("geometries not equal")
	}

	err, result1 = chain.Buffer( 1).Project(3857).Execute()
	if err != nil {
		t.Errorf(err.Error())
	}
	err, e = geomOps.Contains(result1, geomReProjected)
	if err != nil || !e {
		t.Errorf("geometries not equal")
	}

	inputChan := make(chan geom.T, 2)


	inputChan <- geometry2
	inputChan <- geomReProjected
	close(inputChan)
	stream := geomOps.InitStream(inputChan, nil)
	streamOut := stream.Buffer(44).Execute()
	for a := range streamOut {
		err, relation := geomOps.Contains(a.G, geometry2)
		if err != nil || !relation {
			t.Errorf("geometries not equal")
		}
	}
	//err, geom2Project := testHelper.gs.Project(geom2Buff, )
	//
	//// define a geometry from wkt string with spatial reference nad83
	//rightGeometryBag := pb.GeometryData{
	//	Wkt:              geometryString,
	//	Sr: &spatialReferenceNAD27}
	//
	//// Project the geometry to WGS84 and then perform a geodesic buffer of the input geometry with a distance of 1000 meters.
	//// The parameters for the geodesic buffer will be derived from the WGS84 spatial reference (the resulting spatial reference will be wgs84)
	//operatorRight := pb.GeometryRequest{
	//	Geometry:&rightGeometryBag,
	//	Operator:pb.OperatorType_GEODESIC_BUFFER,
	//	BufferParams:&pb.GeometryRequest_BufferParams{
	//		Distance:1000,
	//		UnionResult:false},
	//	OperationSr:&spatialReferenceWGS}
	//
	//// Perform a convex hull operation on the previous buffer operation's result. And then project to Gall
	//operatorNestedRight := pb.GeometryRequest{
	//	GeometryRequest:&operatorRight,
	//	Operator:pb.OperatorType_CONVEX_HULL,
	//	ResultSr:&spatialReferenceGall}
	//
	//// take each of the nested buffer + convex hull and test that the non-geodesic contains the geodesic
	//operatorContains := pb.GeometryRequest{
	//	LeftGeometryRequest:&operatorNestedLeft,
	//	RightGeometryRequest:&operatorNestedRight,
	//	Operator:pb.OperatorType_CONTAINS,
	//	OperationSr:&spatialReferenceMerc}
	//
	//cleanup, client := getConnection()
	//defer cleanup()
	//
	//operatorResultEquals, err := client.Operate(context.Background(), &operatorContains)
	//
	//if err != nil || operatorResultEquals == nil || operatorResultEquals.RelateMap == nil || len(operatorResultEquals.RelateMap) == 0 {
	//	t.Errorf("No results found")
	//	return
	//}
	//
	//result := operatorResultEquals.RelateMap[0]
	//
	//if result != true {
	//	t.Errorf("left nested request geometry should container right geometry nested request\n")
	//}

}

func TestOrb(t *testing.T)  {
	//polygon := orb.Polygon{{{-7864906.47388011, 5332566.927033671}, {-7864916.460312591, 5332570.1636027135}, {-7864924.108904024, 5332574.133715812}, {-7864933.278776623, 5332581.506738894}, {-7864944.012712103, 5332592.283361189}, {-7864954.032848193, 5332603.760638999}, {-7864967.811333832, 5332619.284244942}, {-7864981.180136529, 5332631.382927101}, {-7864989.611952764, 5332636.023656715}, {-7865000.370786544, 5332637.184125917}, {-7865009.579053575, 5332635.628388291}, {-7865016.16789749, 5332631.376844626}, {-7865020.394141447, 5332624.42518574}, {-7865021.597934323, 5332618.21951675}, {-7865020.466137733, 5332610.687416714}, {-7865016.6676847525, 5332604.581174427}, {-7864997.040779059, 5332580.931279262}, {-7864995.677304067, 5332576.837575279}, {-7864990.805540493, 5332574.873665937}, {-7864981.109274853, 5332566.823831909}, {-7864972.303819492, 5332551.888427953}, {-7864968.879215449, 5332543.027557102}, {-7864968.577878266, 5332532.044539555}, {-7864971.298182168, 5332520.314823073}, {-7864975.197879721, 5332511.995919312}, {-7864983.303894385, 5332502.220336648}, {-7864991.004603794, 5332497.947246153}, {-7864998.31802048, 5332495.741763704}, {-7865004.530741485, 5332496.305681743}, {-7865015.674442256, 5332497.458880647}, {-7865023.000967826, 5332495.940023441}, {-7865027.62341107, 5332491.727848984}, {-7865031.127164192, 5332480.66955711}, {-7865034.999796462, 5332470.977394669}, {-7865039.673578698, 5332458.522921438}, {-7865045.220831169, 5332442.616282577}, {-7865052.959301393, 5332429.413875305}, {-7865056.076121762, 5332420.423621443}, {-7865056.087348325, 5332410.121458498}, {-7865055.367551364, 5332403.95439932}, {-7865051.953803687, 5332397.841070666}, {-7865044.680947322, 5332389.056571259}, {-7865037.018977273, 5332384.400770274}, {-7865028.956250465, 5332381.12630789}, {-7865022.437720319, 5332373.70104534}, {-7865005.313263117, 5332355.4962026365}, {-7864989.271556623, 5332337.957206923}, {-7864970.549839121, 5332321.157767649}, {-7864961.353587726, 5332312.411341897}, {-7864954.415974365, 5332309.801462796}, {-7864947.149962618, 5332307.884968461}, {-7864934.172986048, 5332302.647412382}, {-7864921.554071587, 5332296.029743175}, {-7864908.507404905, 5332289.419375367}, {-7864900.04403092, 5332287.5265383925}, {-7864888.129438573, 5332288.449143132}, {-7864881.9973887075, 5332292.0045149}, {-7864878.90076981, 5332295.499453959}, {-7864873.862573904, 5332302.4672505185}, {-7864869.235963491, 5332310.800305942}, {-7864863.79429473, 5332321.209779666}, {-7864861.1612822255, 5332330.876813846}, {-7864857.641596066, 5332343.30820105}, {-7864855.665789155, 5332351.589235708}, {-7864857.947128857, 5332363.21977698}, {-7864859.042652286, 5332377.620986307}, {-7864860.198974066, 5332388.586915882}, {-7864865.522425528, 5332407.025150811}, {-7864871.944724664, 5332424.754782707}, {-7864876.856231828, 5332441.827430327}, {-7864879.137622263, 5332453.458076647}, {-7864879.918258366, 5332469.239071759}, {-7864879.01957216, 5332495.355337941}, {-7864878.1866228385, 5332513.9155625375}, {-7864876.21079405, 5332522.196368905}, {-7864873.494187136, 5332529.805695953}, {-7864867.299262529, 5332538.856117353}, {-7864858.8061716175, 5332550.70001795}, {-7864850.700365193, 5332560.475538614}, {-7864847.986385424, 5332566.023726198}, {-7864847.164824063, 5332574.281741375}, {-7864850.193491394, 5332580.403259908}, {-7864860.929613496, 5332589.119201011}, {-7864873.206998097, 5332595.744679938}, {-7864880.117448498, 5332596.981412669}, {-7864888.184879652, 5332596.135282619}, {-7864892.035097693, 5332593.998375489}, {-7864892.440447267, 5332588.495833605}, {-7864890.154438518, 5332580.985810155}, {-7864887.910804881, 5332573.47571999}, {-7864887.901883692, 5332568.668295522}, {-7864890.255892425, 5332566.561379068}, {-7864896.869057867, 5332565.743632321}, {-7864906.47388011, 5332566.927033671}}}
	//polygonWkt := wkt.MarshalString(polygon)
	//spatialReferenceNAD27 := pb.ProjectionData{Wkid:25830}
	//spatialReferenceWGS := pb.ProjectionData{Wkid:4326}
	//geometryBag := pb.GeometryData{
	//	Wkt:              polygonWkt,
	//	Sr: &spatialReferenceNAD27}
	//
	//operatorLeft := pb.GeometryRequest{
	//	Geometry:&geometryBag,
	//	Operator:pb.OperatorType_BUFFER,
	//	BufferParams:&pb.GeometryRequest_BufferParams{Distance:.5},
	//	ResultSr:&spatialReferenceWGS,
	//	ResultEncoding:pb.Encoding_WKB}
	//
	//operatorContains := pb.GeometryRequest{
	//	LeftGeometryRequest:&operatorLeft,
	//	RightGeometry:&geometryBag,
	//	OperationSr:&spatialReferenceWGS,
	//	Operator:pb.OperatorType_CONTAINS,
	//}
	//
	//cleanup, client := getConnection()
	//defer cleanup()
	//
	//operatorResults, err := client.Operate(context.Background(), &operatorContains)
	//
	//if err != nil || operatorResults == nil || operatorResults.RelateMap == nil || len(operatorResults.RelateMap) == 0 {
	//	t.Errorf("No results found")
	//	return
	//}
	//
	//result := operatorResults.RelateMap[0]
	//
	//if result != true {
	//	t.Errorf("left nested request geometry should container right geometry nested request\n")
	//}
}

func TestOrbBounds(t *testing.T)  {
	//polygon := orb.Polygon{{{-7864906.47388011, 5332566.927033671}, {-7864916.460312591, 5332570.1636027135}, {-7864924.108904024, 5332574.133715812}, {-7864933.278776623, 5332581.506738894}, {-7864944.012712103, 5332592.283361189}, {-7864954.032848193, 5332603.760638999}, {-7864967.811333832, 5332619.284244942}, {-7864981.180136529, 5332631.382927101}, {-7864989.611952764, 5332636.023656715}, {-7865000.370786544, 5332637.184125917}, {-7865009.579053575, 5332635.628388291}, {-7865016.16789749, 5332631.376844626}, {-7865020.394141447, 5332624.42518574}, {-7865021.597934323, 5332618.21951675}, {-7865020.466137733, 5332610.687416714}, {-7865016.6676847525, 5332604.581174427}, {-7864997.040779059, 5332580.931279262}, {-7864995.677304067, 5332576.837575279}, {-7864990.805540493, 5332574.873665937}, {-7864981.109274853, 5332566.823831909}, {-7864972.303819492, 5332551.888427953}, {-7864968.879215449, 5332543.027557102}, {-7864968.577878266, 5332532.044539555}, {-7864971.298182168, 5332520.314823073}, {-7864975.197879721, 5332511.995919312}, {-7864983.303894385, 5332502.220336648}, {-7864991.004603794, 5332497.947246153}, {-7864998.31802048, 5332495.741763704}, {-7865004.530741485, 5332496.305681743}, {-7865015.674442256, 5332497.458880647}, {-7865023.000967826, 5332495.940023441}, {-7865027.62341107, 5332491.727848984}, {-7865031.127164192, 5332480.66955711}, {-7865034.999796462, 5332470.977394669}, {-7865039.673578698, 5332458.522921438}, {-7865045.220831169, 5332442.616282577}, {-7865052.959301393, 5332429.413875305}, {-7865056.076121762, 5332420.423621443}, {-7865056.087348325, 5332410.121458498}, {-7865055.367551364, 5332403.95439932}, {-7865051.953803687, 5332397.841070666}, {-7865044.680947322, 5332389.056571259}, {-7865037.018977273, 5332384.400770274}, {-7865028.956250465, 5332381.12630789}, {-7865022.437720319, 5332373.70104534}, {-7865005.313263117, 5332355.4962026365}, {-7864989.271556623, 5332337.957206923}, {-7864970.549839121, 5332321.157767649}, {-7864961.353587726, 5332312.411341897}, {-7864954.415974365, 5332309.801462796}, {-7864947.149962618, 5332307.884968461}, {-7864934.172986048, 5332302.647412382}, {-7864921.554071587, 5332296.029743175}, {-7864908.507404905, 5332289.419375367}, {-7864900.04403092, 5332287.5265383925}, {-7864888.129438573, 5332288.449143132}, {-7864881.9973887075, 5332292.0045149}, {-7864878.90076981, 5332295.499453959}, {-7864873.862573904, 5332302.4672505185}, {-7864869.235963491, 5332310.800305942}, {-7864863.79429473, 5332321.209779666}, {-7864861.1612822255, 5332330.876813846}, {-7864857.641596066, 5332343.30820105}, {-7864855.665789155, 5332351.589235708}, {-7864857.947128857, 5332363.21977698}, {-7864859.042652286, 5332377.620986307}, {-7864860.198974066, 5332388.586915882}, {-7864865.522425528, 5332407.025150811}, {-7864871.944724664, 5332424.754782707}, {-7864876.856231828, 5332441.827430327}, {-7864879.137622263, 5332453.458076647}, {-7864879.918258366, 5332469.239071759}, {-7864879.01957216, 5332495.355337941}, {-7864878.1866228385, 5332513.9155625375}, {-7864876.21079405, 5332522.196368905}, {-7864873.494187136, 5332529.805695953}, {-7864867.299262529, 5332538.856117353}, {-7864858.8061716175, 5332550.70001795}, {-7864850.700365193, 5332560.475538614}, {-7864847.986385424, 5332566.023726198}, {-7864847.164824063, 5332574.281741375}, {-7864850.193491394, 5332580.403259908}, {-7864860.929613496, 5332589.119201011}, {-7864873.206998097, 5332595.744679938}, {-7864880.117448498, 5332596.981412669}, {-7864888.184879652, 5332596.135282619}, {-7864892.035097693, 5332593.998375489}, {-7864892.440447267, 5332588.495833605}, {-7864890.154438518, 5332580.985810155}, {-7864887.910804881, 5332573.47571999}, {-7864887.901883692, 5332568.668295522}, {-7864890.255892425, 5332566.561379068}, {-7864896.869057867, 5332565.743632321}, {-7864906.47388011, 5332566.927033671}}}
	//polygonWkt := wkt.MarshalString(polygon)
	//spatialReferenceNAD27 := pb.ProjectionData{Wkid:25830}
	//spatialReferenceWGS := pb.ProjectionData{Wkid:4326}
	//geometryBag := pb.GeometryData{
	//	Wkt:              polygonWkt,
	//	Sr: &spatialReferenceNAD27}
	//
	//operatorLeft := pb.GeometryRequest{
	//	Geometry:&geometryBag,
	//	Operator:pb.OperatorType_BUFFER,
	//	BufferParams:&pb.GeometryRequest_BufferParams{Distance:.5},
	//	ResultSr:&spatialReferenceWGS,
	//	ResultEncoding:pb.Encoding_WKB}
	//
	//cleanup, client := getConnection()
	//defer cleanup()
	//
	//operatorResults, err := client.Operate(context.Background(), &operatorLeft)
	//if err != nil || operatorResults == nil || operatorResults.Geometry == nil {
	//	t.Errorf("No results found")
	//	return
	//}
	//
	//geometry, err := wkb.Unmarshal(operatorResults.Geometry.Wkb)
	//if err != nil || geometry == nil {
	//	t.Errorf("No results found")
	//	return
	//}
	//
	//if geometry.Bound().Min.X() < -180 || geometry.Bound().Min.Y() < -90 {
	//	t.Errorf("Not Projected")
	//	return
	//}
}

func TestGeometryRequestsNoBag(t *testing.T) {
	//spatialReferenceWGS := pb.ProjectionData{Wkid:4326}
	//spatialReferenceNAD27 := pb.ProjectionData{Wkid:4267}
	//spatialReferenceMerc := pb.ProjectionData{Wkid:3857}
	//spatialReferenceGall := pb.ProjectionData{Wkid:54016}
	//spatialReferenceMoll := pb.ProjectionData{Proj4:"+proj=moll +lon_0=0 +x_0=0 +y_0=0 +datum=WGS84 +units=m +no_defs"}
	//
	//geometryString := "MULTILINESTRING ((-120 -45, -100 -55, -90 -63, 0 0, 1 1, 100 25, 170 45, 175 65))"
	//
	//// define a geometry from an array of wkt strings (in this case only one geometry) with spatial reference nad83
	//lefGeometry := pb.GeometryData{
	//	Wkt:              geometryString,
	//	Sr: &spatialReferenceNAD27}
	//
	//// define a buffer opertor on geometry then buffered with distance size .5 degrees (I know horrible), and then
	//// the result is transformed to WGS84
	//operatorLeft := pb.GeometryRequest{
	//	Geometry:&lefGeometry,
	//	Operator:pb.OperatorType_BUFFER,
	//	BufferParams:&pb.GeometryRequest_BufferParams{Distance:.5},
	//	ResultSr:&spatialReferenceWGS}
	//
	//// nest the result of previous buffer operation as the geometry input for this operation,
	//// project that resulting buffered geometry to World Mollweide, perform the convex hull operation then
	//// project that result to World Gall Stereo
	//operatorNestedLeft := pb.GeometryRequest{
	//	GeometryRequest:&operatorLeft,
	//	Operator:pb.OperatorType_CONVEX_HULL,
	//	OperationSr:&spatialReferenceMoll,
	//	ResultSr:&spatialReferenceGall}
	//
	//// define a geometry from wkt string with spatial reference nad83
	//rightGeometry := pb.GeometryData{
	//	Wkt:              geometryString,
	//	Sr: &spatialReferenceNAD27}
	//
	//// Project the geometry to WGS84 and then perform a geodesic buffer of the input geometry with a distance of 1000 meters.
	//// The parameters for the geodesic buffer will be derived from the WGS84 spatial reference (the resulting spatial reference will be wgs84)
	//operatorRight := pb.GeometryRequest{
	//	Geometry:&rightGeometry,
	//	Operator:pb.OperatorType_GEODESIC_BUFFER,
	//	BufferParams:&pb.GeometryRequest_BufferParams{
	//		Distance:1000,
	//		UnionResult:false},
	//	OperationSr:&spatialReferenceWGS}
	//
	//// Perform a convex hull operation on the previous buffer operation's result. And then project to Gall
	//operatorNestedRight := pb.GeometryRequest{
	//	GeometryRequest:&operatorRight,
	//	Operator:pb.OperatorType_CONVEX_HULL,
	//	ResultSr:&spatialReferenceGall}
	//
	//// take each of the nested buffer + convex hull and test that the non-geodesic contains the geodesic
	//operatorContains := pb.GeometryRequest{
	//	LeftGeometryRequest:&operatorNestedLeft,
	//	RightGeometryRequest:&operatorNestedRight,
	//	Operator:pb.OperatorType_CONTAINS,
	//	OperationSr:&spatialReferenceMerc}
	//
	//cleanup, client := getConnection()
	//defer cleanup()
	//
	//operatorResultEquals, err := client.Operate(context.Background(), &operatorContains)
	//
	//if err != nil || operatorResultEquals == nil || operatorResultEquals.RelateMap == nil || len(operatorResultEquals.RelateMap) == 0 {
	//	t.Errorf("No results found")
	//	return
	//}
	//
	//result := operatorResultEquals.RelateMap[0]
	//
	//if result != true {
	//	t.Errorf("left nested request geometry should container right geometry nested request\n")
	//}

}
