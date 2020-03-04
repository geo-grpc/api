package org.epl.geometry;

import com.esri.core.geometry.*;
import org.proj4.PJException;

/**
 * Created by davidraleigh on 5/12/17.
 */
class Projector {
    static {
        System.loadLibrary("proj");
    }

    public static int transform(ProjectionTransformation projectionTransformation, Point[] pointsIn,
                                int count, Point[] pointsOut) throws org.proj4.PJException {
        if (pointsIn[0].hasZ())
            return __transform3D(projectionTransformation, pointsIn, count, pointsOut);
        return __transform2D(projectionTransformation, pointsIn, count, pointsOut);
    }

    private static int __transform2D(ProjectionTransformation projectionTransformation,
                                     Point[] pointsIn,
                                     int count,
                                     Point[] pointsOut) throws org.proj4.PJException {
        double[] coordsIn = new double[pointsIn.length * 2];
        for (int i = 0; i < pointsIn.length; i++) {
            coordsIn[i * 2] = pointsIn[i].getX();
            coordsIn[i * 2 + 1] = pointsIn[i].getY();
        }

        Projector.transform(projectionTransformation, coordsIn, false);
        for (int i = 0; i < pointsOut.length; i++) {
            pointsOut[i].setX(coordsIn[i * 2]);
            pointsOut[i].setY(coordsIn[i * 2 + 1]);
        }

        return 0;
    }

    private static int __transform3D(ProjectionTransformation projectionTransformation,
                                     Point[] pointsIn,
                                     int count,
                                     Point[] pointsOut) throws org.proj4.PJException {
        double[] coordsIn = new double[pointsIn.length * 3];
        for (int i = 0; i < pointsIn.length; i++) {
            coordsIn[i * 2] = pointsIn[i].getX();
            coordsIn[i * 2 + 1] = pointsIn[i].getY();
            coordsIn[i * 2 + 2] = pointsIn[i].getZ();
        }

        Projector.transform(projectionTransformation, coordsIn, true);
        for (int i = 0; i < pointsOut.length; i++) {
            pointsOut[i].setX(coordsIn[i * 2]);
            pointsOut[i].setY(coordsIn[i * 2 + 1]);
            pointsOut[i].setZ(coordsIn[i * 2 + 2]);
        }

        return 0;
    }

    public static double[] transform(ProjectionTransformation projectionTransformation,
                                     double[] coordsSrc,
                                     boolean hasZ) throws org.proj4.PJException {
        int n = 2;
        if (hasZ)
            n = 3;

        projectionTransformation.getFromProj().transform(projectionTransformation.getToProj(), n, coordsSrc, 0, coordsSrc.length / n);
        return coordsSrc;
    }

    static Geometry project(Geometry geometry,
                            ProjectionTransformation projectionTransformation,
                            ProgressTracker progressTracker) {
        if (geometry.isEmpty()) {
            return geometry;
        }

        if (projectionTransformation.m_fromSpatialReference == null || projectionTransformation.m_toSpatialReference == null) {
            throw new GeometryException("From and To Spatial references required to Project Geometry");
        }
        // TODO check that all project methods no longer use 'new Geometry'
        // TODO maybe push copy down to each geometry type? Envelope shouldn't create copy, right?
        // TODO is clipping creating a new cloned geometry? Should there should be a check so that there aren't too many unnecessary clones
        Geometry result = geometry.copy();
        try {
            switch (geometry.getType()) {
                case Unknown:
                    break;
                case Point:
                    result = projectPoint(result, projectionTransformation, progressTracker);
                    break;
                case Line:
                    break;
                case Envelope:
                    result = projectEnvelope(result, projectionTransformation, progressTracker);
                    break;
                case MultiPoint:
                    result = projectMultiPoint(result, projectionTransformation, progressTracker);
                    break;
                case Polyline:
                    result = projectPolyline(result, projectionTransformation, progressTracker);
                    break;
                case Polygon:
                    result = projectPolygon(result, projectionTransformation, progressTracker);
                    break;
            }
        } catch (PJException e) {
            throw new GeometryException(String.format("Proj4 projection exception:\n{}\n{}", e.getLocalizedMessage(), e.getStackTrace()));
        }

        return result;
    }

    static Geometry clipGeometry(Geometry geometry, ProjectionTransformation projectionTransformation, ProgressTracker progressTracker) {
        // TODO implement a real horizon
        if (projectionTransformation.m_fromSpatialReference.getCoordinateSystemType() == SpatialReferenceEx.CoordinateSystemType.GEOGRAPHIC) {
            // Fold Geometries into a space that ranges from -180 - 180
            Geometry folded = OperatorProjectLocal.foldInto360Range(geometry, projectionTransformation.m_fromSpatialReference);

            // Cut geometries at horizon

            // union rings
            return folded;
        }

        return geometry;
    }

    static Geometry projectPoint(Geometry geometry,
                                 ProjectionTransformation projectionTransformation,
                                 ProgressTracker progressTracker) throws org.proj4.PJException {
        geometry = clipGeometry(geometry, projectionTransformation, progressTracker);
        Point outpoint = new Point();
        // TODO clean this idea up
        Point[] outputs = {outpoint};
        Point[] inputs = {(Point) geometry};
        transform(projectionTransformation, inputs, 1, outputs);
        // TODO setDirtyFlag?
        return outputs[0];
    }

