/*
Copyright 2017-2020 Echo Park Labs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

For additional information, contact:

email: davidraleigh@gmail.com
*/

package org.epl.geometry;


import com.esri.core.geometry.*;

import java.util.ArrayList;

/**
 * Created by davidraleigh on 2/20/16.
 */
class GeodesicBufferer {
    /**
     * Result is always a polygon. For non positive distance and non-areas
     * returns an empty polygon. For points returns circles.
     */
    static Geometry buffer(Geometry geometry,
                           double distance,
                           SpatialReferenceEx sr,
                           double densify_dist,
                           int max_vertex_in_complete_circle,
                           ProgressTracker progress_tracker) {
        if (geometry == null)
            throw new IllegalArgumentException();

        if (densify_dist < 0)
            throw new IllegalArgumentException();

        if (geometry.isEmpty())
            return new Polygon(geometry.getDescription());

        GeodesicBufferer geodesicBufferer = new GeodesicBufferer(progress_tracker);

        Envelope2D env2D = new Envelope2D();
        geometry.queryLooseEnvelope2D(env2D);

        geodesicBufferer.m_spatialReference = sr;

        if (distance > 0){
            //         env2D.inflate(distance, distance);
            GeoDist.inflateEnv2D(
                    geodesicBufferer.m_spatialReference.getMajorAxis(),
                    geodesicBufferer.m_spatialReference.getEccentricitySquared(),
                    env2D,
                    distance,
                    distance);
        }

        geodesicBufferer.m_geometry = geometry;
        geodesicBufferer.m_tolerance = InternalUtils.calculateToleranceFromGeometry(sr.toSpatialReference(), env2D, true);// conservative to have same effect as simplify
        geodesicBufferer.m_small_tolerance = InternalUtils.calculateToleranceFromGeometry(null, env2D, true);// conservative
        // to have same effect as simplify
        geodesicBufferer.m_distance = distance;
        geodesicBufferer.m_original_geom_type = geometry.getType().value();
        if (max_vertex_in_complete_circle <= 0) {
            max_vertex_in_complete_circle = 96;// 96 is the value used by SG.
            // This is the number of
            // vertices in the full circle.
        }

        geodesicBufferer.m_abs_distance = Math.abs(geodesicBufferer.m_distance);
        geodesicBufferer.m_abs_distance_reversed = geodesicBufferer.m_abs_distance != 0 ? 1.0 / geodesicBufferer.m_abs_distance : 0;

        if (NumberUtils.isNaN(densify_dist) || densify_dist == 0) {
            densify_dist = geodesicBufferer.m_abs_distance * 1e-5;
        } else if (densify_dist > geodesicBufferer.m_abs_distance * 0.5) {
            //TODO put a break point here to see if this is ever a problem
            // do not allow too large densify distance (the value will be adjusted anyway later)
            densify_dist = geodesicBufferer.m_abs_distance * 0.5;
        }

        if (max_vertex_in_complete_circle < 12)
            max_vertex_in_complete_circle = 12;

        // TODO I don't know what max_dd is for. decimal degrees?
        double max_dd = Math.abs(distance) * (1 - Math.cos(Math.PI / max_vertex_in_complete_circle));

        if (max_dd > densify_dist) {
            densify_dist = max_dd;// the densify distance has to agree with the
            // max_vertex_in_complete_circle
        } else {
            double vertex_count = Math.PI / Math.acos(1.0 - densify_dist / Math.abs(distance));
            if (vertex_count < (double) max_vertex_in_complete_circle - 1.0) {
                max_vertex_in_complete_circle = (int) vertex_count;
                if (max_vertex_in_complete_circle < 12) {
                    max_vertex_in_complete_circle = 12;
                    densify_dist = Math.abs(distance) * (1 - Math.cos(Math.PI / max_vertex_in_complete_circle));
                }
            }
        }

        geodesicBufferer.m_densify_dist = densify_dist;
        geodesicBufferer.m_max_vertex_in_complete_circle = max_vertex_in_complete_circle;
        // when filtering close points we do not want the filter to distort
        // generated buffer too much.
        geodesicBufferer.m_filter_tolerance = Math.min(geodesicBufferer.m_small_tolerance, densify_dist * 0.25);
        return geodesicBufferer.buffer_();
    }

    private Geometry m_geometry;

    protected static final class GeodesicBufferCommand {
        protected interface Flags {
            int enum_line = 1;
            int enum_arc = 2;
            int enum_dummy = 4;
            int enum_concave_dip = 8;
            int enum_connection = enum_arc | enum_line;
        }

        protected Point2D m_from;
        protected Point2D m_to;
        protected Point2D m_center;
        protected int m_next;
        protected int m_prev;
        protected int m_type;

        protected GeodesicBufferCommand(Point2D from, Point2D to, Point2D center,
                                        int type, int next, int prev) {
            m_from = new Point2D();
            m_to = new Point2D();
            m_center = new Point2D();
            m_from.setCoords(from);
            m_to.setCoords(to);
            m_center.setCoords(center);
            m_type = type;
            m_next = next;
            m_prev = prev;
        }

        protected GeodesicBufferCommand(Point2D from, Point2D to, int next, int prev,
                                        String dummy) {
            m_from = new Point2D();
            m_to = new Point2D();
            m_center = new Point2D();
            m_from.setCoords(from);
            m_to.setCoords(to);
            m_center.setNaN();
            m_type = 4;
            m_next = next;
            m_prev = prev;
        }
    }

    private ArrayList<GeodesicBufferCommand> m_buffer_commands;

    private int m_original_geom_type;
    private ProgressTracker m_progress_tracker;
    private int m_max_vertex_in_complete_circle;
    private SpatialReferenceEx m_spatialReference;
    private double m_tolerance;
    private double m_small_tolerance;
    private double m_filter_tolerance;
    private double m_densify_dist;
    private double m_distance;
    private double m_abs_distance;
    private double m_abs_distance_reversed;
    private double m_dA;
    private boolean m_b_output_loops;
    private boolean m_bfilter;
//    private double m_a;
//    private double m_e2;
    private static final double RAD_TO_DEG = 180.0 / Math.PI;
    private static final double DEG_TO_RAD = Math.PI / 180.0;

    //    private ArrayList<Point2D> m_left_stack;
    //    private ArrayList<Point2D> m_middle_stack;
    private Line m_helper_line_1;
    private Line m_helper_line_2;
    private Point2D[] m_helper_array;
    private int m_progress_counter;

    private static final class GeometryCursorForMultiPoint extends GeometryCursor {
        private int m_index;
        private MultiPoint m_mp;
        private SpatialReferenceEx m_spatialReference;
        private double m_distance;
        private double m_densify_dist;
        private int m_max_vertex_in_complete_circle;
        private ProgressTracker m_progress_tracker;

        GeometryCursorForMultiPoint(MultiPoint mp,
                                    double distance,
                                    SpatialReferenceEx sr,
                                    double densify_dist,
                                    int max_vertex_in_complete_circle,
                                    ProgressTracker progress_tracker) {
            m_index = 0;
            m_mp = mp;
            m_distance = distance;
            m_spatialReference = sr;
            m_densify_dist = densify_dist;
            m_max_vertex_in_complete_circle = max_vertex_in_complete_circle;
            m_progress_tracker = progress_tracker;
        }

        @Override
        public boolean hasNext() { return m_index < m_mp.getPointCount(); }

        @Override
        public Geometry next() {
            Point point = new Point();
            while (true) {
                if (m_index == m_mp.getPointCount())
                    return null;

                m_mp.getPointByVal(m_index, point);
                m_index++;
                if (point.isEmpty())
                    continue;
                break;
            }

            return GeodesicBufferer.buffer(
                    point,
                    m_distance,
                    m_spatialReference,
                    m_densify_dist,
                    m_max_vertex_in_complete_circle,
                    m_progress_tracker);
        }

