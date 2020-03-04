package org.epl.geometry;

import com.esri.core.geometry.*;

/**
 * Created by davidraleigh on 2/24/16.
 */
class GeodesicDensifier {
	static Geometry densifyByLength(Geometry geom, SpatialReferenceEx sr, double maxLength, ProgressTracker progressTracker) {
		if (geom.isEmpty() || geom.getDimension() < 1)
			return geom;

		int geometryType = geom.getType().value();

		GeodesicDensifier geodesicDensifier = new GeodesicDensifier(maxLength, sr, progressTracker);
		// TODO implement IsMultiPath and remove Polygon and Polyline call to
		// match Native
		// if (Geometry.IsMultiPath(geometryType))
		if (geometryType == Geometry.GeometryType.Polygon)
			return geodesicDensifier.densifyMultiPath((MultiPath) geom);
		else if (Geometry.GeometryType.Polyline == geometryType)
			return geodesicDensifier.densifyMultiPath((MultiPath) geom);
		else if (Geometry.isSegment(geometryType))
			return geodesicDensifier.densifySegment((Segment) geom);
		else if (geometryType == Geometry.GeometryType.Envelope)
			return geodesicDensifier.densifyEnvelope((Envelope) geom);
		else
			// TODO fix geometry exception to match native implementation
			// TODO - [ ] public access to `GeometryException.GeometryInternalError()`
			throw new GeometryException("GeometryInternalError");// GEOMTHROW(internal_error);
	}

	GeodesicDensifier(double maxLength, SpatialReferenceEx sr, ProgressTracker progressTracker) {
		m_startPoint = new Point();
		m_endPoint = new Point();
		m_maxLength = maxLength;

		// sr is used to define these:
		m_a = sr.getMajorAxis(); // radius of spheroid for WGS_1984
		m_e2 = sr.getEccentricitySquared(); // ellipticity for WGS_1984
		m_rpu = Math.PI / 180.0;
		m_dpu = 180.0 / Math.PI;
	}

	private Point m_startPoint;
	private Point m_endPoint;
	private double m_maxLength;
	private double m_a;
	private double m_e2;
	private double m_rpu;
	private double m_dpu;


	private double geodesicDistanceOnWGS84(Point2D startPt2D, Point2D endPt2D) {
		m_startPoint.setXY(startPt2D);
		m_endPoint.setXY(endPt2D);
		return SpatialReferenceExImpl.geodesicDistanceOnWGS84Impl(m_startPoint, m_endPoint);
	}

	private double geodesicDistanceOnWGS84(Segment segment) {
		m_startPoint.setXY(segment.getStartXY());
		m_endPoint.setXY(segment.getEndXY());
		return SpatialReferenceExImpl.geodesicDistanceOnWGS84Impl(m_startPoint, m_endPoint);
	}

	private Geometry densifySegment(Segment geom) {
		double length = geodesicDistanceOnWGS84(geom);
		if (length <= m_maxLength)
			return geom;

		Polyline polyline = new Polyline(geom.getDescription());
		polyline.addSegment(geom, true);
		return densifyMultiPath(polyline);
	}

	private Geometry densifyEnvelope(Envelope geom) {
		Polygon polygon = new Polygon(geom.getDescription());
		polygon.addEnvelope(geom, false);

		Envelope2D env2D = new Envelope2D();
		geom.queryEnvelope2D(env2D);
		double wTop = geodesicDistanceOnWGS84(env2D.getUpperLeft(), env2D.getUpperRight());
		double wBottom = geodesicDistanceOnWGS84(env2D.getLowerLeft(), env2D.getLowerRight());
		double height = geodesicDistanceOnWGS84(env2D.getUpperLeft(), env2D.getLowerLeft());// height on right is same as left. meridians are geodesics

		if (wTop <= m_maxLength && wBottom <= m_maxLength && height <= m_maxLength)
			return polygon;

		return densifyMultiPath(polygon);
	}

	private Geometry densifyMultiPath(MultiPath geom) {
		PeDouble distanceMeters = new PeDouble();
		PeDouble az12 = new PeDouble();
		PeDouble lam2 = new PeDouble();
		PeDouble phi2 = new PeDouble();


		MultiPath densifiedPoly = (MultiPath) geom.createInstance();
		SegmentIterator iter = geom.querySegmentIterator();
		while (iter.nextPath()) {
			boolean bStartNewPath = true;
			while (iter.hasNextSegment()) {
				Segment seg = iter.nextSegment();
				if (seg.getType().value() != Geometry.GeometryType.Line)
					throw new GeometryException("curve densify not implemented");

				boolean bIsClosing = iter.isClosingSegment();

				// also get the segment's azimuth
				GeoDist.geodesic_distance_ngs(
						m_a,
						m_e2,
						seg.getStartX() * m_rpu,
						seg.getStartY() * m_rpu,
						seg.getEndX() * m_rpu,
						seg.getEndY() * m_rpu,
						distanceMeters,
						az12,
						null);

				if (distanceMeters.val > m_maxLength) {// need to split
					double dcount = Math.ceil(distanceMeters.val / m_maxLength);
					double distInterval = distanceMeters.val / dcount;

					Point point = new Point(geom.getDescription());// LOCALREFCLASS1(Point,
					// VertexDescription,
					// point,
					// geom.getDescription());
					if (bStartNewPath) {
						bStartNewPath = false;
						seg.queryStart(point);
						densifiedPoly.startPath(point);
					}

					int n = (int) dcount - 1;
					double distanceAlongGeodesic = 0.0;

					for (int i = 0; i < n; i++) {
						distanceAlongGeodesic += distInterval;
						GeoDist.geodesic_forward(
								m_a,
								m_e2,
								seg.getStartX() * m_rpu,
								seg.getStartY() * m_rpu,
								distanceAlongGeodesic,
								az12.val,
								lam2,
								phi2);

						densifiedPoly.lineTo(lam2.val * m_dpu, phi2.val * m_dpu);
					}

					if (!bIsClosing) {
						seg.queryEnd(point);
						densifiedPoly.lineTo(point);
					} else {
						densifiedPoly.closePathWithLine();
					}

					bStartNewPath = false;
				} else {
					if (!bIsClosing)
						densifiedPoly.addSegment(seg, bStartNewPath);
					else
						densifiedPoly.closePathWithLine();

					bStartNewPath = false;
				}
			}
		}

		return densifiedPoly;
	}
}
