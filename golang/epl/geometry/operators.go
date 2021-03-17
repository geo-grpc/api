package geometry

import (
	"context"
	"encoding/binary"
	"errors"
	"fmt"
	eplpbv1 "github.com/geo-grpc/api/golang/epl/protobuf/v1"
	gogeom "github.com/twpayne/go-geom"
	gogeomewkb "github.com/twpayne/go-geom/encoding/ewkb"
	gogeomwkb "github.com/twpayne/go-geom/encoding/wkb"
	"google.golang.org/grpc"
)

type Service struct {
	ClientV1 eplpbv1.GeometryServiceClient
	CleanupV1 func() error
	Proj4326 *eplpbv1.ProjectionData
}

func (gs *Service) Buffer(t gogeom.T, params *eplpbv1.Params_Buffer, resultEpsg ...int32) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_BUFFER,
		Params:               &eplpbv1.GeometryRequest_BufferParams{BufferParams:params},
	}

	return gs.SingleInputRequest(t, request, resultEpsg)
}

func (gs *Service) GeodesicBuffer(t gogeom.T, params *eplpbv1.Params_GeodeticBuffer, resultEpsg ...int32) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_GEODESIC_BUFFER,
		Params:               &eplpbv1.GeometryRequest_GeodeticBufferParams{GeodeticBufferParams:params},
	}

	return gs.SingleInputRequest(t, request, resultEpsg)
}


func (gs *Service) Simplify(t gogeom.T, params *eplpbv1.Params_Simplify, resultEpsg ...int32) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_SIMPLIFY,
		Params:               &eplpbv1.GeometryRequest_SimplifyParams{SimplifyParams:params},
	}

	return gs.SingleInputRequest(t, request, resultEpsg)
}

func (gs *Service) ConvexHull(t gogeom.T, resultEpsg ...int32) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_CONVEX_HULL,
	}

	return gs.SingleInputRequest(t, request, resultEpsg)
}

func (gs *Service) Project(t gogeom.T, resultEpsg int32) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_PROJECT,
	}
	epsg :=[]int32{resultEpsg}

	return gs.SingleInputRequest(t, request, epsg)
}

func (gs *Service) Contains(left gogeom.T, right gogeom.T) (error, bool) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_CONTAINS,
	}

	return gs.RelationalRequest(left, right, request)
}

func (gs *Service) Touches(left gogeom.T, right gogeom.T) (error, bool) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_TOUCHES,
	}

	return gs.RelationalRequest(left, right, request)
}

func (gs *Service) Within(left gogeom.T, right gogeom.T) (error, bool) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_WITHIN,
	}

	return gs.RelationalRequest(left, right, request)
}

func (gs *Service) Overlaps(left gogeom.T, right gogeom.T) (error, bool) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_OVERLAPS,
	}

	return gs.RelationalRequest(left, right, request)
}

func (gs *Service) Intersects(left gogeom.T, right gogeom.T) (error, bool) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_INTERSECTS,
	}

	return gs.RelationalRequest(left, right, request)
}

func (gs *Service) Disjoint(left gogeom.T, right gogeom.T) (error, bool) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_DISJOINT,
	}

	return gs.RelationalRequest(left, right, request)
}

func (gs *Service) Intersection(left gogeom.T, right gogeom.T, resultEpsg ...int32) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_INTERSECTION,
	}

	return gs.TwoInputRequest(left, right, request, resultEpsg)
}

func (gs *Service) Difference(left gogeom.T, right gogeom.T, resultEpsg ...int32) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_DIFFERENCE,
	}

	return gs.TwoInputRequest(left, right, request, resultEpsg)
}

func (gs *Service) SymmetricDifference(left gogeom.T, right gogeom.T, resultEpsg ...int32) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_SYMMETRIC_DIFFERENCE,
	}

	return gs.TwoInputRequest(left, right, request, resultEpsg)
}