        @Override
        public int getGeometryID() {
            return 0;
        }
    }

    private static final class GeometryCursorForPolyline extends GeometryCursor {
        private GeodesicBufferer m_bufferer;
        private int m_index;
        private boolean m_bfilter;

        GeometryCursorForPolyline(GeodesicBufferer geodesicBufferer, boolean bfilter) {
            m_bufferer = geodesicBufferer;
            m_index = 0;
            m_bfilter = bfilter;
        }

        @Override
        public boolean hasNext() { return m_index < ((MultiPath)m_bufferer.m_geometry).getPathCount(); }

        @Override
        public Geometry next() {
//            MultiPathImpl mp = (MultiPathImpl) (m_bufferer.m_geometry._getImpl());
            MultiPath mp = (MultiPath)m_bufferer.m_geometry;
            // if there is a path left to retrieve
            if (m_index < mp.getPathCount()) {
                int ind = m_index;
                m_index++;

                // TODO isClosedPathInXYPlane?! is the first point the same as the last?
                if (!mp.isClosedPathInXYPlane(ind)) {
                    // TODO ??!! grab 2nd to last last point?
                    Point2D prev_end = mp.getXY(mp.getPathEnd(ind) - 1);
                    while (m_index < mp.getPathCount()) {
                        Point2D start = mp.getXY(mp.getPathStart(m_index));
                        if (mp.isClosedPathInXYPlane(m_index))
                            break;
                        if (start != prev_end)
                            break;

                        prev_end = mp.getXY(mp.getPathEnd(m_index) - 1);
                        m_index++;
                    }
                }


                if (m_index - ind == 1) {
                    // if there is only one path
                    return m_bufferer.bufferPolylinePath_((Polyline) (m_bufferer.m_geometry), ind, m_bfilter);
                } else {
                    Polyline tmp_polyline = new Polyline(m_bufferer.m_geometry.getDescription());
                    MultiPath tmp_mp = (MultiPath)tmp_polyline;
                    tmp_polyline.addPath((Polyline) m_bufferer.m_geometry, ind, true);
                    for (int i = ind + 1; i < m_index; i++) {
                        tmp_mp.addSegmentsFromPath(mp, i,0, mp.getSegmentCount(i),false);
                    }

                    Polygon res = m_bufferer.bufferPolylinePath_(tmp_polyline, 0, m_bfilter);
                    return res;
                }
            }

            return null;
        }

        @Override
        public int getGeometryID() {
            return 0;
        }
    }

//    private static final class GeometryCursorForPolygon extends GeometryCursor {
//        private GeodesicBufferer m_bufferer;
//        private int m_index;
//
//        GeometryCursorForPolygon(GeodesicBufferer geodesicBufferer) {
//            m_bufferer = geodesicBufferer;
//            m_index = 0;
//        }
//
//        @Override
//        public Geometry next() {
//            Polygon input_polygon = (Polygon) (m_bufferer.m_geometry);
//            if (m_index < input_polygon.getPathCount()) {
//                int ind = m_index;
//                double area = input_polygon.calculateRingArea2D(m_index);
//                assert (area > 0);
//                m_index++;
//                while (m_index < input_polygon.getPathCount()) {
//                    double hole_area = input_polygon
//                            .calculateRingArea2D(m_index);
//                    if (hole_area > 0)
//                        break;// not a hole
//                    m_index++;
//                }
//
//                if (ind == 0 && m_index == input_polygon.getPathCount()) {
//                    return m_bufferer.bufferPolygonImpl_(input_polygon, 0,
//                            input_polygon.getPathCount());
//                } else {
//                    return m_bufferer.bufferPolygonImpl_(input_polygon, ind,
//                            m_index);
//                }
//            }
//
//            return null;
//        }
//
//        @Override
//        public int getGeometryID() {
//            return 0;
//        }
//    }
//

    private GeodesicBufferer(ProgressTracker progress_tracker) {
        m_buffer_commands = new ArrayList<GeodesicBufferCommand>(0);
        m_progress_tracker = progress_tracker;
        m_tolerance = 0;
        m_small_tolerance = 0;
        m_filter_tolerance = 0;
        m_distance = 0;
        m_original_geom_type = Geometry.GeometryType.Unknown;
        m_abs_distance_reversed = 0;
        m_abs_distance = 0;
        m_densify_dist = -1;
        m_dA = -1;
        m_b_output_loops = true;
        m_bfilter = true;
//        m_a = 6378137.0; // radius of spheroid for WGS_1984
//        m_e2 = 0.0066943799901413165; // ellipticity for WGS_1984
    }

    private Geometry buffer_() {
        int gt = m_geometry.getType().value();
        if (Geometry.isSegment(gt)) {// convert segment to a polyline and repeat
            // the call
            Polyline polyline = new Polyline(m_geometry.getDescription());
            polyline.addSegment((Segment) (m_geometry), true);
            m_geometry = polyline;
            return buffer_();
        }

        if (m_distance <= m_tolerance) {
            if (Geometry.isArea(gt)) {
                //TODO add geodetic getWidth and getHeight for Envelope
                if (m_distance <= 0) {
                    // if the geometry is area type, then the negative distance
                    // may produce a degenerate shape. Check for this and return
                    // empty geometry.
                    Envelope2D env = new Envelope2D();
                    m_geometry.queryEnvelope2D(env);

                    if (GeoDist.getEnvWidth(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), env) <= -m_distance * 2 ||
                            GeoDist.getEnvHeight(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), env) <= m_distance * 2) {
                        return new Polygon(m_geometry.getDescription());
                    }
                }
            } else {
                return new Polygon(m_geometry.getDescription());
                // return an empty polygon for distance <= m_tolerance
                // and any input other than polygon.
            }
        }

        // Complex cases:
        switch (m_geometry.getType().value()) {
            case Geometry.GeometryType.Point:
                return bufferPoint_();
            case Geometry.GeometryType.MultiPoint:
                return bufferMultiPoint_();
            case Geometry.GeometryType.Polyline:
                return bufferPolyline_();
            case Geometry.GeometryType.Polygon:
                return bufferPolygon_();
            case Geometry.GeometryType.Envelope:
                return bufferEnvelope_();
            default:
                throw new GeometryException("GeometryInternalError");
        }
    }

    private Geometry bufferPolyline_() {
        if (isDegenerateGeometry_(m_geometry)) {
            Point point = new Point();
            ((MultiVertexGeometry) m_geometry).getPointByVal(0, point);
            Envelope2D env2D = new Envelope2D();
            m_geometry.queryEnvelope2D(env2D);
            // TODO get center Geodesic
            point.setXY(env2D.getCenter());
            return bufferPoint_(point);
        }

        // TODO cannot use preparePolyline until there is a Geodetic Generalize
        //m_geometry = preparePolyline_((Polyline) (m_geometry));

        GeometryCursorForPolyline cursor = new GeometryCursorForPolyline(this, m_bfilter);
        GeometryCursor union_cursor = ((OperatorUnion) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Union)).execute(
                cursor,
                m_spatialReference.toSpatialReference(),
                m_progress_tracker);
        Geometry result = union_cursor.next();
        return result;
    }


    private Geometry bufferPolygon_() {
        if (m_distance == 0)
            return m_geometry;// return input to the output.

        OperatorSimplify simplify = (OperatorSimplify) OperatorFactoryLocal
                .getInstance().getOperator(Operator.Type.Simplify);

        m_geometry = simplify.execute(m_geometry, null, false,
                m_progress_tracker);

        Polygon poly = (Polygon) (m_geometry);
        OperatorBoundary boundaryOp = (OperatorBoundary) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Boundary);
        SimpleGeometryCursor inputPolygonCursor = new SimpleGeometryCursor(m_geometry);
        GeometryCursor boundaryLocalCursor = boundaryOp.execute(inputPolygonCursor, null);
        OperatorGeodesicBuffer geodesicOp = (OperatorGeodesicBuffer) OperatorFactoryLocalEx.getInstance().getOperator(OperatorEx.Type.GeodesicBuffer);
        double[] distances = new double[1];
        distances[0] = m_abs_distance;
        GeometryCursor bufferedBoundaryCursor = geodesicOp.execute(boundaryLocalCursor, m_spatialReference, 0, distances, m_densify_dist, false, true, m_progress_tracker);
        if (m_distance < 0) {
            OperatorDifference differenceOp = (OperatorDifference) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Difference);
            SimpleGeometryCursor subtractee = new SimpleGeometryCursor(m_geometry);
            GeometryCursor negativeBufferedGeom = differenceOp.execute(subtractee, bufferedBoundaryCursor, m_spatialReference.toSpatialReference(), m_progress_tracker);
