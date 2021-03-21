package geometry

import (
	"encoding/binary"
	"errors"
	"flag"
	"fmt"
	eplpbv1 "github.com/geo-grpc/api/golang/epl/protobuf/v1"
	"github.com/twpayne/go-geom"
	"github.com/twpayne/go-geom/encoding/ewkb"
	"github.com/twpayne/go-geom/encoding/geojson"
	"github.com/twpayne/go-geom/encoding/wkb"
	"github.com/twpayne/go-geom/encoding/wkt"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"
	"google.golang.org/grpc/testdata"
	"log"
	"os"
	"sync"
)

var (
	tls                = flag.Bool("tls", false, "Connection uses TLS if true, else plain TCP")
	caFile             = flag.String("ca_file", "", "The file containning the CA root cert file")
	serverAddr         = flag.String("server_addr", "localhost:8980",
		"The server address in the format of host:port")
	serverHostOverride = flag.String("server_host_override", "x.test.youtube.com",
		"The server name use to verify the hostname returned by TLS handshake")
)

var once sync.Once

var geometryServiceClient *Service

func getInstance() *Service {
	if geometryServiceClient == nil {
		once.Do(
			func() {
				fmt.Println("Creting Single Instance Now")
				closeFunc, clientV1 := getConnection()
				geometryServiceClient = &Service{
					ClientV1:  clientV1,
					CleanupV1: closeFunc,
					Proj4326:  	&eplpbv1.ProjectionData{Definition:&eplpbv1.ProjectionData_Epsg{Epsg:4326}},
				}
			})
	} else {
		fmt.Println("Single Instance already created-2")
	}
	return geometryServiceClient
}

func getEnv(key, fallback string) string {
	value, exists := os.LookupEnv(key)
	if !exists {
		value = fallback
	}
	return value
}

func getConnection() (func() error, eplpbv1.GeometryServiceClient) {
	flag.Parse()
	var opts []grpc.DialOption
	if *tls {
		if *caFile == "" {
			*caFile = testdata.Path("ca.pem")
		}
		creds, err := credentials.NewClientTLSFromFile(*caFile, *serverHostOverride)
		if err != nil {
			log.Fatalf("Failed to create TLS credentials %v", err)
		}
		opts = append(opts, grpc.WithTransportCredentials(creds))
	} else {
		opts = append(opts, grpc.WithInsecure())
	}
	serverAddr := getEnv("GEOMETRY_SERVICE_HOST", "localhost:8980")
	conn, err := grpc.Dial(serverAddr, opts...)
	if err != nil {
		log.Fatalf("fail to dial: %v", err)
	}

	client := eplpbv1.NewGeometryServiceClient(conn)
	return func() error {
		return conn.Close()
	}, client
}

func SetSRID(g geom.T, srid int) geom.T {
	switch g := g.(type) {
	case *geom.Point:
		g = g.SetSRID(srid)
	case *geom.LineString:
		g = g.SetSRID(srid)
	case *geom.Polygon:
		g = g.SetSRID(srid)
	case *geom.MultiPoint:
		g = g.SetSRID(srid)
	case *geom.MultiLineString:
		g = g.SetSRID(srid)
	case *geom.MultiPolygon:
		g = g.SetSRID(srid)
	case *geom.GeometryCollection:
		g = g.SetSRID(srid)
	default:
		panic(fmt.Sprintf("unknown geometry type %T", g))
	}

	return g
}

