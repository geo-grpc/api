package org.epl.geometry;

/**
 * Created by davidraleigh on 5/10/17.
 */

import com.esri.core.geometry.*;
import org.proj4.PJException;

import java.util.Random;

class RandomPointMaker {

    /**
     * This assumes an equal area projection
     *
     * @param geometry
     * @param pointsPerSquareKm
     * @param sr
     * @param progressTracker
     * @return
     */
    static Geometry generate(Geometry geometry,
                             double pointsPerSquareKm,
                             Random numberGenerator,
                             SpatialReferenceEx sr,
                             ProgressTracker progressTracker) throws PJException {
        if (geometry.getType() != Geometry.Type.Polygon && geometry.getType() != Geometry.Type.Envelope)
            throw new GeometryException("Geometry input must be of type Polygon or Envelope");

        if (sr == null || sr.isLocal())
            throw new GeometryException("Spatial reference must be defined and must have unit definition");

        Polygon polygon = null;
        if (geometry.getType() == Geometry.Type.Envelope) {
            polygon = new Polygon();
            polygon.addEnvelope((Envelope) geometry, false);
        } else {
            polygon = (Polygon) geometry;
        }

        // TODO should iterate over paths
        // TODO iterator should check for containment. If a path is contained within another, random points shouldn't
        // be generated for that contained path
        // Ask Aaron if paths are written to attribute stream such that paths contained come after container paths
        return __makeRandomPoints(polygon, pointsPerSquareKm, numberGenerator, sr, progressTracker);
    }

    // TODO input should be multiplath
    static Geometry __makeRandomPoints(Polygon polygon,
                                       double pointsPerSquareKm,
                                       Random numberGenerator,
                                       SpatialReferenceEx sr,
                                       ProgressTracker progressTracker) throws PJException {
        // TODO for each ring project, in order to prevent from creating an excess of points if two parts of a polygon are on opposite sides of the globe.

        ProjectionTransformation forwardProjectionTransformation = ProjectionTransformation.getEqualArea(polygon, sr);

        Envelope env = new Envelope();
        polygon.queryEnvelope(env);
        // Project bounding coordinates to equal area
        // equalAreaEnvelopeGeom must be a geometry/polygon because projection of envelope will almost certainly
        // or skew geometry
        // TODO, maybe it would be computationally cheaper or more accurate to project input polygon instead of it's envelope
        Geometry equalAreaEnvelopeGeom = OperatorProject.local().execute(env, forwardProjectionTransformation, progressTracker);

        Envelope2D equalAreaEnvelope = new Envelope2D();
        // envelope of projected envelope
        equalAreaEnvelopeGeom.queryEnvelope2D(equalAreaEnvelope);

        double areaKm = equalAreaEnvelope.getArea() / (1000.0 * 1000.0);
        double pointCountNotCast = Math.ceil(areaKm * pointsPerSquareKm);
        //http://stackoverflow.com/questions/3038392/do-java-arrays-have-a-maximum-size
        if (pointCountNotCast * 2 > Integer.MAX_VALUE - 8) {
            throw new GeometryException("Random Point count outside of available");
        }
        int pointCount = (int) pointCountNotCast;

        // TODO if the area of the envelope is more than twice that of the initial polygon, maybe a raster creation
        // of random multipoints would be required...?

        double[] xy = new double[pointCount * 2];

        double xdiff = equalAreaEnvelope.xmax - equalAreaEnvelope.xmin;
        double ydiff = equalAreaEnvelope.ymax - equalAreaEnvelope.ymin;
        for (int i = 0; i < pointCount * 2; i++) {
            if (i % 2 == 0) // x val
                xy[i] = numberGenerator.nextDouble() * xdiff + equalAreaEnvelope.xmin;
            else            // y val
                xy[i] = numberGenerator.nextDouble() * ydiff + equalAreaEnvelope.ymin;
        }

        // Create Multipoint from vertices
        MultiPoint multiPoint = new MultiPoint();
        MultiVertexGeometryImpl multiVertexGeometry = (MultiVertexGeometryImpl) multiPoint._getImpl();
        AttributeStreamOfDbl attributeStreamOfDbl = new AttributeStreamOfDbl(pointCount * 2);

        // TODO it would be better if we could just std::move the array.
        attributeStreamOfDbl.writeRangeMove(xy);
        multiVertexGeometry.setAttributeStreamRef(0, attributeStreamOfDbl);
        //multiVertexGeometry._resizeImpl(pointCount);
        multiPoint.resize(pointCount);
        multiVertexGeometry._setDirtyFlag(MultiVertexGeometryImpl.DirtyFlags.DirtyAll, true);

        ProjectionTransformation backProjectionTransformation = forwardProjectionTransformation.getReverse();
        // project inplace instead of projecting a copy using OperatorProject::execute
        Projector.projectMultiPoint(multiPoint, backProjectionTransformation, progressTracker);


        // TODO project multipoint back to input spatial reference (it is necessary to do it here,
        // because if we projected the above array, then we wouldn't benefit from clipping

        // Intersect by input geometry
        // TODO reduce densify distance?
        Geometry intersector = OperatorGeodeticDensifyByLength.local().execute(polygon, sr, areaKm, GeodeticCurveType.Geodesic, null);
//        double geodeticDensify = 1
        return GeometryEngine.intersect(multiPoint, intersector, sr.toSpatialReference());
    }
}
