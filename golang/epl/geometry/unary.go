package geometry

import (
	"context"
	"errors"
	"fmt"
	eplpbv1 "github.com/geo-grpc/api/golang/epl/protobuf/v1"
	"github.com/twpayne/go-geom"
	"math"
)

type Unary struct {
	geometryRequest *eplpbv1.GeometryRequest
	initialData *eplpbv1.GeometryData
	commonEpsg int32
	err error
}

// init a chain object for rpc method construction
func InitChain(initialGeometry geom.T) *Unary {
	chain := &Unary{
		initialData: &eplpbv1.GeometryData{},
	}
	chain.err, chain.initialData = GeomToGeomPb(initialGeometry, nil)
	chain.commonEpsg = int32(initialGeometry.SRID())

	return chain
}

func (c *Unary) append(request *eplpbv1.GeometryRequest, other *Unary) *Unary {
	if c.err != nil {
		return c
	} else if other != nil && other.err != nil {
		c.err = other.err
		return c
	}

	if c.geometryRequest != nil {
		request.SetLeftGeometryRequest(c.geometryRequest)
	} else if c.initialData != nil {
		request.SetGeometry(c.initialData)
		c.initialData = nil
	} else {
		c.err = errors.New("left geometry broken or missing")
		return c
	}

	if !request.HasGeometryInput() {
		err := errors.New("either left geometry or left chain request must be defined")
		c.err = err
		return c
	}


	if other != nil {
		if other.geometryRequest != nil {
			request.SetRightGeometryRequest(other.geometryRequest)
		} else if other.initialData != nil {
			request.SetRightGeometry(other.initialData)
			other.initialData = nil
		} else {
			c.err = errors.New("right geometry broken or missing")
			return c
		}

		if !request.HasRightGeometryInput() {
			err := errors.New("either right geometry or right chain request must be defined")
			c.err = err
			return c
		}

		if c.commonEpsg != other.commonEpsg {
			request.OperationProj = &eplpbv1.ProjectionData{}
			request.OperationProj.SetEpsg(c.commonEpsg)
			// TODO put a warning in here that c.commonEpsg is going to be the new projection for relational operators
			//   or any derived features.
		}
	}

	request.ResultEncoding = eplpbv1.Encoding_EWKB

	// update unary chain geometry request to be the newly input request
	c.geometryRequest = request
	return c
}

func (c *Unary) relation(other *Unary, operatorType eplpbv1.OperatorType) (bool, error) {
	request := &eplpbv1.GeometryRequest{Operator:operatorType}

	unary := c.append(request, other)
	if unary.err != nil {
		return false, unary.err
	}

	requestResult, err := getInstance().ClientV1.Operate(context.Background(), unary.geometryRequest)
	if err != nil {
		return false, errors.New(fmt.Sprintf("geometry service error with value:\n%v", err.Error()))
	}

	return requestResult.GetSpatialRelationship(), nil
}

func (c *Unary) measure(request *eplpbv1.GeometryRequest) (float64, error) {
	unary := c.append(request, nil)

	if unary.err != nil {
		return math.NaN(), unary.err
	}

	requestResult, err := getInstance().ClientV1.Operate(context.Background(), unary.geometryRequest)
	if err != nil {
		return math.NaN(), errors.New(fmt.Sprintf("geometry service error with value:\n%v", err.Error()))
	}

	return requestResult.GetMeasure(), nil
}

// submit rpc chain of requests for execution
func (c *Unary) Execute() (geom.T, error) {
	if c.err != nil {
		return nil, c.err
	}

	return executeToGeom(c.geometryRequest)
}

func executeToGeom(request* eplpbv1.GeometryRequest) (geom.T, error) {
	requestResult, err := getInstance().ClientV1.Operate(context.Background(), request)
	if err != nil {
		return nil, errors.New(fmt.Sprintf("geometry service error with value:\n%v", err.Error()))
	}

	if requestResult.GetGeometry() == nil || requestResult.GetGeometry().Proj == nil {
		// TODO does this ever occur?
		return nil, errors.New("geometry Result incomplete")
	}

	return GeomPbToGeom(requestResult.GetGeometry())
}


