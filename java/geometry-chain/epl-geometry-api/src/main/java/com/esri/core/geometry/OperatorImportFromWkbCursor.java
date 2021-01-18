package com.esri.core.geometry;

public class OperatorImportFromWkbCursor extends GeometryCursor {
	private ByteBufferCursor m_inputWkbBuffers;
	private int m_importFlags;

	public OperatorImportFromWkbCursor(int importFlags, ByteBufferCursor wkbBuffers) {
		if (wkbBuffers == null)
			throw new GeometryException("invalid argument");

		m_importFlags = importFlags;
		m_inputWkbBuffers = wkbBuffers;
	}

	@Override
	public boolean hasNext() {
		return m_inputWkbBuffers != null && m_inputWkbBuffers.hasNext();
	}

	@Override
	public Geometry next() {
		if (hasNext()) {
			return OperatorImportFromWkbLocal.local().execute(
					m_importFlags,
					Geometry.Type.Unknown,
					m_inputWkbBuffers.next(),
					null);
		}
		return null;
	}

	@Override
	public int getGeometryID() {
		return m_inputWkbBuffers.getByteBufferID();
	}

	@Override
	public SimpleStateEnum getSimpleState() {
		return m_inputWkbBuffers.getSimpleState();
	}

	@Override
	public String getFeatureID() {
		return m_inputWkbBuffers.getFeatureID();
	}
}
