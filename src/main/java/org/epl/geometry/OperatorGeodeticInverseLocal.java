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


class OperatorGeodeticInverseLocal extends OperatorGeodeticInverse {
	@Override
	public InverseResult execute(Geometry geom1, Geometry geom2, SpatialReferenceEx sr1, SpatialReferenceEx sr2, int geodeticCurveType, ProgressTracker progressTracker) {
		if (geodeticCurveType != GeodeticCurveType.Geodesic) {
			throw new GeometryException("only Geodesic implemented");
		}
		if (geom1.getType() != Geometry.Type.Point || geom2.getType() != Geometry.Type.Point) {
			throw new GeometryException("only implemented for points");
		}

        double a, e2;
        Point projected1;
        ProjectionTransformation projectionTransformation;
        if (sr1.getCoordinateSystemType() != SpatialReferenceEx.CoordinateSystemType.GEOGRAPHIC) {
            SpatialReferenceEx wgs84 = SpatialReferenceEx.create(4326);

            projectionTransformation = new ProjectionTransformation(sr1, wgs84);
            sr1 = wgs84;
            projected1 = (Point)OperatorProject.local().execute(geom1, projectionTransformation, progressTracker);
            a = wgs84.getMajorAxis();
            e2 = wgs84.getEccentricitySquared();
        } else {
            projected1 = (Point)geom1;
            a = sr1.getMajorAxis();
            e2 = sr1.getEccentricitySquared();
        }

        projectionTransformation = new ProjectionTransformation(sr2, sr1);
        Point projected2 = (Point)OperatorProject.local().execute(geom2, projectionTransformation, progressTracker);

        return GeoDist.geodesicInverse(a, e2, projected1.getXY(), projected2.getXY());
	}
}
