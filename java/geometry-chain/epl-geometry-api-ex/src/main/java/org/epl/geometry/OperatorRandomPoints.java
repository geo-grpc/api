/**
 * Created by davidraleigh on 5/10/17.
 */

package org.epl.geometry;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryCursor;
import com.esri.core.geometry.ProgressTracker;

public abstract class OperatorRandomPoints extends OperatorEx {
    @Override
    public OperatorEx.Type getType() {
        return Type.RandomPoints;
    }

    public abstract GeometryCursor execute(
            GeometryCursor inputPolygons,
            double[] pointsPerSquareKm,
            long seed,
            SpatialReferenceEx sr,
            ProgressTracker progressTracker);

    public abstract Geometry execute(
            Geometry inputPolygon,
            double pointsPerSquareKm,
            long seed,
            SpatialReferenceEx sr,
            ProgressTracker progressTracker);

    public static OperatorRandomPoints local() {
        return (OperatorRandomPoints) OperatorFactoryLocalEx.getInstance().getOperator(Type.RandomPoints);
    }

}
