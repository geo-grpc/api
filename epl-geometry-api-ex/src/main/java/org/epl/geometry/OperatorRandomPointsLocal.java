package org.epl.geometry;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryCursor;
import com.esri.core.geometry.ProgressTracker;
import com.esri.core.geometry.SimpleGeometryCursor;

/**
 * Created by davidraleigh on 5/10/17.
 */
public class OperatorRandomPointsLocal extends OperatorRandomPoints {
    @Override
    public GeometryCursor execute(
            GeometryCursor inputPolygons,
            double[] pointsPerSquareKm,
            long seed,
            SpatialReferenceEx sr,
            ProgressTracker progressTracker) {
        GeometryCursor randomPointsCursor = new OperatorRandomPointsCursor(inputPolygons, pointsPerSquareKm, seed, sr, progressTracker);
        return randomPointsCursor;
    }

    @Override
    public Geometry execute(
            Geometry inputPolygon,
            double pointsPerSquareKm,
            long seed,
            SpatialReferenceEx sr,
            ProgressTracker progressTracker) {
        double[] perSqrKM = {pointsPerSquareKm};
        GeometryCursor res = execute(new SimpleGeometryCursor(inputPolygon), perSqrKM, seed, sr, progressTracker);
        return res.next();
    }
}
