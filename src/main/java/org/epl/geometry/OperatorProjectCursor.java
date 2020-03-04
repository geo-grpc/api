package org.epl.geometry;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryCursor;
import com.esri.core.geometry.ProgressTracker;

/**
 * Created by davidraleigh on 5/12/17.
 */
public class OperatorProjectCursor extends GeometryCursor {
    ProjectionTransformation m_projectionTransformation;
    ProgressTracker m_progressTracker;

    OperatorProjectCursor(
            GeometryCursor inputGeoms,
            ProjectionTransformation projectionTransformation,
            ProgressTracker progressTracker) {
        m_inputGeoms = inputGeoms;
        m_projectionTransformation = projectionTransformation;
        m_progressTracker = progressTracker;
    }

    @Override
    public Geometry next() {
        if (m_inputGeoms.hasNext()) {
            Geometry geometry = m_inputGeoms.next();
            return Projector.project(geometry, m_projectionTransformation, m_progressTracker);
        }
        return null;
    }
}
