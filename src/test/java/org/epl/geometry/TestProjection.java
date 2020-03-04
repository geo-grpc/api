package org.epl.geometry;

import com.esri.core.geometry.*;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import org.junit.rules.ExpectedException;
import org.proj4.PJ;

import java.util.Arrays;

import static com.esri.core.geometry.Operator.Type.ExportToJson;
import static com.esri.core.geometry.Operator.Type.Project;

/**
 * Created by davidraleigh on 11/3/16.
 */
public class TestProjection extends TestCase {

    static {
        System.loadLibrary("proj");
    }

    SpatialReferenceEx spatialReferenceWGS = SpatialReferenceEx.create(4326);
    SpatialReferenceEx spatialReferenceMerc = SpatialReferenceEx.create(3857);
    ProjectionTransformation projectionTransformationToMerc = new ProjectionTransformation(spatialReferenceWGS, spatialReferenceMerc);
    ProjectionTransformation projectionTransformationToWGS = new ProjectionTransformation(spatialReferenceMerc, spatialReferenceWGS);

    @Before
    public void setUp() {

    }

    @Test
    public void testProj4() throws Exception {
        PJ sourcePJ = new PJ("+init=epsg:32632");                   // (x,y) axis order
        PJ targetPJ = new PJ("+proj=latlong +datum=WGS84");         // (λ,φ) axis order
        PJ sourcePJ_utm = new PJ("+proj=utm +zone=32 +datum=WGS84 +units=m +no_defs ");
        PJ targetPJ_4326 = new PJ("+init=epsg:4326");

        double[] coordinates_32632 = {
                500000, 0,   // First coordinate
                400000, 100000,   // Second coordinate
                600000, -100000    // Third coordinate
        };

        double[] coordinates_WGS84 = Arrays.copyOf(coordinates_32632, coordinates_32632.length);
        double[] coordinates_4326 = Arrays.copyOf(coordinates_32632, coordinates_32632.length);

        sourcePJ.transform(targetPJ, 2, coordinates_WGS84, 0, 3);
        sourcePJ.transform(targetPJ_4326, 2, coordinates_4326, 0, 3);

        for (int i = 0; i < coordinates_WGS84.length; i++) {
            if (i == 1)
                continue;

            assertTrue((Math.abs(coordinates_WGS84[i] - coordinates_32632[i])) > 1);
            assertTrue((Math.abs(coordinates_4326[i] - coordinates_32632[i])) > 1);
            assertEquals(coordinates_4326[i], coordinates_WGS84[i]);
        }
    }

    @Test
    public void testProj4Strings() {
        SpatialReferenceEx spatialReference = SpatialReferenceEx.create(4326);
        assertEquals("+init=epsg:4326", spatialReference.getProj4());

        spatialReference = SpatialReferenceEx.create(32632);
        assertEquals("+init=epsg:32632", spatialReference.getProj4());
    }

