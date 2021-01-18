package com.esri.core.geometry;

public class OperatorExportToWktCursor extends StringCursor {
	private GeometryCursor m_geometryCursor;
	private int m_export_flags;
	private SimpleStateEnum simpleStateEnum = SimpleStateEnum.SIMPLE_UNKNOWN;
	private Envelope2D env2D = new Envelope2D();

	public OperatorExportToWktCursor(int exportFlags, GeometryCursor geometryCursor, ProgressTracker progressTracker) {
		if (geometryCursor == null)
			throw new GeometryException("invalid argument");

		m_export_flags = exportFlags;
		m_geometryCursor = geometryCursor;
	}

	@Override
	public String next() {
		Geometry geometry;
		if (hasNext()) {
			geometry = m_geometryCursor.next();
			geometry.queryEnvelope2D(env2D);
			simpleStateEnum = geometry.getSimpleState();
			StringBuilder stringBuilder = new StringBuilder();
			OperatorExportToWktLocal.exportToWkt(m_export_flags, geometry, stringBuilder);
			return stringBuilder.toString();
		}
		return null;
	}

	@Override
	public boolean hasNext() {
		return m_geometryCursor != null && m_geometryCursor.hasNext();
	}

	@Override
	public int getID() {
		return m_geometryCursor.getGeometryID();
	}

	@Override
	public SimpleStateEnum getSimpleState() {
		return simpleStateEnum;
	}

	@Override
	public Envelope2D getEnvelope2D() {
		return env2D;
	}

	@Override
	public String getFeatureID() {
		return m_geometryCursor.getFeatureID();
	}
}
