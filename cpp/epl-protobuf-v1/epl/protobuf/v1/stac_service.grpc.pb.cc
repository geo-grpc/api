// Generated by the gRPC C++ plugin.
// If you make any local change, they will be lost.
// source: epl/protobuf/v1/stac_service.proto

#include "epl/protobuf/v1/stac_service.pb.h"
#include "epl/protobuf/v1/stac_service.grpc.pb.h"

#include <functional>
#include <grpcpp/impl/codegen/async_stream.h>
#include <grpcpp/impl/codegen/async_unary_call.h>
#include <grpcpp/impl/codegen/channel_interface.h>
#include <grpcpp/impl/codegen/client_unary_call.h>
#include <grpcpp/impl/codegen/client_callback.h>
#include <grpcpp/impl/codegen/message_allocator.h>
#include <grpcpp/impl/codegen/method_handler.h>
#include <grpcpp/impl/codegen/rpc_service_method.h>
#include <grpcpp/impl/codegen/server_callback.h>
#include <grpcpp/impl/codegen/server_callback_handlers.h>
#include <grpcpp/impl/codegen/server_context.h>
#include <grpcpp/impl/codegen/service_type.h>
#include <grpcpp/impl/codegen/sync_stream.h>
namespace epl {
namespace protobuf {
namespace v1 {

static const char* StacService_method_names[] = {
  "/epl.protobuf.v1.StacService/SearchItems",
  "/epl.protobuf.v1.StacService/SearchCollections",
  "/epl.protobuf.v1.StacService/InsertItems",
  "/epl.protobuf.v1.StacService/UpdateItems",
  "/epl.protobuf.v1.StacService/CountItems",
  "/epl.protobuf.v1.StacService/DeleteOneItem",
  "/epl.protobuf.v1.StacService/SearchOneItem",
  "/epl.protobuf.v1.StacService/InsertOneItem",
  "/epl.protobuf.v1.StacService/InsertOneCollection",
  "/epl.protobuf.v1.StacService/UpdateOneItem",
};

std::unique_ptr< StacService::Stub> StacService::NewStub(const std::shared_ptr< ::grpc::ChannelInterface>& channel, const ::grpc::StubOptions& options) {
  (void)options;
  std::unique_ptr< StacService::Stub> stub(new StacService::Stub(channel));
  return stub;
}

StacService::Stub::Stub(const std::shared_ptr< ::grpc::ChannelInterface>& channel)
  : channel_(channel), rpcmethod_SearchItems_(StacService_method_names[0], ::grpc::internal::RpcMethod::SERVER_STREAMING, channel)
  , rpcmethod_SearchCollections_(StacService_method_names[1], ::grpc::internal::RpcMethod::SERVER_STREAMING, channel)
  , rpcmethod_InsertItems_(StacService_method_names[2], ::grpc::internal::RpcMethod::BIDI_STREAMING, channel)
  , rpcmethod_UpdateItems_(StacService_method_names[3], ::grpc::internal::RpcMethod::BIDI_STREAMING, channel)
  , rpcmethod_CountItems_(StacService_method_names[4], ::grpc::internal::RpcMethod::NORMAL_RPC, channel)
  , rpcmethod_DeleteOneItem_(StacService_method_names[5], ::grpc::internal::RpcMethod::NORMAL_RPC, channel)
  , rpcmethod_SearchOneItem_(StacService_method_names[6], ::grpc::internal::RpcMethod::NORMAL_RPC, channel)
  , rpcmethod_InsertOneItem_(StacService_method_names[7], ::grpc::internal::RpcMethod::NORMAL_RPC, channel)
  , rpcmethod_InsertOneCollection_(StacService_method_names[8], ::grpc::internal::RpcMethod::NORMAL_RPC, channel)
  , rpcmethod_UpdateOneItem_(StacService_method_names[9], ::grpc::internal::RpcMethod::NORMAL_RPC, channel)
  {}

::grpc::ClientReader< ::epl::protobuf::v1::StacItem>* StacService::Stub::SearchItemsRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest& request) {
  return ::grpc::internal::ClientReaderFactory< ::epl::protobuf::v1::StacItem>::Create(channel_.get(), rpcmethod_SearchItems_, context, request);
}

void StacService::Stub::experimental_async::SearchItems(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest* request, ::grpc::experimental::ClientReadReactor< ::epl::protobuf::v1::StacItem>* reactor) {
  ::grpc::internal::ClientCallbackReaderFactory< ::epl::protobuf::v1::StacItem>::Create(stub_->channel_.get(), stub_->rpcmethod_SearchItems_, context, request, reactor);
}

::grpc::ClientAsyncReader< ::epl::protobuf::v1::StacItem>* StacService::Stub::AsyncSearchItemsRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest& request, ::grpc::CompletionQueue* cq, void* tag) {
  return ::grpc::internal::ClientAsyncReaderFactory< ::epl::protobuf::v1::StacItem>::Create(channel_.get(), cq, rpcmethod_SearchItems_, context, request, true, tag);
}

::grpc::ClientAsyncReader< ::epl::protobuf::v1::StacItem>* StacService::Stub::PrepareAsyncSearchItemsRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest& request, ::grpc::CompletionQueue* cq) {
  return ::grpc::internal::ClientAsyncReaderFactory< ::epl::protobuf::v1::StacItem>::Create(channel_.get(), cq, rpcmethod_SearchItems_, context, request, false, nullptr);
}

