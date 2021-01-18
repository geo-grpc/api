package com.esri.core.geometry;

public class OperatorImportFromWktCursor extends GeometryCursor {
	private StringCursor m_wktStringCursor;
	private int m_importFlags;

	public OperatorImportFromWktCursor(int import_flags, StringCursor stringCursor) {
		if (stringCursor == null)
			throw new GeometryException("invalid argument");

		m_importFlags = import_flags;
		m_wktStringCursor = stringCursor;
	}

	@Override
	public boolean hasNext() {
		return m_wktStringCursor != null && m_wktStringCursor.hasNext();
	}

	@Override
	public Geometry next() {
		if (hasNext()) {
			return OperatorImportFromWkt.local().execute(
					m_importFlags,
					Geometry.Type.Unknown,
					m_wktStringCursor.next(),
					null);
		}
		return null;
	}

	@Override
	public int getGeometryID() {
		return m_wktStringCursor.getID();
	}

	@Override
	public SimpleStateEnum getSimpleState() {
		return m_wktStringCursor.getSimpleState();
	}

	@Override
	public String getFeatureID() {
		return m_wktStringCursor.getFeatureID();
	}
}
