package org.epl.geometry;

import com.esri.core.geometry.*;
import junit.framework.TestCase;
import org.junit.Test;

public class TestGeodetic extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testCoastalPolyline() throws Exception {
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        String geoJSON = "{ \"type\": \"LineString\", \"coordinates\": [ [ -90.418136, 46.566094 ], [ -90.348407, 46.600635 ], [ -90.327626, 46.607744 ], [ -90.306609, 46.602741 ], [ -90.265294, 46.618516 ], [ -90.237609, 46.624485 ], [ -90.164026, 46.645515 ], [ -90.100695, 46.655132 ], [ -90.045420, 46.668272 ], [ -90.028392, 46.674390 ], [ -89.996034, 46.693225 ], [ -89.985817, 46.703190 ], [ -89.973803, 46.710322 ], [ -89.957101, 46.716929 ], [ -89.918466, 46.740324 ], [ -89.892355, 46.763088 ], [ -89.848652, 46.795711 ], [ -89.831956, 46.804053 ], [ -89.790663, 46.818469 ], [ -89.720277, 46.830413 ], [ -89.673375, 46.833229 ], [ -89.660625, 46.831056 ], [ -89.642255, 46.825340 ], [ -89.634938, 46.819488 ], [ -89.619329, 46.818890 ], [ -89.569808, 46.831859 ], [ -89.535683, 46.835878 ], [ -89.513938, 46.841835 ], [ -89.499080, 46.841621 ], [ -89.491252, 46.838448 ], [ -89.471540, 46.837359 ], [ -89.437047, 46.839512 ], [ -89.415154, 46.843983 ], [ -89.372032, 46.857386 ], [ -89.249143, 46.903326 ], [ -89.227914, 46.912954 ], [ -89.201511, 46.931149 ], [ -89.168244, 46.965536 ], [ -89.142595, 46.984859 ], [ -89.128698, 46.992599 ], [ -89.118339, 46.994220 ], [ -89.113158, 46.989356 ], [ -89.106277, 46.986480 ], [ -89.086742, 46.985298 ], [ -89.063103, 46.988522 ], [ -89.039490, 46.999419 ], [ -89.028930, 47.001140 ], [ -89.022994, 46.995120 ], [ -88.998417, 46.995314 ], [ -88.987197, 46.997239 ], [ -88.972802, 47.002096 ], [ -88.959409, 47.008496 ], [ -88.944045, 47.020129 ], [ -88.924492, 47.042156 ], [ -88.914189, 47.059246 ], [ -88.903706, 47.086161 ], [ -88.889140, 47.100575 ], [ -88.855372, 47.114263 ], [ -88.848176, 47.115065 ], [ -88.814834, 47.141399 ], [ -88.789813, 47.150925 ], [ -88.778022, 47.150465 ], [ -88.764351, 47.155762 ], [ -88.729688, 47.185834 ], [ -88.699660, 47.204831 ], [ -88.672395, 47.219137 ], [ -88.656359, 47.225624 ], [ -88.640323, 47.226784 ], [ -88.623579, 47.232352 ], [ -88.609830, 47.238894 ], [ -88.584912, 47.242361 ], [ -88.573997, 47.245989 ], [ -88.500780, 47.293503 ], [ -88.477733, 47.313460 ], [ -88.470484, 47.327653 ], [ -88.459262, 47.339903 ], [ -88.418673, 47.371188 ], [ -88.389459, 47.384431 ], [ -88.324083, 47.403542 ], [ -88.303447, 47.412204 ], [ -88.285195, 47.422392 ], [ -88.239161, 47.429969 ], [ -88.227446, 47.435093 ], [ -88.218424, 47.441585 ], [ -88.216977, 47.445493 ], [ -88.217822, 47.448738 ], [ -88.181820, 47.457657 ], [ -88.150571, 47.460093 ], [ -88.139651, 47.462693 ], [ -88.085252, 47.468961 ], [ -88.076388, 47.467752 ], [ -88.049326, 47.469785 ], [ -88.048226, 47.470008 ], [ -88.048077, 47.474973 ], [ -88.040291, 47.475999 ], [ -87.978934, 47.479420 ], [ -87.929269, 47.478737 ], [ -87.902416, 47.477045 ], [ -87.898036, 47.474872 ], [ -87.816958, 47.471998 ], [ -87.801184, 47.473301 ], [ -87.756739, 47.460717 ], [ -87.730804, 47.449112 ], [ -87.715942, 47.439816 ], [ -87.710471, 47.406200 ], [ -87.712421, 47.401400 ], [ -87.721274, 47.401032 ], [ -87.742417, 47.405823 ], [ -87.751380, 47.405066 ], [ -87.759057, 47.403013 ], [ -87.765019, 47.398652 ], [ -87.800294, 47.392148 ], [ -87.815371, 47.384790 ], [ -87.827115, 47.386160 ], [ -87.834822, 47.390478 ], [ -87.848252, 47.394864 ], [ -87.856700, 47.395387 ], [ -87.882245, 47.395588 ], [ -87.941613, 47.390073 ], [ -87.957058, 47.387260 ], [ -87.965063, 47.374430 ], [ -87.965598, 47.368645 ], [ -87.962567, 47.362543 ], [ -87.954796, 47.356809 ], [ -87.947397, 47.355461 ], [ -87.938787, 47.346777 ], [ -87.938250, 47.342299 ], [ -87.943360, 47.335899 ], [ -87.946352, 47.334254 ], [ -87.958386, 47.334435 ], [ -87.968604, 47.332582 ], [ -87.989133, 47.322633 ], [ -88.016478, 47.306275 ], [ -88.054849, 47.298240 ], [ -88.060090, 47.295796 ], [ -88.071476, 47.286768 ], [ -88.096851, 47.261351 ], [ -88.108833, 47.259131 ], [ -88.117456, 47.255174 ], [ -88.131943, 47.239554 ], [ -88.163059, 47.216278 ], [ -88.194218, 47.209242 ], [ -88.204849, 47.210498 ], [ -88.212361, 47.209423 ], [ -88.228987, 47.199042 ], [ -88.236892, 47.189236 ], [ -88.242006, 47.174767 ], [ -88.242660, 47.158426 ], [ -88.239470, 47.151137 ], [ -88.236721, 47.149287 ], [ -88.231797, 47.149609 ], [ -88.232164, 47.145975 ], [ -88.239895, 47.139436 ], [ -88.247628, 47.135981 ], [ -88.249571, 47.136231 ], [ -88.250785, 47.140209 ], [ -88.255303, 47.143640 ], [ -88.262972, 47.145174 ], [ -88.272017, 47.143511 ], [ -88.281701, 47.138212 ], [ -88.289040, 47.129689 ], [ -88.289543, 47.126604 ], [ -88.287870, 47.125374 ], [ -88.287173, 47.120420 ], [ -88.288347, 47.114547 ], [ -88.297625, 47.098505 ], [ -88.340052, 47.080494 ], [ -88.346709, 47.079372 ], [ -88.349952, 47.076377 ], [ -88.353191, 47.069063 ], [ -88.353952, 47.058047 ], [ -88.359054, 47.039739 ], [ -88.367624, 47.019213 ], [ -88.373966, 47.012262 ], [ -88.385606, 47.004522 ], [ -88.404498, 46.983353 ], [ -88.411145, 46.977984 ], [ -88.443901, 46.972251 ], [ -88.448570, 46.946769 ], [ -88.455404, 46.923321 ], [ -88.475859, 46.886042 ], [ -88.477935, 46.850560 ], [ -88.483748, 46.831727 ], [ -88.482579, 46.826197 ], [ -88.473342, 46.806226 ], [ -88.462349, 46.786711 ], [ -88.438427, 46.786714 ], [ -88.433835, 46.793502 ], [ -88.415225, 46.811715 ], [ -88.381410, 46.838466 ], [ -88.382204, 46.844477 ], [ -88.382052, 46.845437 ], [ -88.390135, 46.851595 ], [ -88.404008, 46.848331 ], [ -88.389727, 46.867100 ], [ -88.372591, 46.872812 ], [ -88.375855, 46.863428 ], [ -88.369848, 46.857568 ], [ -88.368767, 46.857313 ], [ -88.360868, 46.856202 ], [ -88.351940, 46.857028 ], [ -88.310290, 46.889748 ], [ -88.281244, 46.906632 ], [ -88.261593, 46.915516 ], [ -88.244437, 46.929612 ], [ -88.167227, 46.958855 ], [ -88.155374, 46.965069 ], [ -88.143688, 46.966665 ], [ -88.132876, 46.962204 ], [ -88.150114, 46.943630 ], [ -88.187522, 46.918999 ], [ -88.175197, 46.904580 ], [ -88.161913, 46.904941 ], [ -88.126927, 46.909840 ], [ -88.101315, 46.917207 ], [ -88.081870, 46.920458 ], [ -88.065192, 46.918563 ], [ -88.032408, 46.908890 ], [ -88.004298, 46.906982 ], [ -87.986113, 46.905957 ], [ -87.956000, 46.909051 ], [ -87.900339, 46.909686 ], [ -87.874538, 46.892578 ], [ -87.846195, 46.883905 ], [ -87.841228, 46.884363 ], [ -87.827162, 46.889713 ], [ -87.816794, 46.891154 ], [ -87.813226, 46.888023 ], [ -87.793194, 46.880822 ], [ -87.782461, 46.879859 ], [ -87.776930, 46.876726 ], [ -87.776313, 46.872591 ], [ -87.778752, 46.870422 ], [ -87.776804, 46.866823 ], [ -87.765989, 46.861316 ], [ -87.755868, 46.860453 ], [ -87.746646, 46.865427 ], [ -87.741014, 46.865247 ], [ -87.734870, 46.850120 ], [ -87.736732, 46.847216 ], [ -87.734325, 46.836955 ], [ -87.731522, 46.831196 ], [ -87.727358, 46.827656 ], [ -87.713737, 46.825534 ], [ -87.694590, 46.827182 ], [ -87.685698, 46.832530 ], [ -87.687930, 46.839159 ], [ -87.687164, 46.841742 ], [ -87.680668, 46.842496 ], [ -87.674541, 46.836964 ], [ -87.673177, 46.827593 ], [ -87.674345, 46.824050 ], [ -87.672015, 46.820415 ], [ -87.662261, 46.815157 ], [ -87.651510, 46.812411 ], [ -87.641887, 46.813733 ], [ -87.633300, 46.812107 ], [ -87.628081, 46.805157 ], [ -87.607988, 46.788408 ], [ -87.595307, 46.782950 ], [ -87.590767, 46.753009 ], [ -87.582745, 46.730527 ], [ -87.573203, 46.720471 ], [ -87.523308, 46.688488 ], [ -87.524444, 46.677586 ], [ -87.503025, 46.647497 ], [ -87.492860, 46.642561 ], [ -87.467965, 46.635623 ], [ -87.466537, 46.631555 ], [ -87.467563, 46.626228 ], [ -87.464108, 46.614811 ], [ -87.451368, 46.605923 ], [ -87.442612, 46.602776 ], [ -87.411167, 46.601669 ], [ -87.403275, 46.595215 ], [ -87.383961, 46.593070 ], [ -87.381649, 46.580059 ], [ -87.392974, 46.572523 ], [ -87.392828, 46.570852 ], [ -87.382206, 46.553681 ], [ -87.375613, 46.547140 ], [ -87.390300, 46.542577 ], [ -87.393985, 46.533183 ], [ -87.389290, 46.524472 ], [ -87.381349, 46.517292 ], [ -87.366767, 46.507303 ], [ -87.351071, 46.500749 ], [ -87.310755, 46.492017 ], [ -87.258732, 46.488255 ], [ -87.202404, 46.490827 ], [ -87.175065, 46.497548 ], [ -87.127440, 46.494014 ], [ -87.107559, 46.496124 ], [ -87.098760, 46.503609 ], [ -87.077279, 46.515339 ], [ -87.046022, 46.519956 ], [ -87.029892, 46.525599 ], [ -87.017136, 46.533550 ], [ -87.008724, 46.532723 ], [ -86.976958, 46.526581 ], [ -86.964534, 46.516549 ], [ -86.962842, 46.509646 ], [ -86.946980, 46.484567 ], [ -86.946218, 46.479059 ], [ -86.949526, 46.476315 ], [ -86.947077, 46.472064 ], [ -86.927725, 46.464566 ], [ -86.903742, 46.466138 ], [ -86.889094, 46.458499 ], [ -86.883976, 46.450976 ], [ -86.883919, 46.441514 ], [ -86.875151, 46.437280 ], [ -86.850111, 46.434114 ], [ -86.837448, 46.434186 ], [ -86.816026, 46.437892 ], [ -86.810967, 46.449663 ], [ -86.808817, 46.460611 ], [ -86.803557, 46.466669 ], [ -86.787905, 46.477729 ], [ -86.768516, 46.479072 ], [ -86.750157, 46.479109 ], [ -86.735929, 46.475231 ], [ -86.731096, 46.471760 ], [ -86.730829, 46.468057 ], [ -86.710573, 46.444908 ], [ -86.703230, 46.439378 ], [ -86.698139, 46.438624 ], [ -86.686412, 46.454965 ], [ -86.688816, 46.463152 ], [ -86.686468, 46.471655 ], [ -86.683819, 46.498079 ], [ -86.696001, 46.503160 ], [ -86.701929, 46.511571 ], [ -86.709325, 46.543914 ], [ -86.695645, 46.555026 ], [ -86.678182, 46.561039 ], [ -86.675764, 46.557061 ], [ -86.670927, 46.556489 ], [ -86.656479, 46.558453 ], [ -86.652865, 46.560555 ], [ -86.627380, 46.533710 ], [ -86.629086, 46.518144 ], [ -86.632109, 46.508865 ], [ -86.634530, 46.504523 ], [ -86.641088, 46.500438 ], [ -86.645528, 46.492039 ], [ -86.646393, 46.485776 ], [ -86.636671, 46.478298 ], [ -86.627441, 46.477540 ], [ -86.620603, 46.483873 ], [ -86.618061, 46.489452 ], [ -86.612173, 46.493295 ], [ -86.609393, 46.492976 ], [ -86.606932, 46.478531 ], [ -86.609039, 46.470239 ], [ -86.586168, 46.463324 ], [ -86.557731, 46.487434 ], [ -86.524959, 46.505381 ], [ -86.495054, 46.524874 ], [ -86.484003, 46.535965 ], [ -86.481956, 46.542709 ], [ -86.469306, 46.551422 ], [ -86.459930, 46.551928 ], [ -86.444390, 46.548137 ], [ -86.437167, 46.548960 ], [ -86.390409, 46.563194 ], [ -86.349890, 46.578035 ], [ -86.188024, 46.654008 ], [ -86.161681, 46.669475 ], [ -86.138295, 46.672935 ], [ -86.119862, 46.657256 ], [ -86.112126, 46.655044 ], [ -86.099843, 46.654615 ], [ -86.074219, 46.657799 ], [ -86.036969, 46.667627 ], [ -85.995044, 46.673676 ], [ -85.953670, 46.676869 ], [ -85.924047, 46.684733 ], [ -85.877908, 46.690914 ], [ -85.841057, 46.688896 ], [ -85.794923, 46.681083 ], [ -85.750606, 46.677368 ], [ -85.714415, 46.677156 ], [ -85.668753, 46.680404 ], [ -85.624573, 46.678862 ], [ -85.587345, 46.674627 ], [ -85.542517, 46.674263 ], [ -85.509510, 46.675786 ], [ -85.482096, 46.680432 ], [ -85.369805, 46.713754 ], [ -85.289846, 46.744644 ], [ -85.256860, 46.753380 ], [ -85.173042, 46.763634 ], [ -85.063556, 46.757856 ], [ -85.036286, 46.760435 ], [ -85.009240, 46.769224 ], [ -84.989497, 46.772403 ], [ -84.964652, 46.772845 ], [ -84.954009, 46.771362 ], [ -84.951580, 46.769488 ], [ -84.987539, 46.745483 ], [ -85.007616, 46.728339 ], [ -85.020159, 46.712463 ], [ -85.027513, 46.697451 ], [ -85.030078, 46.684769 ], [ -85.028291, 46.675125 ], [ -85.035504, 46.625021 ], [ -85.037056, 46.600995 ], [ -85.035476, 46.581547 ], [ -85.031507, 46.568703 ], [ -85.029594, 46.554419 ], [ -85.027374, 46.553756 ], [ -85.025491, 46.546397 ], [ -85.027083, 46.543038 ], [ -85.045534, 46.537694 ], [ -85.052954, 46.532827 ], [ -85.056133, 46.526520 ], [ -85.054943, 46.514750 ], [ -85.049847, 46.503963 ], [ -85.033766, 46.487670 ], [ -85.025598, 46.483028 ], [ -85.015211, 46.479712 ], [ -84.969464, 46.476290 ], [ -84.955307, 46.480269 ], [ -84.947269, 46.487399 ], [ -84.937145, 46.489252 ], [ -84.934432, 46.480315 ], [ -84.921931, 46.469962 ], [ -84.915184, 46.467515 ], [ -84.893423, 46.465406 ], [ -84.875070, 46.466781 ], [ -84.861448, 46.469930 ], [ -84.849767, 46.460245 ], [ -84.843907, 46.448661 ], [ -84.829491, 46.444071 ], [ -84.800101, 46.446219 ], [ -84.769151, 46.453523 ], [ -84.723338, 46.468266 ], [ -84.689672, 46.483923 ], [ -84.678423, 46.487694 ], [ -84.653880, 46.482250 ], [ -84.631020, 46.484868 ], [ -84.616489, 46.471870 ], [ -84.607945, 46.456747 ], [ -84.584167, 46.439410 ], [ -84.573522, 46.427895 ], [ -84.551496, 46.418522 ], [ -84.503719, 46.439190 ], [ -84.493401, 46.440313 ], [ -84.479513, 46.432573 ], [ -84.471848, 46.434289 ], [ -84.462597, 46.440940 ], [ -84.455527, 46.453897 ], [ -84.455256, 46.462785 ], [ -84.463322, 46.467435 ] ] }";
        OperatorImportFromGeoJson importerGeoJson = (OperatorImportFromGeoJson) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ImportFromGeoJson);
        Geometry geom = importerGeoJson.execute(0, Geometry.Type.Unknown, geoJSON, null).getGeometry();


        double distance = 9000;
        OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
        Polygon poly = (Polygon) opBuf.execute(geom, sr, GeodeticCurveType.Geodesic, distance, 100.0, false, null);

        String words = GeometryEngine.geometryToWkt(poly, 0);
        assertTrue(poly.getType() == Geometry.Type.Polygon);
    }

    @Test
    public void testTriangleLength() {
        Point pt_0 = new Point(10, 10);
        Point pt_1 = new Point(20, 20);
        Point pt_2 = new Point(20, 10);
        double length = 0.0;
        length += GeometryEngine.geodesicDistanceOnWGS84(pt_0, pt_1);
        length += GeometryEngine.geodesicDistanceOnWGS84(pt_1, pt_2);
        length += GeometryEngine.geodesicDistanceOnWGS84(pt_2, pt_0);
        assertTrue(Math.abs(length - 3744719.4094597572) < 1e-13 * 3744719.4094597572);
    }


    @Test
    public void testGeodesicForward() {
        double latitude = 0;
        double longitude = 0;
        double distance = 1000;
        double azimuth = 0;
        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984
        double rpu = Math.PI / 180.0;
        PeDouble lam2 = new PeDouble();
        PeDouble phi2 = new PeDouble();
        GeoDist.geodesic_forward(a, e2, longitude, latitude, distance, azimuth, lam2, phi2);
        assertEquals(longitude, lam2.val / rpu, 0.00001);
    }

    @Test
    public void testRotationInvariance() {
        Point pt_0 = new Point(10, 40);
        Point pt_1 = new Point(20, 60);
        Point pt_2 = new Point(20, 40);
        double length = 0.0;
        length += GeometryEngine.geodesicDistanceOnWGS84(pt_0, pt_1);
        length += GeometryEngine.geodesicDistanceOnWGS84(pt_1, pt_2);
        length += GeometryEngine.geodesicDistanceOnWGS84(pt_2, pt_0);
        assertTrue(Math.abs(length - 5409156.3896271614) < 1e-13 * 5409156.3896271614);

        for (int i = -540; i < 540; i += 5) {
            pt_0.setXY(i + 10, 40);
            pt_1.setXY(i + 20, 60);
            pt_2.setXY(i + 20, 40);
            length = 0.0;
            length += GeometryEngine.geodesicDistanceOnWGS84(pt_0, pt_1);
            length += GeometryEngine.geodesicDistanceOnWGS84(pt_1, pt_2);
            length += GeometryEngine.geodesicDistanceOnWGS84(pt_2, pt_0);
            assertTrue(Math.abs(length - 5409156.3896271614) < 1e-13 * 5409156.3896271614);
        }
    }

    @Test
    public void testDistanceFailure() {
        {
            Point p1 = new Point(-60.668485, -31.996013333333334);
            Point p2 = new Point(119.13731666666666, 32.251583333333336);
            double d = GeometryEngine.geodesicDistanceOnWGS84(p1, p2);
            assertTrue(Math.abs(d - 19973410.50579736) < 1e-13 * 19973410.50579736);
        }

        {
            Point p1 = new Point(121.27343833333333, 27.467438333333334);
            Point p2 = new Point(-58.55804833333333, -27.035613333333334);
            double d = GeometryEngine.geodesicDistanceOnWGS84(p1, p2);
            assertTrue(Math.abs(d - 19954707.428360686) < 1e-13 * 19954707.428360686);
        }

        {
            Point p1 = new Point(-53.329865, -36.08110166666667);
            Point p2 = new Point(126.52895166666667, 35.97385);
            double d = GeometryEngine.geodesicDistanceOnWGS84(p1, p2);
            assertTrue(Math.abs(d - 19990586.700431127) < 1e-13 * 19990586.700431127);
        }

        {
            Point p1 = new Point(-4.7181166667, 36.1160166667);
            Point p2 = new Point(175.248925, -35.7606716667);
            double d = GeometryEngine.geodesicDistanceOnWGS84(p1, p2);
            assertTrue(Math.abs(d - 19964450.206594173) < 1e-12 * 19964450.206594173);
        }
    }

    @Test
    public void testDensifyPolyline() {
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        {
            Polyline polyline = new Polyline();
            polyline.startPath(0, 0);
            polyline.lineTo(4, 4);
            polyline.lineTo(4, 8);
            polyline.lineTo(8, 20);

            OperatorGeodeticDensifyByLength op = (OperatorGeodeticDensifyByLength) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodeticDensifyByLength);
            Polyline polylineDense = (Polyline) op.execute(polyline, sr, 5000, GeodeticCurveType.Geodesic, null);
            assertEquals(polyline.calculateLength2D(), polylineDense.calculateLength2D(), .001);
            assertEquals(polyline.getPoint(polyline.getPointCount() - 1).getX(), polylineDense.getPoint(polylineDense.getPointCount() - 1).getX());
            assertEquals(polyline.getPoint(polyline.getPointCount() - 1).getY(), polylineDense.getPoint(polylineDense.getPointCount() - 1).getY());
            assertEquals(polyline.getPoint(0).getX(), polylineDense.getPoint(0).getX());
            assertEquals(polyline.getPoint(0).getY(), polylineDense.getPoint(0).getY());

            polyline.startPath(-2, -2);
            polyline.lineTo(-4, -4);
            polyline.lineTo(-8, -8);
            polylineDense = (Polyline) op.execute(polyline, sr, 5000, GeodeticCurveType.Geodesic, null);
            assertEquals(polyline.calculateLength2D(), polylineDense.calculateLength2D(), .001);
            assertEquals(polyline.calculatePathLength2D(0), polylineDense.calculatePathLength2D(0), .001);
            assertEquals(polyline.calculatePathLength2D(1), polylineDense.calculatePathLength2D(1), .001);
        }

        {
            Polyline polyline = new Polyline();
            polyline.startPath(0, 0);
            polyline.lineTo(0, 1);
            OperatorGeodeticDensifyByLength op = (OperatorGeodeticDensifyByLength) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodeticDensifyByLength);
            Polyline polylineDense = (Polyline) op.execute(polyline, sr, 100000, GeodeticCurveType.Geodesic, null);
            assertEquals(polyline.getPoint(polyline.getPointCount() - 1).getX(), polylineDense.getPoint(polylineDense.getPointCount() - 1).getX());
            assertEquals(polyline.getPoint(polyline.getPointCount() - 1).getY(), polylineDense.getPoint(polylineDense.getPointCount() - 1).getY());
            assertEquals(polyline.getPoint(0).getX(), polylineDense.getPoint(0).getX());
            assertEquals(polyline.getPoint(0).getY(), polylineDense.getPoint(0).getY());
            assertEquals(3, polylineDense.getPointCount());

            assertEquals(
                    GeometryEngine.geodesicDistanceOnWGS84(
                            polyline.getPoint(0),
                            polylineDense.getPoint(1)) +
                            GeometryEngine.geodesicDistanceOnWGS84(
                                    polylineDense.getPoint(1),
                                    polyline.getPoint(1)),
                    GeometryEngine.geodesicDistanceOnWGS84(
                            polyline.getPoint(0),
                            polyline.getPoint(1))
            );
        }

        {
            Polyline polyline = new Polyline();
            polyline.startPath(0, 0);
            polyline.lineTo(1, 0);
            polyline.lineTo(2, 0);
            OperatorGeodeticDensifyByLength op = (OperatorGeodeticDensifyByLength) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodeticDensifyByLength);
            Polyline polylineDense = (Polyline) op.execute(polyline, sr, 100000, GeodeticCurveType.Geodesic, null);
            assertEquals(polyline.getPoint(polyline.getPointCount() - 1).getX(), polylineDense.getPoint(polylineDense.getPointCount() - 1).getX());
            assertEquals(polyline.getPoint(polyline.getPointCount() - 1).getY(), polylineDense.getPoint(polylineDense.getPointCount() - 1).getY());
            assertEquals(polyline.getPoint(0).getX(), polylineDense.getPoint(0).getX());
            assertEquals(polyline.getPoint(0).getY(), polylineDense.getPoint(0).getY());
            assertEquals(5, polylineDense.getPointCount());

            assertEquals(polyline.getPoint(1).getX(), polylineDense.getPoint(2).getX());
            assertEquals(polyline.getPoint(1).getY(), polylineDense.getPoint(2).getY());
            assertEquals(
                    GeometryEngine.geodesicDistanceOnWGS84(
                            polyline.getPoint(0),
                            polylineDense.getPoint(1)) +
                            GeometryEngine.geodesicDistanceOnWGS84(
                                    polylineDense.getPoint(1),
                                    polyline.getPoint(1)),
                    GeometryEngine.geodesicDistanceOnWGS84(
                            polyline.getPoint(0),
                            polyline.getPoint(1)),
                    .0000001
            );
        }

        {
            Polyline polyline = new Polyline();
            polyline.startPath(0, 0);
            polyline.lineTo(4, 4);
            polyline.lineTo(4, 8);
            polyline.lineTo(8, 20);
            double max_distance = 55000 / 3.5;
            OperatorGeodeticDensifyByLength op = (OperatorGeodeticDensifyByLength) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodeticDensifyByLength);
            Polyline polylineDense = (Polyline) op.execute(polyline, sr, max_distance, GeodeticCurveType.Geodesic, null);
            String words = GeometryEngine.geometryToWkt(polylineDense, 0);
            assertEquals(
                    GeometryEngine.geodesicDistanceOnWGS84(
                            polyline.getPoint(0),
                            polyline.getPoint(polyline.getPointCount() - 1)),
                    GeometryEngine.geodesicDistanceOnWGS84(
                            polylineDense.getPoint(0),
                            polylineDense.getPoint(polylineDense.getPointCount() - 1)),
                    .0000001
            );
        }
    }

    @Test
    public void testDensifyPolygon() {
        {
            Polygon polygon = new Polygon();
            polygon.startPath(0, 0);
            polygon.lineTo(0, 4);
            polygon.lineTo(4, 4);
            polygon.lineTo(4, 0);
            polygon.closeAllPaths();
            SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
            OperatorGeodeticDensifyByLength op = (OperatorGeodeticDensifyByLength) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodeticDensifyByLength);
            Polygon polygonDense = (Polygon) op.execute(polygon, sr, 5000, GeodeticCurveType.Geodesic, null);
            assertEquals(polygon.calculateLength2D(), polygonDense.calculateLength2D(), .005);
            assertEquals(polygon.calculateArea2D(), polygonDense.calculateArea2D(), 0.007);

            polygon.startPath(-2, -2);
            polygon.lineTo(-4, -4);
            polygon.lineTo(-8, -8);
            polygon.closeAllPaths();
            polygonDense = (Polygon) op.execute(polygon, sr, 5000, GeodeticCurveType.Geodesic, null);
            assertEquals(polygon.calculateLength2D(), polygonDense.calculateLength2D(), .004);
            assertEquals(polygon.calculateArea2D(), polygonDense.calculateArea2D(), 0.1);
        }
    }

    @Test
    public void testInflateEnv2D() {
        Envelope2D envOrig = new Envelope2D(0, -4, 4, 8);
        Envelope2D env2D = new Envelope2D(0, -4, 4, 8);

        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984

        GeoDist.inflateEnv2D(a, e2, env2D, 1000, 2000);
        assertTrue(env2D.xmin < envOrig.xmin);
        assertTrue(env2D.ymax > envOrig.ymax);
        assertTrue(env2D.xmax > envOrig.xmax);
        assertTrue(env2D.ymin < envOrig.ymin);
    }

    @Test
    public void testDeflateEnv2D() {
        Envelope2D envOrig = new Envelope2D(0, -4, 4, 8);
        Envelope2D env2D = new Envelope2D(0, -4, 4, 8);

        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984

        GeoDist.inflateEnv2D(a, e2, env2D, -100, 2000);
        assertTrue(env2D.xmin > envOrig.xmin);
        assertTrue(env2D.ymax > envOrig.ymax);
        assertTrue(env2D.xmax < envOrig.xmax);
        assertTrue(env2D.ymin < envOrig.ymin);
    }

    @Test
    public void testVicenty() {
        // test data from
        // http://geographiclib.sourceforge.net/cgi-bin/GeodSolve

        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984
        double rpu = Math.PI / 180.0;
        double dpu = 180.0 / Math.PI;
        double distance = 2000.0;
        {
            Point p1 = new Point(0.0, 0.0);
            PeDouble lam = new PeDouble();
            PeDouble phi = new PeDouble();
            GeoDist.geodesic_forward(a, e2, p1.getX() * rpu, p1.getY() * rpu, distance, 0.0 * rpu, lam, phi);
            assertEquals(0.0, lam.val * dpu, 0.000001);
            assertEquals(0.01808739, phi.val * dpu, 0.000001);

            PeDouble actualDistance = new PeDouble();
            GeoDist.geodesic_distance_ngs(a, e2, p1.getX() * rpu, p1.getY() * rpu, lam.val, phi.val, actualDistance, null, null);
            assertEquals(actualDistance.val, distance, .02);

        }
        {
            Point p1 = new Point(45.0, 45.0);
            PeDouble lam = new PeDouble();
            PeDouble phi = new PeDouble();
            GeoDist.geodesic_forward(a, e2, p1.getX() * rpu, p1.getY() * rpu, distance, 20.0 * rpu, lam, phi);

            assertEquals(45.01691097, phi.val * dpu, 0.000001);
            assertEquals(45.00867811, lam.val * dpu, 0.000001);
        }
        {
            Point p1 = new Point(60.0, 45.0);
            PeDouble lam = new PeDouble();
            PeDouble phi = new PeDouble();
            GeoDist.geodesic_forward(a, e2, p1.getX() * rpu, p1.getY() * rpu, distance, 20.0 * rpu, lam, phi);

            //45.01691097
            assertEquals(45.01691097, phi.val * dpu, 0.000001);
            assertEquals(60.00867811, lam.val * dpu, 0.000001);
        }
        {
            Point p1 = new Point(-65.0, -45.0);
            PeDouble lam = new PeDouble();
            PeDouble phi = new PeDouble();
            GeoDist.geodesic_forward(a, e2, p1.getX() * rpu, p1.getY() * rpu, distance, -20.0 * rpu, lam, phi);

            //-44.98308832 -65.00867301
            assertEquals(-44.98308832, phi.val * dpu, 0.000001);
            assertEquals(-65.00867301, lam.val * dpu, 0.000001);
        }
        {
            Point p1 = new Point(-165.0, -45.0);
            PeDouble lam = new PeDouble();
            PeDouble phi = new PeDouble();
            GeoDist.geodesic_forward(a, e2, p1.getX() * rpu, p1.getY() * rpu, distance, 220.0 * rpu, lam, phi);

            //-45.01378505 -165.01630863
            assertEquals(-45.01378505, phi.val * dpu, 0.000001);
            assertEquals(-165.01630863, lam.val * dpu, 0.000001);
        }
    }

    @Test
    public void testGeodeticBufferPoint() {
        {
            SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
            Point p1 = new Point(0.0, 0.0);
            OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
            double distance = 1000;
            Polygon poly = (Polygon) opBuf.execute(p1, sr, GeodeticCurveType.Geodesic, distance, 0.1, false, null);
            //String words = GeometryEngine.geometryToWkt(poly, 0);
            assertNotNull(poly);
            assertTrue(poly.getType() == Geometry.Type.Polygon);
            double area = poly.calculateArea2D();
            assertEquals(2.550450219554701E-4, area, 0.0000000001);
            assertEquals(97, poly.getPointCount());

            assertTrue(OperatorContains.local().execute(poly, p1, sr.toSpatialReference(), null));
        }
    }

    @Test
    public void testGeodeticBufferMultiPoint() {
        {
            SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
            MultiPoint mp = new MultiPoint();
            mp.add(0.0, 0.0);
            mp.add(20.0, 0.0);
            OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
            double distance = 100000;
            Polygon poly = (Polygon) opBuf.execute(mp, sr, GeodeticCurveType.Geodesic, distance, 150, false, null);
            String words = GeometryEngine.geometryToWkt(poly, 0);
            assertNotNull(poly);
            assertTrue(poly.getType() == Geometry.Type.Polygon);
            assertEquals(2, poly.getPathCount());
            double area = poly.calculateArea2D();
            assertEquals(5.095268886272399, area, 0.0000000001);
            assertEquals(120, poly.getPointCount());

            assertTrue(OperatorContains.local().execute(poly, mp, sr.toSpatialReference(), null));
        }
    }

    @Test
    public void testGeodeticBufferPolyline() {
        {
            Polyline polyline = new Polyline();
            polyline.startPath(0, 0);
            polyline.lineTo(4, 4);
            polyline.lineTo(4, 8);
            polyline.lineTo(8, 20);
            SpatialReferenceEx sr = SpatialReferenceEx.create(4326);

            OperatorBufferEx opBufNorm = (OperatorBufferEx) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.Buffer);
            Polygon polyNorm = (Polygon) opBufNorm.execute(polyline, sr, .7, null);


            String words = GeometryEngine.geometryToWkt(polyline, 0);
            double distance = 55000;
            OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
            Polygon poly = (Polygon) opBuf.execute(polyline, sr, GeodeticCurveType.Geodesic, distance, 150.0, false, null);

            words = GeometryEngine.geometryToWkt(poly, 0);
            assertNotNull(poly);
            assertTrue(poly.getType() == Geometry.Type.Polygon);
            double area = poly.calculateArea2D();
            assertEquals(23.296270856192834, area, 0.00000000001);
        }
    }

    @Test
    public void testBufferGeodeticPolyline2() {
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        Polyline inputGeom = new Polyline();
        OperatorGeodesicBuffer buffer = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
        OperatorSimplify simplify = (OperatorSimplify) OperatorFactoryLocal
                .getInstance().getOperator(Operator.Type.Simplify);
        inputGeom.startPath(0, 0);
        inputGeom.lineTo(50, 50);
        inputGeom.lineTo(50, 0);
        inputGeom.lineTo(0, 50);

        {
            Geometry result = buffer.execute(inputGeom, sr, 0, 0, 0, false, null);
            assertTrue(result.getType().value() == Geometry.GeometryType.Polygon);
            assertTrue(result.isEmpty());
        }

        {
            Geometry result = buffer.execute(inputGeom, sr, 0, -1, 0, false, null);
            assertTrue(result.getType().value() == Geometry.GeometryType.Polygon);
            assertTrue(result.isEmpty());
        }

        {
            String words = GeometryEngine.geometryToWkt(inputGeom, 0);
            Geometry result = buffer.execute(inputGeom, sr, 0, 40.0, 50, false, null);
            assertTrue(result.getType().value() == Geometry.GeometryType.Polygon);
            Polygon poly = (Polygon) (result);

            words = GeometryEngine.geometryToWkt(poly, 0);
//			Envelope2D env2D = new Envelope2D();
//			result.queryEnvelope2D(env2D);
//			assertTrue(Math.abs(env2D.getWidth() - 80 - 50) < 0.1
//					&& Math.abs(env2D.getHeight() - 80 - 50) < 0.1);
//			assertTrue(Math.abs(env2D.getCenterX() - 25) < 0.1
//					&& Math.abs(env2D.getCenterY() - 25) < 0.1);
            int pathCount = poly.getPathCount();
            // should have a hole in it
            assertEquals(pathCount, 2);

            assertTrue(simplify.isSimpleAsFeature(result, sr.toSpatialReference(), null));
        }

        {
            String words = GeometryEngine.geometryToWkt(inputGeom, 0);
            Geometry result = buffer.execute(inputGeom, sr, 0, 3000000.0, 50, false, null);
            assertTrue(result.getType().value() == Geometry.GeometryType.Polygon);
            Polygon poly = (Polygon) (result);

            words = GeometryEngine.geometryToWkt(poly, 0);
//			Envelope2D env2D = new Envelope2D();
//			result.queryEnvelope2D(env2D);
//			assertTrue(Math.abs(env2D.getWidth() - 80 - 50) < 0.1
//					&& Math.abs(env2D.getHeight() - 80 - 50) < 0.1);
//			assertTrue(Math.abs(env2D.getCenterX() - 25) < 0.1
//					&& Math.abs(env2D.getCenterY() - 25) < 0.1);
            int pathCount = poly.getPathCount();
            // should have a hole in it
            assertEquals(pathCount, 1);

            assertTrue(simplify.isSimpleAsFeature(result, sr.toSpatialReference(), null));
        }
//
//		{
//			Geometry result = buffer.execute(inputGeom, sr, 4.0, null);
//			assertTrue(result.getType().value() == Geometry.GeometryType.Polygon);
//			Polygon poly = (Polygon) (result);
//			Envelope2D env2D = new Envelope2D();
//			result.queryEnvelope2D(env2D);
//			assertTrue(Math.abs(env2D.getWidth() - 8 - 50) < 0.1
//					&& Math.abs(env2D.getHeight() - 8 - 50) < 0.1);
//			assertTrue(Math.abs(env2D.getCenterX() - 25) < 0.1
//					&& Math.abs(env2D.getCenterY() - 25) < 0.1);
//			int pathCount = poly.getPathCount();
//			assertTrue(pathCount == 2);
//			int pointCount = poly.getPointCount();
//			assertTrue(Math.abs(pointCount - 186.0) < 10);
//			assertTrue(simplify.isSimpleAsFeature(result, sr, null));
//		}
//
//		{
//			inputGeom = new Polyline();
//			inputGeom.startPath(0, 0);
//			inputGeom.lineTo(50, 50);
//			inputGeom.startPath(50, 0);
//			inputGeom.lineTo(0, 50);
//
//			Geometry result = buffer.execute(inputGeom, sr, 4.0, null);
//			assertTrue(result.getType().value() == Geometry.GeometryType.Polygon);
//			Polygon poly = (Polygon) (result);
//			Envelope2D env2D = new Envelope2D();
//			result.queryEnvelope2D(env2D);
//			assertTrue(Math.abs(env2D.getWidth() - 8 - 50) < 0.1
//					&& Math.abs(env2D.getHeight() - 8 - 50) < 0.1);
//			assertTrue(Math.abs(env2D.getCenterX() - 25) < 0.1
//					&& Math.abs(env2D.getCenterY() - 25) < 0.1);
//			int pathCount = poly.getPathCount();
//			assertTrue(pathCount == 1);
//			int pointCount = poly.getPointCount();
//			assertTrue(Math.abs(pointCount - 208.0) < 10);
//			assertTrue(simplify.isSimpleAsFeature(result, sr, null));
//		}
    }

    @Test
    public void testGeodeticBufferSegment() {
        {
            Polyline polyline = new Polyline();
            polyline.startPath(0, 0);
            polyline.lineTo(4, 4);
            SegmentIterator segmentIterator = polyline.querySegmentIterator();
            segmentIterator.nextPath();
            Segment segment = segmentIterator.nextSegment();
            SpatialReferenceEx sr = SpatialReferenceEx.create(4326);


            double distance = 55000;
            OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
            Polygon poly = (Polygon) opBuf.execute(segment, sr, GeodeticCurveType.Geodesic, distance, 300.0, false, null);

            String words = GeometryEngine.geometryToWkt(poly, 0);
            assertNotNull(poly);
            assertTrue(poly.getType() == Geometry.Type.Polygon);
            double area = poly.calculateArea2D();
            assertEquals(6.379702184244028, area, .0001);
        }
    }

    @Test
    public void testBufferArcs() {
        Polyline polyline = new Polyline();
        polyline.startPath(5, 25);
        polyline.lineTo(10, 32);
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        OperatorGeodesicBuffer op = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
        Polygon poly = (Polygon) op.execute(polyline, sr, 0, 3000, 500, false, null);
        String words = GeometryEngine.geometryToWkt(poly, 0);
        assertEquals(13, poly.getPointCount());
    }

    @Test
    public void testPolygonBoundaryBug() {
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        OperatorImportFromWkt opWKT = (OperatorImportFromWkt) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ImportFromWkt);
        OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
        double distance = 59000;
        {
            String wkt = "MULTILINESTRING ((5 0,3 38,3.9 37.7,4 40,30 10,5 0))";
            Geometry geom = opWKT.execute(0, Geometry.Type.Polyline, wkt, null);
            Polygon poly = (Polygon) opBuf.execute(geom, sr, GeodeticCurveType.Geodesic, distance, 2000, false, null);
            String words = GeometryEngine.geometryToWkt(poly, 0);
            Envelope2D env2D = new Envelope2D();
            poly.queryEnvelope2D(env2D);
            assertTrue(env2D.xmin < 3);
            assertTrue(env2D.ymin < 0);
            assertTrue(env2D.xmax > 30);
            assertTrue(env2D.ymax > 40);
            assertEquals(Geometry.Type.Polygon, poly.getType());
            assertEquals(24, poly.getPointCount());
        }
        {
            String wkt = "MULTILINESTRING ((15 5, 5 10, 10 20, 10 30, 16.666666666666664 33.333333333333329, 10 40, 20 40, 28.333333333333339 40, 20 45, 40 40, 45 40, 42 36, 45 30, 39.827586206896555 33.103448275862071, 36 28, 35.277777777777779 25.833333333333332, 45 20, 36.25 11.25, 40 10, 33.75 8.7500000000000018, 30 5, 23.333333333333336 6.6666666666666661, 15 5))";
            Geometry geom = opWKT.execute(0, Geometry.Type.Polyline, wkt, null);
            Polygon poly = (Polygon) opBuf.execute(geom, sr, GeodeticCurveType.Geodesic, distance, 2000, false, null);
            Envelope2D env2D = new Envelope2D();
            poly.queryEnvelope2D(env2D);
            assertTrue(env2D.xmin < 5);
            assertTrue(env2D.ymin < 5);
            assertTrue(env2D.xmax > 45);
            assertTrue(env2D.ymax > 45);
            distance = 200000;
            poly = (Polygon) opBuf.execute(geom, sr, GeodeticCurveType.Geodesic, distance, 2000, false, null);
            env2D = new Envelope2D();
            poly.queryEnvelope2D(env2D);
            assertTrue(env2D.xmin < 5);
            assertTrue(env2D.ymin < 5);
            assertTrue(env2D.xmax > 45);
            assertTrue(env2D.ymax > 45);
            distance = 20045;
            poly = (Polygon) opBuf.execute(geom, sr, GeodeticCurveType.Geodesic, distance, 200, false, null);
            env2D = new Envelope2D();
            poly.queryEnvelope2D(env2D);
            assertTrue(env2D.xmin < 5);
            assertTrue(env2D.ymin < 5);
            assertTrue(env2D.xmax > 45);
            assertTrue(env2D.ymax > 45);
        }
    }

    @Test
    public void testImperfectArcEndings() throws Exception {
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        String wkt = "MULTILINESTRING ((79.599689290259803 80.056892196564064, 79.679967837449936 80.045572571657544, 79.760065736245338 80.034233827076164, 79.839983299288846 80.022876027925264, 79.919720840553722 80.011499239123168, 80 80))";
        Polyline polyline = new Polyline();
        polyline.startPath(79, 80);
        polyline.lineTo(82, 82);
        OperatorImportFromWkt opWKT = (OperatorImportFromWkt) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ImportFromWkt);
        Geometry geom = opWKT.execute(0, Geometry.Type.Polyline, wkt, null);
        OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
        double distance = 300;
        Polygon poly = (Polygon) opBuf.execute(polyline, sr, GeodeticCurveType.Geodesic, distance, 5, false, null);
        String words = GeometryEngine.geometryToWkt(poly, 0);
        assertEquals(Geometry.Type.Polygon, poly.getType());
        assertEquals(21, poly.getPointCount());
    }

    @Test
    public void testPolygon() {
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        OperatorImportFromWkt opWKT = (OperatorImportFromWkt) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ImportFromWkt);
        OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
        String wkt = "MULTIPOLYGON (((15 5, 23.333333333333336 6.6666666666666661, 30 5, 33.75 8.7500000000000018, 40 10, 36.25 11.25, 45 20, 35.277777777777779 25.833333333333332, 36 28, 39.827586206896555 33.103448275862071, 45 30, 42 36, 45 40, 40 40, 20 45, 28.333333333333339 40, 20 40, 10 40, 16.666666666666664 33.333333333333329, 10 30, 10 20, 5 10, 15 5)))";
        Geometry geom = opWKT.execute(0, Geometry.Type.Polygon, wkt, null);
        Envelope2D env2DOrig = new Envelope2D();
        geom.queryEnvelope2D(env2DOrig);
        double distance = 300;
        Polygon poly = (Polygon) opBuf.execute(geom, sr, GeodeticCurveType.Geodesic, distance, 5, false, null);
        Envelope2D env2DPositiveBuff = new Envelope2D();
        poly.queryEnvelope2D(env2DPositiveBuff);
        assertTrue(env2DPositiveBuff.xmax > env2DOrig.xmax);
        assertTrue(env2DPositiveBuff.ymax > env2DOrig.ymax);
        assertTrue(env2DPositiveBuff.xmin < env2DOrig.xmin);
        assertTrue(env2DPositiveBuff.ymin < env2DOrig.ymin);

        distance = -300;
        poly = (Polygon) opBuf.execute(geom, sr, GeodeticCurveType.Geodesic, distance, 5, false, null);
        Envelope2D env2DNegativeBuff = new Envelope2D();
        poly.queryEnvelope2D(env2DNegativeBuff);
        assertTrue(env2DNegativeBuff.xmax < env2DOrig.xmax);
        assertTrue(env2DNegativeBuff.ymax < env2DOrig.ymax);
        assertTrue(env2DNegativeBuff.xmin > env2DOrig.xmin);
        assertTrue(env2DNegativeBuff.ymin > env2DOrig.ymin);
    }

    @Test
    public void testEnvelop() {
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        OperatorImportFromWkt opWKT = (OperatorImportFromWkt) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ImportFromWkt);
        OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
        String wkt = "MULTIPOLYGON (((15 5, 23.333333333333336 6.6666666666666661, 30 5, 33.75 8.7500000000000018, 40 10, 36.25 11.25, 45 20, 35.277777777777779 25.833333333333332, 36 28, 39.827586206896555 33.103448275862071, 45 30, 42 36, 45 40, 40 40, 20 45, 28.333333333333339 40, 20 40, 10 40, 16.666666666666664 33.333333333333329, 10 30, 10 20, 5 10, 15 5)))";
        Geometry geom = opWKT.execute(0, Geometry.Type.Polygon, wkt, null);
        Envelope envOrig = new Envelope();
        geom.queryEnvelope(envOrig);
        double distance = 300;
        Polygon poly = (Polygon) opBuf.execute(envOrig, sr, GeodeticCurveType.Geodesic, distance, 5, false, null);
        OperatorContains opContains = (OperatorContains) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Contains);
        assertTrue(opContains.execute(poly, geom, sr.toSpatialReference(), null));
        String words = GeometryEngine.geometryToWkt(poly, 0);
        assertTrue(opContains.execute(poly, envOrig, sr.toSpatialReference(), null));
    }


    @Test
    public void testDegeneratePolyline() {
        String wkt = "LINESTRING (0 0, 0 80)";
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        OperatorImportFromWkt opWKT = (OperatorImportFromWkt) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ImportFromWkt);
        OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
        Geometry geom = opWKT.execute(0, Geometry.Type.Unknown, wkt, null);
        double distance = 30000;
        Polygon poly = (Polygon) opBuf.execute(geom, sr, GeodeticCurveType.Geodesic, distance, 500, false, null);
        OperatorContains opContains = (OperatorContains) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Contains);
        String words = GeometryEngine.geometryToWkt(poly, 0);
        assertTrue(opContains.execute(poly, geom, sr.toSpatialReference(), null));
    }


