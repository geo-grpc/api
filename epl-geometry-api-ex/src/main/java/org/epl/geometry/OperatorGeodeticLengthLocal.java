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

import com.esri.core.geometry.*;

class OperatorGeodeticLengthLocal extends OperatorGeodeticLength {

	@Override
	public double execute(Geometry geom,
	                      SpatialReferenceEx sr,
	                      int geodeticCurveType,
	                      ProgressTracker progressTracker) {
		if (geodeticCurveType != GeodeticCurveType.Geodesic) {
			throw new GeometryException("only Geodesic implemented");
		}
		if (geom.getType() == Geometry.Type.MultiPoint || geom.getType() == Geometry.Type.Point) {
			return 0;
		}

		SegmentIterator segIter;
		double a, e2;
		if (sr.getCoordinateSystemType() != SpatialReferenceEx.CoordinateSystemType.GEOGRAPHIC) {
			// TODO get the GCS from the SpatialReference instead of assuming Geographic == 4326
			SpatialReferenceEx wgs84 = SpatialReferenceEx.create(4326);

			ProjectionTransformation projectionTransformation = new ProjectionTransformation(sr, wgs84);
			Geometry projected = OperatorProject.local().execute(geom, projectionTransformation, progressTracker);
			segIter = ((MultiPath) projected).querySegmentIterator();
			a = wgs84.getMajorAxis();
			e2 = wgs84.getEccentricitySquared();
		} else {
			segIter = ((MultiPath) geom).querySegmentIterator();
			a = sr.getMajorAxis();
			e2 = sr.getEccentricitySquared();
		}

		MathUtils.KahanSummator len = new MathUtils.KahanSummator(0);

		while (segIter.nextPath()) {
			while (segIter.hasNextSegment()) {
				Segment segment = segIter.nextSegment();
				double dist = GeoDist.geodesicDistance(a, e2, segment.getStartXY(), segment.getEndXY(), null, null);
				len.add(dist);
			}
		}

		return len.getResult();
	}
}
