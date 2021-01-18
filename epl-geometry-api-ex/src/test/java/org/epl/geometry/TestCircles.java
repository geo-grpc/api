package org.epl.geometry;

import com.esri.core.geometry.*;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.ArrayDeque;
import java.util.HashMap;

public class TestCircles extends TestCase {
    @Test
    public void testSquare() {
        Polygon polygon = new Polygon();
        polygon.startPath(-2, -2);
        polygon.lineTo(-2, 2);
        polygon.lineTo(2, 2);
        polygon.lineTo(2, -2);
        polygon.closeAllPaths();
        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(polygon), null, null);
        Geometry geometry = operatorEnclosingCircleCursor.next();
        double radius = Math.sqrt(2*2 + 2*2);
        double area = Math.PI * radius * radius;
        assertEquals(geometry.calculateArea2D(), area, 1e-1);
    }

    @Test
    public void testBuffered() {
        Point center = new Point(0,0);
        Geometry buffered = GeometryEngine.buffer(center, SpatialReferenceEx.create(4326).toSpatialReference(), 20);
        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(buffered), null, null);
        Geometry geometry = operatorEnclosingCircleCursor.next();
        assertEquals(geometry.calculateArea2D(), buffered.calculateArea2D(), 1e-10);
        assertTrue(GeometryEngine.equals(geometry, buffered, null));
    }

    @Test
    public void testGeodesicBuffer() {
        Point center = new Point(0,0);
        Geometry buffered = OperatorGeodesicBuffer.local().execute(center,
                SpatialReferenceEx.create(4326),
                GeodeticCurveType.Geodesic,
                4000,
                20,
                false,
                null);

        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(
                new SimpleGeometryCursor(buffered),
                SpatialReferenceEx.create(4326),
                null);

        Geometry geometry = operatorEnclosingCircleCursor.next();
        // This tests fails without multiVertexGeometry._setDirtyFlag(DirtyFlags.dirtyAll, true); in project
        assertEquals(geometry.calculateArea2D(), buffered.calculateArea2D(), 1e-4);

        Geometry bufferedContainer = OperatorGeodesicBuffer.local().execute(center,
                SpatialReferenceEx.create(4326),
                GeodeticCurveType.Geodesic,
                4050,
                20,
                false,
                null);
        assertTrue(GeometryEngine.contains(bufferedContainer, geometry, SpatialReferenceEx.create(4326).toSpatialReference()));
    }

    @Test
    public void testBufferedClipped() {
        Point center = new Point(0,0);
        Geometry buffered = GeometryEngine.buffer(center, SpatialReferenceEx.create(4326).toSpatialReference(), 20);
        Geometry clipped = GeometryEngine.clip(buffered, new Envelope(0, -20, 40, 40), null);
        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(clipped), null, null);
        Geometry geometry = operatorEnclosingCircleCursor.next();
        assertEquals(geometry.calculateArea2D(), buffered.calculateArea2D(), 1e-10);
        assertTrue(GeometryEngine.equals(geometry, buffered, null));
    }

    @Test
    public void testBufferedClippedUnionedSmall() {
        Point center = new Point(0,0);
        Geometry buffered = GeometryEngine.buffer(center, SpatialReferenceEx.create(4326).toSpatialReference(), 20);
        Geometry clipped = GeometryEngine.clip(buffered, new Envelope(0, -20, 40, 40), null);
        Geometry bufferedSmall = GeometryEngine.buffer(center, SpatialReferenceEx.create(4326).toSpatialReference(), 10);
        Geometry[] two = new Geometry[] {bufferedSmall, clipped};
        Geometry unionedGeom = GeometryEngine.union(two, null);
        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(unionedGeom), null, null);
        Geometry geometry = operatorEnclosingCircleCursor.next();
        assertEquals(geometry.calculateArea2D(), buffered.calculateArea2D(), 1e-10);
        assertTrue(GeometryEngine.equals(geometry, buffered, null));
    }


