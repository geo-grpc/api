// Code generated by protoc-gen-go. DO NOT EDIT.
// source: epl/protobuf/naip.proto

package protobuf // import "github.com/geo-grpc/api/golang/epl/protobuf"

import proto "github.com/golang/protobuf/proto"
import fmt "fmt"
import math "math"
import timestamp "github.com/golang/protobuf/ptypes/timestamp"

// Reference imports to suppress errors if they are not otherwise used.
var _ = proto.Marshal
var _ = fmt.Errorf
var _ = math.Inf

// This is a compile-time assertion to ensure that this generated file
// is compatible with the proto package it is being compiled against.
// A compilation error at this line likely means your copy of the
// proto package needs to be updated.
const _ = proto.ProtoPackageIsVersion2 // please upgrade the proto package

type NaipProperties struct {
	SrcImgDate           *timestamp.Timestamp `protobuf:"bytes,1,opt,name=src_img_date,json=srcImgDate,proto3" json:"src_img_date,omitempty"`
	Usgsid               string               `protobuf:"bytes,2,opt,name=usgsid,proto3" json:"usgsid,omitempty"`
	XXX_NoUnkeyedLiteral struct{}             `json:"-"`
	XXX_unrecognized     []byte               `json:"-"`
	XXX_sizecache        int32                `json:"-"`
}

func (m *NaipProperties) Reset()         { *m = NaipProperties{} }
func (m *NaipProperties) String() string { return proto.CompactTextString(m) }
func (*NaipProperties) ProtoMessage()    {}
func (*NaipProperties) Descriptor() ([]byte, []int) {
	return fileDescriptor_naip_9c02332488f8f0e7, []int{0}
}
func (m *NaipProperties) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_NaipProperties.Unmarshal(m, b)
}
func (m *NaipProperties) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_NaipProperties.Marshal(b, m, deterministic)
}
func (dst *NaipProperties) XXX_Merge(src proto.Message) {
	xxx_messageInfo_NaipProperties.Merge(dst, src)
}
func (m *NaipProperties) XXX_Size() int {
	return xxx_messageInfo_NaipProperties.Size(m)
}
func (m *NaipProperties) XXX_DiscardUnknown() {
	xxx_messageInfo_NaipProperties.DiscardUnknown(m)
}

var xxx_messageInfo_NaipProperties proto.InternalMessageInfo

func (m *NaipProperties) GetSrcImgDate() *timestamp.Timestamp {
	if m != nil {
		return m.SrcImgDate
	}
	return nil
}

func (m *NaipProperties) GetUsgsid() string {
	if m != nil {
		return m.Usgsid
	}
	return ""
}

type NaipRequest struct {
	SrcImageDate         *TimestampField `protobuf:"bytes,1,opt,name=src_image_date,json=srcImageDate,proto3" json:"src_image_date,omitempty"`
	Usgsid               *StringField    `protobuf:"bytes,2,opt,name=usgsid,proto3" json:"usgsid,omitempty"`
	XXX_NoUnkeyedLiteral struct{}        `json:"-"`
	XXX_unrecognized     []byte          `json:"-"`
	XXX_sizecache        int32           `json:"-"`
}

func (m *NaipRequest) Reset()         { *m = NaipRequest{} }
func (m *NaipRequest) String() string { return proto.CompactTextString(m) }
func (*NaipRequest) ProtoMessage()    {}
func (*NaipRequest) Descriptor() ([]byte, []int) {
	return fileDescriptor_naip_9c02332488f8f0e7, []int{1}
}
func (m *NaipRequest) XXX_Unmarshal(b []byte) error {
	return xxx_messageInfo_NaipRequest.Unmarshal(m, b)
}
func (m *NaipRequest) XXX_Marshal(b []byte, deterministic bool) ([]byte, error) {
	return xxx_messageInfo_NaipRequest.Marshal(b, m, deterministic)
}
func (dst *NaipRequest) XXX_Merge(src proto.Message) {
	xxx_messageInfo_NaipRequest.Merge(dst, src)
}
func (m *NaipRequest) XXX_Size() int {
	return xxx_messageInfo_NaipRequest.Size(m)
}
func (m *NaipRequest) XXX_DiscardUnknown() {
	xxx_messageInfo_NaipRequest.DiscardUnknown(m)
}

var xxx_messageInfo_NaipRequest proto.InternalMessageInfo

func (m *NaipRequest) GetSrcImageDate() *TimestampField {
	if m != nil {
		return m.SrcImageDate
	}
	return nil
}

