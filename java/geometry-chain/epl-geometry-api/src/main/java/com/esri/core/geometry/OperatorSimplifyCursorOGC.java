/*
 Copyright 1995-2015 Esri

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
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */
package com.esri.core.geometry;

class OperatorSimplifyCursorOGC extends GeometryCursor {
	SpatialReference m_spatialReference;
	ProgressTracker m_progressTracker;
	boolean m_bForceSimplify;

	OperatorSimplifyCursorOGC(GeometryCursor geoms,
	                          SpatialReference spatialRef, boolean bForceSimplify,
	                          ProgressTracker progressTracker) {
		m_progressTracker = progressTracker;
		m_bForceSimplify = bForceSimplify;

		if (geoms == null)
			throw new IllegalArgumentException();

		m_inputGeoms = geoms;

		m_spatialReference = spatialRef;
	}

	@Override
	public Geometry next() {
		if (hasNext()) {
			if ((m_progressTracker != null) && !(m_progressTracker.progress(-1, -1)))
				throw new RuntimeException("user_canceled");
			return simplify(m_inputGeoms.next());
		}
		return null;
	}

	Geometry simplify(Geometry geometry) {
		if (geometry == null)
			throw new IllegalArgumentException();

		return OperatorSimplifyLocalHelper.simplifyOGC(geometry,
				m_spatialReference, m_bForceSimplify, m_progressTracker);
	}
}
