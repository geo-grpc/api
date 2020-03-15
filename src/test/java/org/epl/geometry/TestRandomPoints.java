package org.epl.geometry;

import com.esri.core.geometry.*;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * Created by davidraleigh on 5/10/17.
 */
public class TestRandomPoints extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testPointCreate() {
        OperatorRandomPoints operatorRandomPoints = OperatorRandomPoints.local();
        Polygon poly = new Polygon();
        poly.startPath(0, 0);
        poly.lineTo(0, 10);
        poly.lineTo(10, 10);
        poly.closePathWithLine();
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        MultiPoint geometry = (MultiPoint) operatorRandomPoints.execute(poly, 1.3, 1977, sr, null);
        assertNotNull(geometry);
        assertEquals(788593, geometry.getPointCount());
        assertNotNull(geometry.getXY(0));
        assertNotNull(geometry.getXY(geometry.getPointCount() - 1));
        Polygon bufferedpoly = (Polygon) OperatorBuffer.local().execute(poly, sr.toSpatialReference(), sr.getTolerance() * 2, null);
        boolean t = OperatorContains.local().execute(bufferedpoly, geometry, sr.toSpatialReference(), null);
        assertTrue(t);
    }

    @Test
    public void testPolygonWithHole() {
        String wktpolygon = "POLYGON((0 0, 0 10, 10 10, 10 0),(3 3, 7 3, 7 7, 3 7))";
        Geometry geometry = GeometryEngine.geometryFromWkt(wktpolygon, 0, Geometry.Type.Unknown);
        MultiPoint multiPoint = (MultiPoint)OperatorRandomPoints.local().execute(geometry, .0013, 1977, SpatialReferenceEx.create(4326), null);

        String wktPolygonNoRing = "POLYGON((0 0, 0 10, 10 10, 10 0))";
        Geometry geometryNoRing = GeometryEngine.geometryFromWkt(wktPolygonNoRing, 0, Geometry.Type.Unknown);
        MultiPoint multiPointNoRing = (MultiPoint)OperatorRandomPoints.local().execute(geometryNoRing, 0.0013, 1977, SpatialReferenceEx.create(4326), null);

        Geometry intersector = OperatorGeodeticDensifyByLength.local().execute(geometry, SpatialReferenceEx.create(4326), 1232535.5660433513, GeodeticCurveType.Geodesic, null);
        Geometry geom = GeometryEngine.intersect(intersector, multiPointNoRing, SpatialReferenceEx.create(4326).toSpatialReference());

        assertEquals(multiPoint.getPointCount(), ((MultiPoint)geom).getPointCount());
    }

    @Test
    public void testMultiPartPolygonCreate() {
        String wktpolygon2 = "MULTIPOLYGON (((0 0, 0 10, 10 10, 10 0)), ((20 0, 20 10, 30 10, 30 0)))";
        Geometry geometry2 = GeometryEngine.geometryFromWkt(wktpolygon2, 0, Geometry.Type.Unknown);
        MultiPoint multiPoint2 = (MultiPoint)OperatorRandomPoints.local().execute(geometry2, 1.3, 1977, SpatialReferenceEx.create(4326), null);

        String wktPolygon = "POLYGON((0 0, 0 10, 10 10, 10 0))";
        Geometry geometry = GeometryEngine.geometryFromWkt(wktPolygon, 0, Geometry.Type.Unknown);
        MultiPoint multiPoint = (MultiPoint)OperatorRandomPoints.local().execute(geometry, 1.3, 1977, SpatialReferenceEx.create(4326), null);

        assertTrue(multiPoint.getPointCount() * 2 > 3179429);
        assertTrue(multiPoint2.getPointCount() * 2 > 3179429);
    }

    @Test
    public void testExcpetion() {
        String wkt = "Polygon((0 0, 0 10, 10 10,10 0))";
        Geometry geometry = GeometryEngine.geometryFromWkt(wkt, 0, Geometry.Type.Unknown);
        try {
            // TODO exception java.lang.OutOfMemoryError: Java heap space for pointsPerSquareKm == 300
            MultiPoint multiPoint = (MultiPoint)OperatorRandomPoints.local().execute(geometry, 1000, 1977, SpatialReferenceEx.create(4326), null);
            fail("Expected an GeometryException to be thrown");
        } catch (GeometryException geometryException) {
            assertEquals(geometryException.getMessage(), "Random Point count outside of available");
        }
    }

    @Test
    public void testSpecificArea() {
        double pointsPerKmSquare = 1;
        Double areaKm = 16207.53;
        int lowEstimate = areaKm.intValue() - 20;
        String wkt = "POLYGON ((46.030485054706105 26.017389342815264, 45.997526070331105 25.016021311217134, 47.469694039081105 24.996108304947285, 47.447721382831105 26.007515943484098, 46.030485054706105 26.017389342815264))";
        Geometry geometry = GeometryEngine.geometryFromWkt(wkt, 0, Geometry.Type.Unknown);

        MultiPoint multiPoint = (MultiPoint)OperatorRandomPoints.local().execute(geometry, pointsPerKmSquare, 1977, SpatialReferenceEx.create(4326), null);

        assertTrue(multiPoint.getPointCount() > lowEstimate);
        assertTrue(multiPoint.getPointCount() < lowEstimate + 20);
    }
}
