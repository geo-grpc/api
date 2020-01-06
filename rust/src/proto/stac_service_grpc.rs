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

const METHOD_STAC_SERVICE_SEARCH: ::grpcio::Method<super::stac::StacRequest, super::stac::StacItem> = ::grpcio::Method {
    ty: ::grpcio::MethodType::ServerStreaming,
    name: "/epl.protobuf.StacService/Search",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_STAC_SERVICE_INSERT: ::grpcio::Method<super::stac::StacItem, super::stac::StacDbResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Duplex,
    name: "/epl.protobuf.StacService/Insert",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_STAC_SERVICE_UPDATE: ::grpcio::Method<super::stac::StacItem, super::stac::StacDbResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Duplex,
    name: "/epl.protobuf.StacService/Update",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_STAC_SERVICE_COUNT: ::grpcio::Method<super::stac::StacRequest, super::stac::StacDbResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Unary,
    name: "/epl.protobuf.StacService/Count",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_STAC_SERVICE_DELETE_ONE: ::grpcio::Method<super::stac::StacItem, super::stac::StacDbResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Unary,
    name: "/epl.protobuf.StacService/DeleteOne",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_STAC_SERVICE_SEARCH_ONE: ::grpcio::Method<super::stac::StacRequest, super::stac::StacItem> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Unary,
    name: "/epl.protobuf.StacService/SearchOne",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_STAC_SERVICE_INSERT_ONE: ::grpcio::Method<super::stac::StacItem, super::stac::StacDbResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Unary,
    name: "/epl.protobuf.StacService/InsertOne",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

const METHOD_STAC_SERVICE_UPDATE_ONE: ::grpcio::Method<super::stac::StacItem, super::stac::StacDbResponse> = ::grpcio::Method {
    ty: ::grpcio::MethodType::Unary,
    name: "/epl.protobuf.StacService/UpdateOne",
    req_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
    resp_mar: ::grpcio::Marshaller { ser: ::grpcio::pb_ser, de: ::grpcio::pb_de },
};

#[derive(Clone)]
pub struct StacServiceClient {
    client: ::grpcio::Client,
}

impl StacServiceClient {
    pub fn new(channel: ::grpcio::Channel) -> Self {
        StacServiceClient {
            client: ::grpcio::Client::new(channel),
        }
    }

    pub fn search_opt(&self, req: &super::stac::StacRequest, opt: ::grpcio::CallOption) -> ::grpcio::Result<::grpcio::ClientSStreamReceiver<super::stac::StacItem>> {
        self.client.server_streaming(&METHOD_STAC_SERVICE_SEARCH, req, opt)
    }

    pub fn search(&self, req: &super::stac::StacRequest) -> ::grpcio::Result<::grpcio::ClientSStreamReceiver<super::stac::StacItem>> {
        self.search_opt(req, ::grpcio::CallOption::default())
    }

    pub fn insert_opt(&self, opt: ::grpcio::CallOption) -> ::grpcio::Result<(::grpcio::ClientDuplexSender<super::stac::StacItem>, ::grpcio::ClientDuplexReceiver<super::stac::StacDbResponse>)> {
        self.client.duplex_streaming(&METHOD_STAC_SERVICE_INSERT, opt)
    }

    pub fn insert(&self) -> ::grpcio::Result<(::grpcio::ClientDuplexSender<super::stac::StacItem>, ::grpcio::ClientDuplexReceiver<super::stac::StacDbResponse>)> {
        self.insert_opt(::grpcio::CallOption::default())
    }

    pub fn update_opt(&self, opt: ::grpcio::CallOption) -> ::grpcio::Result<(::grpcio::ClientDuplexSender<super::stac::StacItem>, ::grpcio::ClientDuplexReceiver<super::stac::StacDbResponse>)> {
        self.client.duplex_streaming(&METHOD_STAC_SERVICE_UPDATE, opt)
    }

    pub fn update(&self) -> ::grpcio::Result<(::grpcio::ClientDuplexSender<super::stac::StacItem>, ::grpcio::ClientDuplexReceiver<super::stac::StacDbResponse>)> {
        self.update_opt(::grpcio::CallOption::default())
    }

    pub fn count_opt(&self, req: &super::stac::StacRequest, opt: ::grpcio::CallOption) -> ::grpcio::Result<super::stac::StacDbResponse> {
        self.client.unary_call(&METHOD_STAC_SERVICE_COUNT, req, opt)
    }

    pub fn count(&self, req: &super::stac::StacRequest) -> ::grpcio::Result<super::stac::StacDbResponse> {
        self.count_opt(req, ::grpcio::CallOption::default())
    }

    pub fn count_async_opt(&self, req: &super::stac::StacRequest, opt: ::grpcio::CallOption) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::stac::StacDbResponse>> {
        self.client.unary_call_async(&METHOD_STAC_SERVICE_COUNT, req, opt)
    }

    pub fn count_async(&self, req: &super::stac::StacRequest) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::stac::StacDbResponse>> {
        self.count_async_opt(req, ::grpcio::CallOption::default())
    }

    pub fn delete_one_opt(&self, req: &super::stac::StacItem, opt: ::grpcio::CallOption) -> ::grpcio::Result<super::stac::StacDbResponse> {
        self.client.unary_call(&METHOD_STAC_SERVICE_DELETE_ONE, req, opt)
    }

    pub fn delete_one(&self, req: &super::stac::StacItem) -> ::grpcio::Result<super::stac::StacDbResponse> {
        self.delete_one_opt(req, ::grpcio::CallOption::default())
    }

    pub fn delete_one_async_opt(&self, req: &super::stac::StacItem, opt: ::grpcio::CallOption) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::stac::StacDbResponse>> {
        self.client.unary_call_async(&METHOD_STAC_SERVICE_DELETE_ONE, req, opt)
    }

    pub fn delete_one_async(&self, req: &super::stac::StacItem) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::stac::StacDbResponse>> {
        self.delete_one_async_opt(req, ::grpcio::CallOption::default())
    }

    pub fn search_one_opt(&self, req: &super::stac::StacRequest, opt: ::grpcio::CallOption) -> ::grpcio::Result<super::stac::StacItem> {
        self.client.unary_call(&METHOD_STAC_SERVICE_SEARCH_ONE, req, opt)
    }

    pub fn search_one(&self, req: &super::stac::StacRequest) -> ::grpcio::Result<super::stac::StacItem> {
        self.search_one_opt(req, ::grpcio::CallOption::default())
    }

    pub fn search_one_async_opt(&self, req: &super::stac::StacRequest, opt: ::grpcio::CallOption) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::stac::StacItem>> {
        self.client.unary_call_async(&METHOD_STAC_SERVICE_SEARCH_ONE, req, opt)
    }

    pub fn search_one_async(&self, req: &super::stac::StacRequest) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::stac::StacItem>> {
        self.search_one_async_opt(req, ::grpcio::CallOption::default())
    }

    pub fn insert_one_opt(&self, req: &super::stac::StacItem, opt: ::grpcio::CallOption) -> ::grpcio::Result<super::stac::StacDbResponse> {
        self.client.unary_call(&METHOD_STAC_SERVICE_INSERT_ONE, req, opt)
    }

    pub fn insert_one(&self, req: &super::stac::StacItem) -> ::grpcio::Result<super::stac::StacDbResponse> {
        self.insert_one_opt(req, ::grpcio::CallOption::default())
    }

    pub fn insert_one_async_opt(&self, req: &super::stac::StacItem, opt: ::grpcio::CallOption) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::stac::StacDbResponse>> {
        self.client.unary_call_async(&METHOD_STAC_SERVICE_INSERT_ONE, req, opt)
    }

    pub fn insert_one_async(&self, req: &super::stac::StacItem) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::stac::StacDbResponse>> {
        self.insert_one_async_opt(req, ::grpcio::CallOption::default())
    }

    pub fn update_one_opt(&self, req: &super::stac::StacItem, opt: ::grpcio::CallOption) -> ::grpcio::Result<super::stac::StacDbResponse> {
        self.client.unary_call(&METHOD_STAC_SERVICE_UPDATE_ONE, req, opt)
    }

    pub fn update_one(&self, req: &super::stac::StacItem) -> ::grpcio::Result<super::stac::StacDbResponse> {
        self.update_one_opt(req, ::grpcio::CallOption::default())
    }

    pub fn update_one_async_opt(&self, req: &super::stac::StacItem, opt: ::grpcio::CallOption) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::stac::StacDbResponse>> {
        self.client.unary_call_async(&METHOD_STAC_SERVICE_UPDATE_ONE, req, opt)
    }

    pub fn update_one_async(&self, req: &super::stac::StacItem) -> ::grpcio::Result<::grpcio::ClientUnaryReceiver<super::stac::StacDbResponse>> {
        self.update_one_async_opt(req, ::grpcio::CallOption::default())
    }
    pub fn spawn<F>(&self, f: F) where F: ::futures::Future<Item = (), Error = ()> + Send + 'static {
        self.client.spawn(f)
    }
}