func GeomPbToGeom(geometryData *eplpbv1.GeometryData) (geom.T, error) {
	if geometryData.Proj.GetEpsg() == 0 {
		return nil, errors.New("geometryData needs epsg defined")
	}

	var geometry geom.T
	var err error
	if geometryData.GetEwkb() != nil {
		geometry, err = ewkb.Unmarshal(geometryData.GetEwkb())
	} else if geometryData.GetWkb() != nil {
		geometry, err = wkb.Unmarshal(geometryData.GetWkb())
	} else if len(geometryData.GetWkt()) != 0 {
		geometry, err = wkt.Unmarshal(geometryData.GetWkt())
	} else if len(geometryData.GetGeojson()) != 0 {
		err = geojson.Unmarshal([]byte(geometryData.GetGeojson()), &geometry)
	} else if geometryData.GetEsriShape() != nil {
		request := eplpbv1.GeometryRequest{
			Operator:eplpbv1.OperatorType_IMPORT_FROM_ESRI_SHAPE,
			ResultEncoding:eplpbv1.Encoding_WKB}
		request.SetGeometry(geometryData)

		geometry, err = executeToGeom(&request)
	} else {
		err = errors.New("no geometry information in ewkb, wkb, geojson, or wkt")
	}

	if err != nil {
		return nil, err
	}

	geometry = SetSRID(geometry, int(geometryData.Proj.GetEpsg()))

	return geometry, nil
}

func GeomToGeomPb(t geom.T, g *eplpbv1.GeometryData) (error, *eplpbv1.GeometryData) {
	if t.SRID() == 0 {
		return errors.New("need SRID"), nil
	}

	b, err := wkb.Marshal(t, binary.BigEndian)
	if err != nil {
		return err, nil
	}

	if g == nil {
		g = &eplpbv1.GeometryData{}
	}
	g.SetWkb(b)
	g.Proj = &eplpbv1.ProjectionData{}
	g.Proj.SetEpsg(int32(t.SRID()))

	return nil, g
}

type Service struct {
	ClientV1 eplpbv1.GeometryServiceClient
	CleanupV1 func() error
	Proj4326 *eplpbv1.ProjectionData
}

func relation(left geom.T, right geom.T, operatorType eplpbv1.OperatorType) (bool, error) {
	leftChain := InitChain(left)
	rightChain := InitChain(right)
	return leftChain.relation(rightChain, operatorType)
}

// Creates a buffer around the input geometry
func Buffer(t geom.T, distance float64) (geom.T, error) {
	return InitChain(t).Buffer(distance).Execute()
}

// Calculates the convex hull geometry.
//
// Point - Returns the same point.
// MultiPoint - If the point count is one, returns the same multipoint. If the point count is two, returns a polyline of the points. Otherwise, computes and returns the convex hull polygon.
// Polyline - If consists of only one segment, returns the same polyline. Otherwise, computes and returns the convex hull polygon.
// Polygon - If more than one path or if the path isn't already convex, computes and returns the convex hull polygon. Otherwise, returns the same polygon.
func ConvexHull(t geom.T) (geom.T, error) {
	return InitChain(t).ConvexHull().Execute()
}

// Densify boundary of geometry (LineString and Polygon only)
//
// `maxLength` The maximum segment length allowed. Must be a positive value.
// Curves are densified to straight segments using the
// maxSegmentLength. Curves are split into shorter subcurves such
// that the length of subcurves is shorter than maxSegmentLength.
// After that the curves are replaced with straight segments.
//
func DensifyByLength(t geom.T, maxLength float64) (geom.T, error) {
	return InitChain(t).DensifyByLength(maxLength).Execute()
}

// Performs the Generalize operation on a geometry. Point and
// multipoint geometries are left unchanged.
func Generalize(t geom.T, maxDeviation float64, removeDegenerates bool) (geom.T, error) {
	return InitChain(t).Generalize(maxDeviation, removeDegenerates).Execute()
}

// Creates a geodesic buffer around the input geometries
//
// `distanceMeters` The buffer distances in meters for the Geometry.
// `maxDeviationMeters` The deviation offset to use for convergence.
// The geodesic arcs of the resulting buffer will be closer than the max deviation of the true buffer.
// Pass in NaN to use the default deviation.
func GeodesicBuffer(t geom.T, distanceMeters float64, maxDeviationMeters float64) (geom.T, error) {
	return InitChain(t).GeodesicBuffer(distanceMeters, maxDeviationMeters).Execute()
}