::grpc::ClientReader< ::epl::protobuf::v1::Collection>* StacService::Stub::SearchCollectionsRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::CollectionRequest& request) {
  return ::grpc::internal::ClientReaderFactory< ::epl::protobuf::v1::Collection>::Create(channel_.get(), rpcmethod_SearchCollections_, context, request);
}

void StacService::Stub::experimental_async::SearchCollections(::grpc::ClientContext* context, const ::epl::protobuf::v1::CollectionRequest* request, ::grpc::experimental::ClientReadReactor< ::epl::protobuf::v1::Collection>* reactor) {
  ::grpc::internal::ClientCallbackReaderFactory< ::epl::protobuf::v1::Collection>::Create(stub_->channel_.get(), stub_->rpcmethod_SearchCollections_, context, request, reactor);
}

::grpc::ClientAsyncReader< ::epl::protobuf::v1::Collection>* StacService::Stub::AsyncSearchCollectionsRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::CollectionRequest& request, ::grpc::CompletionQueue* cq, void* tag) {
  return ::grpc::internal::ClientAsyncReaderFactory< ::epl::protobuf::v1::Collection>::Create(channel_.get(), cq, rpcmethod_SearchCollections_, context, request, true, tag);
}

::grpc::ClientAsyncReader< ::epl::protobuf::v1::Collection>* StacService::Stub::PrepareAsyncSearchCollectionsRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::CollectionRequest& request, ::grpc::CompletionQueue* cq) {
  return ::grpc::internal::ClientAsyncReaderFactory< ::epl::protobuf::v1::Collection>::Create(channel_.get(), cq, rpcmethod_SearchCollections_, context, request, false, nullptr);
}

::grpc::ClientReaderWriter< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::InsertItemsRaw(::grpc::ClientContext* context) {
  return ::grpc::internal::ClientReaderWriterFactory< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>::Create(channel_.get(), rpcmethod_InsertItems_, context);
}

void StacService::Stub::experimental_async::InsertItems(::grpc::ClientContext* context, ::grpc::experimental::ClientBidiReactor< ::epl::protobuf::v1::StacItem,::epl::protobuf::v1::StacDbResponse>* reactor) {
  ::grpc::internal::ClientCallbackReaderWriterFactory< ::epl::protobuf::v1::StacItem,::epl::protobuf::v1::StacDbResponse>::Create(stub_->channel_.get(), stub_->rpcmethod_InsertItems_, context, reactor);
}

::grpc::ClientAsyncReaderWriter< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::AsyncInsertItemsRaw(::grpc::ClientContext* context, ::grpc::CompletionQueue* cq, void* tag) {
  return ::grpc::internal::ClientAsyncReaderWriterFactory< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>::Create(channel_.get(), cq, rpcmethod_InsertItems_, context, true, tag);
}

