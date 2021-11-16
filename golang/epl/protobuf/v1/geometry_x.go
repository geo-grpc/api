package v1
//oneof ugly: https://github.com/golang/protobuf/issues/283
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

func (m *ProjectionData) SetEpsg(def int32) {
	m.Definition = &ProjectionData_Epsg{Epsg:def}
}

func (m *ProjectionData) SetProj4(def string) {
	m.Definition = &ProjectionData_Proj4{Proj4:def}
}

func (m *ProjectionData) SetWkt(def string) {
	m.Definition = &ProjectionData_Wkt{Wkt:def}
}

func (m *ProjectionData) SetCustom(custom *ProjectionData_Custom) {
	// how totally gross is this.
	m.Definition = &ProjectionData_Custom_{Custom:custom}
}

func (m *GeometryRequest) SetGeometry(geometry *GeometryData) {
	m.Left = &GeometryRequest_LeftGeometry{LeftGeometry:geometry}
}

func (m *GeometryRequest) SetGeometryRequest(request *GeometryRequest) {
	m.Left = &GeometryRequest_LeftGeometryRequest{LeftGeometryRequest:request}
}

func (m *GeometryRequest) SetLeftGeometry(geometry *GeometryData) {
	m.Left = &GeometryRequest_LeftGeometry{LeftGeometry:geometry}
}

func (m *GeometryRequest) SetLeftGeometryRequest(request *GeometryRequest) {
	m.Left = &GeometryRequest_LeftGeometryRequest{LeftGeometryRequest:request}
}

func (m *GeometryRequest) SetRightGeometry(geometry *GeometryData) {
	m.Right = &GeometryRequest_RightGeometry{RightGeometry:geometry}
}

func (m *GeometryRequest) SetRightGeometryRequest(request *GeometryRequest) {
	m.Right = &GeometryRequest_RightGeometryRequest{RightGeometryRequest:request}
}

func (m *GeometryRequest) HasLeftGeometryInput() bool {
	return m.GetLeftGeometry() != nil || m.GetGeometry() != nil || m.GetLeftGeometryRequest() != nil || m.GetGeometryRequest() != nil
}

func (m *GeometryRequest) HasGeometryInput() bool {
	return m.HasLeftGeometryInput()
}

func (m *GeometryRequest) HasRightGeometryInput() bool {
	return m.GetRightGeometry() != nil || m.GetRightGeometryRequest() != nil
}