// Creates a buffer around the input geometry
func (c *Unary) Buffer(distance float64) *Unary {
	params := &eplpbv1.Params_Buffer{Distance:distance}
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_BUFFER,
		Params:               &eplpbv1.GeometryRequest_BufferParams{BufferParams:params},
	}

	return c.append(request, nil)
}

// Calculates the convex hull geometry.
//
// Point - Returns the same point.
// MultiPoint - If the point count is one, returns the same multipoint. If the point count is two, returns a polyline of the points. Otherwise, computes and returns the convex hull polygon.
// Polyline - If consists of only one segment, returns the same polyline. Otherwise, computes and returns the convex hull polygon.
// Polygon - If more than one path or if the path isn't already convex, computes and returns the convex hull polygon. Otherwise, returns the same polygon.
func (c *Unary) ConvexHull() *Unary {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_CONVEX_HULL,
	}

	return c.append(request, nil)
}

// Densify boundary of geometry (LineString and Polygon only)
//
// `maxLength` The maximum segment length allowed. Must be a positive value.
// Curves are densified to straight segments using the
// maxSegmentLength. Curves are split into shorter subcurves such
// that the length of subcurves is shorter than maxSegmentLength.
// After that the curves are replaced with straight segments.
//
func (c *Unary) DensifyByLength(maxLength float64) *Unary {
	params := &eplpbv1.Params_Densify{MaxLength: maxLength}
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_DENSIFY_BY_LENGTH,
		Params:               &eplpbv1.GeometryRequest_DensifyParams{DensifyParams:params},
	}

	return c.append(request, nil)
}

// Performs the Generalize operation on a geometry. Point and
// multipoint geometries are left unchanged.
func (c *Unary) Generalize(maxDeviation float64, removeDegenerates bool) *Unary {
	params := &eplpbv1.Params_Generalize{
		MaxDeviation:         maxDeviation,
		RemoveDegenerates:    removeDegenerates,
	}
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_GENERALIZE,
		Params:&eplpbv1.GeometryRequest_GeneralizeParams{GeneralizeParams:params},
	}

	return c.append(request, nil)
}

// Creates a geodesic buffer around the input geometries
//
// `distanceMeters` The buffer distances in meters for the Geometry.
// `maxDeviationMeters` The deviation offset to use for convergence.
// The geodesic arcs of the resulting buffer will be closer than the max deviation of the true buffer.
// Pass in NaN to use the default deviation.
func (c *Unary) GeodesicBuffer(distanceMeters float64, maxDeviationMeters float64) *Unary {
	params := &eplpbv1.Params_GeodeticBuffer{
		Distance:		distanceMeters,
		MaxDeviation:	maxDeviationMeters,
	}
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_GEODESIC_BUFFER,
		Params:               &eplpbv1.GeometryRequest_GeodeticBufferParams{GeodeticBufferParams:params},
	}

	return c.append(request, nil)
}

// Densifies input geometries. Attributes are interpolated along the scalar t-values of the
// input segments obtained from the length ratios along the densified segments.
//
// `maxSegmentLengthMeters` The maximum segment length (in meters) allowed. Must be a positive value.
func (c *Unary) GeodeticDensifyByLength(maxLengthMeters float64) *Unary {
	params := &eplpbv1.Params_Densify{MaxLength: maxLengthMeters}
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_GEODETIC_DENSIFY_BY_LENGTH,
		Params:               &eplpbv1.GeometryRequest_DensifyParams{DensifyParams:params},
	}

	return c.append(request, nil)
}

