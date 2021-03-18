package geometry

import (
	"context"
	"encoding/binary"
	"errors"
	"fmt"
	gogeom "github.com/davidraleigh/go-geom"
	gogeomewkb "github.com/davidraleigh/go-geom/encoding/ewkb"
	gogeomgeojson "github.com/davidraleigh/go-geom/encoding/geojson"
	gogeomwkb "github.com/davidraleigh/go-geom/encoding/wkb"
	gogeomwkt "github.com/davidraleigh/go-geom/encoding/wkt"
	eplpbv1 "github.com/geo-grpc/api/golang/epl/protobuf/v1"
	"google.golang.org/grpc"
)

type ExecuteBlocking func(eplpbv1.GeometryRequest) (error, gogeom.T)

type ChainRequest struct {
	geometryRequest *eplpbv1.GeometryRequest
	gs *Service
	err *error
}

type Service struct {
	ClientV1 eplpbv1.GeometryServiceClient
	CleanupV1 func() error
	Proj4326 *eplpbv1.ProjectionData
}

func (c *ChainRequest) ExecuteBlockingSingle() (*error, gogeom.T) {
	if c.err != nil {
		return c.err, nil
	}

	requestResult, err := c.gs.ClientV1.Operate(context.Background(), c.geometryRequest)
	if err != nil {
		newerr := errors.New(fmt.Sprintf("geometry service error with value:\n%v", err.Error()))
		return &newerr, nil
	}

	if requestResult.GetGeometry() == nil || requestResult.GetGeometry().Proj == nil {
		// TODO does this ever occur?
		err = errors.New("geometry Result incomplete")
		return &err, nil
	}

	return c.gs.ParseToGoGeom(requestResult.GetGeometry())
}

func (gs *Service) SingleInputChain(t gogeom.T, request *eplpbv1.GeometryRequest, c *ChainRequest) *ChainRequest {
	if c != nil && c.err != nil {
		return c
	}

	newChain := &ChainRequest{
		gs:              gs,
	}

	if request.GetGeometry() != nil {

	} else if c != nil {
		request.SetLeftGeometryRequest(c.geometryRequest)
	} else if t != nil {
		err, geometryData := ParseToGeometryData(t)
		if err != nil {
			newChain.err = &err
			return newChain
		}
		request.SetGeometry(geometryData)
	} else if request.GetGeometry() == nil && request.GetGeometryRequest() == nil {
		err := errors.New("either geometry or chain request must be defined")
		newChain.err = &err
		return newChain
	}

	request.ResultEncoding = eplpbv1.Encoding_EWKB

	newChain.geometryRequest = request
	return newChain
}

func (gs *Service) Buffer(t gogeom.T, params *eplpbv1.Params_Buffer) (*error, gogeom.T) {
	chain := gs.BufferChain(t, nil, params)
	if chain.err != nil {
		return chain.err, nil
	}
	return chain.ExecuteBlockingSingle()
}

func (gs *Service) BufferChain(t gogeom.T, c *ChainRequest, params *eplpbv1.Params_Buffer) *ChainRequest {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_BUFFER,
		Params:               &eplpbv1.GeometryRequest_BufferParams{BufferParams:params},
	}

	return gs.SingleInputChain(t, request, c)
}

func (gs *Service) GeodesicBuffer(t gogeom.T, params *eplpbv1.Params_GeodeticBuffer) (*error, gogeom.T) {
	chain := gs.GeodesicBufferChain(t, nil, params)
	if chain.err != nil {
		return chain.err, nil
	}
	return chain.ExecuteBlockingSingle()
}

func (gs *Service) GeodesicBufferChain(t gogeom.T, c *ChainRequest, params *eplpbv1.Params_GeodeticBuffer) *ChainRequest {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_GEODESIC_BUFFER,
		Params:               &eplpbv1.GeometryRequest_GeodeticBufferParams{GeodeticBufferParams:params},
	}

	return gs.SingleInputChain(t, request, c)
}

func (gs *Service) Simplify(t gogeom.T, params *eplpbv1.Params_Simplify) (*error, gogeom.T) {
	chain := gs.SimplifyChain(t, nil, params)
	if chain.err != nil {
		return chain.err, nil
	}
	return chain.ExecuteBlockingSingle()
}

