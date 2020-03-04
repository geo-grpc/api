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

package org.epl.geometry;

import com.esri.core.geometry.*;

//This is a stub
class OperatorGeodeticDensifyLocal extends OperatorGeodeticDensifyByLength {

	@Override
	public GeometryCursor execute(GeometryCursor geoms,
	                              SpatialReferenceEx sr,
	                              double maxSegmentLengthMeters,
	                              int curveType,
	                              ProgressTracker progressTracker) {
		if (maxSegmentLengthMeters <= 0)
			// TODO fix geometry exception to match native implementation
			throw new GeometryException("max segment length must be positive and greater than 0");// GEOMTHROW(invalid_argument);

		return new OperatorGeodeticDensifyCursor(geoms, sr, maxSegmentLengthMeters, progressTracker);
	}

	@Override
	public Geometry execute(Geometry geom,
	                        SpatialReferenceEx sr,
	                        double maxSegmentLengthMeters,
	                        int curveType,
	                        ProgressTracker progressTracker) {
		SimpleGeometryCursor inputCursor = new SimpleGeometryCursor(geom);
		GeometryCursor outputCursor = execute(inputCursor, sr, maxSegmentLengthMeters, curveType, progressTracker);
		return outputCursor.next();
	}
}
