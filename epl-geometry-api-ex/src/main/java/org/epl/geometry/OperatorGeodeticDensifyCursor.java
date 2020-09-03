/*
Copyright 2017-2020 Echo Park Labs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

For additional information, contact:

email: davidraleigh@gmail.com
*/

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
