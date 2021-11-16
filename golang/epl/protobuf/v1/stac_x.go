package v1

import "github.com/golang/protobuf/ptypes/timestamp"

//oneof ugly: https://github.com/golang/protobuf/issues/283
func (m *StacRequest) SetObserved(timestampFilter *TimestampFilter) {
	m.Temporal = &StacRequest_Observed{Observed: timestampFilter}
}

func (m *StacRequest) SetDatetime(timestampFilter *TimestampFilter) {
	m.Temporal = &StacRequest_Datetime{Datetime: timestampFilter}
}

func (m *StacRequest) SetIntersects(geometry *GeometryData) {
	m.Spatial = &StacRequest_Intersects{Intersects:geometry}
}

func (m *StacRequest) SetBbox(geometry *EnvelopeData) {
	m.Spatial = &StacRequest_Bbox{Bbox:geometry}
}

func (m *StacRequest) SetGeometryRequest(geometry *GeometryRequest) {
	m.Spatial = &StacRequest_GeometryRequest{GeometryRequest:geometry}
}


func (m *StacItem) SetGeometry(geometry *GeometryData) {
	m.Geometry = geometry
	m.Bbox = geometry.Envelope
}

func (m *StacItem) SetObserved(dt *timestamp.Timestamp) {
	m.Temporal = &StacItem_Observed{Observed:dt}
	m.TemporalDeprecated = &StacItem_Datetime{Datetime:dt}
}

func (m *StacItem) SetDatetime(dt *timestamp.Timestamp) {
	m.SetObserved(dt)
}
