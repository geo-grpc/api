// This file is generated. Do not edit
// @generated

// https://github.com/Manishearth/rust-clippy/issues/702
#![allow(unknown_lints)]
#![allow(clippy::all)]

#![cfg_attr(rustfmt, rustfmt_skip)]

#![allow(box_pointers)]
#![allow(dead_code)]
#![allow(missing_docs)]
#![allow(non_camel_case_types)]
#![allow(non_snake_case)]
#![allow(non_upper_case_globals)]
#![allow(trivial_casts)]
#![allow(unsafe_code)]
#![allow(unused_imports)]
#![allow(unused_results)]

const METHOD_GEOMETRY_SERVICE_OPERATE: ::grpcio::Method<super::geometry::GeometryRequest, super::geometry::GeometryResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Unary,
    name: "/epl.protobuf.GeometryService/Operate",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_GEOMETRY_SERVICE_OPERATE_BI_STREAM: ::grpcio::Method<super::geometry::GeometryRequest, super::geometry::GeometryResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Duplex,
    name: "/epl.protobuf.GeometryService/OperateBiStream",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_GEOMETRY_SERVICE_OPERATE_BI_STREAM_FLOW: ::grpcio::Method<super::geometry::GeometryRequest, super::geometry::GeometryResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Duplex,
    name: "/epl.protobuf.GeometryService/OperateBiStreamFlow",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_GEOMETRY_SERVICE_OPERATE_SERVER_STREAM: ::grpcio::Method<super::geometry::GeometryRequest, super::geometry::GeometryResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::ServerStreaming,
    name: "/epl.protobuf.GeometryService/OperateServerStream",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_GEOMETRY_SERVICE_OPERATE_CLIENT_STREAM: ::grpcio::Method<super::geometry::GeometryRequest, super::geometry::GeometryResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::ClientStreaming,
    name: "/epl.protobuf.GeometryService/OperateClientStream",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_GEOMETRY_SERVICE_FILE_OPERATE_BI_STREAM_FLOW: ::grpcio::Method<super::geometry::FileRequestChunk, super::geometry::GeometryResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Duplex,
    name: "/epl.protobuf.GeometryService/FileOperateBiStreamFlow",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

#[derive(Clone)]
pub struct GeometryServiceClient {
    client: ::grpcio::Client,
}

impl GeometryServiceClient {
    pub fn new(channel: ::grpcio::Channel) -> Self {
        GeometryServiceClient {
            client: ::grpcio::Client::new(channel),
        }
    }

    pub fn operate_opt(&self, req: &super::geometry::GeometryRequest, opt: ::grpcio::CallOption) -> ::grpcio::Result<super::geometry::GeometryResponse> {
        self.client.unary_call(&METHOD_GEOMETRY_SERVICE_OPERATE, req, opt)
    }

    pub fn operate(&self, req: &super::geometry::GeometryRequest) -> ::grpcio::Result<super::geometry::GeometryResponse> {
        self.operate_opt(req, ::grpcio::CallOption::default())
    }

    pub fn operate_async_opt(&self, req: &super::geometry::GeometryRequest, opt: ::grpcio::CallOption) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::geometry::GeometryResponse>> {
        self.client.unary_call_async(&METHOD_GEOMETRY_SERVICE_OPERATE, req, opt)
    }

    pub fn operate_async(&self, req: &super::geometry::GeometryRequest) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::geometry::GeometryResponse>> {
        self.operate_async_opt(req, ::grpcio::CallOption::default())
    }

    pub fn operate_bi_stream_opt(&self, opt: ::grpcio::CallOption) -> ::grpcio::Result<(::grpcio::ClientDuplexSender<super::geometry::GeometryRequest>, ::grpcio::ClientDuplexReceiver<super::geometry::GeometryResponse>)> {
        self.client.duplex_streaming(&METHOD_GEOMETRY_SERVICE_OPERATE_BI_STREAM, opt)
    }

    pub fn operate_bi_stream(&self) -> ::grpcio::Result<(::grpcio::ClientDuplexSender<super::geometry::GeometryRequest>, ::grpcio::ClientDuplexReceiver<super::geometry::GeometryResponse>)> {
        self.operate_bi_stream_opt(::grpcio::CallOption::default())
    }

    pub fn operate_bi_stream_flow_opt(&self, opt: ::grpcio::CallOption) -> ::grpcio::Result<(::grpcio::ClientDuplexSender<super::geometry::GeometryRequest>, ::grpcio::ClientDuplexReceiver<super::geometry::GeometryResponse>)> {
        self.client.duplex_streaming(&METHOD_GEOMETRY_SERVICE_OPERATE_BI_STREAM_FLOW, opt)
    }

    pub fn operate_bi_stream_flow(&self) -> ::grpcio::Result<(::grpcio::ClientDuplexSender<super::geometry::GeometryRequest>, ::grpcio::ClientDuplexReceiver<super::geometry::GeometryResponse>)> {
        self.operate_bi_stream_flow_opt(::grpcio::CallOption::default())
    }

    pub fn operate_server_stream_opt(&self, req: &super::geometry::GeometryRequest, opt: ::grpcio::CallOption) -> ::grpcio::Result<::grpcio::ClientSStreamReceiver<super::geometry::GeometryResponse>> {
        self.client.server_streaming(&METHOD_GEOMETRY_SERVICE_OPERATE_SERVER_STREAM, req, opt)
    }

