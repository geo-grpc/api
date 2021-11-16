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

final class OperatorBoundaryLocalCursor extends GeometryCursor {
	ProgressTracker m_progress_tracker;

	OperatorBoundaryLocalCursor(GeometryCursor inputGeoms,
	                            ProgressTracker tracker) {
		m_inputGeoms = inputGeoms;
		m_progress_tracker = tracker;
	}

	@Override
	public Geometry next() {
		if (hasNext()) {
			return calculate_boundary(m_inputGeoms.next(), m_progress_tracker);
		}

		return null;
	}

	private static Geometry calculate_boundary(Geometry geom,
	                                           ProgressTracker progress_tracker) {
		Geometry res = Boundary.calculate(geom, progress_tracker);
		if (res == null)
			return new Point(geom.getDescription());// cannot return null
		else
			return res;
	}
}
