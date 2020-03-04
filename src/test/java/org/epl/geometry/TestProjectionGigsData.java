package org.epl.geometry;

import com.esri.core.geometry.MultiPoint;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by davidraleigh on 9/11/17.
 * https://stackoverflow.com/questions/358802/junit-test-with-dynamic-number-of-tests
 * https://nofluffjuststuff.com/blog/paul_duvall/2007/04/take_heed_of_mixing_junit_4_s_parameterized_tests
 * https://github.com/junit-team/junit4/wiki/Parameterized-tests
 */
@RunWith(Parameterized.class)
public class TestProjectionGigsData extends TestCase {
    static {
        System.loadLibrary("proj");
    }

    private Path path;
    private String testName;
    private String testData;
    private String description;
    private double[] tolConversions_i0 = new double[3];
    private double[] tolConversions_i1 = new double[3];
    private double[] tolRoundTrips_i0 = new double[3];
    private double[] tolRoundTrips_i1 = new double[3];
    private int roundTripTimes;
    ProjectionTransformation leftToRightTransformation;
    ProjectionTransformation rightToLeftTransformation;
    private Polyline leftPolyline = new Polyline();
    private Polyline rightPolyline = new Polyline();
    private Polygon leftPolygon = new Polygon();
    private Polygon rightPolygon = new Polygon();
    private MultiPoint leftMultiPoint = new MultiPoint();
    private MultiPoint rightMultiPoint = new MultiPoint();


    public TestProjectionGigsData(Path path, String testName) throws java.io.IOException, org.json.JSONException {
        this.path = path;
        this.testName = testName;
        // http://www.adam-bien.com/roller/abien/entry/java_8_reading_a_file
        String content = new String(Files.readAllBytes(path), Charset.defaultCharset());
        JSONObject obj = new JSONObject(content);
        this.description = obj.getString("description");
        JSONArray projectionsItems = obj.getJSONArray("projections");
        String leftProjection = projectionsItems.getString(0);
        String rightProjection = projectionsItems.getString(1);
        SpatialReferenceEx leftSR = SpatialReferenceEx.createFromProj4(leftProjection);
        SpatialReferenceEx rightSR = SpatialReferenceEx.createFromProj4(rightProjection);
        this.leftToRightTransformation = new ProjectionTransformation(leftSR, rightSR);
        this.rightToLeftTransformation = new ProjectionTransformation(rightSR, leftSR);

        JSONArray coordinatePairs = obj.getJSONArray("coordinates");
        for (int i = 0; i < coordinatePairs.length(); i++) {
            JSONArray coordinatePair = coordinatePairs.getJSONArray(i);
            JSONArray pt1Array = coordinatePair.getJSONArray(0);
            JSONArray pt2Array = coordinatePair.getJSONArray(1);

            Point pt1 = new Point(pt1Array.getDouble(0), pt1Array.getDouble(1));
            Point pt2 = new Point(pt2Array.getDouble(0), pt2Array.getDouble(1));
            if (coordinatePair.getJSONArray(0).length() == 3) {
                // if point3d
                pt1.setZ(pt1Array.getDouble(2));
                pt2.setZ(pt2Array.getDouble(2));
            }

            if (i == 0) {
                leftPolyline.startPath(pt1);
                leftPolygon.startPath(pt1);

                rightPolyline.startPath(pt2);
                rightPolygon.startPath(pt2);
            } else {
                leftPolyline.lineTo(pt1);
                leftPolygon.lineTo(pt1);

                rightPolyline.lineTo(pt2);
                rightPolygon.lineTo(pt2);
            }
            leftMultiPoint.add(pt1);
            rightMultiPoint.add(pt2);
        }

        leftPolygon.closeAllPaths();
        rightPolygon.closeAllPaths();

        /*
        * "tests": [
        * {"tolerances": [2.7777777777777776e-07, 0.03], "type": "conversion"},
        * {"times": 1000, "tolerances": [5.555555555555556e-08, 0.006], "type": "roundtrip"}]
        * */
        JSONArray testsItems = obj.getJSONArray("tests");
        JSONObject testObj1 = testsItems.getJSONObject(0);
        JSONObject testObj2 = testsItems.getJSONObject(1);

        double tolObj1Index1 = testObj1.getJSONArray("tolerances").getDouble(0);
        double tolObj2Index1 = testObj2.getJSONArray("tolerances").getDouble(0);
        double[] tolObj1Index2 = new double[3];
        double[] tolObj2Index2 = new double[3];

        if (testObj1.getJSONArray("tolerances").optJSONArray(1) != null) {
            for (int i = 0; i < 3; i++) {
                tolObj1Index2[i] = testObj1.getJSONArray("tolerances").getJSONArray(1).getDouble(i);
                tolObj2Index2[i] = testObj2.getJSONArray("tolerances").getJSONArray(1).getDouble(i);
            }
        } else {
            Arrays.fill(tolObj1Index2, testObj1.getJSONArray("tolerances").getDouble(1));
            Arrays.fill(tolObj2Index2, testObj2.getJSONArray("tolerances").getDouble(1));
        }

        if (testObj1.getString("type").equals("conversion")) {
            Arrays.fill(this.tolConversions_i0, tolObj1Index1);
            this.tolConversions_i1 = tolObj1Index2;

            Arrays.fill(this.tolRoundTrips_i0, tolObj2Index1);
            this.tolRoundTrips_i1 = tolObj2Index2;
            this.roundTripTimes = testObj2.getInt("times");
        } else {
            Arrays.fill(this.tolConversions_i0, tolObj2Index1);
            this.tolConversions_i1 = tolObj2Index2;

            Arrays.fill(this.tolRoundTrips_i0, tolObj1Index1);
            this.tolRoundTrips_i1 = tolObj1Index2;
            this.roundTripTimes = testObj1.getInt("times");
        }
//        leftPolyline = (Polyline)leftPolygon.getBoundary();
//        rightPolyline = (Polyline)rightPolyline.getBoundary();
    }