    @Test
    public void testProjectionCursor_1() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(32632), SpatialReferenceEx.create(4326));
        Point point = new Point(500000, 0);
        Point pointOut = (Point) OperatorProject.local().execute(point, projectionTransformation, null);
        assertNotNull(pointOut);
        assertTrue(Math.abs(point.getX() - pointOut.getY()) > 1);
        projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(4326), SpatialReferenceEx.create(32632));
        Point originalPoint = (Point) OperatorProject.local().execute(pointOut, projectionTransformation, null);
        assertEquals(originalPoint.getX(), point.getX());
        assertEquals(originalPoint.getY(), point.getY());
    }

    @Test
    public void testProjectMultiPoint() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(32632), SpatialReferenceEx.create(4326));
        MultiPoint multiPoint = new MultiPoint();
        multiPoint.add(500000, 0);
        multiPoint.add(400000, 100000);
        multiPoint.add(600000, -100000);
        MultiPoint multiPointOut = (MultiPoint) OperatorProject.local().execute(multiPoint, projectionTransformation, null);
        assertNotNull(multiPointOut);
        assertFalse(multiPointOut.equals(multiPoint));
        assertEquals(multiPoint.getPointCount(), multiPointOut.getPointCount());
        projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(4326), SpatialReferenceEx.create(32632));
        MultiPoint originalMultiPoint = (MultiPoint) OperatorProject.local().execute(multiPointOut, projectionTransformation, null);

        for (int i = 0; i < multiPoint.getPointCount(); i++) {
            assertEquals(multiPoint.getPoint(i).getX(), originalMultiPoint.getPoint(i).getX(), 1e-9);
            assertEquals(multiPoint.getPoint(i).getY(), originalMultiPoint.getPoint(i).getY(), 1e-9);
        }
    }

    @Test
    public void testProjectPolyline() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(32632), SpatialReferenceEx.create(4326));
        Polyline polyline = new Polyline();
        polyline.startPath(500000, 0);
        polyline.lineTo(400000, 100000);
        polyline.lineTo(600000, -100000);
        Polyline polylineOut = (Polyline) OperatorProject.local().execute(polyline, projectionTransformation, null);
        assertNotNull(polylineOut);
        assertFalse(polylineOut.equals(polyline));

        MultiPath polyline_impl = (MultiPath) polylineOut;
        int point_count = polyline_impl.getPointCount();
        int path_count = polyline_impl.getPathCount();
        assertEquals(point_count, 3);
        assertEquals(path_count, 1);

        assertEquals(polyline.getPointCount(), polylineOut.getPointCount());
        projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(4326), SpatialReferenceEx.create(32632));
        Polyline originalPolyline = (Polyline) OperatorProject.local().execute(polylineOut, projectionTransformation, null);
        for (int i = 0; i < polyline.getPointCount(); i++) {
            assertEquals(polyline.getPoint(i).getX(), originalPolyline.getPoint(i).getX(), 1e-9);
            assertEquals(polyline.getPoint(i).getY(), originalPolyline.getPoint(i).getY(), 1e-9);
        }
    }

    @Test
    public void testProjectPolygon() {
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(32632), SpatialReferenceEx.create(4326));
        Polygon polygon = new Polygon();
        polygon.startPath(500000, 0);
        polygon.lineTo(400000, 100000);
        polygon.lineTo(600000, -100000);
        polygon.closeAllPaths();
        Polygon polygonOut = (Polygon) OperatorProject.local().execute(polygon, projectionTransformation, null);
        assertNotNull(polygonOut);
        assertFalse(polygonOut.equals(polygon));
        assertEquals(polygon.getPointCount(), polygonOut.getPointCount());
        projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(4326), SpatialReferenceEx.create(32632));
        Polygon originalPolygon = (Polygon) OperatorProject.local().execute(polygonOut, projectionTransformation, null);

        for (int i = 0; i < polygon.getPointCount(); i++) {
            assertEquals(polygon.getPoint(i).getX(), originalPolygon.getPoint(i).getX(), 1e-9);
            assertEquals(polygon.getPoint(i).getY(), originalPolygon.getPoint(i).getY(), 1e-9);
        }
    }

    @Test
    public void testProjectEnvelope() {
        Envelope2D envelope2D = new Envelope2D(-10000, -10000, 10000, 10000);
        String proj4 = String.format(
                "+proj=laea +lat_0=%f +lon_0=%f +x_0=0.0 +y_0=0.0 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs",
                0f, 0f);

        SpatialReferenceEx spatialReference = SpatialReferenceEx.createFromProj4(proj4);
        SpatialReferenceEx spatialReferenceWgs84 = SpatialReferenceEx.create(4326);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(spatialReference, spatialReferenceWgs84);
        Polygon polygon = (Polygon) OperatorProject.local().execute(new Envelope(envelope2D), projectionTransformation, null);
        assertNotNull(polygon);
        Point2D point2D = new Point2D();
        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984
        Envelope2D gcsEnvelope = new Envelope2D();
        polygon.queryEnvelope2D(gcsEnvelope);
        GeoDist.getEnvCenter(a, e2, gcsEnvelope, point2D);
        assertEquals(point2D.x, 0, 1e-14);
        assertEquals(point2D.y, 0, 1e-6);

        // TODO
    }

    @Test
    public void testEPSGCodes() {
        String wktGeom = "MULTIPOLYGON (((6311583.246999994 1871386.1630000025, 6311570 1871325, 6311749.093999997 1871285.9699999988, 6311768.118000001 1871345.9619999975, 6311583.246999994 1871386.1630000025)))";
        SpatialReferenceEx spatialReference = SpatialReferenceEx.create(2230);
        SpatialReferenceEx spatialReferenceWgs84 = SpatialReferenceEx.create(4326);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(spatialReference, spatialReferenceWgs84);
        SimpleStringCursor simpleStringCursor = new SimpleStringCursor(wktGeom);
        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, projectionTransformation, null);
        Geometry geometry = projectCursor.next();

        assertNotNull(geometry);
    }

    @Test
    public void testFoldInto360() {
        String wktGeom = "POLYGON((120 48.2246726495652,140 48.2246726495652,140 25.799891182088334,120 25.799891182088334,120 48.2246726495652))";
        SimpleStringCursor result = new SimpleStringCursor(wktGeom);

        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, result);
        Geometry expectedGeometry = wktCursor.next();

        String wktGeom360 = "POLYGON((480 48.2246726495652,500 48.2246726495652,500 25.799891182088334,480 25.799891182088334,480 48.2246726495652))";
        SimpleStringCursor test = new SimpleStringCursor(wktGeom360);
        wktCursor = new OperatorImportFromWktCursor(0, test);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, this.projectionTransformationToMerc, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, this.projectionTransformationToWGS, null);

        Polygon actualGeometry = (Polygon) reProjectCursor.next();

        assertTrue(GeometryEngineEx.equals(actualGeometry, expectedGeometry, spatialReferenceWGS));
    }


    @Test
    public void testWrapTriangle() {
        String wktGeom = "POLYGON((167 30, 201 49, 199 18, 167 30))";
        SimpleStringCursor simpleStringCursor = new SimpleStringCursor(wktGeom);
        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, this.projectionTransformationToMerc, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, this.projectionTransformationToWGS, null);

        Polygon result = (Polygon) reProjectCursor.next();
        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        OperatorSimplify simplify = (OperatorSimplify) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Simplify);
        boolean isSimple = simplify.isSimpleAsFeature(result, spatialReferenceWGS.toSpatialReference(), true, nonSimpleResult, null);

        simpleStringCursor = new SimpleStringCursor(wktGeom);
        wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        Polygon expected = (Polygon) wktCursor.next();
        assertTrue(GeometryEngineEx.isSimple(expected, spatialReferenceWGS.toSpatialReference()));

        assertEquals(expected.calculateArea2D(), result.calculateArea2D(), 1e-10);
    }

    @Test
    public void testWrapTriangleOtherSide() {
        String wktGeom = "POLYGON((-193 -30, -160 -29, -158 -40, -193 -30))";
        SimpleStringCursor simpleStringCursor = new SimpleStringCursor(wktGeom);
        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, this.projectionTransformationToMerc, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, this.projectionTransformationToWGS, null);

        Polygon result = (Polygon) reProjectCursor.next();
        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        OperatorSimplify simplify = (OperatorSimplify) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Simplify);
        boolean isSimple = simplify.isSimpleAsFeature(result, spatialReferenceWGS.toSpatialReference(), true, nonSimpleResult, null);

        simpleStringCursor = new SimpleStringCursor(wktGeom);
        wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        Polygon expected = (Polygon) wktCursor.next();
        assertTrue(GeometryEngineEx.isSimple(expected, spatialReferenceWGS.toSpatialReference()));

        assertEquals(expected.calculateArea2D(), result.calculateArea2D(), .00000000001);
    }

    @Test
    public void testWrap() {
        String wktGeom = "POLYGON((167.87109375 30.751277776257812," +
                                 "201.43359375 49.38237278700955," +
                                 "232.49609375 -5.266007882805485," +
                                 "116.19500625 -17.308687886770024," +
                                 "199.54296875 18.979025953255267," +
                                 "126.03515625 12.897489183755892," +
                                 "167.87109375 30.751277776257812))";
        SimpleStringCursor simpleStringCursor = new SimpleStringCursor(wktGeom);
        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, this.projectionTransformationToMerc, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, this.projectionTransformationToWGS, null);

        Polygon result = (Polygon) reProjectCursor.next();
        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        OperatorSimplify simplify = (OperatorSimplify) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Simplify);
        boolean isSimple = simplify.isSimpleAsFeature(result, spatialReferenceWGS.toSpatialReference(), true, nonSimpleResult, null);

        simpleStringCursor = new SimpleStringCursor(wktGeom);
        wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        Polygon expected = (Polygon) wktCursor.next();
        assertTrue(GeometryEngineEx.isSimple(expected, spatialReferenceWGS));

        assertEquals(expected.calculateArea2D(), result.calculateArea2D(), 1e-10);
    }

    @Test
    public void testAlbers() {
        String wktGeom = "MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)), ((20 35, 45 20, 30 5, 10 10, 10 30, 20 35), (30 20, 20 25, 20 15, 30 20)))";

        SpatialReferenceEx sr = SpatialReferenceEx.create(2163);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(spatialReferenceWGS, sr);

        SimpleStringCursor simpleStringCursor = new SimpleStringCursor(wktGeom);
        OperatorImportFromWktCursor wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(wktCursor, projectionTransformation, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, projectionTransformation.getReverse(), null);

        Polygon result = (Polygon) reProjectCursor.next();
        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        OperatorSimplify simplify = (OperatorSimplify) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Simplify);
        boolean isSimple = simplify.isSimpleAsFeature(result, spatialReferenceWGS.toSpatialReference(), true, nonSimpleResult, null);
        assertTrue(isSimple);

        simpleStringCursor = new SimpleStringCursor(wktGeom, 99);
        wktCursor = new OperatorImportFromWktCursor(0, simpleStringCursor);
        Polygon expected = (Polygon) wktCursor.next();
        assertTrue(GeometryEngineEx.isSimple(expected, spatialReferenceWGS));
        assertEquals(wktCursor.getGeometryID(), 99);
        assertEquals(expected.calculateArea2D(), result.calculateArea2D(), 1e-10);
    }

    @Test
    public void testProjectionTransformation() {
        int count = 400;
        Envelope e = new Envelope(0,0,40, 40);
        RandomCoordinateGenerator randomCoordinateGenerator = new RandomCoordinateGenerator(count, e, SpatialReferenceEx.create(4326).getTolerance());
        MultiPoint multiPoint = new MultiPoint();
        for (int i = 0; i < count; i++) {
            multiPoint.add(randomCoordinateGenerator._GenerateNewPoint());
        }

        ProjectionTransformation projectionTransformation = ProjectionTransformation.getEqualArea(multiPoint, spatialReferenceWGS);
        Geometry projected = OperatorProject.local().execute(multiPoint, projectionTransformation, null);
        Geometry reprojected = OperatorProject.local().execute(projected, projectionTransformation.getReverse(), null);

        assertTrue(OperatorEquals.local().execute(reprojected, multiPoint, SpatialReferenceEx.create(104919).toSpatialReference(), null));

        Geometry reProjectedConvexhull = OperatorProject.local().execute(OperatorConvexHull.local().execute(projected, null), projectionTransformation.getReverse(), null);
        Geometry convexHull = OperatorConvexHull.local().execute(multiPoint, null);

        assertEquals(convexHull.calculateArea2D(), reProjectedConvexhull.calculateArea2D(), 1);
    }

    @Test
    public void testGeometryEnvelope() {
        MultiPoint multiPoint = new MultiPoint();
        multiPoint.add(0,0);
        multiPoint.add(0,20);
        multiPoint.add(40,40);

        ProjectionTransformation projectionTransformation = ProjectionTransformation.getEqualArea(multiPoint, spatialReferenceWGS);
        Geometry projected = OperatorProject.local().execute(multiPoint, projectionTransformation, null);

        Envelope2D envelope2D = new Envelope2D();
        projected.queryEnvelope2D(envelope2D);

        assertTrue(envelope2D.xmax != 40);

    }

    @Test
    public void testDateline() {
        String wktGeom = "POLYGON((-185 0, -185 10, -170 10, -170 0),(-182 3, -172 3, -172 7, -182 7))";
        Geometry original = OperatorImportFromWkt.local().execute(
                0,
                Geometry.Type.Unknown,
                wktGeom,
                null);
        Geometry projected = OperatorProject.local().execute(
                original,
                projectionTransformationToMerc, null);

        assertNotNull(projected);

        Geometry reProjected = OperatorProject.local().execute(projected, projectionTransformationToWGS, null);
        assertNotNull(reProjected);

        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        assertTrue(OperatorSimplify.local().isSimpleAsFeature(reProjected, spatialReferenceWGS.toSpatialReference(), true, nonSimpleResult, null));

        assertEquals(original.calculateArea2D(), reProjected.calculateArea2D(), 0.00001);
    }

    @Test
    public void testWrapNotWGS84() {
        String wktGeom = "POLYGON((-185 0, -185 10, -170 10, -170 0),(-182 3, -172 3, -172 7, -182 7))";
        Geometry original = OperatorImportFromWkt.local().execute(0,Geometry.Type.Unknown, wktGeom,null);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(4269), spatialReferenceMerc);
        Geometry projected = OperatorProject.local().execute(
                original,
                projectionTransformation, null);

        assertNotNull(projected);

        Geometry reProjected = OperatorProject.local().execute(projected, projectionTransformation.getReverse(), null);
        assertNotNull(reProjected);

        NonSimpleResult nonSimpleResult = new NonSimpleResult();
        assertTrue(OperatorSimplify.local().isSimpleAsFeature(reProjected, SpatialReferenceEx.create(4269).toSpatialReference(), true, nonSimpleResult, null));

        assertEquals(original.calculateArea2D(), reProjected.calculateArea2D(), 0.00001);
    }

    @Test
    public void testReProjectMultiPoint() {
        MultiPoint multiPoint = new MultiPoint();
        for (double longitude = -180; longitude < 180; longitude+=10.0) {
            for (double latitude = -80; latitude < 80; latitude+=10.0) {
                multiPoint.add(longitude, latitude);
            }
        }

        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(4326), SpatialReferenceEx.create(3857));
        Geometry projected = OperatorProject.local().execute(multiPoint, projectionTransformation, null);
        Geometry reprojected = OperatorProject.local().execute(projected, projectionTransformation.getReverse(), null);

        assertTrue(OperatorEquals.local().execute(multiPoint, reprojected, SpatialReferenceEx.create(4326).toSpatialReference(), null));
    }

    @Test
    public void testExampleLAEA_bug() {
        double distance = 800;
        SpatialReferenceEx spatialReferenceLAEAeurope = SpatialReferenceEx.create(3035);
        ProjectionTransformation projectionTransformation = new ProjectionTransformation(spatialReferenceWGS, spatialReferenceLAEAeurope);
        Point europeCenter = new Point(10, 52);
        Point projectedCenter = (Point)OperatorProject.local().execute(europeCenter, projectionTransformation, null);
        Point projected200mN = new Point(projectedCenter.getX(), projectedCenter.getY() + distance);
        Point projected200mE = new Point(projectedCenter.getX() - distance, projectedCenter.getY());
        Point reprojected200mN = (Point)OperatorProject.local().execute(projected200mN, projectionTransformation.getReverse(), null);
        Point reprojected200mE = (Point)OperatorProject.local().execute(projected200mE, projectionTransformation.getReverse(), null);
        double distanceEast = SpatialReferenceExImpl.geodesicDistanceOnWGS84Impl(europeCenter, reprojected200mE);
        double distanceNorth = SpatialReferenceExImpl.geodesicDistanceOnWGS84Impl(europeCenter, reprojected200mN);
        assertEquals(distance, distanceNorth, 1e-3);
        assertEquals(distance, distanceEast, 1e-6);

        projectionTransformation = null;
        ProjectionTransformation projectionTransformationEA = ProjectionTransformation.getEqualArea(europeCenter, spatialReferenceWGS);
        projectedCenter = (Point)OperatorProject.local().execute(europeCenter, projectionTransformationEA, null);
        projected200mN = new Point(projectedCenter.getX(), projectedCenter.getY() + distance);
        projected200mE = new Point(projectedCenter.getX() - distance, projectedCenter.getY());
        reprojected200mN = (Point)OperatorProject.local().execute(projected200mN, projectionTransformationEA.getReverse(), null);
        reprojected200mE = (Point)OperatorProject.local().execute(projected200mE, projectionTransformationEA.getReverse(), null);
        distanceEast = SpatialReferenceExImpl.geodesicDistanceOnWGS84Impl(europeCenter, reprojected200mE);
        distanceNorth = SpatialReferenceExImpl.geodesicDistanceOnWGS84Impl(europeCenter, reprojected200mN);
        assertEquals(distance, distanceNorth, 1e-3);
        assertEquals(distance, distanceEast, 1e-6);

    }