//            Polygon buffered_result = bufferPolygonImpl_(poly, 0,
//                    poly.getPathCount());
//            return simplify.execute(buffered_result, m_spatialReference, false,
//                    m_progress_tracker);
            return negativeBufferedGeom.next();
        } else {
            if (isDegenerateGeometry_(m_geometry)) {
                Point point = new Point();
                ((MultiVertexGeometry) m_geometry).getPointByVal(0, point);
                Envelope2D env2D = new Envelope2D();
                m_geometry.queryEnvelope2D(env2D);
                // TODO get center Geodesic
                point.setXY(env2D.getCenter());
                return bufferPoint_(point);
            }
            return ((OperatorUnion) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Union)).execute(bufferedBoundaryCursor.next(), m_geometry, m_spatialReference.toSpatialReference(), null);
//            // For the positive distance we need to process polygon in the parts
//            // such that each exterior ring with holes is processed separatelly.
//            GeometryCursorForPolygon cursor = new GeometryCursorForPolygon(this);
//            GeometryCursor union_cursor = ((OperatorUnion) OperatorFactoryLocal
//                    .getInstance().getOperator(Operator.Type.Union)).execute(
//                    cursor, m_spatialReference, m_progress_tracker);
//            Geometry result = union_cursor.next();
//            return result;
        }
    }
//
//    private Polygon bufferPolygonImpl_(Polygon input_geom, int ipath_begin,
//                                       int ipath_end) {
//        MultiPath input_mp = (MultiPath) (input_geom);
//        MultiPathImpl mp_impl = (MultiPathImpl) (input_mp._getImpl());
//        Polygon intermediate_polygon = new Polygon(input_geom.getDescription());
//        for (int ipath = ipath_begin; ipath < ipath_end; ipath++) {
//            if (mp_impl.getPathSize(ipath) < 1)
//                continue;
//
//            double path_area = mp_impl.calculateRingArea2D(ipath);
//            Envelope2D env2D = new Envelope2D();
//            mp_impl.queryPathEnvelope2D(ipath, env2D);
//
//            if (m_distance > 0) {
//                if (path_area > 0) {
//                    if (isDegeneratePath_(mp_impl, ipath)) {// if a path is
//                        // degenerate
//                        // (almost a point),
//                        // then we can draw
//                        // a circle instead
//                        // of it as a buffer
//                        // and nobody would
//                        // notice :)
//                        Point point = new Point();
//                        mp_impl.getPointByVal(mp_impl.getPathStart(ipath),
//                                point);
//                        point.setXY(env2D.getCenter());
//                        addCircle_(
//                                (MultiPathImpl) intermediate_polygon._getImpl(),
//                                point);
//                    } else {
//                        Polyline result_polyline = new Polyline(
//                                input_geom.getDescription());
//                        MultiPathImpl result_mp = (MultiPathImpl) result_polyline
//                                ._getImpl();
//
//                        // We often see convex hulls, buffering those is an
//                        // extremely simple task.
//                        boolean bConvex = ConvexHull.isPathConvex(
//                                (Polygon) (m_geometry), ipath,
//                                m_progress_tracker);
//                        if (bConvex
//                                || bufferClosedPath_(m_geometry, ipath,
//                                result_mp, true, 1) == 2) {
//                            Polygon buffered_path = bufferConvexPath_(input_mp,
//                                    ipath);
//                            intermediate_polygon.add(buffered_path, false);
//                        } else {
//                            Polygon buffered_path = bufferCleanup_(
//                                    result_polyline, false);
//                            intermediate_polygon.add(buffered_path, false);
//                        }
//                    }
//                } else {
//                    if (env2D.getWidth() + m_tolerance <= 2 * m_abs_distance
//                            || env2D.getHeight() + m_tolerance <= 2 * m_abs_distance) // skip
//                        // parts
//                        // that
//                        // will
//                        // dissapear
//                        continue;
//
//                    Polyline result_polyline = new Polyline(
//                            input_geom.getDescription());
//                    MultiPathImpl result_mp = (MultiPathImpl) result_polyline
//                            ._getImpl();
//                    bufferClosedPath_(m_geometry, ipath, result_mp, true, 1);
//                    if (!result_polyline.isEmpty()) {
//                        Envelope2D env = new Envelope2D();
//                        env.setCoords(env2D);
//                        env.inflate(m_abs_distance, m_abs_distance);
//                        result_mp.addEnvelope(env, false);
//                        Polygon buffered_path = bufferCleanup_(result_polyline,
//                                false);
//                        // intermediate_polygon.reserve(intermediate_polygon.getPointCount()
//                        // + buffered_path.getPointCount() - 4);
//                        for (int i = 1, n = buffered_path.getPathCount(); i < n; i++)
//                            intermediate_polygon
//                                    .addPath(buffered_path, i, true);
//                    }
//                }
//            } else {
//                if (path_area > 0) {
//                    if (env2D.getWidth() + m_tolerance <= 2 * m_abs_distance
//                            || env2D.getHeight() + m_tolerance <= 2 * m_abs_distance) // skip
//                        // parts
//                        // that
//                        // will
//                        // dissapear
//                        continue;
//
//                    Polyline result_polyline = new Polyline(
//                            input_geom.getDescription());
//                    MultiPathImpl result_mp = (MultiPathImpl) result_polyline
//                            ._getImpl();
//                    bufferClosedPath_(m_geometry, ipath, result_mp, true, -1);// this
//                    // will
//                    // provide
//                    // a
//                    // shape
//                    // buffered
//                    // inwards.
//                    // It
//                    // has
//                    // counterclockwise
//                    // orientation
//                    if (!result_polyline.isEmpty()) {
//                        Envelope2D env = new Envelope2D();
//                        result_mp.queryLooseEnvelope2D(env);
//                        env.inflate(m_abs_distance, m_abs_distance);
//                        result_mp.addEnvelope(env, false);// add an envelope
//                        // exterior shell
//                        Polygon buffered_path = bufferCleanup_(result_polyline,
//                                false);// simplify with winding rule
//                        // extract all parts but the first one (which is the
//                        // envelope we added previously)
//                        for (int i = 1, npaths = buffered_path.getPathCount(); i < npaths; i++) {
//                            // the extracted parts have inverted orientation.
//                            intermediate_polygon
//                                    .addPath(buffered_path, i, true);
//                        }
//                    } else {
//                        // the path has been erased
//                    }
//                } else {
//                    // When buffering a hole with negative distance, buffer it
//                    // as if it is an exterior ring buffered with positive
//                    // distance
//                    Polyline result_polyline = new Polyline(
//                            input_geom.getDescription());
//                    MultiPathImpl result_mp = (MultiPathImpl) result_polyline
//                            ._getImpl();
//                    bufferClosedPath_(m_geometry, ipath, result_mp, true, -1);// this
//                    // will
//                    // provide
//                    // a
//                    // shape
//                    // buffered
//                    // inwards.
//                    Polygon buffered_path = bufferCleanup_(result_polyline,
//                            false);
//                    for (int i = 0, npaths = buffered_path.getPathCount(); i < npaths; i++) {
//                        intermediate_polygon.addPath(buffered_path, i, true);// adds
//                        // buffered
//                        // hole
//                        // reversed
//                        // as
//                        // if
//                        // it
//                        // is
//                        // exteror
//                        // ring
//                    }
//                }
//
//                // intermediate_polygon has inverted orientation.
//            }
//        }
//
//        if (m_distance > 0) {
//            if (intermediate_polygon.getPathCount() > 1) {
//                Polygon cleaned_polygon = bufferCleanup_(intermediate_polygon,
//                        false);
//                return cleaned_polygon;
//            } else {
//                return setWeakSimple_(intermediate_polygon);
//            }
//        } else {
//            Envelope2D polyenv = new Envelope2D();
//            intermediate_polygon.queryLooseEnvelope2D(polyenv);
//            if (!intermediate_polygon.isEmpty()) {
//                // negative buffer distance. We got buffered holes and exterior
//                // rings. They all have wrong orientation.
//                // we need to apply winding simplify again to ensure all holes
//                // are unioned.
//                // For that create a big envelope and add all rings of the
//                // intermediate_polygon to it.
//                polyenv.inflate(m_abs_distance, m_abs_distance);
//                intermediate_polygon.addEnvelope(polyenv, false);
//                Polygon cleaned_polygon = bufferCleanup_(intermediate_polygon,
//                        false);
//                // intermediate_polygon.reset();//free memory
//
//                Polygon result_polygon = new Polygon(
//                        cleaned_polygon.getDescription());
//                for (int i = 1, n = cleaned_polygon.getPathCount(); i < n; i++) {
//                    result_polygon.addPath(cleaned_polygon, i, false);
//                }
//                return setWeakSimple_(result_polygon);
//            } else {
//                return setWeakSimple_(intermediate_polygon);
//            }
//        }
//    }
//

    private Geometry bufferPoint_() {
        return bufferPoint_((Point) (m_geometry));
    }

    private Geometry bufferPoint_(Point point) {
        assert (m_distance > 0);
        Polygon resultPolygon = new Polygon(m_geometry.getDescription());
        addCircle_((MultiPathImpl) resultPolygon._getImpl(), point);
        return setStrongSimple_(resultPolygon);
    }

    private Geometry bufferMultiPoint_() {
        assert (m_distance > 0);
        GeometryCursorForMultiPoint mpCursor = new GeometryCursorForMultiPoint(
                (MultiPoint) m_geometry,
                m_distance,
                m_spatialReference,
                m_densify_dist,
                m_max_vertex_in_complete_circle,
                m_progress_tracker);
        // TODO is this union necessary??!??!??!
        GeometryCursor c = ((OperatorUnion) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Union)).execute(
                mpCursor,
                m_spatialReference.toSpatialReference(),
                m_progress_tracker);
        return c.next();
    }

    private Geometry bufferEnvelope_() {
        Polygon polygon = new Polygon(m_geometry.getDescription());
        if (m_distance <= 0) {
            if (m_distance == 0)
                polygon.addEnvelope((Envelope) (m_geometry), false);
            else {
                Envelope2D env = new Envelope2D();
                m_geometry.queryEnvelope2D(env);
                GeoDist.inflateEnv2D(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), env, m_distance, m_distance);
//                env.inflate(m_distance, m_distance);
                polygon.addEnvelope(env, false);
            }

            return polygon;// nothing is easier than negative buffer on the envelope.
        }

        polygon.addEnvelope((Envelope) (m_geometry), false);
        m_geometry = polygon;
        return bufferPolygon_();
        //return bufferConvexPath_(polygon, 0);
    }