    pub fn operate_server_stream(&self, req: &super::geometry::GeometryRequest) -> ::grpcio::Result<::grpcio::ClientSStreamReceiver<super::geometry::GeometryResponse>> {
        self.operate_server_stream_opt(req, ::grpcio::CallOption::default())
    }

    pub fn operate_client_stream_opt(&self, opt: ::grpcio::CallOption) -> ::grpcio::Result<(::grpcio::ClientCStreamSender<super::geometry::GeometryRequest>, ::grpcio::ClientCStreamReceiver<super::geometry::GeometryResponse>)> {
        self.client.client_streaming(&METHOD_GEOMETRY_SERVICE_OPERATE_CLIENT_STREAM, opt)
    }

    pub fn operate_client_stream(&self) -> ::grpcio::Result<(::grpcio::ClientCStreamSender<super::geometry::GeometryRequest>, ::grpcio::ClientCStreamReceiver<super::geometry::GeometryResponse>)> {
        self.operate_client_stream_opt(::grpcio::CallOption::default())
    }

    pub fn file_operate_bi_stream_flow_opt(&self, opt: ::grpcio::CallOption) -> ::grpcio::Result<(::grpcio::ClientDuplexSender<super::geometry::FileRequestChunk>, ::grpcio::ClientDuplexReceiver<super::geometry::GeometryResponse>)> {
        self.client.duplex_streaming(&METHOD_GEOMETRY_SERVICE_FILE_OPERATE_BI_STREAM_FLOW, opt)
    }

    pub fn file_operate_bi_stream_flow(&self) -> ::grpcio::Result<(::grpcio::ClientDuplexSender<super::geometry::FileRequestChunk>, ::grpcio::ClientDuplexReceiver<super::geometry::GeometryResponse>)> {
        self.file_operate_bi_stream_flow_opt(::grpcio::CallOption::default())
    }
    pub fn spawn<F>(&self, f: F) where F: ::futures::Future<Item = (), Error = ()> + Send + 'static {
        self.client.spawn(f)
    }
}

pub trait GeometryService {
    fn operate(&mut self, ctx: ::grpcio::RpcContext, req: super::geometry::GeometryRequest, sink: ::grpcio::UnarySink<super::geometry::GeometryResponse>);
    fn operate_bi_stream(&mut self, ctx: ::grpcio::RpcContext, stream: ::grpcio::RequestStream<super::geometry::GeometryRequest>, sink: ::grpcio::DuplexSink<super::geometry::GeometryResponse>);
    fn operate_bi_stream_flow(&mut self, ctx: ::grpcio::RpcContext, stream: ::grpcio::RequestStream<super::geometry::GeometryRequest>, sink: ::grpcio::DuplexSink<super::geometry::GeometryResponse>);
    fn operate_server_stream(&mut self, ctx: ::grpcio::RpcContext, req: super::geometry::GeometryRequest, sink: ::grpcio::ServerStreamingSink<super::geometry::GeometryResponse>);
    fn operate_client_stream(&mut self, ctx: ::grpcio::RpcContext, stream: ::grpcio::RequestStream<super::geometry::GeometryRequest>, sink: ::grpcio::ClientStreamingSink<super::geometry::GeometryResponse>);
    fn file_operate_bi_stream_flow(&mut self, ctx: ::grpcio::RpcContext, stream: ::grpcio::RequestStream<super::geometry::FileRequestChunk>, sink: ::grpcio::DuplexSink<super::geometry::GeometryResponse>);
}

pub fn create_geometry_service<S: GeometryService + Send + Clone + 'static>(s: S) -> ::grpcio::Service {
    let mut builder = ::grpcio::ServiceBuilder::new();
    let mut instance = s.clone();
    builder = builder.add_unary_handler(&METHOD_GEOMETRY_SERVICE_OPERATE, move |ctx, req, resp| {
        instance.operate(ctx, req, resp)
    });
    let mut instance = s.clone();
    builder = builder.add_duplex_streaming_handler(&METHOD_GEOMETRY_SERVICE_OPERATE_BI_STREAM, move |ctx, req, resp| {
        instance.operate_bi_stream(ctx, req, resp)
    });
    let mut instance = s.clone();
    builder = builder.add_duplex_streaming_handler(&METHOD_GEOMETRY_SERVICE_OPERATE_BI_STREAM_FLOW, move |ctx, req, resp| {
        instance.operate_bi_stream_flow(ctx, req, resp)
    });
    let mut instance = s.clone();
    builder = builder.add_server_streaming_handler(&METHOD_GEOMETRY_SERVICE_OPERATE_SERVER_STREAM, move |ctx, req, resp| {
        instance.operate_server_stream(ctx, req, resp)
    });
    let mut instance = s.clone();
    builder = builder.add_client_streaming_handler(&METHOD_GEOMETRY_SERVICE_OPERATE_CLIENT_STREAM, move |ctx, req, resp| {
        instance.operate_client_stream(ctx, req, resp)
    });
    let mut instance = s;
    builder = builder.add_duplex_streaming_handler(&METHOD_GEOMETRY_SERVICE_FILE_OPERATE_BI_STREAM_FLOW, move |ctx, req, resp| {
        instance.file_operate_bi_stream_flow(ctx, req, resp)
    });
    builder.build()
}
