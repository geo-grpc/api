package org.epl.geometry;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryCursor;
import com.esri.core.geometry.ProgressTracker;
import com.esri.core.geometry.SimpleGeometryCursor;

public class OperatorEnclosingCircleLocal extends OperatorEnclosingCircle {
    @Override
    public GeometryCursor execute(GeometryCursor geoms, SpatialReferenceEx spatialReference, ProgressTracker progressTracker) {
        return new OperatorEnclosingCircleCursor(geoms, spatialReference, progressTracker);
    }

    @Override
    public Geometry execute(Geometry geom, SpatialReferenceEx spatialReference, ProgressTracker progressTracker) {
        OperatorEnclosingCircleCursor operatorEnclosingCircleCursor = new OperatorEnclosingCircleCursor(new SimpleGeometryCursor(geom), spatialReference, progressTracker);
        return operatorEnclosingCircleCursor.next();
    }
}