::grpc::ClientAsyncReaderWriter< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::PrepareAsyncInsertItemsRaw(::grpc::ClientContext* context, ::grpc::CompletionQueue* cq) {
  return ::grpc::internal::ClientAsyncReaderWriterFactory< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>::Create(channel_.get(), cq, rpcmethod_InsertItems_, context, false, nullptr);
}

::grpc::ClientReaderWriter< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::UpdateItemsRaw(::grpc::ClientContext* context) {
  return ::grpc::internal::ClientReaderWriterFactory< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>::Create(channel_.get(), rpcmethod_UpdateItems_, context);
}

void StacService::Stub::experimental_async::UpdateItems(::grpc::ClientContext* context, ::grpc::experimental::ClientBidiReactor< ::epl::protobuf::v1::StacItem,::epl::protobuf::v1::StacDbResponse>* reactor) {
  ::grpc::internal::ClientCallbackReaderWriterFactory< ::epl::protobuf::v1::StacItem,::epl::protobuf::v1::StacDbResponse>::Create(stub_->channel_.get(), stub_->rpcmethod_UpdateItems_, context, reactor);
}

::grpc::ClientAsyncReaderWriter< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::AsyncUpdateItemsRaw(::grpc::ClientContext* context, ::grpc::CompletionQueue* cq, void* tag) {
  return ::grpc::internal::ClientAsyncReaderWriterFactory< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>::Create(channel_.get(), cq, rpcmethod_UpdateItems_, context, true, tag);
}

::grpc::ClientAsyncReaderWriter< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::PrepareAsyncUpdateItemsRaw(::grpc::ClientContext* context, ::grpc::CompletionQueue* cq) {
  return ::grpc::internal::ClientAsyncReaderWriterFactory< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>::Create(channel_.get(), cq, rpcmethod_UpdateItems_, context, false, nullptr);
}

::grpc::Status StacService::Stub::CountItems(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest& request, ::epl::protobuf::v1::StacDbResponse* response) {
  return ::grpc::internal::BlockingUnaryCall< ::epl::protobuf::v1::StacRequest, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), rpcmethod_CountItems_, context, request, response);
}

void StacService::Stub::experimental_async::CountItems(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest* request, ::epl::protobuf::v1::StacDbResponse* response, std::function<void(::grpc::Status)> f) {
  ::grpc::internal::CallbackUnaryCall< ::epl::protobuf::v1::StacRequest, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_CountItems_, context, request, response, std::move(f));
}

void StacService::Stub::experimental_async::CountItems(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest* request, ::epl::protobuf::v1::StacDbResponse* response, ::grpc::experimental::ClientUnaryReactor* reactor) {
  ::grpc::internal::ClientCallbackUnaryFactory::Create< ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_CountItems_, context, request, response, reactor);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::PrepareAsyncCountItemsRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest& request, ::grpc::CompletionQueue* cq) {
  return ::grpc::internal::ClientAsyncResponseReaderHelper::Create< ::epl::protobuf::v1::StacDbResponse, ::epl::protobuf::v1::StacRequest, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), cq, rpcmethod_CountItems_, context, request);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::AsyncCountItemsRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest& request, ::grpc::CompletionQueue* cq) {
  auto* result =
    this->PrepareAsyncCountItemsRaw(context, request, cq);
  result->StartCall();
  return result;
}

::grpc::Status StacService::Stub::DeleteOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem& request, ::epl::protobuf::v1::StacDbResponse* response) {
  return ::grpc::internal::BlockingUnaryCall< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), rpcmethod_DeleteOneItem_, context, request, response);
}

void StacService::Stub::experimental_async::DeleteOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem* request, ::epl::protobuf::v1::StacDbResponse* response, std::function<void(::grpc::Status)> f) {
  ::grpc::internal::CallbackUnaryCall< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_DeleteOneItem_, context, request, response, std::move(f));
}