    @Test
    public void testConversionPoints() throws Exception {
        assertTrue(this.description, true);

        Point[] leftExpected = new Point[leftMultiPoint.getPointCount()];
        leftMultiPoint.queryCoordinates(leftExpected);

        Point[] rightExpected = new Point[rightMultiPoint.getPointCount()];
        rightMultiPoint.queryCoordinates(rightExpected);

        Point[] rightActual = IntStream.range(0, rightMultiPoint.getPointCount())
                .mapToObj(i -> new Point())
                .toArray(Point[]::new);

        Point[] leftActual = IntStream.range(0, leftMultiPoint.getPointCount())
                .mapToObj(i -> new Point())
                .toArray(Point[]::new);


        OperatorProject.local().transform(this.leftToRightTransformation, leftExpected, leftExpected.length, rightActual);
        //        test_right = self.transform(self.proj_left, self.proj_right, self.coords_left)
        StringBuilder errorMessages = new StringBuilder();
        errorMessages.append("\n").append(this.testName);
        errorMessages.append("\n").append(this.description);
        int nonMatches = listCountMatches(rightActual, rightExpected, this.tolConversions_i1, errorMessages);
        assertEquals(errorMessages.toString(), 0, nonMatches);

        //        results1 = list_count_matches(test_right, self.coords_right, tolerances[1])

        OperatorProject.local().transform(this.rightToLeftTransformation, rightExpected, rightExpected.length, leftActual);
        //        test_left = self.transform(self.proj_right, self.proj_left, self.coords_right)
        nonMatches += listCountMatches(leftActual, leftExpected, this.tolConversions_i0, errorMessages);
        //        results2 = list_count_matches(test_left, self.coords_left, tolerances[0])
        assertEquals(errorMessages.toString(), 0, nonMatches);


//        tolerances = kwargs.get('tolerances', [0.0000000000001, 0.0000000000001])
//


//

//        results2 = list_count_matches(test_left, self.coords_left, tolerances[0])
//
//        return (results1[0] + results2[0], results1[1] + results2[1])
    }

    /**
     * counts coordinates in lists that match and don't match.
     * assumes that lists are the same length (they should be)
     * <p>
     * returns tuple (matches, non_matches)
     * """
     * matches, non_matches = 0, 0
     * iter_ex_coords = iter(ex_coords)
     * for c in coords:
     * ex_coord = next(iter_ex_coords)
     * if match_func(c, ex_coord, tolerance):
     * matches = matches + 1
     * else:
     * non_matches = non_matches + 1
     * <p>
     * return (matches, non_matches)
     */
    public static int listCountMatches(Point[] actualPoints, Point[] expectedPoints, double[] tolerances, StringBuilder errorMessages) {
        // matches, non_matches = 0, 0
        int nonMatches = 0;
        for (int i = 0; i < actualPoints.length; i++) {
            Point actualPoint = actualPoints[i];
            Point expectedPoint = expectedPoints[i];
            String message = matchCheck(actualPoint, expectedPoint, tolerances);
            if (message != null) {
                nonMatches += 1;
                errorMessages.append("\nError at index: ").append(i).append("\n");
                errorMessages.append(message);
            }
        }

        return nonMatches;
    }

