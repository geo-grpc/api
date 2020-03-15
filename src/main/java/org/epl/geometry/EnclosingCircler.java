package org.epl.geometry;

import com.esri.core.geometry.*;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EnclosingCircler {
    class Circle {
        final Point2D m_center;
        final double m_radius;
        Circle(Point2D center, double radius) {
            m_center = center;
            m_radius = radius;
        }

        public boolean contains(Point2D point2D) {
            return Point2D.distance(point2D, m_center) <= m_radius * (1 + m_tolerance / 2.0);
        }
    };

    private Random m_random;

    // P is the set of all points to be processed.
    private List<Integer> m_indices;

    // S is the set of processed points. that will be kept track of using an index into the indices stream
    private int m_processedIndex = 0;

    private Circle m_circle = null;
    // Geometry to search
    private MultiVertexGeometryImpl m_multiVertexGeometry;

    private int m_circleCount = 96;
    SpatialReferenceEx m_bufferSpatialReference = null;
    private ProjectionTransformation m_projectionTransformation;
    private ProgressTracker m_progressTracker;
    private double m_tolerance = 1e-10;

    EnclosingCircler(Geometry geometry, SpatialReferenceEx spatialReference, ProgressTracker progressTracker) {
        if (spatialReference != null && !spatialReference.isLocal()) {
            m_projectionTransformation = ProjectionTransformation.getEqualArea(geometry, spatialReference);
            m_bufferSpatialReference = m_projectionTransformation.m_toSpatialReference;
            m_tolerance = m_bufferSpatialReference.getTolerance();
            m_multiVertexGeometry = (MultiVertexGeometryImpl)Projector.project(geometry, m_projectionTransformation, progressTracker)._getImpl();
            m_multiVertexGeometry._updateAllDirtyIntervals(true);
        } else {
            m_multiVertexGeometry = (MultiVertexGeometryImpl) geometry._getImpl();
        }
        m_progressTracker = progressTracker;
        m_random = new Random(1977);
    }

    Geometry search() {
        m_indices = IntStream.range(0, m_multiVertexGeometry.getPointCount()).boxed().collect(Collectors.toList());
        Collections.shuffle(m_indices, m_random);

        // Place first two points in boundary list
        Point2D pt1 = __getShuffledPoint(m_processedIndex++);
        Point2D pt2 = __getShuffledPoint(m_processedIndex++);
        Point2D testCenter = new Point2D();
        testCenter.interpolate(pt1, pt2, .5);
        double radius = Point2D.distance(pt1, pt2) / 2.0;

        m_circle = new Circle(testCenter, radius);

        __updateCircle();

        if (m_projectionTransformation == null)
            return __constructCircle();

        return Projector.project(__constructCircle(), m_projectionTransformation.getReverse(), m_progressTracker);
    }

    private Geometry __constructCircle() {
        Point point = new Point(m_circle.m_center);

        return OperatorBufferEx.local().execute(point, m_bufferSpatialReference, m_circle.m_radius, m_progressTracker);
    }

    private Point2D __getShuffledPoint(int index) {
        return m_multiVertexGeometry.getXY(m_indices.get(index));
    }

    private void __updateCircle() {
        // loop through all points in geometry
        Circle circle = new Circle(m_circle.m_center, m_circle.m_radius);
        while (m_processedIndex < m_multiVertexGeometry.getPointCount()) {
            Point2D testPoint = __getShuffledPoint(m_processedIndex++);
            if (!circle.contains(testPoint)) {
                // if the point is outside the current circle
                circle = __updateCircle(testPoint);
            }
        }

        m_circle = circle;
    }

    private Circle __updateCircle(Point2D newBoundaryPoint) {
        // two point option
        Point2D testCenter = new Point2D();

        // TODO geodesic
        testCenter.interpolate(newBoundaryPoint, __getShuffledPoint(0), .5);

        Circle circle = new Circle(testCenter, Point2D.distance(testCenter, newBoundaryPoint));
        for (int i = 1; i < m_processedIndex; i++) {
            Point2D testPoint = __getShuffledPoint(i);
            if (!circle.contains(testPoint)) {
                circle = __updateCircle(newBoundaryPoint, testPoint);
            }
        }

        return circle;
    }

    private Circle __updateCircle(Point2D newBoundaryPoint, Point2D testPoint) {
        Point2D testCenter = new Point2D();

        // TODO geodesic
        testCenter.interpolate(newBoundaryPoint, testPoint, .5);

        Circle circle = new Circle(testCenter, Point2D.distance(testCenter, newBoundaryPoint));
        for (int i = 0; i < m_processedIndex; i++) {
            if (circle.contains(__getShuffledPoint(i))) {
                continue;
            }

            // test current index against testPoint
            Point2D testCurrentCenter = new Point2D();
            testCurrentCenter.interpolate(testPoint, __getShuffledPoint(i), .5);
            Circle testCurrentCircle = new Circle(testCurrentCenter, Point2D.distance(testCurrentCenter, __getShuffledPoint(i)));
            if (testCurrentCircle.contains(newBoundaryPoint)) {
                circle = testCurrentCircle;
                continue;
            }

            Point2D newCurrentCenter = new Point2D();
            newCurrentCenter.interpolate(newBoundaryPoint, __getShuffledPoint(i), .5);
            Circle newCurrentCircle = new Circle(newCurrentCenter, Point2D.distance(newCurrentCenter, __getShuffledPoint(i)));
            if (newCurrentCircle.contains(testPoint)) {
                circle = newCurrentCircle;
                continue;
            }


            // create circle from three points
            testCenter = Point2D.calculateCircleCenterFromThreePoints(newBoundaryPoint, testPoint, __getShuffledPoint(i));
            circle = new Circle(testCenter, Point2D.distance(testCenter, newBoundaryPoint));
        }

        return circle;
    }
}
