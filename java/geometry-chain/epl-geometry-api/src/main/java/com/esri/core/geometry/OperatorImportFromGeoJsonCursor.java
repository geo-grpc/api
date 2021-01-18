package com.esri.core.geometry;

public class OperatorImportFromGeoJsonCursor extends MapGeometryCursor {
	StringCursor m_jsonStringCursor;
	int m_import_flags;
	int m_count;

	// TODO, this was never implemented. Maybe remove?
//    public OperatorImportFromGeoJsonCursor(int import_flags, String geoJsonString, ProgressTracker progressTracker) {
//        m_geoJsonString = geoJsonString;
//        m_import_flags = import_flags;
//        m_index = -1;
//        m_count = 1;
//    }

	public OperatorImportFromGeoJsonCursor(int import_flags, StringCursor stringCursor, ProgressTracker progressTracker) {
		m_jsonStringCursor = stringCursor;
		m_import_flags = import_flags;
		m_count = 1;
	}

	@Override
	public MapGeometry next() {
		String nextString;
		if ((nextString = m_jsonStringCursor.next()) != null) {
			JsonReader jsonReader = JsonParserReader.createFromString(nextString);
			return OperatorImportFromGeoJsonLocal.OperatorImportFromGeoJsonHelper.importFromGeoJson(m_import_flags, Geometry.Type.Unknown, jsonReader, null, false);
		}
		return null;
	}

	@Override
	public SimpleStateEnum getSimpleState() {
		return m_jsonStringCursor.getSimpleState();
	}

	@Override
	public String getFeatureID() {
		return m_jsonStringCursor.getFeatureID();
	}

	@Override
	public int getGeometryID() {
		return m_jsonStringCursor.getID();
	}

	@Override
	public boolean hasNext() {
		return m_jsonStringCursor != null && m_jsonStringCursor.hasNext();
	}
}
