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
	commonProjectionData *eplpbv1.ProjectionData

	err error
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

		bSame, message := CompareProjectionData(c.commonProjectionData, other.commonProjectionData)
		if !bSame {
			request.OperationProj = c.commonProjectionData
			// TODO put a warning in here that c.commonEpsg is going to be the new projection for relational operators
			//   or any derived features.
			println("automatically choosing projection because '%s'", message)
		}
	}

	request.ResultEncoding = eplpbv1.Encoding_EWKB

	// update unary chain geometry request to be the newly input request
	c.geometryRequest = request
	return c
}

func (c *Unary) relation(other *Unary, operatorType eplpbv1.OperatorType, de9im string) (bool, error) {
	request := &eplpbv1.GeometryRequest{Operator:operatorType}
	if operatorType == eplpbv1.OperatorType_RELATE {
		request.Params = &eplpbv1.GeometryRequest_RelateParams{RelateParams:&eplpbv1.Params_Relate{De_9Im:de9im}}
	}

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


// init a chain object for rpc method construction
func InitChain(initialGeometry geom.T) *Unary {
	err, initialData := GeomToGeomPb(initialGeometry, nil)
	commonEpsg := int32(initialGeometry.SRID())
	commonProjectionData := &eplpbv1.ProjectionData{}
	commonProjectionData.SetEpsg(commonEpsg)
	chain := &Unary{
		geometryRequest: nil,
		initialData:     initialData,
		commonProjectionData:commonProjectionData,
		err:             err,
	}

	return chain
}

func InitChainGeometryData(geometryData *eplpbv1.GeometryData) *Unary {
	chain := &Unary{
		initialData:geometryData,
		commonProjectionData:geometryData.Proj,
	}

	if geometryData.Proj == nil {
		chain.err = errors.New("initial geometry must have a spatial reference")
	}

	return chain
}

// override the projection of the initial data
func (c *Unary) InitProjection(projData *eplpbv1.ProjectionData) *Unary {
	if c.initialData != nil {
		c.initialData.Proj = projData
		c.commonProjectionData = projData
	} else {
		c.err = errors.New("InitProjection must be called immediately after InitChain")
	}

	return c
}

// submit rpc chain of requests for execution
func (c *Unary) Execute() (geom.T, error) {
	if c.geometryRequest == nil {
		c.err = errors.New("cannot execute without geometryRequest")
	}
	if c.err != nil {
		return nil, c.err
	}

	return executeToGeom(c.geometryRequest)
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

// Clip the geometry by and envelope defined by `other`
func (c *Unary) Clip(other *Unary) *Unary {
	request := &eplpbv1.GeometryRequest{Operator:eplpbv1.OperatorType_CLIP}

	return c.append(request, other)
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

// `distance`: The offset distance for the Geometries.
// `joinType`: The join type of the offset geometry. eplpbv1.Params_Offset_BEVEL, eplpbv1.Params_Offset_MITER, eplpbv1.Params_Offset_ROUND, eplpbv1.Params_Offset_SQUARE
// `bevelRatio`: The ratio used to produce a bevel join instead of a miter join (used only when joins is Miter)
// `flattenError`: The maximum distance of the resulting segments compared to the true circular arc (used only when joins is Round). If flattenError is 0, tolerance value is used. Also, the algorithm never produces more than around 180 vertices for each round join.
func (c *Unary) Offset(distance float64, joinType eplpbv1.Params_Offset_OffsetJoinType, bevelRatio float64, flattenError float64) *Unary {
	params := &eplpbv1.GeometryRequest_OffsetParams{OffsetParams:&eplpbv1.Params_Offset{
		Distance:             distance,
		JoinType:             joinType,
		BevelRatio:           bevelRatio,
		FlattenError:         flattenError,
	}}
	request := &eplpbv1.GeometryRequest{
		Params:params,
		Operator:eplpbv1.OperatorType_OFFSET,
	}

	return c.append(request, nil)
}

// Performs the Project operation
func (c *Unary) ProjectEPSG(resultEpsg int32) *Unary {
	proj := &eplpbv1.ProjectionData{
		Definition:           &eplpbv1.ProjectionData_Epsg{Epsg:resultEpsg},
	}
	return c.ProjectProtobuf(proj)
}

func (c *Unary) ProjectProtobuf(projData *eplpbv1.ProjectionData) *Unary {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_PROJECT,
	}

	request.ResultProj = projData

	c.commonProjectionData = projData

	return c.append(request, nil)
}

// Performs the Simplify operation
//
// `force` When True, the Geometry will be simplified regardless of the internal IsKnownSimple flag.
func (c *Unary) Simplify(force bool) *Unary {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_SIMPLIFY,
		Params:               &eplpbv1.GeometryRequest_SimplifyParams{SimplifyParams:&eplpbv1.Params_Simplify{Force:force}},
	}

	return c.append(request, nil)
}

func (c *Unary) SimplifyOGC(force bool) *Unary {
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_SIMPLIFY_OGC,
		Params:               &eplpbv1.GeometryRequest_SimplifyParams{SimplifyParams:&eplpbv1.Params_Simplify{Force:force}},
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
	return c.relation(other, eplpbv1.OperatorType_CONTAINS, "")
}

// Relational operation Crosses.
func (c *Unary) Crosses(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_CROSSES, "")
}

// Relational operation Disjoint.
func (c *Unary) Disjoint(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_DISJOINT, "")
}

// Relational operation Equals.
func (c *Unary) Equals(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_EQUALS, "")
}

// Relational operation Intersects.
func (c *Unary) Intersects(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_INTERSECTS, "")
}

// Relational operation Overlaps.
func (c *Unary) Overlaps(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_OVERLAPS, "")
}

// relational operation DE-9IM
//
// more here: https://en.wikipedia.org/wiki/DE-9IM#Spatial_predicates
func (c *Unary) Relate(other *Unary, de9im string) (bool, error) {
	if len(de9im) != 9 {
		err := errors.New("relate operator de9im input must be a string of 9 characters")
		return false, err
	}

	return c.relation(other, eplpbv1.OperatorType_RELATE, de9im)
}

// Relational operation Touches.
func (c *Unary) Touches(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_TOUCHES, "")
}

// Relational operation Within.
func (c *Unary) Within(other *Unary) (bool, error) {
	return c.relation(other, eplpbv1.OperatorType_WITHIN, "")
}

// It splits the target polyline or polygon where the polygon/polyline is crossed by the cutter polyline.
//
// `considerTouch` True/False indicates whether we consider a touch event a cut
// `other` LineString which will divide the cuttee into pieces where it crosses the cutter.
func (c *Unary) Cut(other *Unary, considerTouch bool) *Unary {
	request := &eplpbv1.GeometryRequest{
		Operator:eplpbv1.OperatorType_CUT,
		Params:&eplpbv1.GeometryRequest_CutParams{CutParams:&eplpbv1.Params_Cut{ConsiderTouch: considerTouch}},
	}

	return c.append(request, other)
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