//	@Test
//	public void testInwardSpikesBug() {
//		String wkt = "MULTIPOLYGON (((0.92559027418805195 -45.599676354983472, 2.2649850283358415 -45.48147830753507, 46.534530341779551 -43.413080539546634, 11.22755121895433 -31.61421433801998, -1.401557702671544 -38.899575885915091, -1.6611345760810066 -39.024502078460699, -2.3748527669947075 -39.472397720083308, 0.92559027418805195 -45.599676354983472)))";
//		SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
//		OperatorImportFromWkt opWKT = (OperatorImportFromWkt) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.ImportFromWkt);
//		OperatorGeodesicBuffer opBuf = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
//		Geometry geom = opWKT.execute(0, Geometry.Type.Unknown, wkt, null);
//		double distance = 300000;
//		Polygon poly = (Polygon)opBuf.execute(geom, sr, GeodeticCurveType.Geodesic, distance, 50, false, null);
//		OperatorContains opContains = (OperatorContains)OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Contains);
//		String words = GeometryEngine.geometryToWkt(poly, 0);
//
//		String wktConvexHull = "MULTIPOLYGON (((0.9089046882067352 -48.29821705193546, 1.1863571595767088 -48.292332051775411, 47.82242915305379 -45.951655393799292, 48.063359465978309 -45.882452469302159, 48.296415704125721 -45.801606429574932, 48.520438131815979 -45.709533458046636, 48.734329757433294 -45.606705141924508, 48.93706270676406 -45.493645183075678, 49.127683729236487 -45.370925820991054, 49.305318808821802 -45.239164013518995, 49.469176871136796 -45.099017421538605, 49.618552596685944 -44.951180243185775, 49.752828366656694 -44.796378941772254, 49.871475381830926 -44.635367909303227, 49.974054006759687 -44.468925104685226, 50.060213400309706 -44.297847702511667, 50.129690500167094 -44.122947784895814, 50.182308433177973 -43.945048105303613, 50.217974425889949 -43.764977949736668, 50.236677290575663 -43.583569116840323, 50.238484561299586 -43.401652034454806, 50.223539351847286 -43.220052025836537, 50.192057002221887 -43.039585734585927, 50.144321573156112 -42.861057713773583, 50.080682239733406 -42.685257182292766, 50.001549627188815 -42.512954950085337, 49.907392125266014 -42.344900513153419, 49.798732212228884 -42.181819318628094, 49.676142815161754 -42.024410199302551, 49.54024372887072 -41.873342975979902, 49.391698111197975 -41.729256224947129, 49.231209067957252 -41.592755207078895, 49.059516336252528 -41.464409954591567, 48.877393070903288 -41.344753511299601, 48.6856427352119 -41.234280322303988, 48.485096094408405 -41.133444769287799, 12.367951381848586 -29.09519676592031, 12.175744186073088 -29.036048174081564, 11.979702842662022 -28.987893414119039, 11.780623991428131 -28.950927888506527, 11.579313800069148 -28.925301449016882, 11.376585410919665 -28.911117906328901, 11.173256388575595 -28.908434690779561, 10.970146168054551 -28.917262664846383, 10.768073503178082 -28.937566087692581, 10.567853914873456 -28.969262731839144, 10.370297139088287 -29.012224151754676, 10.176204573992516 -29.066276103892736, 9.9863667261187086 -29.131199117469993, 9.801560655078573 -29.206729215078262, 9.6225474165128926 -29.292558782068209, -3.7643500390225628 -36.895361922941838, -4.4904966350672346 -37.343116137195821, -4.6691363968621715 -37.45960384540907, -4.8379956556783039 -37.585263238883662, -4.9963037536009622 -37.719555997866081, -5.1433240289217048 -37.861904159017264, -5.2783570150494707 -38.011692034666453, -5.4007437035934922 -38.168268296109588, -5.5098688663992261 -38.330948223471374, -5.6051644276362769 -38.499016124576002, -5.6861128727684322 -38.671727924869288, -5.7522506766490213 -38.848313929575951, -5.8031717286040667 -39.027981757931798, -5.8385307287783856 -39.209919447716956, -5.8580465273471765 -39.393298726920456, -5.8615053755483038 -39.577278448672502, -5.8487640531943192 -39.761008185524524, -5.8197528302886008 -39.943631978862697, -5.7744782115154418 -40.124292237549554, -5.7130254043292377 -40.302133776372663, -5.6355604464479869 -40.476307980224568, -2.6829201612622011 -46.591539197089858, -2.5809698045106346 -46.763021099807965, -2.4614638331884864 -46.929167919911855, -2.3248405644153958 -47.089144707460214, -2.1716400855169957 -47.242139835859057, -2.002504427969539 -47.387369896582548, -1.8181768198266057 -47.524084593153006, -1.6194999412599023 -47.651571588547469, -1.4074131218100461 -47.769161256263828, -1.1829484355289481 -47.876231282022097, -0.94722567117225265 -47.972211060733848, -0.70144617849499724 -48.056585832168651, -0.44688561781805886 -48.128900498861178, -0.18488566747456844 -48.188763071371675, 0.083155228532603459 -48.23584768910839, 0.35579196347893255 -48.269897169551278, 0.63154358749412232 -48.290725044816675, 0.9089046882067352 -48.29821705193546)))";
//		Geometry geomConvex = opWKT.execute(0, Geometry.Type.Polygon, wktConvexHull, null);
//		assertEquals(geomConvex.calculateArea2D(), poly.calculateArea2D());
//		assertEquals(geomConvex.calculateLength2D(), poly.calculateLength2D());
//		assertTrue(opContains.execute(poly, geom, sr, null));
//
//		// This data fails at distance 300000 and deviation 50
//		String wktPolylineSpike = "MULTILINESTRING ((0.92559027418805195 -45.599676354983472, 11.22755121895433 -31.61421433801998, 46.534530341779551 -43.413080539546634, 2.2649850283358415 -45.48147830753507, 0.92559027418805195 -45.599676354983472))";
//	}