void StacService::Stub::experimental_async::DeleteOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem* request, ::epl::protobuf::v1::StacDbResponse* response, ::grpc::experimental::ClientUnaryReactor* reactor) {
  ::grpc::internal::ClientCallbackUnaryFactory::Create< ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_DeleteOneItem_, context, request, response, reactor);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::PrepareAsyncDeleteOneItemRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem& request, ::grpc::CompletionQueue* cq) {
  return ::grpc::internal::ClientAsyncResponseReaderHelper::Create< ::epl::protobuf::v1::StacDbResponse, ::epl::protobuf::v1::StacItem, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), cq, rpcmethod_DeleteOneItem_, context, request);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::AsyncDeleteOneItemRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem& request, ::grpc::CompletionQueue* cq) {
  auto* result =
    this->PrepareAsyncDeleteOneItemRaw(context, request, cq);
  result->StartCall();
  return result;
}

::grpc::Status StacService::Stub::SearchOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest& request, ::epl::protobuf::v1::StacItem* response) {
  return ::grpc::internal::BlockingUnaryCall< ::epl::protobuf::v1::StacRequest, ::epl::protobuf::v1::StacItem, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), rpcmethod_SearchOneItem_, context, request, response);
}

void StacService::Stub::experimental_async::SearchOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest* request, ::epl::protobuf::v1::StacItem* response, std::function<void(::grpc::Status)> f) {
  ::grpc::internal::CallbackUnaryCall< ::epl::protobuf::v1::StacRequest, ::epl::protobuf::v1::StacItem, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_SearchOneItem_, context, request, response, std::move(f));
}

void StacService::Stub::experimental_async::SearchOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest* request, ::epl::protobuf::v1::StacItem* response, ::grpc::experimental::ClientUnaryReactor* reactor) {
  ::grpc::internal::ClientCallbackUnaryFactory::Create< ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_SearchOneItem_, context, request, response, reactor);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacItem>* StacService::Stub::PrepareAsyncSearchOneItemRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest& request, ::grpc::CompletionQueue* cq) {
  return ::grpc::internal::ClientAsyncResponseReaderHelper::Create< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacRequest, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), cq, rpcmethod_SearchOneItem_, context, request);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacItem>* StacService::Stub::AsyncSearchOneItemRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacRequest& request, ::grpc::CompletionQueue* cq) {
  auto* result =
    this->PrepareAsyncSearchOneItemRaw(context, request, cq);
  result->StartCall();
  return result;
}

::grpc::Status StacService::Stub::InsertOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem& request, ::epl::protobuf::v1::StacDbResponse* response) {
  return ::grpc::internal::BlockingUnaryCall< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), rpcmethod_InsertOneItem_, context, request, response);
}

void StacService::Stub::experimental_async::InsertOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem* request, ::epl::protobuf::v1::StacDbResponse* response, std::function<void(::grpc::Status)> f) {
  ::grpc::internal::CallbackUnaryCall< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_InsertOneItem_, context, request, response, std::move(f));
}

void StacService::Stub::experimental_async::InsertOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem* request, ::epl::protobuf::v1::StacDbResponse* response, ::grpc::experimental::ClientUnaryReactor* reactor) {
  ::grpc::internal::ClientCallbackUnaryFactory::Create< ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_InsertOneItem_, context, request, response, reactor);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::PrepareAsyncInsertOneItemRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem& request, ::grpc::CompletionQueue* cq) {
  return ::grpc::internal::ClientAsyncResponseReaderHelper::Create< ::epl::protobuf::v1::StacDbResponse, ::epl::protobuf::v1::StacItem, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), cq, rpcmethod_InsertOneItem_, context, request);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::AsyncInsertOneItemRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem& request, ::grpc::CompletionQueue* cq) {
  auto* result =
    this->PrepareAsyncInsertOneItemRaw(context, request, cq);
  result->StartCall();
  return result;
}

::grpc::Status StacService::Stub::InsertOneCollection(::grpc::ClientContext* context, const ::epl::protobuf::v1::Collection& request, ::epl::protobuf::v1::StacDbResponse* response) {
  return ::grpc::internal::BlockingUnaryCall< ::epl::protobuf::v1::Collection, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), rpcmethod_InsertOneCollection_, context, request, response);
}