func (gs *Service) SimplifyChain(t gogeom.T, c *ChainRequest, params *eplpbv1.Params_Simplify) *ChainRequest {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_SIMPLIFY,
		Params:               &eplpbv1.GeometryRequest_SimplifyParams{SimplifyParams:params},
	}

	return gs.SingleInputChain(t, request, c)
}

func (gs *Service) ConvexHull(t gogeom.T) (*error, gogeom.T) {
	chain := gs.ConvexHullChain(t, nil)
	if chain.err != nil {
		return chain.err, nil
	}
	return chain.ExecuteBlockingSingle()
}

func (gs *Service) ConvexHullChain(t gogeom.T, c *ChainRequest) *ChainRequest {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_CONVEX_HULL,
	}

	return gs.SingleInputChain(t, request, c)
}

func (gs *Service) Project(t gogeom.T, resultEpsg int32) (*error, gogeom.T) {
	chain := gs.ProjectChain(t, nil, resultEpsg)
	if chain.err != nil {
		return chain.err, nil
	}
	return chain.ExecuteBlockingSingle()
}

func (gs *Service) ProjectChain(t gogeom.T, c *ChainRequest, resultEpsg int32) *ChainRequest {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_PROJECT,
	}

	request.ResultProj = &eplpbv1.ProjectionData{
		Definition:           &eplpbv1.ProjectionData_Epsg{Epsg:resultEpsg},
	}

	return gs.SingleInputChain(t, request, c)
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

func (gs *Service) Intersection(left gogeom.T, right gogeom.T) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_INTERSECTION,
	}

	return gs.TwoInputRequest(left, right, request)
}

func (gs *Service) Difference(left gogeom.T, right gogeom.T) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_DIFFERENCE,
	}

	return gs.TwoInputRequest(left, right, request)
}

func (gs *Service) SymmetricDifference(left gogeom.T, right gogeom.T) (error, gogeom.T) {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_SYMMETRIC_DIFFERENCE,
	}

	return gs.TwoInputRequest(left, right, request)
}

func (gs *Service) TwoInputRequest(left gogeom.T, right gogeom.T, request *eplpbv1.GeometryRequest, resultEpsg ...int32) (error, gogeom.T) {
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
		return errors.New(fmt.Sprintf("geometry service error with value:\n%v", err.Error())), false
	}

	return nil, requestResult.GetSpatialRelationship()
}

func (gs *Service) GeometryResultRequest(request *eplpbv1.GeometryRequest) (error, gogeom.T) {
	requestResult, err := gs.ClientV1.Operate(context.Background(), request)
	if err != nil {
		return err, nil
	}

	if requestResult.GetGeometry() == nil || requestResult.GetGeometry().Proj == nil || requestResult.GetGeometry().GetWkb() == nil {
		// TODO does this ever occur?
		err = errors.New("geometry Result incomplete")
		return err, nil
	}

	err2, geom := gs.ParseToGoGeom(requestResult.GetGeometry())
	return *err2, geom
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

func (gs *Service) ParseToGoGeom(geometryData *eplpbv1.GeometryData) (*error, gogeom.T) {
	if geometryData.Proj.GetEpsg() == 0 {
		err := errors.New("geometryData needs epsg defined")
		return &err, nil
	}

	var geometry gogeom.T
	var err error
	if geometryData.GetEwkb() != nil {
		geometry, err = gogeomewkb.Unmarshal(geometryData.GetEwkb())
	} else if geometryData.GetWkb() != nil {
		geometry, err = gogeomwkb.Unmarshal(geometryData.GetWkb())
	} else if len(geometryData.GetWkt()) != 0 {
		geometry, err = gogeomwkt.Unmarshal(geometryData.GetWkt())
	} else if len(geometryData.GetGeojson()) != 0 {
		err = gogeomgeojson.Unmarshal([]byte(geometryData.GetGeojson()), &geometry)
	} else if geometryData.GetEsriShape() != nil {
		emptyRequest := eplpbv1.GeometryRequest{}
		emptyRequest.SetLeftGeometry(geometryData)
		var err2 *error
		err2, geometry = gs.SingleInputChain(nil, &emptyRequest, nil).ExecuteBlockingSingle()
		err = *err2
	} else {
		err = errors.New("no geometry information in ewkb, wkb, geojson, or wkt")
	}

	if err != nil {
		return &err, nil
	}

	geometry.SetSRIDEx(int(geometryData.Proj.GetEpsg()))

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