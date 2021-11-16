package com.esri.core.geometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OperatorExportToWkbCursor extends ByteBufferCursor {
	private GeometryCursor m_geometryCursor;
	int m_exportFlags;
	private SimpleStateEnum simpleStateEnum = SimpleStateEnum.SIMPLE_UNKNOWN;
	private Envelope2D env2D = new Envelope2D();

	public OperatorExportToWkbCursor(int exportFlags, GeometryCursor geometryCursor) {
		if (geometryCursor == null)
			throw new GeometryException("invalid argument");

		m_exportFlags = exportFlags;
		m_geometryCursor = geometryCursor;
	}

	@Override
	public boolean hasNext() {
		return m_geometryCursor != null && m_geometryCursor.hasNext();
	}

	@Override
	public ByteBuffer next() {
		Geometry geometry;
		if (hasNext()) {
			geometry = m_geometryCursor.next();
			geometry.queryEnvelope2D(env2D);
			simpleStateEnum = geometry.getSimpleState();
			int size = OperatorExportToWkbLocal.exportToWKB(m_exportFlags, geometry, null, 0);
			ByteBuffer wkbBuffer = ByteBuffer.allocate(size).order(ByteOrder.nativeOrder());
			OperatorExportToWkbLocal.exportToWKB(m_exportFlags, geometry, wkbBuffer, 0);
			return wkbBuffer;
		}
		return null;
	}

	@Override
	public int getByteBufferID() {
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
