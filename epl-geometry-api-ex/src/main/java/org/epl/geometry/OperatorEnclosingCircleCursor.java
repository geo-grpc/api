package org.epl.geometry;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryCursor;
import com.esri.core.geometry.MultiVertexGeometry;
import com.esri.core.geometry.ProgressTracker;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class OperatorEnclosingCircleCursor extends GeometryCursor {
    SpatialReferenceEx m_spatialReference;
    ProgressTracker m_progressTracker;

    public OperatorEnclosingCircleCursor(GeometryCursor geoms, SpatialReferenceEx spatialReference, ProgressTracker progressTracker) {
        m_inputGeoms = geoms;
        m_spatialReference = spatialReference;
        m_progressTracker = progressTracker;
    }

    @Override
    public Geometry next() {
        if (hasNext())
            return getCircle(m_inputGeoms.next());

        return null;
    }

    protected Geometry getCircle(Geometry geometry) {
        if (geometry.getType() == Geometry.Type.Point || ((MultiVertexGeometry)geometry).getPointCount() <= 1)
            return geometry;

        EnclosingCircler enclosingCircler = new EnclosingCircler(geometry, m_spatialReference, m_progressTracker);
        return enclosingCircler.search();
    }


}