void StacService::Stub::experimental_async::InsertOneCollection(::grpc::ClientContext* context, const ::epl::protobuf::v1::Collection* request, ::epl::protobuf::v1::StacDbResponse* response, std::function<void(::grpc::Status)> f) {
  ::grpc::internal::CallbackUnaryCall< ::epl::protobuf::v1::Collection, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_InsertOneCollection_, context, request, response, std::move(f));
}

void StacService::Stub::experimental_async::InsertOneCollection(::grpc::ClientContext* context, const ::epl::protobuf::v1::Collection* request, ::epl::protobuf::v1::StacDbResponse* response, ::grpc::experimental::ClientUnaryReactor* reactor) {
  ::grpc::internal::ClientCallbackUnaryFactory::Create< ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_InsertOneCollection_, context, request, response, reactor);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::PrepareAsyncInsertOneCollectionRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::Collection& request, ::grpc::CompletionQueue* cq) {
  return ::grpc::internal::ClientAsyncResponseReaderHelper::Create< ::epl::protobuf::v1::StacDbResponse, ::epl::protobuf::v1::Collection, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), cq, rpcmethod_InsertOneCollection_, context, request);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::AsyncInsertOneCollectionRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::Collection& request, ::grpc::CompletionQueue* cq) {
  auto* result =
    this->PrepareAsyncInsertOneCollectionRaw(context, request, cq);
  result->StartCall();
  return result;
}

::grpc::Status StacService::Stub::UpdateOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem& request, ::epl::protobuf::v1::StacDbResponse* response) {
  return ::grpc::internal::BlockingUnaryCall< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), rpcmethod_UpdateOneItem_, context, request, response);
}

void StacService::Stub::experimental_async::UpdateOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem* request, ::epl::protobuf::v1::StacDbResponse* response, std::function<void(::grpc::Status)> f) {
  ::grpc::internal::CallbackUnaryCall< ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_UpdateOneItem_, context, request, response, std::move(f));
}

void StacService::Stub::experimental_async::UpdateOneItem(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem* request, ::epl::protobuf::v1::StacDbResponse* response, ::grpc::experimental::ClientUnaryReactor* reactor) {
  ::grpc::internal::ClientCallbackUnaryFactory::Create< ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(stub_->channel_.get(), stub_->rpcmethod_UpdateOneItem_, context, request, response, reactor);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::PrepareAsyncUpdateOneItemRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem& request, ::grpc::CompletionQueue* cq) {
  return ::grpc::internal::ClientAsyncResponseReaderHelper::Create< ::epl::protobuf::v1::StacDbResponse, ::epl::protobuf::v1::StacItem, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(channel_.get(), cq, rpcmethod_UpdateOneItem_, context, request);
}

::grpc::ClientAsyncResponseReader< ::epl::protobuf::v1::StacDbResponse>* StacService::Stub::AsyncUpdateOneItemRaw(::grpc::ClientContext* context, const ::epl::protobuf::v1::StacItem& request, ::grpc::CompletionQueue* cq) {
  auto* result =
    this->PrepareAsyncUpdateOneItemRaw(context, request, cq);
  result->StartCall();
  return result;
}

