package com.esri.core.geometry;

public class OperatorImportFromEWkbCursor extends MapGeometryCursor {
	private ByteBufferCursor m_inputEWkbBuffers;
	private int m_importFlags;

	public OperatorImportFromEWkbCursor(int importFlags, ByteBufferCursor eWkbBuffers) {
		if (eWkbBuffers == null)
			throw new GeometryException("invalid argument");

		m_importFlags = importFlags;
		m_inputEWkbBuffers = eWkbBuffers;
	}

	@Override
	public boolean hasNext() {
		return m_inputEWkbBuffers != null && m_inputEWkbBuffers.hasNext();
	}

	@Override
	public MapGeometry next() {
		if (hasNext()) {
			return OperatorImportFromEWkbLocal.local().execute(
					m_importFlags,
					Geometry.Type.Unknown,
					m_inputEWkbBuffers.next(),
					null);
		}
		return null;
	}

	@Override
	public int getGeometryID() {
		return m_inputEWkbBuffers.getByteBufferID();
	}

	@Override
	public SimpleStateEnum getSimpleState() {
		return m_inputEWkbBuffers.getSimpleState();
	}

	@Override
	public String getFeatureID() {
		return m_inputEWkbBuffers.getFeatureID();
	}
}