pub trait StacService {
    fn search(&mut self, ctx: ::grpcio::RpcContext, req: super::stac::StacRequest, sink: ::grpcio::ServerStreamingSink<super::stac::StacItem>);
    fn insert(&mut self, ctx: ::grpcio::RpcContext, stream: ::grpcio::RequestStream<super::stac::StacItem>, sink: ::grpcio::DuplexSink<super::stac::StacDbResponse>);
    fn update(&mut self, ctx: ::grpcio::RpcContext, stream: ::grpcio::RequestStream<super::stac::StacItem>, sink: ::grpcio::DuplexSink<super::stac::StacDbResponse>);
    fn count(&mut self, ctx: ::grpcio::RpcContext, req: super::stac::StacRequest, sink: ::grpcio::UnarySink<super::stac::StacDbResponse>);
    fn delete_one(&mut self, ctx: ::grpcio::RpcContext, req: super::stac::StacItem, sink: ::grpcio::UnarySink<super::stac::StacDbResponse>);
    fn search_one(&mut self, ctx: ::grpcio::RpcContext, req: super::stac::StacRequest, sink: ::grpcio::UnarySink<super::stac::StacItem>);
    fn insert_one(&mut self, ctx: ::grpcio::RpcContext, req: super::stac::StacItem, sink: ::grpcio::UnarySink<super::stac::StacDbResponse>);
    fn update_one(&mut self, ctx: ::grpcio::RpcContext, req: super::stac::StacItem, sink: ::grpcio::UnarySink<super::stac::StacDbResponse>);
}