func (gs *Service) TwoInputRequest(left gogeom.T, right gogeom.T, request *eplpbv1.GeometryRequest, resultEpsg []int32) (error, gogeom.T) {
	err, leftGeom := ParseToGeometryData(left)
	if err != nil {
		return err, nil
	}
	request.SetLeftGeometry(leftGeom)

	err, rightGeom := ParseToGeometryData(right)
	if err != nil {
		return err, nil
	}
	request.SetLeftGeometry(rightGeom)

	if len(resultEpsg) > 0 {
		request.ResultProj = &eplpbv1.ProjectionData{
			Definition:           &eplpbv1.ProjectionData_Epsg{Epsg:resultEpsg[0]},
		}
	}

	request.ResultEncoding = eplpbv1.Encoding_EWKB

	return gs.GeometryResultRequest(request)
}

func (gs *Service) RelationalRequest(left gogeom.T, right gogeom.T, request *eplpbv1.GeometryRequest) (error, bool) {
	err, leftGeom := ParseToGeometryData(left)
	if err != nil {
		return err, false
	}
	request.SetLeftGeometry(leftGeom)

	err, rightGeom := ParseToGeometryData(right)
	if err != nil {
		return err, false
	}
	request.SetLeftGeometry(rightGeom)

	requestResult, err := gs.ClientV1.Operate(context.Background(), request)
	if err != nil {
		return errors.New(fmt.Sprintf("geometry service error with value:\n%v", err.Error())), nil
	}

	return nil, requestResult.GetSpatialRelationship()
}

func (gs *Service) SingleInputRequest(t gogeom.T, request *eplpbv1.GeometryRequest, resultEpsg []int32) (error, gogeom.T) {
	err, geometryData := ParseToGeometryData(t)
	if err != nil {
		return err, nil
	}
	request.SetGeometry(geometryData)

	if len(resultEpsg) > 0 {
		request.ResultProj = &eplpbv1.ProjectionData{
			Definition:           &eplpbv1.ProjectionData_Epsg{Epsg:resultEpsg[0]},
		}
	}

	request.ResultEncoding = eplpbv1.Encoding_EWKB

	return gs.GeometryResultRequest(request)
}

func (gs *Service) GeometryResultRequest(request *eplpbv1.GeometryRequest) (error, gogeom.T) {
	requestResult, err := gs.ClientV1.Operate(context.Background(), request)
	if err != nil {
		return errors.New(fmt.Sprintf("geometry service error with value:\n%v", err.Error())), nil
	}

	if requestResult.GetGeometry() == nil || requestResult.GetGeometry().Proj == nil || requestResult.GetGeometry().GetWkb() == nil {
		// TODO does this ever occur?
		err = errors.New("geometry Result incomplete")
		return err, nil
	}

	return ParseToGoGeom(requestResult.GetGeometry())
}

func ParseToGeometryData(t gogeom.T) (error, *eplpbv1.GeometryData) {
	if t.SRID() == 0 {
		return errors.New("need SRID"), nil
	}

	b, err := gogeomwkb.Marshal(t, binary.BigEndian)
	if err != nil {
		return err, nil
	}

	// TODO could probably simplify this and convert to ewkb
	return nil, &eplpbv1.GeometryData{
		Data:                 &eplpbv1.GeometryData_Wkb{Wkb:b},
		Proj:                 &eplpbv1.ProjectionData{
			Definition:           &eplpbv1.ProjectionData_Epsg{Epsg:int32(t.SRID())},
		},
	}
}

func ParseToGoGeom(geometryData *eplpbv1.GeometryData) (error, gogeom.T) {
	if geometryData.GetEwkb() == nil {
		return errors.New("only parsing ewkb"), nil
	}

	geometry, err := gogeomewkb.Unmarshal(geometryData.GetEwkb())
	if err != nil {
		return err, nil
	}

	if geometryData.Proj.GetEpsg() == 0 {
		return errors.New("geometryData needs epsg defined"), nil
	}

	return nil, geometry
}

func NewGeometryService(serverAddrGeometryV1 string) *Service {
	var opts []grpc.DialOption
	opts = append(opts, grpc.WithInsecure())

	connV1, err := grpc.Dial(serverAddrGeometryV1, opts...)
	if err != nil {
		return nil
	}

	cleanupV1 := func () error {
		return connV1.Close()
	}

	clientV1 := eplpbv1.NewGeometryServiceClient(connV1)

	proj4326 := &eplpbv1.ProjectionData{}
	proj4326.SetEpsg(4326)

	gs := &Service{
		ClientV1:		clientV1,
		CleanupV1:		cleanupV1,
		Proj4326:		proj4326,
	}

	return gs
}