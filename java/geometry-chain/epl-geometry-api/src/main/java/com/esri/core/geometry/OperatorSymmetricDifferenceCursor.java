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

public class OperatorSymmetricDifferenceCursor extends GeometryCursor {
	ProgressTracker m_progress_tracker;
	SpatialReference m_spatial_reference;
	Geometry m_rightGeom;
	boolean m_bEmpty;

	OperatorSymmetricDifferenceCursor(GeometryCursor inputGeoms,
	                                  GeometryCursor rightGeom, SpatialReference sr,
	                                  ProgressTracker progress_tracker) {
		m_bEmpty = rightGeom == null;
		m_inputGeoms = inputGeoms;
		m_spatial_reference = sr;
		m_rightGeom = rightGeom.next();
		m_progress_tracker = progress_tracker;
	}

	@Override
	public Geometry next() {
		if (m_bEmpty)
			return null;

		if (hasNext()) {
			return OperatorSymmetricDifferenceLocal.symmetricDifference(
					m_inputGeoms.next(),
					m_rightGeom,
					m_spatial_reference,
					m_progress_tracker);
		}

		return null;
	}
}