// Performs the Project operation
func (c *Unary) Project(resultEpsg int32) *Unary {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_PROJECT,
	}

	request.ResultProj = &eplpbv1.ProjectionData{
		Definition:           &eplpbv1.ProjectionData_Epsg{Epsg:resultEpsg},
	}

	c.commonEpsg = resultEpsg

	return c.append(request, nil)
}

// Performs the Simplify operation
//
// `force` When True, the Geometry will be simplified regardless of the internal IsKnownSimple flag.
func (c *Unary) Simplify(force bool) *Unary {
	params := &eplpbv1.Params_Simplify{Force:force}
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_SIMPLIFY,
		Params:               &eplpbv1.GeometryRequest_SimplifyParams{SimplifyParams:params},
	}

	return c.append(request, nil)
}

// Shift a geometry in the x and y direction. If geodetic is true, then xOffset and yOffset are in meters
func (c *Unary) ShiftXY(geodetic bool, xOffset float64, yOffset float64) *Unary {
	params := &eplpbv1.Params_AffineTransform{
		Geodetic:             geodetic,
		XOffset:              xOffset,
		YOffset:              yOffset,
	}
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_AFFINE_TRANSFORM,
		Params:&eplpbv1.GeometryRequest_AffineTransformParams{AffineTransformParams:params},
	}

	return c.append(request, nil)
}


// Relational operation Contains.
func (c *Unary) Contains(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_CONTAINS)
}

// Relational operation Crosses.
func (c *Unary) Crosses(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_CROSSES)
}

// Relational operation Disjoint.
func (c *Unary) Disjoint(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_DISJOINT)
}

// Relational operation Equals.
func (c *Unary) Equals(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_EQUALS)
}

// Relational operation Intersects.
func (c *Unary) Intersects(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_INTERSECTS)
}

// Relational operation Overlaps.
func (c *Unary) Overlaps(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_OVERLAPS)
}

// Relational operation Touches.
func (c *Unary) Touches(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_TOUCHES)
}

// Relational operation Within.
func (c *Unary) Within(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_WITHIN)
}


// Performs the Topological Difference operation on the two geometries.
//
// `left` is the Geometry instance on the left hand side of the subtraction.
// `right` is the Geometry on the right hand side being subtracted.
// Returns the result of subtraction
func (c *Unary) Difference(other *Unary) *Unary {
	request := &eplpbv1.GeometryRequest{
		Operator: eplpbv1.OperatorType_DIFFERENCE,
	}

	return c.append(request, other)
}

// Performs the Topological Intersection operation on the geometry.
//
// The result has the lower of dimensions of the two geometries. That is, the dimension of the
// Polyline/Polyline intersection is always 1 (that is, for polylines it never returns crossing
// points, but the overlaps only).
func (c *Unary) Intersection(other *Unary) *Unary {
	request := &eplpbv1.GeometryRequest{
		Operator: eplpbv1.OperatorType_INTERSECTION,
	}

	return c.append(request, other)
}

// Performs the Symmetric Difference operation on the two geometries.
func (c *Unary) SymmetricDifference(other *Unary) *Unary {
	request := &eplpbv1.GeometryRequest{
		Operator: eplpbv1.OperatorType_SYMMETRIC_DIFFERENCE,
	}

	return c.append(request, other)
}

// Performs the Topological Union operation on two geometries.
func (c *Unary) Union(other *Unary) *Unary {
	request := &eplpbv1.GeometryRequest{
		Operator: eplpbv1.OperatorType_UNION,
	}

	return c.append(request, other)
}


// Calculates the geodetic length of the input Geometry.
func (c *Unary) GeodeticLength() (float64, error) {
	request := &eplpbv1.GeometryRequest{
		Operator: eplpbv1.OperatorType_GEODETIC_LENGTH,
	}

	return c.measure(request)
}

// Calculates the geodetic area of the input Geometry.
func (c *Unary) GeodeticArea() (float64, error) {
	request := &eplpbv1.GeometryRequest{
		Operator: eplpbv1.OperatorType_GEODETIC_AREA,
	}

	return c.measure(request)
}