//    TODO finish implementation of OperatorSimpleRelation and all inheriting relationships (equals, touches, etc)
//    @Test
//    public void testRandomPoints() {
//        int count = 400;
//        Envelope e = new Envelope(0,0,40, 40);
//        RandomCoordinateGenerator randomCoordinateGenerator = new RandomCoordinateGenerator(count, e, SpatialReferenceEx.create(4326).getTolerance());
//        MultiPoint multiPoint = new MultiPoint();
//        for (int i = 0; i < count; i++) {
//            multiPoint.add(randomCoordinateGenerator._GenerateNewPoint());
//        }
//
//        int run_count = 10;
//        ArrayDeque<Geometry> geometries = new ArrayDeque<>();
//        for (int i = 0; i < run_count; i++) {
//            OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(multiPoint), SpatialReferenceEx.create(4326), null);
//            Geometry geometry = operatorEnclosingCircleCursor.next();
//            geometries.add(geometry);
//        }
//
//
////        http://opensourceconnections.com/blog/2014/04/11/indexing-polygons-in-lucene-with-accuracy/
////        http://my-spatial4j-project.blogspot.be/2014/01/minimum-bounding-circle-algorithm-jts.html
//        Geometry firstGeometry = geometries.peekFirst();
//        OperatorSimpleRelationEx operatorEquals = (OperatorSimpleRelationEx) (OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Equals));
//        HashMap<Integer, Boolean> results = operatorEquals.execute(firstGeometry, new SimpleGeometryCursor(geometries), SpatialReferenceEx.create(4326).toSpatialReference(), null);
//        for (Integer key : results.keySet()) {
//            assertTrue(results.get(key));
//        }
//    }

    @Test
    public void testReproject() {
        String wktGeom = "MULTIPOLYGON (((-70.64802717477117 43.128706076602874, -70.6479465414909 43.128714649179535, -70.64788357509747 43.1287487128581, -70.64783854531692 43.12880812176545, -70.64781164513992 43.12889262152996, -70.64780298999258 43.12900185036985, -70.64781261723796 43.12913534064154, -70.6478404860113 43.12929252084091, -70.64788647738838 43.129472718047886, -70.64795039488814 43.129675160806016, -70.6480319653057 43.129898982422475, -70.64813083987421 43.13014322467642, -70.64824659574889 43.13040684191825, -70.64837873780841 43.13068870554328, -70.64852670076544 43.1309876088204, -70.64868985157774 43.131302272055365, -70.64886749214907 43.13163134806583, -70.64905886230987 43.13197342794668, -70.64926314306291 43.132327047097704, -70.64947946008277 43.13269069149233, -70.64970688745207 43.13306280415637, -70.64994445161977 43.13344179183187, -70.65019113556465 43.133826031796644, -70.65044588314564 43.134213878809994, -70.6507076036211 43.13460367215594, -70.65097517631722 43.134993742752336, -70.6512474554263 43.135382420297674, -70.65152327491325 43.135768040422036, -70.65180145351054 43.13614895181459, -70.65208079977953 43.13652352329523, -70.652360117216 43.13689015080003, -70.65263820937935 43.1372472642522, -70.65291388502193 43.137593334287445, -70.65318596319706 43.13792687880496, -70.65345327832425 43.138246469317686, -70.65371468518873 43.138550737072336, -70.65396906385456 43.138838378914386, -70.65421532446969 43.139108162872276, -70.65445241194281 43.139358933437414, -70.65467931047083 43.13958961651619, -70.6548950478984 43.13979922403416, -70.65509869988989 43.1399868581709, -70.65528939389672 43.14015171520967, -70.65546631290157 43.14029308898237, -70.65562869892466 43.140410373897815, -70.65577585627611 43.140503067538305, -70.65590715454054 43.14057077281427, -70.65602203128121 43.14061319966741, -70.65611999445184 43.14063016631512, -70.65620062450573 43.14062160003068, -70.65626357619321 43.14058753745568, -70.65630858004002 43.14052812444448, -70.65633544349907 43.14044361543887, -70.65634405177256 43.1403343723787, -70.6563343682994 43.14020086315083, -70.6563064349065 43.14004365958389, -70.65626037162373 43.13986343499769, -70.65619637616243 43.13966096131707, -70.65611472306145 43.13943710576406, -70.65601576250236 43.139192827140086, -70.65589991880132 43.13892917171714, -70.65576768858229 43.13864726875311, -70.65561963864108 43.13834832565221, -70.65545640350835 43.13803362279019, -70.65527868272287 43.13770450802718, -70.65508723782693 43.13736239093172, -70.65488288909637 43.137008736740526, -70.65466651201972 43.13664506007974, -70.65443903354196 43.136272918475676, -70.6542014280886 43.13589390568142, -70.65395471338735 43.13550964484906, -70.65369994610545 43.13512178157618, -70.65343821732164 43.13473197685713, -70.65317064785184 43.13434189996841, -70.65289838344866 43.1339532213195, -70.6526225898955 43.133567605299014, -70.65234444801612 43.13318670314755, -70.652065148621 43.13281214588721, -70.65178588741227 43.132445537338526, -70.6515078598685 43.13208844725394, -70.65123225613159 43.13174240459792, -70.65096025591802 43.13140889100291, -70.65069302347425 43.131089334426754, -70.65043170260032 43.13078510304208, -70.65017741176095 43.130497499381256, -70.64993123930526 43.130227754762316, -70.64969423881631 43.12997702402096, -70.64946742460877 43.12974638056925, -70.64925176739531 43.12953681180378, -70.64904819013898 43.12934921488169, -70.64885756410969 43.12918439288311, -70.64868070516164 43.12904305137656, -70.64851837024698 43.128925795401116, -70.64837125418116 43.12883312687935, -70.64823998667316 43.12876544247107, -70.64812512963337 43.128723031877364, -70.64802717477117 43.128706076602874)))";
        Geometry geometry = OperatorImportFromWkt.local().execute(0, Geometry.Type.Unknown, wktGeom, null);
        ProjectionTransformation projectionTransformation = ProjectionTransformation.getEqualArea(geometry, SpatialReferenceEx.create(4326));
        Geometry projected = OperatorProject.local().execute(geometry, projectionTransformation, null);
        Envelope envelope = new Envelope();
        projected.queryEnvelope(envelope);
        Point2D centerXY = envelope.getCenter2D();

        double minRadius = Double.MAX_VALUE;
        double maxRadius = Double.MIN_VALUE;
        for (Point2D point2D : ((MultiVertexGeometry)geometry).getCoordinates2D()) {
            double radius = Point2D.distance(point2D, centerXY);
            if (radius > maxRadius) {
                maxRadius = radius;
            }
            if (radius < minRadius) {
                minRadius = radius;
            }
        }
        assertEquals(minRadius, maxRadius, 0.01);
    }

    @Test
    public void testVsGeodesicBuffer() {
        //        POINT (-70.651886936570745 43.134525834585826)
        Point point = new Point(-70.651886936570745, 43.1345088834585826);
        SpatialReferenceEx spatialReferenceWGS84 = SpatialReferenceEx.create(4326);
        Geometry geodesicBuffered = OperatorGeodesicBuffer.local().execute(
                point,
                spatialReferenceWGS84,
                GeodeticCurveType.Geodesic,
                148.7,
                0.07,
                false,
                null);

        String wktInput = "POLYGON ((-70.651656936570745 43.135157834585826,-70.651570654984525 43.135150076925918,-70.651511247908587 43.135155437576621,-70.651490101488349 43.135169249238288,-70.651490181628759 43.135200763774868,-70.651510336532638 43.135249995303397,-70.651530872098633 43.135299226354952,-70.651527230781454 43.13533529757845,-70.651492643684321 43.135349305799508,-70.651420172717224 43.135354852495823,-70.651358095085072 43.135346745253884,-70.651247805462717 43.1353033127629,-70.651151361236941 43.135246176463966,-70.651124154255413 43.135206047556842,-70.651131534466685 43.135151913051203,-70.651155914561755 43.135115542493892,-70.651228730259774 43.135051460048707,-70.651305024993519 43.134973818456416,-70.651360674948108 43.134914489191551,-70.651385078643216 43.134864606853156,-70.651402827815218 43.134810322639233,-70.651410310326284 43.134688652284062,-70.651418383361829 43.134517448375689,-70.651411370788409 43.134413996600074,-70.651390876709428 43.134337752182525,-70.651346755889904 43.134225832686319,-70.651289063395154 43.134109606122337,-70.651241242017051 43.133988734078358,-70.651230854601764 43.133916846697829,-70.651221013347353 43.133822439140715,-70.651200519724128 43.133746194674302,-70.651218268699594 43.133691907971006,-70.651249886578313 43.133610413350418,-70.651273539332081 43.133547040518167,-70.651322422674269 43.133478800504768,-70.651363984222755 43.133424172537978,-70.651409243106613 43.133378494590104,-70.651437060507462 43.133355583224628,-70.651492145648632 43.133332275678633,-70.651599176252716 43.133326227462319,-70.651675204034788 43.133338636120527,-70.65179240423565 43.133381970944697,-70.651905761872953 43.133425353603045,-70.652022336036893 43.133459688797871,-70.652087607730962 43.133472252516562,-70.652149929372143 43.133489361765591,-70.652232540703992 43.133546699550045,-70.652400720753761 43.133656829257461,-70.652544825855031 43.133771806958798,-70.652698657471404 43.133891149422396,-70.652757214424 43.133939825915917,-70.652829643131227 43.133961291723317,-70.652898471779253 43.133991812912122,-70.652963804959569 43.134049399817712,-70.652994471176711 43.134089475795044,-70.653000937222828 43.134129903976664,-70.653000836372897 43.134197439788288,-70.652972837499135 43.134256375321229,-70.652903321638362 43.134342923487154,-70.652853489821567 43.134447199036558,-70.652811504521395 43.134528843881576,-70.652776716073816 43.134592380423527,-70.652745241323998 43.134664872485907,-70.652703717209832 43.134692485155824,-70.65263790191085 43.134702441934749,-70.652537796343609 43.13469488220742,-70.652481986521252 43.134691185475688,-70.652416288981371 43.134705643385693,-70.652347112331768 43.134733655368471,-70.652274294763117 43.134797738553125,-70.652239263183958 43.134852272522785,-70.652214826278225 43.134929165745028,-70.652217533236211 43.135001163954207,-70.652248296977746 43.135059250554882,-70.652327397729081 43.135157158057844,-70.652414500765317 43.135209927851037,-70.6524582645621 43.135222802033766,-70.652470512866358 43.135249637832572,-70.652646824360005 43.135404671730349,-70.652680946443695 43.135444700365824,-70.652691113545444 43.135494075980539,-70.652680299690047 43.135534756334458,-70.652642334694633 43.135580326883741,-70.6525831461027 43.135608197219,-70.65250042683256 43.135618395612582,-70.652403778584315 43.1356107883392,-70.652328034290349 43.135580366756479,-70.652207940292428 43.135501055652128,-70.652084166050003 43.135399292820672,-70.651994153636025 43.135324055019495,-70.651897729053019 43.13525341019011,-70.651815354685922 43.135205077192047,-70.651746646220062 43.135179051560065,-70.651656936570745 43.135157834585826))";
        Geometry input = OperatorImportFromWkt.local().execute(0, Geometry.Type.Unknown, wktInput, null);
        Geometry encircled = OperatorEnclosingCircle.local().execute(input, spatialReferenceWGS84, null);

//        assertEquals(encircled.calculateArea2D(), geodesicBuffered.calculateArea2D(), 1e-8);
//        assertEquals(0.0, OperatorDifference.local().execute(encircled, geodesicBuffered, spatialReferenceWGS84, null).calculateArea2D(), 1e-8);
        assertTrue(GeometryEngine.contains(geodesicBuffered, encircled, spatialReferenceWGS84.toSpatialReference()));
    }

//    @Test
//    public void testCircleVertexContain() {
//        String wkt = "MULTIPOLYGON (((40 40, 20 45, 45 30, 40 40)), ((20 35, 45 20, 30 5, 10 10, 10 30, 20 35), (30 20, 20 25, 20 15, 30 20)))";
//        Geometry input = OperatorImportFromWkt.local().execute(0, Geometry.Type.Unknown, wkt, null);
//        Geometry encircled = OperatorEnclosingCircle.local().execute(input, SpatialReferenceEx.create(4326), null);
//        assertTrue(GeometryEngine.contains(encircled, input, SpatialReferenceEx.create(4326)));
//    }
}