//
//    private Polygon bufferConvexPath_(MultiPath src, int ipath) {
//        generateCircleTemplate_();
//
//        Polygon resultPolygon = new Polygon(src.getDescription());
//        MultiPathImpl result_mp = (MultiPathImpl) resultPolygon._getImpl();
//
//        // resultPolygon.reserve((m_circle_template.size() / 10 + 4) *
//        // src.getPathSize(ipath));
//
//        Point2D pt_1_tmp = new Point2D(), pt_1 = new Point2D();
//        Point2D pt_2_tmp = new Point2D(), pt_2 = new Point2D();
//        Point2D pt_3_tmp = new Point2D(), pt_3 = new Point2D();
//        Point2D v_1 = new Point2D();
//        Point2D v_2 = new Point2D();
//        MultiPathImpl src_mp = (MultiPathImpl) src._getImpl();
//        int path_size = src.getPathSize(ipath);
//        int path_start = src.getPathStart(ipath);
//        for (int i = 0, n = src.getPathSize(ipath); i < n; i++) {
//            src_mp.getXY(path_start + i, pt_1);
//            src_mp.getXY(path_start + (i + 1) % path_size, pt_2);
//            src_mp.getXY(path_start + (i + 2) % path_size, pt_3);
//            v_1.sub(pt_2, pt_1);
//            if (v_1.length() == 0)
//                throw GeometryException.GeometryInternalError();
//
//            v_1.leftPerpendicular();
//            v_1.normalize();
//            v_1.scale(m_abs_distance);
//            pt_1_tmp.add(v_1, pt_1);
//            pt_2_tmp.add(v_1, pt_2);
//            if (i == 0)
//                result_mp.startPath(pt_1_tmp);
//            else {
//                result_mp.lineTo(pt_1_tmp);
//            }
//
//            result_mp.lineTo(pt_2_tmp);
//
//            v_2.sub(pt_3, pt_2);
//            if (v_2.length() == 0)
//                throw GeometryException.GeometryInternalError();
//
//            v_2.leftPerpendicular();
//            v_2.normalize();
//            v_2.scale(m_abs_distance);
//            pt_3_tmp.add(v_2, pt_2);
//
//            addJoin_(result_mp, pt_2, pt_2_tmp, pt_3_tmp, false, false);
//        }
//
//        return setWeakSimple_(resultPolygon);
//    }

    private Polygon bufferPolylinePath_(Polyline polyline, int ipath, boolean bfilter) {
        assert (m_distance != 0);
        //TODO, circle template doesn't work with Geodesics (unless all circles are on the same line of latitude)
//        generateCircleTemplate_();

        MultiPath input_multi_path = polyline;
        MultiPathImpl mp_impl = (MultiPathImpl) (input_multi_path._getImpl());

        if (mp_impl.getPathSize(ipath) < 1)
            return null;

        if (isDegeneratePath_(mp_impl, ipath) && m_distance > 0) {// if a path
            // is degenerate (almost a point), then we can draw a circle instead
            // of it as a buffer and nobody would notice :)

            Point point = new Point();
            mp_impl.getPointByVal(mp_impl.getPathStart(ipath), point);
            Envelope2D env2D = new Envelope2D();
            mp_impl.queryPathEnvelope2D(ipath, env2D);
            point.setXY(env2D.getCenter());
            return (Polygon) (bufferPoint_(point));
        }

        Polyline result_polyline = new Polyline(polyline.getDescription());
        //TODO what is this commented out code?
        // result_polyline.reserve((m_circle_template.size() / 10 + 4) *
        // mp_impl.getPathSize(ipath));

        MultiPathImpl result_mp = (MultiPathImpl) result_polyline._getImpl();
        boolean b_closed = mp_impl.isClosedPathInXYPlane(ipath);

        if (b_closed) {
            bufferClosedPath_(input_multi_path, ipath, result_mp, bfilter, 1);
            bufferClosedPath_(input_multi_path, ipath, result_mp, bfilter, -1);
        } else {
            Polyline tmpPoly = new Polyline(input_multi_path.getDescription());
            tmpPoly.addPath(input_multi_path, ipath, false);
            ((MultiPathImpl) tmpPoly._getImpl()).addSegmentsFromPath(
                    (MultiPathImpl) input_multi_path._getImpl(),
                    ipath,
                    0,
                    input_multi_path.getSegmentCount(ipath),
                    false);
            bufferClosedPath_(tmpPoly, 0, result_mp, bfilter, 1);
//            // Operator_factory_local::SaveJSONToTextFileDbg("c:/temp/buffer_prepare.txt",
//            // *result_polyline, nullptr);
        }

        return bufferCleanup_(result_polyline, false);
    }

    // Planar and Geodesic are equivalent
    private void progress_() {
        m_progress_counter++;
        if (m_progress_counter % 1024 == 0) {
            if ((m_progress_tracker != null)
                    && !(m_progress_tracker.progress(-1, -1)))
                throw new RuntimeException("user_canceled");
        }
    }


    private Polygon bufferCleanup_(MultiPath multi_path, boolean simplify_result) {
        double tol = simplify_result ? m_tolerance : m_small_tolerance;
        String words = GeometryEngine.geometryToWkt((Polyline) multi_path, 0);
        Polygon resultPolygon = (Polygon) (TopologicalOperations
                .planarSimplify(multi_path, tol, true, !simplify_result,
                        m_progress_tracker));
        assert (InternalUtils.isWeakSimple(resultPolygon, 0.0));
        return resultPolygon;
    }

    //TODO prepare this for Geodesic
    private int calcN_(int minN) {
        if (m_densify_dist == 0)
            return m_max_vertex_in_complete_circle;

        double r = m_densify_dist * Math.abs(m_abs_distance_reversed);
        double cos_a = 1 - r;
        double N;
        if (cos_a < -1)
            N = minN;
        else
            N = 2.0 * Math.PI / Math.acos(cos_a) + 0.5;

        if (N < minN)
            N = minN;
        else if (N > m_max_vertex_in_complete_circle)
            N = m_max_vertex_in_complete_circle;

        return (int) N;
    }

    private void addJoin_(MultiPathImpl dst,
                          Point2D center,
                          Point2D arcStartPt,
                          Point2D arcEndPt,
                          boolean bStartPath,
                          boolean bFinishAtToPt) {
        addArc_(dst, center, arcStartPt, arcEndPt, bStartPath, bFinishAtToPt);

//        v_1.sub(fromPt, center);
//        v_1.scale(m_abs_distance_reversed);
//        Point2D v_2 = new Point2D();
//        v_2.sub(toPt, center);
//        v_2.scale(m_abs_distance_reversed);
//        double angle_from = Math.atan2(v_1.y, v_1.x);
//        double dindex_from = angle_from / m_dA;
//        if (dindex_from < 0)
//            dindex_from = (double) m_circle_template.size() + dindex_from;
//
//        dindex_from = (double) m_circle_template.size() - dindex_from;
//
//        double angle_to = Math.atan2(v_2.y, v_2.x);
//        double dindex_to = angle_to / m_dA;
//        if (dindex_to < 0)
//            dindex_to = (double) m_circle_template.size() + dindex_to;
//
//        dindex_to = (double) m_circle_template.size() - dindex_to;
//
//        if (dindex_to < dindex_from)
//            dindex_to += (double) m_circle_template.size();
//        assert (dindex_to >= dindex_from);
//
//        int index_to = (int) dindex_to;
//        int index_from = (int) Math.ceil(dindex_from);
//
//        if (bStartPath) {
//            dst.startPath(fromPt);
//            bStartPath = false;
//        }
//
//        Point2D p = new Point2D();
//        p.setCoords(m_circle_template.get(index_from % m_circle_template.size()));
//        p.scaleAdd(m_abs_distance, center);
//        double ddd = m_tolerance * 10;
//        p.sub(fromPt);
//        if (p.length() < ddd)// if too close to the fromPt, then use the next
//            // point
//            index_from += 1;
//
//        p.setCoords(m_circle_template.get(index_to % m_circle_template.size()));
//        p.scaleAdd(m_abs_distance, center);
//        p.sub(toPt);
//        if (p.length() < ddd)// if too close to the toPt, then use the prev
//            // point
//            index_to -= 1;
//
//        int count = index_to - index_from;
//        count++;
//
//        for (int i = 0, j = index_from % m_circle_template.size(); i < count; i++, j = (j + 1)
//                % m_circle_template.size()) {
//            p.setCoords(m_circle_template.get(j));
//            p.scaleAdd(m_abs_distance, center);
//            dst.lineTo(p);
//            progress_();
//        }

//        if (bFinishAtToPt) {
//            dst.lineTo(toPt);
//        }
    }

    private int bufferClosedPath_(Geometry input_geom,
                                  int ipath,
                                  MultiPathImpl result_mp,
                                  boolean bfilter,
                                  int dir) {
        // Use temporary polyline for the path buffering.
        EditShape edit_shape = new EditShape();
        int geom = edit_shape.addPathFromMultiPath((MultiPath) input_geom, ipath, true);

        //TODO not sure what filtering does
        edit_shape.filterClosePoints(m_filter_tolerance, false, false);

        if (edit_shape.getPointCount(geom) < 2) {
            // Got degenerate output.
            // Wither bail out or
            // produce a circle.
            if (dir < 0)
                return 1;// negative direction produces nothing.

            MultiPath mpIn = (MultiPath) input_geom;
            // Add a circle
            Point pt = new Point();
            mpIn.getPointByVal(mpIn.getPathStart(ipath), pt);
            addCircle_(result_mp, pt);
            return 1;
        }

        assert (edit_shape.getFirstPath(geom) != -1);
        assert (edit_shape.getFirstVertex(edit_shape.getFirstPath(geom)) != -1);

        //TODO this won't work with Geodesic operations!!! It will ruin the results
//        Point2D origin = edit_shape.getXY(edit_shape.getFirstVertex(edit_shape.getFirstPath(geom)));
//        Transformation2D tr = new Transformation2D();
//        tr.setShift(-origin.x, -origin.y);
//        // move the path to origin for better accuracy in calculations.
//        edit_shape.applyTransformation(tr);

        //TODO prepare filter for geodesics
        //if (bfilter) {
        if (false) {
            // try removing the noise that does not contribute to the buffer.
            int res_filter = filterPath_(edit_shape, geom, dir, true);
            assert (res_filter == 1);
            // Operator_factory_local::SaveJSONToTextFileDbg("c:/temp/buffer_filter.txt",
            // *edit_shape.get_geometry(geom), nullptr);
            if (edit_shape.getPointCount(geom) < 2) {// got degenerate output.
                // Wither bail out or
                // produce a circle.
                if (dir < 0)
                    return 1;// negative direction produces nothing.

                MultiPath mpIn = (MultiPath) input_geom;
                // Add a circle
                Point pt = new Point();
                mpIn.getPointByVal(mpIn.getPathStart(ipath), pt);
                addCircle_(result_mp, pt);
                return 1;
            }
        }

        m_buffer_commands.clear();
        int path = edit_shape.getFirstPath(geom);
        int ivert = edit_shape.getFirstVertex(path);
        int iprev = dir == 1 ? edit_shape.getPrevVertex(ivert) : edit_shape.getNextVertex(ivert);
        int inext = dir == 1 ? edit_shape.getNextVertex(ivert) : edit_shape.getPrevVertex(ivert);
        boolean b_first = true;

        // current point
        Point2D pt_current = new Point2D();
        // next point
        Point2D pt_after = new Point2D();
        // previous point
        Point2D pt_before = new Point2D();


        Point2D pt_left_prev = new Point2D();
        Point2D pt = new Point2D();
        Point2D pt1 = new Point2D();

        Point2D v_after = new Point2D();
        Point2D v_before = new Point2D();
//        Point2D v_left = new Point2D();
//        Point2D v_left_prev = new Point2D();

        PeDouble az12 = new PeDouble();
        PeDouble lam2 = new PeDouble();
        PeDouble phi2 = new PeDouble();

        double abs_d = m_abs_distance;
        int ncount = edit_shape.getPathSize(path);

        // write out buffer commands as a set of arcs and line segments.
        // if we'd convert this directly to a polygon and draw using winding
        // fill rule, we'd get the buffered result.
        for (int index = 0; index < ncount; index++) {
            edit_shape.getXY(inext, pt_after);

            if (b_first) {
                // grab the first point
                edit_shape.getXY(ivert, pt_current);
                // get the previous point (TODO if polygon?!?)
                edit_shape.getXY(iprev, pt_before);

                // not sure is this is the right direction. might want before to current
                GeoDist.geodesic_distance_ngs(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), pt_before.x * DEG_TO_RAD, pt_before.y * DEG_TO_RAD, pt_current.x * DEG_TO_RAD, pt_current.y * DEG_TO_RAD, null, az12, null);
                // not sure if this is the correct rotation (maybe should be -Math.PI/2.0)
                GeoDist.geodesic_forward(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), pt_current.x * DEG_TO_RAD, pt_current.y * DEG_TO_RAD, abs_d, az12.val - Math.PI / 2.0, lam2, phi2);
                pt_left_prev.x = lam2.val * RAD_TO_DEG;
                pt_left_prev.y = phi2.val * RAD_TO_DEG;

                // move v_before position as if pt_current was origin
                v_before.sub(pt_current, pt_before);
                // change v_before into unit vector
                v_before.normalize();
//                // create unit vector that is 90 degree counter-clockwise of v_before
//                v_left_prev.leftPerpendicular(v_before);
//                // scale the left perpendicular vector by the distance
//                v_left_prev.scale(abs_d);
//                // create the pt left previous by shifting the left perpendicular vector by the current point
//                pt_left_prev.add(v_left_prev, pt_current);
            }

            // not sure is this is the right direction. might want before to current
            GeoDist.geodesic_distance_ngs(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), pt_current.x * DEG_TO_RAD, pt_current.y * DEG_TO_RAD, pt_after.x * DEG_TO_RAD, pt_after.y * DEG_TO_RAD, null, az12, null);
            // not sure if this is the correct rotation (maybe should be -Math.PI/2.0)
            GeoDist.geodesic_forward(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), pt_current.x * DEG_TO_RAD, pt_current.y * DEG_TO_RAD, abs_d, az12.val - Math.PI / 2.0, lam2, phi2);
            pt.x = lam2.val * RAD_TO_DEG;
            pt.y = phi2.val * RAD_TO_DEG;

            // v_after is the vector of pt_after with the pt_center at origin
            v_after.sub(pt_after, pt_current);
            // v_after is normalized to be a unit vector
            v_after.normalize();
