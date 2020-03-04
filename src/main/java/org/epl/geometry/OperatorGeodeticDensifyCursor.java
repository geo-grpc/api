package org.epl.geometry;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryCursor;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.ProgressTracker;

/**
 * Created by davidraleigh on 2/21/16.
 */
public class OperatorGeodeticDensifyCursor extends GeometryCursor {
	SpatialReferenceExImpl m_spatialReference;
	double m_maxLength;
	Point m_startPoint;
	Point m_endPoint;
	ProgressTracker m_progressTracker;

	public OperatorGeodeticDensifyCursor(GeometryCursor inputGeoms1,
	                                     SpatialReferenceEx spatialReference,
	                                     double maxLength,
	                                     ProgressTracker progressTracker) {
		m_inputGeoms = inputGeoms1;
		m_maxLength = maxLength;
		m_spatialReference = (SpatialReferenceExImpl) spatialReference;
		m_startPoint = new Point();
		m_endPoint = new Point();
		m_progressTracker = progressTracker;
	}

	@Override
	public Geometry next() {
		if (hasNext())
			return GeodesicDensifier.densifyByLength(m_inputGeoms.next(), m_spatialReference, m_maxLength, m_progressTracker);

		return null;
	}
}