// Densifies input geometries. Attributes are interpolated along the scalar t-values of the
// input segments obtained from the length ratios along the densified segments.
//
// `maxSegmentLengthMeters` The maximum segment length (in meters) allowed. Must be a positive value.
func GeodeticDensifyByLength(t geom.T, maxDistanceMeters float64) (geom.T, error) {
	return InitChain(t).GeodeticDensifyByLength(maxDistanceMeters).Execute()
}

// Performs the Project operation
func Project(t geom.T, resultEpsg int32) (geom.T, error) {
	return InitChain(t).Project(resultEpsg).Execute()
}

// Performs the Simplify operation
//
// `force` When True, the Geometry will be simplified regardless of the internal IsKnownSimple flag.
func Simplify(t geom.T, force bool) (geom.T, error) {
	return InitChain(t).Simplify(force).Execute()
}

// Shift a geometry in the x and y direction. If geodetic is true, then xOffset and yOffset are in meters
func ShiftXY(t geom.T, geodetic bool, xOffset float64, yOffset float64) (geom.T, error) {
	return InitChain(t).ShiftXY(geodetic, xOffset, yOffset).Execute()
}


// Relational operation Contains.
func Contains(left geom.T, right geom.T) (bool, error) {
	return relation(left, right, eplpbv1.OperatorType_CONTAINS)
}

// Relational operation Crosses.
func Crosses(left geom.T, right geom.T) (bool, error) {
	return relation(left, right, eplpbv1.OperatorType_CROSSES)
}

// Relational operation Disjoint.
func Disjoint(left geom.T, right geom.T) (bool, error) {
	return relation(left, right, eplpbv1.OperatorType_DISJOINT)
}

// Relational operation Equals.
func Equals(left geom.T, right geom.T) (bool, error) {
	return relation(left, right, eplpbv1.OperatorType_EQUALS)
}

// Relational operation Intersects.
func Intersects(left geom.T, right geom.T) (bool, error) {
	return relation(left, right, eplpbv1.OperatorType_INTERSECTS)
}

// Relational operation Overlaps.
func Overlaps(left geom.T, right geom.T) (bool, error) {
	return relation(left, right, eplpbv1.OperatorType_OVERLAPS)
}

// Relational operation Touches.
func Touches(left geom.T, right geom.T) (bool, error) {
	return relation(left, right, eplpbv1.OperatorType_TOUCHES)
}

// Relational operation Within.
func Within(left geom.T, right geom.T) (bool, error) {
	return relation(left, right, eplpbv1.OperatorType_WITHIN)
}


// Performs the Topological Difference operation on the two geometries.
//
// `left` is the Geometry instance on the left hand side of the subtraction.
// `right` is the Geometry on the right hand side being subtracted.
// Returns the result of subtraction
func Difference(left geom.T, right geom.T) (geom.T, error) {
	leftChain := InitChain(left)
	return leftChain.Difference(InitChain(right)).Execute()
}

// Performs the Topological Intersection operation on the geometry.
//
// The result has the lower of dimensions of the two geometries. That is, the dimension of the
// Polyline/Polyline intersection is always 1 (that is, for polylines it never returns crossing
// points, but the overlaps only).
func Intersection(left geom.T, right geom.T) (geom.T, error) {
	leftChain := InitChain(left)
	return leftChain.Intersection(InitChain(right)).Execute()
}

// Performs the Symmetric Difference operation on the two geometries.
func SymmetricDifference(left geom.T, right geom.T) (geom.T, error) {
	leftChain := InitChain(left)
	return leftChain.SymmetricDifference(InitChain(right)).Execute()
}

// Performs the Topological Union operation on two geometries.
func Union(left geom.T, right geom.T) (geom.T, error) {
	leftChain := InitChain(left)
	return leftChain.Union(InitChain(right)).Execute()
}


// Calculates the geodetic length of the input Geometry.
func GeodeticLength(g geom.T) (float64, error) {
	return InitChain(g).GeodeticLength()
}

// Calculates the geodetic area of the input Geometry.
func GeodeticArea(g geom.T) (float64, error) {
	return InitChain(g).GeodeticArea()
}