    /**
     * Check if coordinate matches expected coordinate within a given tolerance.
     * float coordinate elements will be checked based on this value
     * list/tuple coordinate elements will be checked based on the
     * corresponding values
     *
     * @param pt
     * @param expectedPoint
     * @param tolerance     error rate
     * @return string
     */
    public static String matchCheck(Point pt, Point expectedPoint, double[] tolerance) {
        double[] coord_diff = new double[]{Math.abs(pt.getX() - expectedPoint.getX()), Math.abs(pt.getY() - expectedPoint.getY())};
        if (pt.hasZ())
            coord_diff = new double[]{Math.abs(pt.getX() - expectedPoint.getX()), Math.abs(pt.getY() - expectedPoint.getY()), Math.abs(pt.getZ() - expectedPoint.getZ())};

        boolean matching = true;
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < coord_diff.length; i++) {
            if (coord_diff[i] > tolerance[i]) {
                matching = false;

                stringBuilder.append("Non-match at ");
                if (i == 0)
                    stringBuilder.append(" x position\n");
                else if (i == 1)
                    stringBuilder.append(" y position\n");
                else
                    stringBuilder.append(" z position\n");
                stringBuilder.append("Actual coordinate:\n");
                stringBuilder.append(pt.toString());
                stringBuilder.append("\nExpected coordinate:\n");
                stringBuilder.append(expectedPoint.toString());
                stringBuilder.append("\nActual difference:\n");
                stringBuilder.append("x: ").append(coord_diff[0]).append(" y:").append(coord_diff[1]);
                if (pt.hasZ())
                    stringBuilder.append(" z:").append(coord_diff[2]);
                stringBuilder.append("\nFor tolerances:\n");
                stringBuilder.append("x:").append(tolerance[0]).append(" y:").append(tolerance[1]);
                if (pt.hasZ())
                    stringBuilder.append(" z:").append(tolerance[2]);
                stringBuilder.append("\n\n");
            }
        }
        if (!matching)
            return stringBuilder.toString();

        return null;
    }

//            if len(exc) == 3:
//            # coordinate triples
//    coord_diff = abs(pt.[0] - expectedPoint.[0]), abs(pt.getY() - expectedPoint.getY()), abs(pt.getZ() - expectedPoint.getZ())
//        if isinstance(tolerance, float):
//    matching = coord_diff < (tolerance, tolerance, tolerance)
//    elif isinstance(tolerance, (list, tuple)):  # FIXME is list's length atleast 3?
//    matching = coord_diff < tuple(tolerance)
//            else:
//            # assume coordinate pairs
//    coord_diff = abs(pt.[0] - expectedPoint.[0]), abs(pt.getY() - expectedPoint.getY())
//            if isinstance(tolerance, float):
//    matching = coord_diff < (tolerance, tolerance)
//    elif isinstance(tolerance, (list, tuple)):  # FIXME is list's length atleast 2?
//    matching = coord_diff < tuple(tolerance)
//
//            if matching is False:
//            logging.info('non-match,  calculated coordinate: {c1}\n'
//            'expected coordinate: {c2}\n difference:{res}\n'
//            'tolerance: {tol}\n'
//            ''.format(c1=cor, c2=exc, res=coord_diff, tol=tolerance))
//

    @Test
    public void testRoundtripPoints() throws Exception {
        assertTrue(this.description, true);
    }


    @Test
    public void testConversionMultiPoints() throws Exception {
        assertTrue(this.description, true);
    }

    @Test
    public void testRoundtripMultiPoints() throws Exception {
        assertTrue(this.description, true);
    }

    @Test
    public void testConversionPolyline() throws Exception {
        assertTrue(this.description, true);
    }

    @Test
    public void testRoundtripPolyline() throws Exception {
        assertTrue(this.description, true);
    }

    @Test
    public void testConversionPolygon() throws Exception {
        assertTrue(this.description, true);
    }

    @Test
    public void testRoundtripPolygon() throws Exception {
        assertTrue(this.description, true);
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() throws java.io.IOException, java.net.URISyntaxException {
        // load the files as you want
        URL urls = TestProjectionGigsData.class.getResource("gigs");
        Path gigsDir = Paths.get(urls.toURI());

        // https://stackoverflow.com/a/36815191/445372
        Stream<Path> paths = Files.walk(gigsDir, 1, FileVisitOption.FOLLOW_LINKS);

        Collection<Object[]> data = paths
                // https://stackoverflow.com/a/20533064/445372
                .filter(p -> p.toString().toLowerCase().endsWith(".json"))
                // https://www.mkyong.com/java8/java-8-filter-a-map-examples/
                .map(p -> new Object[]{p, p.getFileName().toString().split(".json")[0].replace('.', '_')})
                // https://www.javabrahman.com/java-8/java-8-how-to-use-collectors-tocollection-collector-with-examples/
                .collect(Collectors.toCollection(ArrayList::new));

        return data;
    }


}