//	@Test
//	public void testLengthAccurateCR191313() {
//		/*
//		 * // random_test(); OperatorFactoryLocal engine =
//		 * OperatorFactoryLocal.getInstance(); //TODO: Make this:
//		 * OperatorShapePreservingLength geoLengthOp =
//		 * (OperatorShapePreservingLength)
//		 * factory.getOperator(Operator.Type.ShapePreservingLength);
//		 * SpatialReferenceEx spatialRef = SpatialReferenceEx.create(102631);
//		 * //[6097817.59407673
//		 * ,17463475.2931517],[-1168053.34617516,11199801.3734424
//		 * ]]],"spatialReference":{"wkid":102631}
//		 *
//		 * Polyline polyline = new Polyline();
//		 * polyline.startPath(6097817.59407673, 17463475.2931517);
//		 * polyline.lineTo(-1168053.34617516, 11199801.3734424); double length =
//		 * geoLengthOp.execute(polyline, spatialRef, null);
//		 * assertTrue(Math.abs(length - 2738362.3249366437) < 2e-9 * length);
//		 */
//	}

    @Test
    public void testEnvelopeMidpoint() {
        Envelope2D envelope2D = new Envelope2D(45, -10, 55, 10);
        Point2D centerPoint = new Point2D();
        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984
        GeoDist.getEnvCenter(a, e2, envelope2D, centerPoint);
        assertEquals(50, centerPoint.x, 1e-12);
        assertEquals(0, centerPoint.y, 1e-4);
    }

    @Test
    public void testEnvelopeMidpointDateline() {
        Envelope2D envelope2D = new Envelope2D(175, -10, -175, 10);
        Point2D centerPoint = new Point2D();
        double a = 6378137.0; // radius of spheroid for WGS_1984
        double e2 = 0.0066943799901413165; // ellipticity for WGS_1984
        GeoDist.getEnvCenter(a, e2, envelope2D, centerPoint);
        assertEquals(180, Math.abs(centerPoint.x), 1e-12);
        assertEquals(0, centerPoint.y, 1e-4);
    }

    @Test
    public void testDifferentDatums() {
        SpatialReferenceEx spatialReferenceWgs = SpatialReferenceEx.create(4326);
        SpatialReferenceEx spatialReferenceNad = SpatialReferenceEx.create(4269);

        Polyline polyline = new Polyline();
        polyline.startPath(-172.54, 23.81);
        polyline.lineTo(-47.74, 86.46);

        Geometry geometryW = OperatorGeodeticDensifyByLength.local().execute(polyline, spatialReferenceWgs, 5000, GeodeticCurveType.Geodesic, null);
        Geometry geometryW2 = OperatorGeodeticDensifyByLength.local().execute(polyline, spatialReferenceWgs, 5000, GeodeticCurveType.Geodesic, null);
        Geometry geometryN = OperatorGeodeticDensifyByLength.local().execute(polyline, spatialReferenceNad, 5000, GeodeticCurveType.Geodesic, null);
        assertFalse(geometryN.equals(geometryW));
        assertTrue(geometryW.equals(geometryW2));

        Geometry geometryWBuff = OperatorGeodesicBuffer.local().execute(polyline, spatialReferenceWgs, GeodeticCurveType.Geodesic, 200, 20, false, null);
        Geometry geometryNBuff = OperatorGeodesicBuffer.local().execute(polyline, spatialReferenceNad, GeodeticCurveType.Geodesic, 200, 20, false, null);
        assertFalse(geometryNBuff.equals(geometryWBuff));

    }

    @Test
    public void testProjectedGeodetic() {
        /*
                // POINT (4322181.519435114 3212199.338618969) proj4: "+proj=laea +lat_0=31.593750 +lon_0=-94.718750 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs"
        SpatialReferenceData spatialReferenceData = SpatialReferenceData.newBuilder().setProj4("+proj=laea +lat_0=31.593750 +lon_0=-94.718750 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs").build();
        GeometryData geometryData = GeometryData.newBuilder().setWkt("POINT (4322181.519435114 3212199.338618969)").setSr(spatialReferenceData).build();
        GeometryRequest geometryRequest = GeometryRequest
                .newBuilder()
                .setGeometry(geometryData)
                .setOperator(OperatorType.GEODESIC_BUFFER)
                .setBufferParams(GeometryRequest.BufferParams.newBuilder().setDistance(200).build())
                .setResultEncoding(Encoding.WKT)
                .build();

        GeometryServiceGrpc.GeometryServiceBlockingStub stub = GeometryServiceGrpc.newBlockingStub(inProcessChannel);
        GeometryResponse geometryResponse = stub.geometryOperationUnary(geometryRequest);

        GeometryRequest geometryRequest1 = GeometryRequest
                .newBuilder()
                .setLeftGeometry(geometryResponse.getGeometry())
                .setRightGeometry(geometryData)
                .setOperator(OperatorType.INTERSECTS)
                .build();

        GeometryResponse geometryResponse1 = stub.geometryOperationUnary(geometryRequest1);
        assertTrue(geometryResponse1.getSpatialRelationship());
         */
        Geometry point = GeometryEngine.geometryFromWkt("POINT (4322181.519435114 3212199.338618969)", 0, Geometry.Type.Unknown);
        SpatialReferenceEx spatialReference = SpatialReferenceEx.createFromProj4("+proj=laea +lat_0=31.593750 +lon_0=-94.718750 +x_0=4321000 +y_0=3210000 +ellps=GRS80 +towgs84=0,0,0,0,0,0,0 +units=m +no_defs");
        OperatorGeodesicBuffer operatorGeodesicBuffer = (OperatorGeodesicBuffer)OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
        Geometry buffered = operatorGeodesicBuffer.execute(point, spatialReference, GeodeticCurveType.Geodesic, 400, Double.NaN, false, null);

        assertFalse(GeometryEngine.disjoint(buffered, point, SpatialReference.create(4326)));
    }


    public void testLengthAccurateCR191313() {
        /*
		 * // random_test(); OperatorFactoryLocal engine =
		 * OperatorFactoryLocal.getInstance(); //TODO: Make this:
		 * OperatorShapePreservingLength geoLengthOp =
		 * (OperatorShapePreservingLength)
		 * factory.getOperator(Operator.Type.ShapePreservingLength);
		 * SpatialReferenceEx spatialRef = SpatialReferenceEx.create(102631);
		 * //[6097817.59407673
		 * ,17463475.2931517],[-1168053.34617516,11199801.3734424
		 * ]]],"spatialReference":{"wkid":102631}
		 *
		 * Polyline polyline = new Polyline();
		 * polyline.startPath(6097817.59407673, 17463475.2931517);
		 * polyline.lineTo(-1168053.34617516, 11199801.3734424); double length =
		 * geoLengthOp.execute(polyline, spatialRef, null);
		 * assertTrue(Math.abs(length - 2738362.3249366437) < 2e-9 * length);
		 */
    }

    public void testGeodeticLength() {
        Polyline polyline = new Polyline();
        polyline.startPath(0,0);
        polyline.lineTo(1, 0);
        double length = OperatorGeodeticLength.local().execute(polyline, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(111319.4907932264, length);

        polyline = new Polyline();
        polyline.startPath(0,0);
        polyline.lineTo(-1, 0);
        length = OperatorGeodeticLength.local().execute(polyline, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(111319.4907932264, length);

        polyline = new Polyline();
        polyline.startPath(179,0);
        polyline.lineTo(-180, 0);
        length = OperatorGeodeticLength.local().execute(polyline, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(111319.4907932264, length, 14);

        polyline = new Polyline();
        polyline.startPath(-179,0);
        polyline.lineTo(-180, 0);
        length = OperatorGeodeticLength.local().execute(polyline, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(111319.4907932264, length, 14);

        polyline = new Polyline();
        polyline.startPath(179,0);
        polyline.lineTo(180, 0);
        length = OperatorGeodeticLength.local().execute(polyline, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(111319.4907932264, length, 14);

        polyline = new Polyline();
        polyline.startPath(180,0);
        polyline.lineTo(179, 0);
        length = OperatorGeodeticLength.local().execute(polyline, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(111319.4907932264, length, 14);

        polyline = new Polyline();
        polyline.startPath(180,0);
        polyline.lineTo(177, 0);
        length = OperatorGeodeticLength.local().execute(polyline, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(3 * 111319.4907932264, length, 14);

        polyline = new Polyline();
        polyline.startPath(0,90);
        polyline.lineTo(0, 0);
        length = OperatorGeodeticLength.local().execute(polyline, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(10001.96572931 * 1000, length, 14);

        polyline = new Polyline();
        polyline.startPath(1,0);
        polyline.lineTo(1, 90);
        length = OperatorGeodeticLength.local().execute(polyline, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(10001.96572931 * 1000, length, 14);

        polyline = new Polyline();
        polyline.startPath(1,90);
        polyline.lineTo(1, 0);
        polyline.lineTo(0, 0);
        polyline.lineTo(0,90);
        length = OperatorGeodeticLength.local().execute(polyline, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(10001.96572931 * 1000 * 2 + 111319.4907932264, length, 14);

        Polygon polygon = new Polygon();
        polygon.startPath(1,90);
        polygon.lineTo(1, 0);
        polygon.lineTo(0, 0);
        polygon.lineTo(0,90);
        length = OperatorGeodeticLength.local().execute(polygon, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(10001.96572931 * 1000 * 2 + 111319.4907932264, length, 14);


        polygon = new Polygon();
        polygon.startPath(10,90);
        polygon.lineTo(10, 0);
        polygon.lineTo(0, 0);
        polygon.lineTo(0,90);
        polygon.closeAllPaths();
        length = OperatorGeodeticLength.local().execute(polygon, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(2.11171263665557E7, length, 14);

        polygon = new Polygon();
        polygon.startPath(10,90);
        polygon.lineTo(10, 0);
        polygon.lineTo(0, 0);
        polygon.lineTo(0,90);
        polygon.startPath(8, 3);
        polygon.lineTo(2, 3);
        polygon.lineTo(5, 80);
        length = OperatorGeodeticLength.local().execute(polygon, SpatialReferenceEx.create(4326), GeodeticCurveType.Geodesic, null);
        assertEquals(3.889408543061711E7, length, 14);

    }

    public void testInverse() {
        SpatialReferenceEx sr = SpatialReferenceEx.create(4326);
        Point point1 = new Point(0,0);
        Point point2 = new Point(-1, 0);
        InverseResult inverseResult = OperatorGeodeticInverse.local().execute(point1, point2, sr, sr, GeodeticCurveType.Geodesic, null);

        assertEquals(111319.4907932264, inverseResult.getDistance_m());
        assertEquals(-Math.PI / 2, inverseResult.getAz12_rad());
        assertEquals(Math.PI / 2, inverseResult.getAz21_rad());

        point1 = new Point(179,0);
        point2 = new Point(-180, 0);
        inverseResult = OperatorGeodeticInverse.local().execute(point1, point2, sr, sr, GeodeticCurveType.Geodesic, null);
        assertEquals(111319.4907932264, inverseResult.getDistance_m(), 14);
        assertEquals(Math.PI / 2, inverseResult.getAz12_rad());
        assertEquals(-Math.PI / 2, inverseResult.getAz21_rad());

        point2 = new Point(179,0);
        point1 = new Point(-180, 0);
        inverseResult = OperatorGeodeticInverse.local().execute(point1, point2, sr, sr, GeodeticCurveType.Geodesic, null);
        assertEquals(111319.4907932264, inverseResult.getDistance_m(), 14);
        assertEquals(-Math.PI / 2, inverseResult.getAz12_rad());
        assertEquals(Math.PI / 2, inverseResult.getAz21_rad());


        point1 = new Point(0,90);
        point2 = new Point(0, 0);
        inverseResult = OperatorGeodeticInverse.local().execute(point1, point2, sr, sr, GeodeticCurveType.Geodesic, null);
        assertEquals(10001.96572931 * 1000, inverseResult.getDistance_m(), 14);
        assertEquals(Math.PI, inverseResult.getAz12_rad());
        assertEquals(0, inverseResult.getAz21_rad(), 14);

        point1 = new Point(1,0);
        point2 = new Point(1, 90);
        inverseResult = OperatorGeodeticInverse.local().execute(point1, point2, sr, sr, GeodeticCurveType.Geodesic, null);
        assertEquals(10001.96572931 * 1000, inverseResult.getDistance_m(), 14);
        assertEquals(0, inverseResult.getAz12_rad(), 14);
        assertEquals(Math.PI, inverseResult.getAz21_rad(), 14);
    }
}