func (m *NaipRequest) GetUsgsid() *StringField {
	if m != nil {
		return m.Usgsid
	}
	return nil
}

func init() {
	proto.RegisterType((*NaipProperties)(nil), "epl.protobuf.NaipProperties")
	proto.RegisterType((*NaipRequest)(nil), "epl.protobuf.NaipRequest")
}

func init() { proto.RegisterFile("epl/protobuf/naip.proto", fileDescriptor_naip_9c02332488f8f0e7) }

var fileDescriptor_naip_9c02332488f8f0e7 = []byte{
	// 280 bytes of a gzipped FileDescriptorProto
	0x1f, 0x8b, 0x08, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0xff, 0x54, 0xd0, 0xcd, 0x4a, 0xc3, 0x40,
	0x10, 0x07, 0x70, 0x52, 0xa4, 0xd0, 0x6d, 0x28, 0x92, 0x83, 0xc6, 0x20, 0x58, 0x7a, 0x2a, 0x88,
	0xbb, 0x58, 0xaf, 0x9e, 0x82, 0x08, 0x5e, 0x4a, 0x48, 0x3d, 0x79, 0x29, 0x9b, 0x64, 0x3a, 0x2e,
	0x64, 0xb3, 0xdb, 0xfd, 0x38, 0x78, 0xf7, 0x49, 0x7c, 0x52, 0x31, 0x49, 0x6d, 0x72, 0x9c, 0x9d,
	0xff, 0xec, 0x8f, 0x19, 0x72, 0x0d, 0xba, 0x66, 0xda, 0x28, 0xa7, 0x0a, 0x7f, 0x60, 0x0d, 0x17,
	0x9a, 0xb6, 0x55, 0x14, 0x82, 0xae, 0xe9, 0xa9, 0x91, 0xdc, 0xa1, 0x52, 0x58, 0xc3, 0x39, 0xe9,
	0x84, 0x04, 0xeb, 0xb8, 0xec, 0xe3, 0x49, 0x3c, 0xfa, 0xe7, 0xe8, 0xc1, 0x7c, 0x75, 0x9d, 0xd5,
	0x81, 0x2c, 0xb6, 0x5c, 0xe8, 0xcc, 0x28, 0x0d, 0xc6, 0x09, 0xb0, 0xd1, 0x33, 0x09, 0xad, 0x29,
	0xf7, 0x42, 0xe2, 0xbe, 0xe2, 0x0e, 0xe2, 0x60, 0x19, 0xac, 0xe7, 0x9b, 0x84, 0x76, 0xc6, 0x3f,
	0x4a, 0xdf, 0x4f, 0x46, 0x4e, 0xac, 0x29, 0xdf, 0x24, 0xbe, 0x70, 0x07, 0xd1, 0x15, 0x99, 0x7a,
	0x8b, 0x56, 0x54, 0xf1, 0x64, 0x19, 0xac, 0x67, 0x79, 0x5f, 0xad, 0xbe, 0x03, 0x32, 0xff, 0x83,
	0x72, 0x38, 0x7a, 0xb0, 0x2e, 0x4a, 0xc9, 0xa2, 0x53, 0x38, 0xc2, 0xd0, 0xb9, 0xa5, 0xc3, 0xcd,
	0xce, 0xc8, 0xab, 0x80, 0xba, 0xca, 0xc3, 0x56, 0xe2, 0x08, 0xad, 0xf5, 0x38, 0xb2, 0xe6, 0x9b,
	0x9b, 0xf1, 0xec, 0xce, 0x19, 0xd1, 0x60, 0x37, 0xd8, 0x07, 0xd3, 0x1d, 0xb9, 0x2c, 0x95, 0x1c,
	0xe5, 0xd2, 0x59, 0x7f, 0x00, 0xa7, 0xb2, 0xe0, 0xe3, 0x1e, 0x85, 0xfb, 0xf4, 0x05, 0x2d, 0x95,
	0x64, 0x08, 0xea, 0x01, 0x8d, 0x2e, 0x19, 0xd7, 0x82, 0xa1, 0xaa, 0x79, 0x83, 0x6c, 0x78, 0xc8,
	0x9f, 0xc9, 0xc5, 0x36, 0xcb, 0xd2, 0x62, 0xda, 0x3e, 0x3c, 0xfd, 0x06, 0x00, 0x00, 0xff, 0xff,
	0xef, 0x2e, 0x77, 0x84, 0xae, 0x01, 0x00, 0x00,
}
