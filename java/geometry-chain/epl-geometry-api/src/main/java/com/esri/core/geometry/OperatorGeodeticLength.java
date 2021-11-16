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

import com.esri.core.geometry.Operator.Type;

/**
 * 
 * Geodetic length calculation.
 * 
 */
abstract class OperatorGeodeticLength extends Operator {

	@Override
	public Type getType() {
		return Operator.Type.GeodeticLength;
	}

	/**
	 * Calculates the geodetic length of the input Geometry.
	 * 
	 * @param geom
	 *            The input Geometry for the geodetic length calculation.
	 * @param sr
	 *            The SpatialReference of the Geometry.
	 * @param geodeticCurveType
	 *            Use the {@link GeodeticCurveType} interface to choose the
	 *            interpretation of a line connecting two points.
	 * @param progressTracker
	 * @return Returns the geoetic length of the Geometry.
	 */
	public abstract double execute(Geometry geom, SpatialReference sr,
			int geodeticCurveType, ProgressTracker progressTracker);

	public static OperatorGeodeticLength local() {
		return (OperatorGeodeticLength) OperatorFactoryLocal.getInstance()
				.getOperator(Type.GeodeticLength);
	}

}