pub fn create_stac_service<S: StacService + Send + Clone + 'static>(s: S) -> ::grpcio::Service {
    let mut builder = ::grpcio::ServiceBuilder::new();
    let mut instance = s.clone();
    builder = builder.add_server_streaming_handler(&METHOD_STAC_SERVICE_SEARCH, move |ctx, req, resp| {
        instance.search(ctx, req, resp)
    });
    let mut instance = s.clone();
    builder = builder.add_duplex_streaming_handler(&METHOD_STAC_SERVICE_INSERT, move |ctx, req, resp| {
        instance.insert(ctx, req, resp)
    });
    let mut instance = s.clone();
    builder = builder.add_duplex_streaming_handler(&METHOD_STAC_SERVICE_UPDATE, move |ctx, req, resp| {
        instance.update(ctx, req, resp)
    });
    let mut instance = s.clone();
    builder = builder.add_unary_handler(&METHOD_STAC_SERVICE_COUNT, move |ctx, req, resp| {
        instance.count(ctx, req, resp)
    });
    let mut instance = s.clone();
    builder = builder.add_unary_handler(&METHOD_STAC_SERVICE_DELETE_ONE, move |ctx, req, resp| {
        instance.delete_one(ctx, req, resp)
    });
    let mut instance = s.clone();
    builder = builder.add_unary_handler(&METHOD_STAC_SERVICE_SEARCH_ONE, move |ctx, req, resp| {
        instance.search_one(ctx, req, resp)
    });
    let mut instance = s.clone();
    builder = builder.add_unary_handler(&METHOD_STAC_SERVICE_INSERT_ONE, move |ctx, req, resp| {
        instance.insert_one(ctx, req, resp)
    });
    let mut instance = s;
    builder = builder.add_unary_handler(&METHOD_STAC_SERVICE_UPDATE_ONE, move |ctx, req, resp| {
        instance.update_one(ctx, req, resp)
    });
    builder.build()
}