    static Geometry projectMultiPoint(Geometry geometry,
                                      ProjectionTransformation projectionTransformation,
                                      ProgressTracker progressTracker) throws org.proj4.PJException {
        MultiPoint multiPoint = (MultiPoint) clipGeometry(geometry, projectionTransformation, progressTracker);

        int pointCount = multiPoint.getPointCount();
        MultiVertexGeometryImpl multiVertexGeometry = (MultiVertexGeometryImpl) multiPoint._getImpl();
//        MultiVertexGeometry multiVertexGeometry = (MultiVertexGeometry) multiPoint;

        AttributeStreamOfDbl xyPositions = (AttributeStreamOfDbl) multiVertexGeometry.getAttributeStreamRef(0);
        // TODO check that there isn't a way for grabbing xyzPositions
        transform(projectionTransformation, xyPositions.m_buffer, false);
        multiVertexGeometry._setDirtyFlag(MultiVertexGeometryImpl.DirtyFlags.DirtyAll, true);
//        AttributeStreamOfDbl attributeStreamOfDbl = new AttributeStreamOfDbl(pointCount * 2);
//        attributeStreamOfDbl.writeRange(0, pointCount * 2, output, 0, true);
//
//        MultiPoint multiPointOut = new MultiPoint(multiPoint.getDescription());
//        MultiVertexGeometryImpl multiVertexGeometryOut = (MultiVertexGeometryImpl)multiPointOut._getImpl();
//
//        multiVertexGeometryOut.setAttributeStreamRef(0, attributeStreamOfDbl);
//        multiVertexGeometryOut._resizeImpl(pointCount);
//        multiPointOut.resize(pointCount);
        multiVertexGeometry._updateAllDirtyIntervals(true);

        return multiPoint;
    }

    static Geometry projectPolyline(Geometry geometry,
                                    ProjectionTransformation projectionTransformation,
                                    ProgressTracker progressTracker) throws org.proj4.PJException {
        Polyline polyline = (Polyline) clipGeometry(geometry, projectionTransformation, progressTracker);

        int pointCount = polyline.getPointCount();
        MultiVertexGeometryImpl multiVertexGeometry = (MultiVertexGeometryImpl) polyline._getImpl();

        AttributeStreamOfDbl xyPositions = (AttributeStreamOfDbl) multiVertexGeometry.getAttributeStreamRef(0);
        // TODO check that there isn't a way for grabbing xyzPositions
        transform(projectionTransformation, xyPositions.m_buffer, false);
        multiVertexGeometry._setDirtyFlag(MultiVertexGeometryImpl.DirtyFlags.DirtyAll, true);
//        AttributeStreamOfDbl attributeStreamOfDbl = new AttributeStreamOfDbl(pointCount * 2);
//        attributeStreamOfDbl.writeRange(0, pointCount * 2, output, 0, true);

//        Polyline polylineOut = new Polyline(polyline.getDescription());
//        MultiVertexGeometryImpl multiVertexGeometryOut = (MultiVertexGeometryImpl)polylineOut._getImpl();
//
//        multiVertexGeometryOut.setAttributeStreamRef(0, attributeStreamOfDbl);
//        multiVertexGeometryOut._resizeImpl(pointCount);
        multiVertexGeometry._updateAllDirtyIntervals(true);

        return polyline;
    }

    static Geometry projectPolygon(Geometry geometry,
                                   ProjectionTransformation projectionTransformation,
                                   ProgressTracker progressTracker) throws org.proj4.PJException {
        Polygon polygon = (Polygon) clipGeometry(geometry, projectionTransformation, progressTracker);

        MultiVertexGeometryImpl multiVertexGeometry = (MultiVertexGeometryImpl) polygon._getImpl();

        AttributeStreamOfDbl xyPositions = (AttributeStreamOfDbl) multiVertexGeometry.getAttributeStreamRef(0);
        // TODO check that there isn't a way for grabbing xyzPositions
        transform(projectionTransformation, xyPositions.m_buffer, false);
        multiVertexGeometry._setDirtyFlag(MultiVertexGeometryImpl.DirtyFlags.DirtyAll, true);
//        AttributeStreamOfDbl attributeStreamOfDbl = new AttributeStreamOfDbl(pointCount * 2);
//        attributeStreamOfDbl.writeRange(0, pointCount * 2, output, 0, true);
//
//        Polygon polygonOut = new Polygon(polygon.getDescription());
//        MultiVertexGeometryImpl multiVertexGeometryOut = (MultiVertexGeometryImpl)polygonOut._getImpl();
//
//        multiVertexGeometryOut.setAttributeStreamRef(0, attributeStreamOfDbl);
//        multiVertexGeometryOut._resizeImpl(pointCount);
        multiVertexGeometry._updateAllDirtyIntervals(true);
        return polygon;
    }

    static Geometry projectEnvelope(Geometry geometry,
                                    ProjectionTransformation projectionTransformation,
                                    ProgressTracker progressTracker) throws org.proj4.PJException {
        Envelope envelope = (Envelope) geometry;
        // TODO how to properly copy envelope into polygon
        Polygon polygon = new Polygon();
        polygon.addEnvelope(envelope, false);

        return projectPolygon(polygon, projectionTransformation, progressTracker);
    }
}