//    @Test
//    public void testEqualAreaProjection() {
//        //        POINT (-70.651886936570745 43.134525834585826)
//        Point point = new Point(-70.651886936570745, 43.1345088834585826);
//        SpatialReferenceEx utmZone = SpatialReferenceEx.createUTM(point);
//        SpatialReferenceEx spatialReferenceWGS84 = SpatialReferenceEx.create(4326);
//        ProjectionTransformation projectionTransformationUTM = new ProjectionTransformation(spatialReferenceWGS84, utmZone);
//
//        ProjectionTransformation projectionTransformationEA = ProjectionTransformation.getEqualArea(point, spatialReferenceWGS84);
//
//        Geometry geodesicBuffered = OperatorGeodesicBuffer.local().execute(
//                point,
//                spatialReferenceWGS84,
//                GeodeticCurveType.Geodesic,
//                135.7,
//                0.07,
//                false,
//                null);
//
//        Geometry utmPoint = GeometryEngineEx.project(point, spatialReferenceWGS84, utmZone);
//        Geometry utmPoint2 = OperatorProject.local().execute(point, projectionTransformationUTM, null);
//        assertTrue(utmPoint.equals(utmPoint2));
//
//        Geometry utmReprojected = GeometryEngineEx.project(utmPoint, utmZone, spatialReferenceWGS84);
//        Geometry utmReprojected2 = OperatorProject.local().execute(utmPoint2, projectionTransformationUTM.getReverse(), null);
//        assertTrue(utmReprojected.equals(utmReprojected2));
//
//        Geometry utmBuffer = OperatorBuffer.local().execute(utmPoint, utmZone.toSpatialReference(), 135.6, null);
//        Geometry reProjectedUTMBuffer = OperatorProject.local().execute(utmBuffer, projectionTransformationUTM.getReverse(), null);
//        double differenceVal = GeometryEngineEx.difference(reProjectedUTMBuffer, geodesicBuffered, spatialReferenceWGS84).calculateArea2D();
//        assertEquals(differenceVal, 0.0, 1e-14);
//
//        assertTrue(GeometryEngineEx.contains(geodesicBuffered, reProjectedUTMBuffer, spatialReferenceWGS84));
//
//        Geometry aziEAPoint = OperatorProject.local().execute(point, projectionTransformationEA, null);
//        Geometry aziEABuffere = OperatorBuffer.local().execute(aziEAPoint, null, 135.6, null);
//        assertEquals(aziEABuffere.calculateArea2D(), utmBuffer.calculateArea2D(), 1e-5);
//        Geometry reProjectedAZIBuffer = OperatorProject.local().execute(aziEABuffere, projectionTransformationEA.getReverse(), null);
//
//        Geometry projectedGeodesicAZI = OperatorProject.local().execute(geodesicBuffered, projectionTransformationEA, null);
//        Geometry reprojectedGeodesicAZI = OperatorProject.local().execute(projectedGeodesicAZI, projectionTransformationEA.getReverse(), null);
//        differenceVal = GeometryEngineEx.difference(reProjectedAZIBuffer, reprojectedGeodesicAZI, spatialReferenceWGS84).calculateArea2D();
//        assertEquals(differenceVal, 0.0, 1e-14);
//
//        differenceVal = GeometryEngineEx.difference(reProjectedAZIBuffer, geodesicBuffered, spatialReferenceWGS84).calculateArea2D();
//        assertEquals(differenceVal, 0.0, 1e-14);
//
//        assertTrue(GeometryEngineEx.contains(geodesicBuffered, reProjectedAZIBuffer, spatialReferenceWGS84));
//
////        String wktInput = "POLYGON ((-70.651656936570745 43.135157834585826,-70.651570654984525 43.135150076925918,-70.651511247908587 43.135155437576621,-70.651490101488349 43.135169249238288,-70.651490181628759 43.135200763774868,-70.651510336532638 43.135249995303397,-70.651530872098633 43.135299226354952,-70.651527230781454 43.13533529757845,-70.651492643684321 43.135349305799508,-70.651420172717224 43.135354852495823,-70.651358095085072 43.135346745253884,-70.651247805462717 43.1353033127629,-70.651151361236941 43.135246176463966,-70.651124154255413 43.135206047556842,-70.651131534466685 43.135151913051203,-70.651155914561755 43.135115542493892,-70.651228730259774 43.135051460048707,-70.651305024993519 43.134973818456416,-70.651360674948108 43.134914489191551,-70.651385078643216 43.134864606853156,-70.651402827815218 43.134810322639233,-70.651410310326284 43.134688652284062,-70.651418383361829 43.134517448375689,-70.651411370788409 43.134413996600074,-70.651390876709428 43.134337752182525,-70.651346755889904 43.134225832686319,-70.651289063395154 43.134109606122337,-70.651241242017051 43.133988734078358,-70.651230854601764 43.133916846697829,-70.651221013347353 43.133822439140715,-70.651200519724128 43.133746194674302,-70.651218268699594 43.133691907971006,-70.651249886578313 43.133610413350418,-70.651273539332081 43.133547040518167,-70.651322422674269 43.133478800504768,-70.651363984222755 43.133424172537978,-70.651409243106613 43.133378494590104,-70.651437060507462 43.133355583224628,-70.651492145648632 43.133332275678633,-70.651599176252716 43.133326227462319,-70.651675204034788 43.133338636120527,-70.65179240423565 43.133381970944697,-70.651905761872953 43.133425353603045,-70.652022336036893 43.133459688797871,-70.652087607730962 43.133472252516562,-70.652149929372143 43.133489361765591,-70.652232540703992 43.133546699550045,-70.652400720753761 43.133656829257461,-70.652544825855031 43.133771806958798,-70.652698657471404 43.133891149422396,-70.652757214424 43.133939825915917,-70.652829643131227 43.133961291723317,-70.652898471779253 43.133991812912122,-70.652963804959569 43.134049399817712,-70.652994471176711 43.134089475795044,-70.653000937222828 43.134129903976664,-70.653000836372897 43.134197439788288,-70.652972837499135 43.134256375321229,-70.652903321638362 43.134342923487154,-70.652853489821567 43.134447199036558,-70.652811504521395 43.134528843881576,-70.652776716073816 43.134592380423527,-70.652745241323998 43.134664872485907,-70.652703717209832 43.134692485155824,-70.65263790191085 43.134702441934749,-70.652537796343609 43.13469488220742,-70.652481986521252 43.134691185475688,-70.652416288981371 43.134705643385693,-70.652347112331768 43.134733655368471,-70.652274294763117 43.134797738553125,-70.652239263183958 43.134852272522785,-70.652214826278225 43.134929165745028,-70.652217533236211 43.135001163954207,-70.652248296977746 43.135059250554882,-70.652327397729081 43.135157158057844,-70.652414500765317 43.135209927851037,-70.6524582645621 43.135222802033766,-70.652470512866358 43.135249637832572,-70.652646824360005 43.135404671730349,-70.652680946443695 43.135444700365824,-70.652691113545444 43.135494075980539,-70.652680299690047 43.135534756334458,-70.652642334694633 43.135580326883741,-70.6525831461027 43.135608197219,-70.65250042683256 43.135618395612582,-70.652403778584315 43.1356107883392,-70.652328034290349 43.135580366756479,-70.652207940292428 43.135501055652128,-70.652084166050003 43.135399292820672,-70.651994153636025 43.135324055019495,-70.651897729053019 43.13525341019011,-70.651815354685922 43.135205077192047,-70.651746646220062 43.135179051560065,-70.651656936570745 43.135157834585826))";
////        Geometry input = OperatorImportFromWkt.local().execute(0, Geometry.Type.Unknown, wktInput, null);
////        Geometry encircled = OperatorEnclosingCircle.local().execute(input, spatialReferenceWGS84, null);
////
////        assertEquals(encircled.calculateArea2D(), buffered.calculateArea2D(), 1e-8);
////        assertEquals(0.0, OperatorDifference.local().execute(encircled, buffered, spatialReferenceWGS84, null).calculateArea2D(), 1e-8);
////        assertTrue(GeometryEngineEx.contains(buffered, encircled, spatialReferenceWGS84));
//    }

    @Test
    public void testETRS() {
        Point point = new Point(-180, -90);
        SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(point);

        ProjectionTransformation projectionTransformation = new ProjectionTransformation(SpatialReferenceEx.create(4326), SpatialReferenceEx.create(3035));
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(simpleGeometryCursor, projectionTransformation, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, projectionTransformation.getReverse(), null);

        while (reProjectCursor.hasNext()) {
            Geometry geometry = reProjectCursor.next();
            assertTrue(geometry.isEmpty());
        }
    }

    @Test
    public void testProjectionTrans() {
        Point point = new Point(-180, -90);
        SimpleGeometryCursor simpleGeometryCursor = new SimpleGeometryCursor(point);

        ProjectionTransformation projectionTransformation = new ProjectionTransformation(null, SpatialReferenceEx.create(3035));
        OperatorProjectCursor projectCursor = new OperatorProjectCursor(simpleGeometryCursor, projectionTransformation, null);
        OperatorProjectCursor reProjectCursor = new OperatorProjectCursor(projectCursor, projectionTransformation.getReverse(), null);


        while (reProjectCursor.hasNext()) {
            try {
                Geometry geometry = reProjectCursor.next();
                assertTrue(geometry.isEmpty());
            } catch (GeometryException exception) {
                assertEquals("From and To Spatial references required to Project Geometry", exception.getMessage());
            }
        }
    }

    @Test
    public void testIdNumber() {
        assertEquals(0, Project.ordinal());
        assertEquals(1, ExportToJson.ordinal());
    }

    @Test
    public void testAziEA() {
        MultiPoint multiPoint = new MultiPoint();
        multiPoint.add(0,0);
        multiPoint.add(1,1);
        multiPoint.add(-1,-1);
        SpatialReferenceEx utmZone = SpatialReferenceEx.createUTM(multiPoint);
        SpatialReferenceEx spatialReferenceWGS84 = SpatialReferenceEx.create(4326);
        ProjectionTransformation projectionTransformationUTM = new ProjectionTransformation(spatialReferenceWGS84, utmZone);
        Geometry utmPoint = GeometryEngineEx.project(multiPoint, spatialReferenceWGS84, utmZone);
        Geometry utmPoint2 = OperatorProject.local().execute(multiPoint, projectionTransformationUTM, null);
        ProjectionTransformation projectionTransformationAziUTM = ProjectionTransformation.getEqualArea(utmPoint, utmZone);
        ProjectionTransformation projectionTransformationAziWGS84 = ProjectionTransformation.getEqualArea(multiPoint, spatialReferenceWGS84);
        Geometry aziPoint1 = OperatorProject.local().execute(utmPoint, projectionTransformationAziUTM, null);
        Geometry aziPoint2 = OperatorProject.local().execute(multiPoint, projectionTransformationAziWGS84, null);
        assertEquals(((MultiPoint)aziPoint1).getPoint(0).getX(), ((MultiPoint)aziPoint2).getPoint(0).getX());
        Geometry utmPoint1 = OperatorProject.local().execute(aziPoint1, projectionTransformationAziUTM.getReverse(), null);
        Geometry wgs84Point1 = OperatorProject.local().execute(aziPoint2, projectionTransformationAziWGS84.getReverse(), null);
        Geometry wgs84Point2 = OperatorProject.local().execute(utmPoint1, projectionTransformationUTM.getReverse(), null);
        assertEquals(((MultiPoint)wgs84Point1).getPoint(2).getY(), ((MultiPoint)wgs84Point2).getPoint(2).getY(), 0.0000000000001);
    }

    @Test
    public void testAziRoundTrip() {
        String wkt= "POLYGON ((39.99430071558862 19.99640653733888, 39.99430071558862 20.00359325081125, 40.00569928441138 20.00359325081125, 40.00569928441138 19.99640653733888, 39.99430071558862 19.99640653733888))";
        Geometry geometry = GeometryEngineEx.geometryFromWkt(wkt, 0, Geometry.Type.Unknown);
        SpatialReferenceEx spatialReference4326 = SpatialReferenceEx.create(4326);
        SpatialReferenceEx spatialReferenceAzi = SpatialReferenceEx.createEqualArea(40, 20);

        // TODO this loses data
        Geometry roundTrip = GeometryEngineEx.project(GeometryEngineEx.project(geometry, spatialReference4326, spatialReferenceAzi), spatialReferenceAzi, spatialReference4326);
        // TODO lost data if it were assertTrue(GeometryEngineEx.equals(geometry, roundTrip, spatialReference4326)); it would faile

        Geometry buffered = GeometryEngineEx.buffer(geometry, spatialReference4326, spatialReference4326.getTolerance() * 3);
        assertTrue(GeometryEngineEx.contains(buffered, roundTrip, spatialReference4326));
        Geometry difference = GeometryEngineEx.difference(geometry, roundTrip, spatialReference4326);
        assertEquals(difference.calculateArea2D(), 0.0);
    }
}