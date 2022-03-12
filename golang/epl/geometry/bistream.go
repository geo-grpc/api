package geometry

import (
	"context"
	eplpbv1 "github.com/geo-grpc/api/golang/epl/protobuf/v1"
	"github.com/twpayne/go-geom"
	"io"
)

type Stream struct {
	geometryRequest *eplpbv1.GeometryRequest
	leftData *eplpbv1.GeometryData
	rightData *eplpbv1.GeometryData
	leftChan chan geom.T
	rightChan chan geom.T
	err error
}

type StreamResult struct {
	G geom.T
	Err error
}

func InitStream(leftChan chan geom.T, rightChan chan geom.T) *Stream {
	chain := &Stream{
		leftData:        &eplpbv1.GeometryData{},
		leftChan:        leftChan,
	}
	chain.geometryRequest = &eplpbv1.GeometryRequest{}
	chain.geometryRequest.SetLeftGeometry(chain.leftData)

	if rightChan != nil {
		chain.rightData = &eplpbv1.GeometryData{}
		chain.rightChan = rightChan
		chain.geometryRequest.SetRightGeometry(chain.rightData)
	}

	return chain
}

func (b *Stream) Execute() <- chan StreamResult {
	out := make(chan StreamResult)
	streamClient, err := getInstance().ClientV1.OperateBiStream(context.Background())
	if err != nil {
		res := StreamResult{Err:err}
		out <- res
		return out
	}

	go func() {
		for {
			result, err := streamClient.Recv()
			if err == io.EOF {
				close(out)
				return
			} else if err != nil {
				res := StreamResult{Err:err}
				out <- res
				return
			}
			gOut, err := GeomPbToGeom(result.GetGeometry())
			if err != nil {
				res := StreamResult{Err:err}
				out <- res
				return
			}
			res := StreamResult{G:gOut}
			out <- res
		}
	}()

	for g := range b.leftChan {
		b.leftData, err = GeomToGeomPb(g, b.leftData)
		b.geometryRequest.SetLeftGeometry(b.leftData)
		err := streamClient.Send(b.geometryRequest)
		if err != nil && err != io.EOF {
			res := StreamResult{Err:err}
			out <- res
			close(out)
			return out
		}
	}
	err = streamClient.CloseSend()
	if err != nil {
		res := StreamResult{Err:err}
		out <- res
		return out
	}

	return out
}

func (b *Stream) Buffer(distance float64) *Stream {
	params := &eplpbv1.Params_Buffer{Distance:distance}
	request := &eplpbv1.GeometryRequest{
		Operator:             eplpbv1.OperatorType_BUFFER,
		Params:               &eplpbv1.GeometryRequest_BufferParams{BufferParams:params},
	}
	b.geometryRequest = request
	return b
}

func (b *Stream) Project(epsg int32) *Stream {
	return nil
}

func (b *Stream) Union() *Unary {
	return nil
}


