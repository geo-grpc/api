package v1
//oneof ugly: https://github.com/golang/protobuf/issues/283
import "github.com/golang/protobuf/ptypes/timestamp"

func (m *FloatFilter) SetValue(value float32) {
	m.Data = &FloatFilter_Value{Value: value}
}

func (m *DoubleFilter) SetValue(value float64) {
	m.Data = &DoubleFilter_Value{Value: value}
}

func (m *UInt32Filter) SetValue(value uint32) {
	m.Data = &UInt32Filter_Value{Value: value}
}

func (m *TimestampFilter) SetValue(value *timestamp.Timestamp) {
	m.Data = &TimestampFilter_Value{Value:value}
}


func (m *FloatFilter) SetStartStop(start float32, end float32) {
	m.Data = &FloatFilter_Start{Start: start}
	m.End = end
}

func (m *UInt32Filter) SetStartStop(start uint32, end uint32) {
	m.Data = &UInt32Filter_Start{Start: start}
	m.End = end
}

func (m *DoubleFilter) SetStart(start float64, end float64) {
	m.Data = &DoubleFilter_Start{Start: start}
	m.End = end
}

func (m *TimestampFilter) SetStart(start *timestamp.Timestamp, end *timestamp.Timestamp) {
	m.Data = &TimestampFilter_Start{Start: start}
	m.End = end
}