//            // v_left is a perpendicular to the left of v_after vector, centered at pt_current
//            v_left.leftPerpendicular(v_after);
//            // scale v_left by the buffer distance
//            v_left.scale(abs_d);
//            // shift the vector back relative to pt_current
//            pt.add(pt_current, v_left);
            // Use these two calculations to determine if the angle is concave or convex
            double cross = v_before.crossProduct(v_after);
            double dot = v_before.dotProduct(v_after);
            boolean bDoJoin = cross < 0 || (dot < 0 && cross == 0);

            if (bDoJoin) {
                // create an arc
                m_buffer_commands.add(
                        new GeodesicBufferCommand(
                                pt_left_prev,
                                pt,
                                pt_current,
                                GeodesicBufferCommand.Flags.enum_arc,
                                m_buffer_commands.size() + 1,
                                m_buffer_commands.size() - 1));
            } else if (!pt_left_prev.isEqual(pt)) {
                // create straight edge?
                m_buffer_commands.add(
                        new GeodesicBufferCommand(
                                pt_left_prev,
                                pt_current,
                                m_buffer_commands.size() + 1,
                                m_buffer_commands.size() - 1,
                                "dummy"));
                m_buffer_commands.add(
                        new GeodesicBufferCommand(
                                pt_current,
                                pt,
                                m_buffer_commands.size() + 1,
                                m_buffer_commands.size() - 1,
                                "dummy"));
            }

            GeoDist.geodesic_forward(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), pt_after.x * DEG_TO_RAD, pt_after.y * DEG_TO_RAD, abs_d, az12.val - Math.PI / 2.0, lam2, phi2);
            pt1.x = lam2.val * RAD_TO_DEG;
            pt1.y = phi2.val * RAD_TO_DEG;
