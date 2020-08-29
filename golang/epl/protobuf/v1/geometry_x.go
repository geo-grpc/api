package v1

func (m *GeometryData) SetWkt(req string) {
	m.Data = &GeometryData_Wkt{Wkt:req}
}

func (m *GeometryData) SetWkb(req []byte) {
	m.Data = &GeometryData_Wkb{Wkb:req}
}

func (m *GeometryData) SetGeojson(req string) {
	m.Data = &GeometryData_Geojson{Geojson:req}
}

func (m *GeometryData) SetEsriShape(req []byte) {
	m.Data = &GeometryData_EsriShape{EsriShape:req}
}