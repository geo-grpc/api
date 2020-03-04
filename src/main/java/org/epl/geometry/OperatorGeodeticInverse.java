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

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.ProgressTracker;

/**
 * Geodetic length calculation.
 */
public abstract class OperatorGeodeticInverse extends OperatorEx {

	@Override
	public Type getType() {
		return Type.GeodeticInverse;
	}

	/**
	 * calculate the ngs forward equations for distance and azimuth calculations
	 *
	 * @param geom1             point1 (for now only supports point, could support centroids in future)
	 * @param geom2             point2 (for now only supports point, could support centroids in future)
	 * @param sr1               spatial reference of point one (this is the ellipsoid that the calculation will use)
	 * @param sr2               spatial reference of point two
	 * @param geodeticCurveType for now only ellipsoid geodesic
	 * @param progressTracker   not used
	 * @return forward results of azimuth from 1 to 2, azimuth from 2 to 1, and the distance
	 */
	public abstract InverseResult execute(Geometry geom1,
	                                      Geometry geom2,
	                                      SpatialReferenceEx sr1,
	                                      SpatialReferenceEx sr2,
	                                      int geodeticCurveType,
	                                      ProgressTracker progressTracker);

	public static OperatorGeodeticInverse local() {
		return (OperatorGeodeticInverse) OperatorFactoryLocalEx.getInstance()
				.getOperator(Type.GeodeticInverse);
	}

}
