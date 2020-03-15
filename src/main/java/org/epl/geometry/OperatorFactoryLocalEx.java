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

import java.util.HashMap;

/**
 * An abstract class that represent the basic OperatorFactory interface.
 */
public class OperatorFactoryLocalEx extends OperatorFactoryEx {
	private static final OperatorFactoryLocalEx INSTANCE = new OperatorFactoryLocalEx();

	private static final HashMap<OperatorEx.Type, OperatorEx> st_supportedOperators = new HashMap<OperatorEx.Type, OperatorEx>();

	static {
		// Register all implemented operator allocators in the dictionary

		st_supportedOperators.put(OperatorEx.Type.Project, new OperatorProjectLocal());
//		st_supportedOperators.put(Type.ExportToJson, new OperatorExportToJsonLocal());
//		st_supportedOperators.put(Type.ImportFromJson, new OperatorImportFromJsonLocal());
////		st_supportedOperators.put(Type.ImportMapGeometryFromJson, new OperatorImportFromJsonLocal());
//		st_supportedOperators.put(Type.ExportToESRIShape, new OperatorExportToESRIShapeLocal());
//		st_supportedOperators.put(Type.ImportFromESRIShape, new OperatorImportFromESRIShapeLocal());
//
//		st_supportedOperators.put(Type.Proximity2D, new OperatorProximity2DLocal());
//		st_supportedOperators.put(Type.DensifyByLength, new OperatorDensifyByLengthLocal());
//
//		st_supportedOperators.put(Type.Relate, new OperatorRelateLocal());
//		st_supportedOperators.put(Type.Equals, new OperatorEqualsLocal());
//		st_supportedOperators.put(Type.Disjoint, new OperatorDisjointLocal());
//
//		st_supportedOperators.put(Type.Intersects, new OperatorIntersectsLocal());
//		st_supportedOperators.put(Type.Within, new OperatorWithinLocal());
//		st_supportedOperators.put(Type.Contains, new OperatorContainsLocal());
//		st_supportedOperators.put(Type.Crosses, new OperatorCrossesLocal());
//		st_supportedOperators.put(Type.Touches, new OperatorTouchesLocal());
//		st_supportedOperators.put(Type.Overlaps, new OperatorOverlapsLocal());
//
//		st_supportedOperators.put(Type.SimplifyOGC, new OperatorSimplifyLocalOGC());
//		st_supportedOperators.put(Type.Simplify, new OperatorSimplifyLocal());
//		st_supportedOperators.put(Type.Offset, new OperatorOffsetLocal());
//
		st_supportedOperators.put(OperatorEx.Type.GeodeticDensifyByLength, new OperatorGeodeticDensifyLocal());
//
//		st_supportedOperators.put(Type.ShapePreservingDensify, new OperatorShapePreservingDensifyLocal());
//
		st_supportedOperators.put(OperatorEx.Type.GeodesicBuffer, new OperatorGeodesicBufferLocal());
//
		st_supportedOperators.put(OperatorEx.Type.GeodeticLength, new OperatorGeodeticLengthLocal());
//		st_supportedOperators.put(Type.GeodeticArea, new OperatorGeodeticAreaLocal());

		st_supportedOperators.put(OperatorEx.Type.Buffer, new OperatorBufferExLocal());
//		st_supportedOperators.put(Type.Distance, new OperatorDistanceLocal());
//		st_supportedOperators.put(Type.Intersection, new OperatorIntersectionLocal());
//		st_supportedOperators.put(Type.Difference, new OperatorDifferenceLocal());
//		st_supportedOperators.put(Type.SymmetricDifference, new OperatorSymmetricDifferenceLocal());
//		st_supportedOperators.put(Type.Clip, new OperatorClipLocal());
//		st_supportedOperators.put(Type.Cut, new OperatorCutLocal());
//		st_supportedOperators.put(Type.ExportToWkb, new OperatorExportToWkbLocal());
//		st_supportedOperators.put(Type.ImportFromWkb, new OperatorImportFromWkbLocal());
//		st_supportedOperators.put(Type.ExportToWkt, new OperatorExportToWktLocal());
//		st_supportedOperators.put(Type.ImportFromWkt, new OperatorImportFromWktLocal());
//		st_supportedOperators.put(Type.ImportFromGeoJson, new OperatorImportFromGeoJsonLocal());
//		st_supportedOperators.put(Type.ExportToGeoJson, new OperatorExportToGeoJsonLocal());
//		st_supportedOperators.put(Type.Union, new OperatorUnionLocal());
		st_supportedOperators.put(OperatorEx.Type.GeneralizeByArea, new OperatorGeneralizeByAreaLocal());
//		st_supportedOperators.put(Type.Generalize, new OperatorGeneralizeLocal());
//		st_supportedOperators.put(Type.ConvexHull, new OperatorConvexHullLocal());
//		st_supportedOperators.put(Type.Boundary, new OperatorBoundaryLocal());
//
		st_supportedOperators.put(OperatorEx.Type.RandomPoints, new OperatorRandomPointsLocal());
		st_supportedOperators.put(OperatorEx.Type.EnclosingCircle, new OperatorEnclosingCircleLocal());
		st_supportedOperators.put(OperatorEx.Type.GeodeticInverse, new OperatorGeodeticInverseLocal());
		st_supportedOperators.put(OperatorEx.Type.SimpleRelation, new OperatorSimpleRelationEx());

		// LabelPoint, - not ported

	}

	private OperatorFactoryLocalEx() {

	}


	/**
	 * Returns a reference to the singleton.
	 */
	public static OperatorFactoryLocalEx getInstance() {
		return INSTANCE;
	}

	@Override
	public OperatorEx getOperator(OperatorEx.Type type) {
		if (st_supportedOperators.containsKey(type)) {
			return st_supportedOperators.get(type);
		} else {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public boolean isOperatorSupported(OperatorEx.Type type) {
		return st_supportedOperators.containsKey(type);
	}
}
