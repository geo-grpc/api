package org.epl.geometry;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryCursor;
import com.esri.core.geometry.ProgressTracker;

public abstract class OperatorEnclosingCircle extends OperatorEx {
    @Override
    public Type getType() {
        return Type.EnclosingCircle;
    }


    public abstract GeometryCursor execute(GeometryCursor geoms, SpatialReferenceEx spatialReference, ProgressTracker progressTracker);

    /**
     * Performs the Generalize operation on a single geometry. Point and
     * multipoint geometries are left unchanged. An envelope is converted to a
     * polygon.
     */
    public abstract Geometry execute(Geometry geom, SpatialReferenceEx spatialReference, ProgressTracker progressTracker);

    public static OperatorEnclosingCircle local() {
        return (OperatorEnclosingCircle) OperatorFactoryLocalEx.getInstance().getOperator(Type.EnclosingCircle);
    }
}
