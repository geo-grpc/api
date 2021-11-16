package com.esri.core.geometry;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class OperatorExportToEWkbCursor extends ByteBufferCursor {
	private GeometryCursor m_geometryCursor;
	int m_exportFlags;
	private SimpleStateEnum simpleStateEnum = SimpleStateEnum.SIMPLE_UNKNOWN;
	private Envelope2D env2D = new Envelope2D();
	private int m_srid = 0;

	public OperatorExportToEWkbCursor(int exportFlags, GeometryCursor geometryCursor, SpatialReference spatialReference) {
		if (geometryCursor == null)
			throw new GeometryException("invalid argument");

		m_exportFlags = exportFlags | WkbExportFlags.wkbExportAsExtendedWkb;
		m_geometryCursor = geometryCursor;
		m_srid = OperatorExportToEWkbLocal.getSrid(spatialReference);
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
			int size = OperatorExportToWkbLocal.exportToWKB(m_exportFlags, geometry,null, m_srid);
			ByteBuffer wkbBuffer = ByteBuffer.allocate(size).order(ByteOrder.nativeOrder());
			OperatorExportToWkbLocal.exportToWKB(m_exportFlags, geometry, wkbBuffer, m_srid);
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