StacService::Service::Service() {
  AddMethod(new ::grpc::internal::RpcServiceMethod(
      StacService_method_names[0],
      ::grpc::internal::RpcMethod::SERVER_STREAMING,
      new ::grpc::internal::ServerStreamingHandler< StacService::Service, ::epl::protobuf::v1::StacRequest, ::epl::protobuf::v1::StacItem>(
          [](StacService::Service* service,
             ::grpc::ServerContext* ctx,
             const ::epl::protobuf::v1::StacRequest* req,
             ::grpc::ServerWriter<::epl::protobuf::v1::StacItem>* writer) {
               return service->SearchItems(ctx, req, writer);
             }, this)));
  AddMethod(new ::grpc::internal::RpcServiceMethod(
      StacService_method_names[1],
      ::grpc::internal::RpcMethod::SERVER_STREAMING,
      new ::grpc::internal::ServerStreamingHandler< StacService::Service, ::epl::protobuf::v1::CollectionRequest, ::epl::protobuf::v1::Collection>(
          [](StacService::Service* service,
             ::grpc::ServerContext* ctx,
             const ::epl::protobuf::v1::CollectionRequest* req,
             ::grpc::ServerWriter<::epl::protobuf::v1::Collection>* writer) {
               return service->SearchCollections(ctx, req, writer);
             }, this)));
  AddMethod(new ::grpc::internal::RpcServiceMethod(
      StacService_method_names[2],
      ::grpc::internal::RpcMethod::BIDI_STREAMING,
      new ::grpc::internal::BidiStreamingHandler< StacService::Service, ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>(
          [](StacService::Service* service,
             ::grpc::ServerContext* ctx,
             ::grpc::ServerReaderWriter<::epl::protobuf::v1::StacDbResponse,
             ::epl::protobuf::v1::StacItem>* stream) {
               return service->InsertItems(ctx, stream);
             }, this)));
  AddMethod(new ::grpc::internal::RpcServiceMethod(
      StacService_method_names[3],
      ::grpc::internal::RpcMethod::BIDI_STREAMING,
      new ::grpc::internal::BidiStreamingHandler< StacService::Service, ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse>(
          [](StacService::Service* service,
             ::grpc::ServerContext* ctx,
             ::grpc::ServerReaderWriter<::epl::protobuf::v1::StacDbResponse,
             ::epl::protobuf::v1::StacItem>* stream) {
               return service->UpdateItems(ctx, stream);
             }, this)));
  AddMethod(new ::grpc::internal::RpcServiceMethod(
      StacService_method_names[4],
      ::grpc::internal::RpcMethod::NORMAL_RPC,
      new ::grpc::internal::RpcMethodHandler< StacService::Service, ::epl::protobuf::v1::StacRequest, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(
          [](StacService::Service* service,
             ::grpc::ServerContext* ctx,
             const ::epl::protobuf::v1::StacRequest* req,
             ::epl::protobuf::v1::StacDbResponse* resp) {
               return service->CountItems(ctx, req, resp);
             }, this)));
  AddMethod(new ::grpc::internal::RpcServiceMethod(
      StacService_method_names[5],
      ::grpc::internal::RpcMethod::NORMAL_RPC,
      new ::grpc::internal::RpcMethodHandler< StacService::Service, ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(
          [](StacService::Service* service,
             ::grpc::ServerContext* ctx,
             const ::epl::protobuf::v1::StacItem* req,
             ::epl::protobuf::v1::StacDbResponse* resp) {
               return service->DeleteOneItem(ctx, req, resp);
             }, this)));
  AddMethod(new ::grpc::internal::RpcServiceMethod(
      StacService_method_names[6],
      ::grpc::internal::RpcMethod::NORMAL_RPC,
      new ::grpc::internal::RpcMethodHandler< StacService::Service, ::epl::protobuf::v1::StacRequest, ::epl::protobuf::v1::StacItem, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(
          [](StacService::Service* service,
             ::grpc::ServerContext* ctx,
             const ::epl::protobuf::v1::StacRequest* req,
             ::epl::protobuf::v1::StacItem* resp) {
               return service->SearchOneItem(ctx, req, resp);
             }, this)));
  AddMethod(new ::grpc::internal::RpcServiceMethod(
      StacService_method_names[7],
      ::grpc::internal::RpcMethod::NORMAL_RPC,
      new ::grpc::internal::RpcMethodHandler< StacService::Service, ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(
          [](StacService::Service* service,
             ::grpc::ServerContext* ctx,
             const ::epl::protobuf::v1::StacItem* req,
             ::epl::protobuf::v1::StacDbResponse* resp) {
               return service->InsertOneItem(ctx, req, resp);
             }, this)));
  AddMethod(new ::grpc::internal::RpcServiceMethod(
      StacService_method_names[8],
      ::grpc::internal::RpcMethod::NORMAL_RPC,
      new ::grpc::internal::RpcMethodHandler< StacService::Service, ::epl::protobuf::v1::Collection, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(
          [](StacService::Service* service,
             ::grpc::ServerContext* ctx,
             const ::epl::protobuf::v1::Collection* req,
             ::epl::protobuf::v1::StacDbResponse* resp) {
               return service->InsertOneCollection(ctx, req, resp);
             }, this)));
  AddMethod(new ::grpc::internal::RpcServiceMethod(
      StacService_method_names[9],
      ::grpc::internal::RpcMethod::NORMAL_RPC,
      new ::grpc::internal::RpcMethodHandler< StacService::Service, ::epl::protobuf::v1::StacItem, ::epl::protobuf::v1::StacDbResponse, ::grpc::protobuf::MessageLite, ::grpc::protobuf::MessageLite>(
          [](StacService::Service* service,
             ::grpc::ServerContext* ctx,
             const ::epl::protobuf::v1::StacItem* req,
             ::epl::protobuf::v1::StacDbResponse* resp) {
               return service->UpdateOneItem(ctx, req, resp);
             }, this)));
}