//            pt1.add(pt_after, v_left);
            m_buffer_commands.add(
                    new GeodesicBufferCommand(
                            pt,
                            pt1,
                            pt_current,
                            GeodesicBufferCommand.Flags.enum_line,
                            m_buffer_commands.size() + 1,
                            m_buffer_commands.size() - 1));

            pt_left_prev.setCoords(pt1);
//            v_left_prev.setCoords(v_left);
            pt_before.setCoords(pt_current);
            pt_current.setCoords(pt_after);
            v_before.setCoords(v_after);
            iprev = ivert;
            ivert = inext;
            b_first = false;
            inext = dir == 1 ? edit_shape.getNextVertex(ivert) : edit_shape.getPrevVertex(ivert);
        }

        m_buffer_commands.get(m_buffer_commands.size() - 1).m_next = 0;
        m_buffer_commands.get(0).m_prev = m_buffer_commands.size() - 1;
        processBufferCommands_(result_mp);
//        tr.setShift(origin.x, origin.y);// move the path to improve precision.
//        result_mp.applyTransformation(tr, result_mp.getPathCount() - 1);
        return 1;
    }

    private void processBufferCommands_(MultiPathImpl result_mp) {
        int ifirst_seg = cleanupBufferCommands_();
        boolean first = true;
        int iseg_next = ifirst_seg + 1;
        for (int iseg = ifirst_seg; iseg_next != ifirst_seg; iseg = iseg_next) {
            GeodesicBufferCommand command = m_buffer_commands.get(iseg);
            iseg_next = command.m_next != -1 ? command.m_next : (iseg + 1) % m_buffer_commands.size();

            if (command.m_type == 0)
                continue;// deleted segment

            if (first) {
                result_mp.startPath(command.m_from);
                first = false;
            }

            if (command.m_type == GeodesicBufferCommand.Flags.enum_arc) {// arc
                addJoin_(result_mp, command.m_center, command.m_from, command.m_to, false, true);
            } else {
                result_mp.lineTo(command.m_to);
            }
            first = false;
        }
//        if (result_mp.getPoint(0).getX() != result_mp.getPoint(result_mp.getPointCount() - 1).getX() &&
//                result_mp.getPoint(0).getY() != result_mp.getPoint(result_mp.getPointCount() - 1).getY())
//            result_mp.lineTo(result_mp.getPoint(0));
    }

    //TODO this seems to be fine for Geodesic vs Planar. The intersect test might be a little off, but this should work?
    private int cleanupBufferCommands_() {
        // The purpose of this function is to remove as many self intersections
        // from the buffered shape as possible.
        // The buffer works without cleanup also, but slower.

        if (m_helper_array == null)
            m_helper_array = new Point2D[9];

        int istart = 0;
        for (int iseg = 0, nseg = m_buffer_commands.size(); iseg < nseg; ) {
            GeodesicBufferCommand command = m_buffer_commands.get(iseg);
            if ((command.m_type & GeodesicBufferCommand.Flags.enum_connection) != 0) {
                //TODO put a breakpoint. needs test case
                istart = iseg;
                break;
            }

            iseg = command.m_next;
        }

        int iseg_next = istart + 1;
        for (int iseg = istart; iseg_next != istart; iseg = iseg_next) {
            GeodesicBufferCommand command = m_buffer_commands.get(iseg);
            iseg_next = command.m_next;
            int count = 1;
            GeodesicBufferCommand command_next = null;
            while (iseg_next != iseg) {// find next segement
                command_next = m_buffer_commands.get(iseg_next);
                if ((command_next.m_type & GeodesicBufferCommand.Flags.enum_connection) != 0)
                    break;

                iseg_next = command_next.m_next;
                count++;
            }

            if (count == 1) {
                // Next segment starts where this one ends. Skip this case as it
                // is simple.
                assert (command.m_to.isEqual(command_next.m_from, 0.01));
//                assert (command.m_to.isEqual(command_next.m_from));
                continue;
            }

            if ((command.m_type & command_next.m_type) == GeodesicBufferCommand.Flags.enum_line) {// simplest
                // cleanup
                // -
                // intersect
                // lines
                if (m_helper_line_1 == null) {
                    m_helper_line_1 = new Line();
                    m_helper_line_2 = new Line();
                }
                m_helper_line_1.setStartXY(command.m_from);
                m_helper_line_1.setEndXY(command.m_to);
                m_helper_line_2.setStartXY(command_next.m_from);
                m_helper_line_2.setEndXY(command_next.m_to);

                int count_ = m_helper_line_1.intersect(m_helper_line_2,
                        m_helper_array, null, null, m_small_tolerance);
                if (count_ == 1) {
                    command.m_to.setCoords(m_helper_array[0]);
                    command_next.m_from.setCoords(m_helper_array[0]);
                    command.m_next = iseg_next;// skip until iseg_next
                    command_next.m_prev = iseg;
                } else if (count_ == 2) {// TODO: this case needs improvement
                }
            }
        }

        return istart;
    }

    private boolean isGap_(Point2D pt_before, Point2D pt_current,
                           Point2D pt_after) {
        Point2D v_gap = new Point2D();
        v_gap.sub(pt_after, pt_before);
        double gap_length = v_gap.length();
        double sqr_delta = m_abs_distance * m_abs_distance - gap_length
                * gap_length * 0.25;
        if (sqr_delta > 0) {
            double delta = Math.sqrt(sqr_delta);
            v_gap.normalize();
            v_gap.rightPerpendicular();
            Point2D p = new Point2D();
            p.sub(pt_current, pt_before);
            double d = p.dotProduct(v_gap);
            if (d + delta >= m_abs_distance) {
                return true;
            }
        }

        return false;
    }

    private int filterPath_(EditShape edit_shape,
                            int geom,
                            int dir,
                            boolean closed) {
        // **********************!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // return 1;

        boolean bConvex = true;
        for (int pass = 0; pass < 1; pass++) {
            boolean b_filtered = false;
            int ipath = edit_shape.getFirstPath(geom);
            int isize = edit_shape.getPathSize(ipath);
            if (isize == 0)
                return 0;

            int ncount = isize;
            if (isize < 3)
                return 1;

            if (closed && !edit_shape.isClosedPath(ipath))// the path is closed
            // only virtually
            {
                ncount = isize - 1;
            }

            assert (dir == 1 || dir == -1);
            int ivert = edit_shape.getFirstVertex(ipath);
            if (!closed)
                edit_shape.getNextVertex(ivert);

            int iprev = dir > 0 ? edit_shape.getPrevVertex(ivert) : edit_shape.getNextVertex(ivert);
            int inext = dir > 0 ? edit_shape.getNextVertex(ivert) : edit_shape.getPrevVertex(ivert);
            int ibefore = iprev;
            boolean reload = true;

            Point2D pt_current = new Point2D(), pt_after = new Point2D(), pt_before = new Point2D(), pt_before_before = new Point2D(), pt_middle = new Point2D(), pt_gap_last = new Point2D(
                    0, 0);
            Point2D v_after = new Point2D(), v_before = new Point2D(), v_gap = new Point2D();
            Point2D temp = new Point2D();
            double abs_d = m_abs_distance;

            // When the path is open we cannot process the first and the last
            // vertices, so we process size - 2.
            // When the path is closed, we can process all vertices.
            int iter_count = closed ? ncount : isize - 2;
            int gap_counter = 0;
            for (int iter = 0; iter < iter_count; ) {
                edit_shape.getXY(inext, pt_after);

                if (reload) {
                    edit_shape.getXY(ivert, pt_current);
                    edit_shape.getXY(iprev, pt_before);
                    ibefore = iprev;
                }

                v_before.sub(pt_current, pt_before);
                v_before.normalize();

                v_after.sub(pt_after, pt_current);
                v_after.normalize();

                if (ibefore == inext) {
                    break;
                }

                double cross = v_before.crossProduct(v_after);
                double dot = v_before.dotProduct(v_after);
                boolean bDoJoin = cross < 0 || (dot < 0 && cross == 0);
                boolean b_write = true;
                if (!bDoJoin) {
                    if (isGap_(pt_before, pt_current, pt_after)) {
                        pt_gap_last.setCoords(pt_after);
                        b_write = false;
                        ++gap_counter;
                        b_filtered = true;
                    }

                    bConvex = false;
                }

                if (b_write) {
                    if (gap_counter > 0) {
                        for (; ; ) {// re-test back
                            int ibefore_before = dir > 0 ? edit_shape
                                    .getPrevVertex(ibefore) : edit_shape
                                    .getNextVertex(ibefore);
                            if (ibefore_before == ivert)
                                break;

                            edit_shape.getXY(ibefore_before, pt_before_before);
                            if (isGap_(pt_before_before, pt_before, pt_gap_last)) {
                                pt_before.setCoords(pt_before_before);
                                ibefore = ibefore_before;
                                b_write = false;
                                ++gap_counter;
                                continue;
                            } else {
                                if (ibefore_before != inext
                                        && isGap_(pt_before_before, pt_before,
                                        pt_after)
                                        && isGap_(pt_before_before, pt_current,
                                        pt_after)) {// now the current
                                    // point is a part
                                    // of the gap also.
                                    // We retest it.
                                    pt_before.setCoords(pt_before_before);
                                    ibefore = ibefore_before;
                                    b_write = false;
                                    ++gap_counter;
                                }
                            }
                            break;
                        }
                    }

                    if (!b_write)
                        continue;// retest forward

                    if (gap_counter > 0) {
                        // remove all but one gap vertices.
                        int p = dir > 0 ? edit_shape.getPrevVertex(iprev)
                                : edit_shape.getNextVertex(iprev);
                        for (int i = 1; i < gap_counter; i++) {
                            int pp = dir > 0 ? edit_shape.getPrevVertex(p)
                                    : edit_shape.getNextVertex(p);
                            edit_shape.removeVertex(p, true);
                            p = pp;
                        }

                        v_gap.sub(pt_current, pt_before);
                        double gap_length = v_gap.length();
                        double sqr_delta = abs_d * abs_d - gap_length
                                * gap_length * 0.25;
                        double delta = Math.sqrt(sqr_delta);
                        if (abs_d - delta > m_densify_dist * 0.5) {
                            pt_middle.add(pt_before, pt_current);
                            pt_middle.scale(0.5);
                            v_gap.normalize();
                            v_gap.rightPerpendicular();
                            temp.setCoords(v_gap);
                            temp.scale(abs_d - delta);
                            pt_middle.add(temp);
                            edit_shape.setXY(iprev, pt_middle);
                        } else {
                            // the gap is too short to be considered. Can close
                            // it with the straight segment;
                            edit_shape.removeVertex(iprev, true);
                        }

                        gap_counter = 0;
                    }

                    pt_before.setCoords(pt_current);
                    ibefore = ivert;
                }

                pt_current.setCoords(pt_after);
                iprev = ivert;
                ivert = inext;
                // reload = false;
                inext = dir > 0 ? edit_shape.getNextVertex(ivert) : edit_shape
                        .getPrevVertex(ivert);
                iter++;
                reload = false;
            }

            if (gap_counter > 0) {
                int p = dir > 0 ? edit_shape.getPrevVertex(iprev) : edit_shape
                        .getNextVertex(iprev);
                for (int i = 1; i < gap_counter; i++) {
                    int pp = dir > 0 ? edit_shape.getPrevVertex(p) : edit_shape
                            .getNextVertex(p);
                    edit_shape.removeVertex(p, true);
                    p = pp;
                }

                pt_middle.add(pt_before, pt_current);
                pt_middle.scale(0.5);

                v_gap.sub(pt_current, pt_before);
                double gap_length = v_gap.length();
                double sqr_delta = abs_d * abs_d - gap_length * gap_length
                        * 0.25;
                assert (sqr_delta > 0);
                double delta = Math.sqrt(sqr_delta);
                v_gap.normalize();
                v_gap.rightPerpendicular();
                temp.setCoords(v_gap);
                temp.scale(abs_d - delta);
                pt_middle.add(temp);
                edit_shape.setXY(iprev, pt_middle);
            }

            edit_shape.filterClosePoints(m_filter_tolerance, false, false);

            if (!b_filtered)
                break;
        }

        return 1;
    }

    private boolean isDegeneratePath_(MultiPathImpl mp_impl, int ipath) {
        if (mp_impl.getPathSize(ipath) == 1)
            return true;
        Envelope2D env = new Envelope2D();
        mp_impl.queryPathEnvelope2D(ipath, env);
        return isDegenerateEnv2D(env);
    }

    private boolean isDegenerateEnv2D(Envelope2D env2D) {
        double width = GeoDist.getEnvWidth(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), env2D);
        double height = GeoDist.getEnvHeight(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), env2D);

        if (Math.max(width, height) < m_densify_dist * 0.5)
            return true;

        return false;
    }

    private boolean isDegenerateGeometry_(Geometry geom) {
        Envelope2D env2D = new Envelope2D();
        geom.queryEnvelope2D(env2D);
        return isDegenerateEnv2D(env2D);
    }

    private Polyline preparePolyline_(Polyline input_geom) {
        // Generalize it firstly using 25% of the densification deviation as a
        // criterion.
        //TODO create geodetic Geodetic Generalize
        Polyline generalized_polyline = (Polyline) ((OperatorGeneralize) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Generalize)).execute(
                input_geom,
                m_densify_dist * 0.25,
                false,
                m_progress_tracker);

        int path_point_count = 0;
        for (int i = 0, npath = generalized_polyline.getPathCount(); i < npath; i++) {
            path_point_count = Math.max(generalized_polyline.getPathSize(i), path_point_count);
        }

        if (path_point_count < 32) {
            m_bfilter = false;
            return generalized_polyline;
        } else {
            m_bfilter = true;
            // If we apply a filter to the polyline, then we have to resolve all
            // self intersections.
            Polyline simple_polyline = (Polyline) (TopologicalOperations.planarSimplify(
                    generalized_polyline,
                    m_small_tolerance,
                    false,
                    true,
                    m_progress_tracker));
            // Operator_factory_local::SaveJSONToTextFileDbg("c:/temp/buffer_simplify.txt", simple_polyline, nullptr);
            return simple_polyline;
        }
    }


    private void addArc_(MultiPathImpl dst,
                         Point2D center,
                         Point2D arcStartPt,
                         Point2D arcEndPt,
                         boolean bStartPath,
                         boolean bFinishArcEndPt) {
        // TODO move this logic into the constructor, eh?
        int N = calcN_(4);
        int real_size = ((N + 3) / 4) * 4;
        double dA = (2 * Math.PI) / real_size;

        //TODO this might be good for memory allocations?
        // result_mp.reserve(real_size * 4);

        // center point
        double lamCenter = center.x * DEG_TO_RAD;
        double phiCenter = center.y * DEG_TO_RAD;

        double startAzimuth = 0.0;

        if (arcStartPt != null && arcEndPt != null) {
            PeDouble az12 = new PeDouble();
            GeoDist.geodesic_distance_ngs(
                    m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), lamCenter, phiCenter,
                    arcStartPt.x * DEG_TO_RAD, arcStartPt.y * DEG_TO_RAD,
                    null, az12, null);
            startAzimuth = az12.val;

            GeoDist.geodesic_distance_ngs(
                    m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), lamCenter, phiCenter,
                    arcEndPt.x * DEG_TO_RAD, arcEndPt.y * DEG_TO_RAD,
                    null, az12, null);
            double endAzimuth = az12.val;

            if (startAzimuth < 0)
                startAzimuth = Math.PI * 2 + startAzimuth;
            if (endAzimuth < 0)
                endAzimuth = Math.PI * 2 + endAzimuth;
            double angleDifference = endAzimuth - startAzimuth;
            if (endAzimuth < startAzimuth)
                angleDifference = 2 * Math.PI - startAzimuth + endAzimuth;

            double ratio = angleDifference / (2 * Math.PI);
            real_size = (int) Math.floor(real_size * ratio);
            // change the angle to be distributed about the real_size interval
            dA = angleDifference / ((double) real_size);
        }


        PeDouble lam2 = new PeDouble();
        PeDouble phi2 = new PeDouble();

        if (bStartPath) {
            GeoDist.geodesic_forward(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), lamCenter, phiCenter, m_abs_distance, startAzimuth, lam2, phi2);
            dst.startPath(lam2.val * RAD_TO_DEG, phi2.val * RAD_TO_DEG);
            if (arcEndPt == null)
                arcEndPt = new Point2D(lam2.val * RAD_TO_DEG, phi2.val * RAD_TO_DEG);
        }
        startAzimuth += dA;

        for (int i = 1; i < real_size; i++) {
            GeoDist.geodesic_forward(m_spatialReference.getMajorAxis(), m_spatialReference.getEccentricitySquared(), lamCenter, phiCenter, m_abs_distance, startAzimuth, lam2, phi2);
            dst.lineTo(lam2.val * RAD_TO_DEG, phi2.val * RAD_TO_DEG);
            startAzimuth += dA;
        }

        if (bFinishArcEndPt) {
            dst.lineTo(arcEndPt);
        }
    }


    private void addCircle_(MultiPathImpl dst, Point point) {
        addArc_(dst, point.getXY(), null, null, true, true);
    }

    // Planar and Geodesic are equivalent
    private static Polygon setWeakSimple_(Polygon poly) {
        ((MultiPathImpl) poly._getImpl()).setIsSimple(MultiVertexGeometryImpl.GeometryXSimple.Weak, 0.0, false);
        return poly;
    }

    // Planar and Geodesic are equivalent
    private Polygon setStrongSimple_(Polygon poly) {
        ((MultiPathImpl) poly._getImpl()).setIsSimple(MultiVertexGeometryImpl.GeometryXSimple.Strong, m_tolerance, false);
        ((MultiPathImpl) poly._getImpl())._updateOGCFlags();
        return poly;
    }

}