StacService::Service::~Service() {
}

::grpc::Status StacService::Service::SearchItems(::grpc::ServerContext* context, const ::epl::protobuf::v1::StacRequest* request, ::grpc::ServerWriter< ::epl::protobuf::v1::StacItem>* writer) {
  (void) context;
  (void) request;
  (void) writer;
  return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
}

::grpc::Status StacService::Service::SearchCollections(::grpc::ServerContext* context, const ::epl::protobuf::v1::CollectionRequest* request, ::grpc::ServerWriter< ::epl::protobuf::v1::Collection>* writer) {
  (void) context;
  (void) request;
  (void) writer;
  return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
}

::grpc::Status StacService::Service::InsertItems(::grpc::ServerContext* context, ::grpc::ServerReaderWriter< ::epl::protobuf::v1::StacDbResponse, ::epl::protobuf::v1::StacItem>* stream) {
  (void) context;
  (void) stream;
  return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
}

::grpc::Status StacService::Service::UpdateItems(::grpc::ServerContext* context, ::grpc::ServerReaderWriter< ::epl::protobuf::v1::StacDbResponse, ::epl::protobuf::v1::StacItem>* stream) {
  (void) context;
  (void) stream;
  return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
}

::grpc::Status StacService::Service::CountItems(::grpc::ServerContext* context, const ::epl::protobuf::v1::StacRequest* request, ::epl::protobuf::v1::StacDbResponse* response) {
  (void) context;
  (void) request;
  (void) response;
  return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
}

::grpc::Status StacService::Service::DeleteOneItem(::grpc::ServerContext* context, const ::epl::protobuf::v1::StacItem* request, ::epl::protobuf::v1::StacDbResponse* response) {
  (void) context;
  (void) request;
  (void) response;
  return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
}

::grpc::Status StacService::Service::SearchOneItem(::grpc::ServerContext* context, const ::epl::protobuf::v1::StacRequest* request, ::epl::protobuf::v1::StacItem* response) {
  (void) context;
  (void) request;
  (void) response;
  return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
}

::grpc::Status StacService::Service::InsertOneItem(::grpc::ServerContext* context, const ::epl::protobuf::v1::StacItem* request, ::epl::protobuf::v1::StacDbResponse* response) {
  (void) context;
  (void) request;
  (void) response;
  return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
}

::grpc::Status StacService::Service::InsertOneCollection(::grpc::ServerContext* context, const ::epl::protobuf::v1::Collection* request, ::epl::protobuf::v1::StacDbResponse* response) {
  (void) context;
  (void) request;
  (void) response;
  return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
}

::grpc::Status StacService::Service::UpdateOneItem(::grpc::ServerContext* context, const ::epl::protobuf::v1::StacItem* request, ::epl::protobuf::v1::StacDbResponse* response) {
  (void) context;
  (void) request;
  (void) response;
  return ::grpc::Status(::grpc::StatusCode::UNIMPLEMENTED, "");
}


}  // namespace epl
}  // namespace protobuf
}  // namespace v1

