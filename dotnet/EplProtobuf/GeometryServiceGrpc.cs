// <auto-generated>
//     Generated by the protocol buffer compiler.  DO NOT EDIT!
//     source: epl/protobuf/v1/geometry_service.proto
// </auto-generated>
// Original file comments:
//
// Copyright 2017-2019 Echo Park Labs
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// For additional information, contact:
//
// email: info@echoparklabs.io
//
#pragma warning disable 0414, 1591
#region Designer generated code

using grpc = global::Grpc.Core;

namespace com.epl.protobuf.v1 {
  /// <summary>
  ///
  ///gRPC Interfaces for working with geometry operators
  /// </summary>
  public static partial class GeometryService
  {
    static readonly string __ServiceName = "epl.protobuf.v1.GeometryService";

    static readonly grpc::Marshaller<global::com.epl.protobuf.v1.GeometryRequest> __Marshaller_epl_protobuf_v1_GeometryRequest = grpc::Marshallers.Create((arg) => global::Google.Protobuf.MessageExtensions.ToByteArray(arg), global::com.epl.protobuf.v1.GeometryRequest.Parser.ParseFrom);
    static readonly grpc::Marshaller<global::com.epl.protobuf.v1.GeometryResponse> __Marshaller_epl_protobuf_v1_GeometryResponse = grpc::Marshallers.Create((arg) => global::Google.Protobuf.MessageExtensions.ToByteArray(arg), global::com.epl.protobuf.v1.GeometryResponse.Parser.ParseFrom);
    static readonly grpc::Marshaller<global::com.epl.protobuf.v1.FileRequestChunk> __Marshaller_epl_protobuf_v1_FileRequestChunk = grpc::Marshallers.Create((arg) => global::Google.Protobuf.MessageExtensions.ToByteArray(arg), global::com.epl.protobuf.v1.FileRequestChunk.Parser.ParseFrom);

    static readonly grpc::Method<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> __Method_Operate = new grpc::Method<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse>(
        grpc::MethodType.Unary,
        __ServiceName,
        "Operate",
        __Marshaller_epl_protobuf_v1_GeometryRequest,
        __Marshaller_epl_protobuf_v1_GeometryResponse);

    static readonly grpc::Method<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> __Method_OperateBiStream = new grpc::Method<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse>(
        grpc::MethodType.DuplexStreaming,
        __ServiceName,
        "OperateBiStream",
        __Marshaller_epl_protobuf_v1_GeometryRequest,
        __Marshaller_epl_protobuf_v1_GeometryResponse);

    static readonly grpc::Method<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> __Method_OperateBiStreamFlow = new grpc::Method<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse>(
        grpc::MethodType.DuplexStreaming,
        __ServiceName,
        "OperateBiStreamFlow",
        __Marshaller_epl_protobuf_v1_GeometryRequest,
        __Marshaller_epl_protobuf_v1_GeometryResponse);

    static readonly grpc::Method<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> __Method_OperateServerStream = new grpc::Method<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse>(
        grpc::MethodType.ServerStreaming,
        __ServiceName,
        "OperateServerStream",
        __Marshaller_epl_protobuf_v1_GeometryRequest,
        __Marshaller_epl_protobuf_v1_GeometryResponse);

    static readonly grpc::Method<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> __Method_OperateClientStream = new grpc::Method<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse>(
        grpc::MethodType.ClientStreaming,
        __ServiceName,
        "OperateClientStream",
        __Marshaller_epl_protobuf_v1_GeometryRequest,
        __Marshaller_epl_protobuf_v1_GeometryResponse);

    static readonly grpc::Method<global::com.epl.protobuf.v1.FileRequestChunk, global::com.epl.protobuf.v1.GeometryResponse> __Method_FileOperateBiStreamFlow = new grpc::Method<global::com.epl.protobuf.v1.FileRequestChunk, global::com.epl.protobuf.v1.GeometryResponse>(
        grpc::MethodType.DuplexStreaming,
        __ServiceName,
        "FileOperateBiStreamFlow",
        __Marshaller_epl_protobuf_v1_FileRequestChunk,
        __Marshaller_epl_protobuf_v1_GeometryResponse);

    /// <summary>Service descriptor</summary>
    public static global::Google.Protobuf.Reflection.ServiceDescriptor Descriptor
    {
      get { return global::com.epl.protobuf.v1.GeometryServiceReflection.Descriptor.Services[0]; }
    }

    /// <summary>Base class for server-side implementations of GeometryService</summary>
    [grpc::BindServiceMethod(typeof(GeometryService), "BindService")]
    public abstract partial class GeometryServiceBase
    {
      /// <summary>
      /// Execute a single blocking geometry operation
      /// </summary>
      /// <param name="request">The request received from the client.</param>
      /// <param name="context">The context of the server-side call handler being invoked.</param>
      /// <returns>The response to send back to the client (wrapped by a task).</returns>
      public virtual global::System.Threading.Tasks.Task<global::com.epl.protobuf.v1.GeometryResponse> Operate(global::com.epl.protobuf.v1.GeometryRequest request, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

      /// <summary>
      /// stream in operator requests and get back a stream of results
      /// </summary>
      /// <param name="requestStream">Used for reading requests from the client.</param>
      /// <param name="responseStream">Used for sending responses back to the client.</param>
      /// <param name="context">The context of the server-side call handler being invoked.</param>
      /// <returns>A task indicating completion of the handler.</returns>
      public virtual global::System.Threading.Tasks.Task OperateBiStream(grpc::IAsyncStreamReader<global::com.epl.protobuf.v1.GeometryRequest> requestStream, grpc::IServerStreamWriter<global::com.epl.protobuf.v1.GeometryResponse> responseStream, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

      /// <summary>
      /// manual flow control bi-directional stream. example
      /// go shouldn't use this because of https://groups.google.com/forum/#!topic/grpc-io/6_B46Oszb4k ?
      /// </summary>
      /// <param name="requestStream">Used for reading requests from the client.</param>
      /// <param name="responseStream">Used for sending responses back to the client.</param>
      /// <param name="context">The context of the server-side call handler being invoked.</param>
      /// <returns>A task indicating completion of the handler.</returns>
      public virtual global::System.Threading.Tasks.Task OperateBiStreamFlow(grpc::IAsyncStreamReader<global::com.epl.protobuf.v1.GeometryRequest> requestStream, grpc::IServerStreamWriter<global::com.epl.protobuf.v1.GeometryResponse> responseStream, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

      /// <summary>
      /// Maybe a cut operation that returns a lot of different geometries? for now, this is not implemented.
      /// </summary>
      /// <param name="request">The request received from the client.</param>
      /// <param name="responseStream">Used for sending responses back to the client.</param>
      /// <param name="context">The context of the server-side call handler being invoked.</param>
      /// <returns>A task indicating completion of the handler.</returns>
      public virtual global::System.Threading.Tasks.Task OperateServerStream(global::com.epl.protobuf.v1.GeometryRequest request, grpc::IServerStreamWriter<global::com.epl.protobuf.v1.GeometryResponse> responseStream, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

      /// <summary>
      /// Maybe something like a union operation. for now, this is not implemented.
      /// </summary>
      /// <param name="requestStream">Used for reading requests from the client.</param>
      /// <param name="context">The context of the server-side call handler being invoked.</param>
      /// <returns>The response to send back to the client (wrapped by a task).</returns>
      public virtual global::System.Threading.Tasks.Task<global::com.epl.protobuf.v1.GeometryResponse> OperateClientStream(grpc::IAsyncStreamReader<global::com.epl.protobuf.v1.GeometryRequest> requestStream, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

      /// <summary>
      /// stream in file chunks for a geometry file type and stream back results for each geometry encountered
      /// </summary>
      /// <param name="requestStream">Used for reading requests from the client.</param>
      /// <param name="responseStream">Used for sending responses back to the client.</param>
      /// <param name="context">The context of the server-side call handler being invoked.</param>
      /// <returns>A task indicating completion of the handler.</returns>
      public virtual global::System.Threading.Tasks.Task FileOperateBiStreamFlow(grpc::IAsyncStreamReader<global::com.epl.protobuf.v1.FileRequestChunk> requestStream, grpc::IServerStreamWriter<global::com.epl.protobuf.v1.GeometryResponse> responseStream, grpc::ServerCallContext context)
      {
        throw new grpc::RpcException(new grpc::Status(grpc::StatusCode.Unimplemented, ""));
      }

    }

    /// <summary>Client for GeometryService</summary>
    public partial class GeometryServiceClient : grpc::ClientBase<GeometryServiceClient>
    {
      /// <summary>Creates a new client for GeometryService</summary>
      /// <param name="channel">The channel to use to make remote calls.</param>
      public GeometryServiceClient(grpc::ChannelBase channel) : base(channel)
      {
      }
      /// <summary>Creates a new client for GeometryService that uses a custom <c>CallInvoker</c>.</summary>
      /// <param name="callInvoker">The callInvoker to use to make remote calls.</param>
      public GeometryServiceClient(grpc::CallInvoker callInvoker) : base(callInvoker)
      {
      }
      /// <summary>Protected parameterless constructor to allow creation of test doubles.</summary>
      protected GeometryServiceClient() : base()
      {
      }
      /// <summary>Protected constructor to allow creation of configured clients.</summary>
      /// <param name="configuration">The client configuration.</param>
      protected GeometryServiceClient(ClientBaseConfiguration configuration) : base(configuration)
      {
      }

      /// <summary>
      /// Execute a single blocking geometry operation
      /// </summary>
      /// <param name="request">The request to send to the server.</param>
      /// <param name="headers">The initial metadata to send with the call. This parameter is optional.</param>
      /// <param name="deadline">An optional deadline for the call. The call will be cancelled if deadline is hit.</param>
      /// <param name="cancellationToken">An optional token for canceling the call.</param>
      /// <returns>The response received from the server.</returns>
      public virtual global::com.epl.protobuf.v1.GeometryResponse Operate(global::com.epl.protobuf.v1.GeometryRequest request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return Operate(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      /// <summary>
      /// Execute a single blocking geometry operation
      /// </summary>
      /// <param name="request">The request to send to the server.</param>
      /// <param name="options">The options for the call.</param>
      /// <returns>The response received from the server.</returns>
      public virtual global::com.epl.protobuf.v1.GeometryResponse Operate(global::com.epl.protobuf.v1.GeometryRequest request, grpc::CallOptions options)
      {
        return CallInvoker.BlockingUnaryCall(__Method_Operate, null, options, request);
      }
      /// <summary>
      /// Execute a single blocking geometry operation
      /// </summary>
      /// <param name="request">The request to send to the server.</param>
      /// <param name="headers">The initial metadata to send with the call. This parameter is optional.</param>
      /// <param name="deadline">An optional deadline for the call. The call will be cancelled if deadline is hit.</param>
      /// <param name="cancellationToken">An optional token for canceling the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncUnaryCall<global::com.epl.protobuf.v1.GeometryResponse> OperateAsync(global::com.epl.protobuf.v1.GeometryRequest request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return OperateAsync(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      /// <summary>
      /// Execute a single blocking geometry operation
      /// </summary>
      /// <param name="request">The request to send to the server.</param>
      /// <param name="options">The options for the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncUnaryCall<global::com.epl.protobuf.v1.GeometryResponse> OperateAsync(global::com.epl.protobuf.v1.GeometryRequest request, grpc::CallOptions options)
      {
        return CallInvoker.AsyncUnaryCall(__Method_Operate, null, options, request);
      }
      /// <summary>
      /// stream in operator requests and get back a stream of results
      /// </summary>
      /// <param name="headers">The initial metadata to send with the call. This parameter is optional.</param>
      /// <param name="deadline">An optional deadline for the call. The call will be cancelled if deadline is hit.</param>
      /// <param name="cancellationToken">An optional token for canceling the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncDuplexStreamingCall<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> OperateBiStream(grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return OperateBiStream(new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      /// <summary>
      /// stream in operator requests and get back a stream of results
      /// </summary>
      /// <param name="options">The options for the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncDuplexStreamingCall<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> OperateBiStream(grpc::CallOptions options)
      {
        return CallInvoker.AsyncDuplexStreamingCall(__Method_OperateBiStream, null, options);
      }
      /// <summary>
      /// manual flow control bi-directional stream. example
      /// go shouldn't use this because of https://groups.google.com/forum/#!topic/grpc-io/6_B46Oszb4k ?
      /// </summary>
      /// <param name="headers">The initial metadata to send with the call. This parameter is optional.</param>
      /// <param name="deadline">An optional deadline for the call. The call will be cancelled if deadline is hit.</param>
      /// <param name="cancellationToken">An optional token for canceling the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncDuplexStreamingCall<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> OperateBiStreamFlow(grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return OperateBiStreamFlow(new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      /// <summary>
      /// manual flow control bi-directional stream. example
      /// go shouldn't use this because of https://groups.google.com/forum/#!topic/grpc-io/6_B46Oszb4k ?
      /// </summary>
      /// <param name="options">The options for the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncDuplexStreamingCall<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> OperateBiStreamFlow(grpc::CallOptions options)
      {
        return CallInvoker.AsyncDuplexStreamingCall(__Method_OperateBiStreamFlow, null, options);
      }
      /// <summary>
      /// Maybe a cut operation that returns a lot of different geometries? for now, this is not implemented.
      /// </summary>
      /// <param name="request">The request to send to the server.</param>
      /// <param name="headers">The initial metadata to send with the call. This parameter is optional.</param>
      /// <param name="deadline">An optional deadline for the call. The call will be cancelled if deadline is hit.</param>
      /// <param name="cancellationToken">An optional token for canceling the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncServerStreamingCall<global::com.epl.protobuf.v1.GeometryResponse> OperateServerStream(global::com.epl.protobuf.v1.GeometryRequest request, grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return OperateServerStream(request, new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      /// <summary>
      /// Maybe a cut operation that returns a lot of different geometries? for now, this is not implemented.
      /// </summary>
      /// <param name="request">The request to send to the server.</param>
      /// <param name="options">The options for the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncServerStreamingCall<global::com.epl.protobuf.v1.GeometryResponse> OperateServerStream(global::com.epl.protobuf.v1.GeometryRequest request, grpc::CallOptions options)
      {
        return CallInvoker.AsyncServerStreamingCall(__Method_OperateServerStream, null, options, request);
      }
      /// <summary>
      /// Maybe something like a union operation. for now, this is not implemented.
      /// </summary>
      /// <param name="headers">The initial metadata to send with the call. This parameter is optional.</param>
      /// <param name="deadline">An optional deadline for the call. The call will be cancelled if deadline is hit.</param>
      /// <param name="cancellationToken">An optional token for canceling the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncClientStreamingCall<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> OperateClientStream(grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return OperateClientStream(new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      /// <summary>
      /// Maybe something like a union operation. for now, this is not implemented.
      /// </summary>
      /// <param name="options">The options for the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncClientStreamingCall<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse> OperateClientStream(grpc::CallOptions options)
      {
        return CallInvoker.AsyncClientStreamingCall(__Method_OperateClientStream, null, options);
      }
      /// <summary>
      /// stream in file chunks for a geometry file type and stream back results for each geometry encountered
      /// </summary>
      /// <param name="headers">The initial metadata to send with the call. This parameter is optional.</param>
      /// <param name="deadline">An optional deadline for the call. The call will be cancelled if deadline is hit.</param>
      /// <param name="cancellationToken">An optional token for canceling the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncDuplexStreamingCall<global::com.epl.protobuf.v1.FileRequestChunk, global::com.epl.protobuf.v1.GeometryResponse> FileOperateBiStreamFlow(grpc::Metadata headers = null, global::System.DateTime? deadline = null, global::System.Threading.CancellationToken cancellationToken = default(global::System.Threading.CancellationToken))
      {
        return FileOperateBiStreamFlow(new grpc::CallOptions(headers, deadline, cancellationToken));
      }
      /// <summary>
      /// stream in file chunks for a geometry file type and stream back results for each geometry encountered
      /// </summary>
      /// <param name="options">The options for the call.</param>
      /// <returns>The call object.</returns>
      public virtual grpc::AsyncDuplexStreamingCall<global::com.epl.protobuf.v1.FileRequestChunk, global::com.epl.protobuf.v1.GeometryResponse> FileOperateBiStreamFlow(grpc::CallOptions options)
      {
        return CallInvoker.AsyncDuplexStreamingCall(__Method_FileOperateBiStreamFlow, null, options);
      }
      /// <summary>Creates a new instance of client from given <c>ClientBaseConfiguration</c>.</summary>
      protected override GeometryServiceClient NewInstance(ClientBaseConfiguration configuration)
      {
        return new GeometryServiceClient(configuration);
      }
    }

    /// <summary>Creates service definition that can be registered with a server</summary>
    /// <param name="serviceImpl">An object implementing the server-side handling logic.</param>
    public static grpc::ServerServiceDefinition BindService(GeometryServiceBase serviceImpl)
    {
      return grpc::ServerServiceDefinition.CreateBuilder()
          .AddMethod(__Method_Operate, serviceImpl.Operate)
          .AddMethod(__Method_OperateBiStream, serviceImpl.OperateBiStream)
          .AddMethod(__Method_OperateBiStreamFlow, serviceImpl.OperateBiStreamFlow)
          .AddMethod(__Method_OperateServerStream, serviceImpl.OperateServerStream)
          .AddMethod(__Method_OperateClientStream, serviceImpl.OperateClientStream)
          .AddMethod(__Method_FileOperateBiStreamFlow, serviceImpl.FileOperateBiStreamFlow).Build();
    }

    /// <summary>Register service method with a service binder with or without implementation. Useful when customizing the  service binding logic.
    /// Note: this method is part of an experimental API that can change or be removed without any prior notice.</summary>
    /// <param name="serviceBinder">Service methods will be bound by calling <c>AddMethod</c> on this object.</param>
    /// <param name="serviceImpl">An object implementing the server-side handling logic.</param>
    public static void BindService(grpc::ServiceBinderBase serviceBinder, GeometryServiceBase serviceImpl)
    {
      serviceBinder.AddMethod(__Method_Operate, serviceImpl == null ? null : new grpc::UnaryServerMethod<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse>(serviceImpl.Operate));
      serviceBinder.AddMethod(__Method_OperateBiStream, serviceImpl == null ? null : new grpc::DuplexStreamingServerMethod<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse>(serviceImpl.OperateBiStream));
      serviceBinder.AddMethod(__Method_OperateBiStreamFlow, serviceImpl == null ? null : new grpc::DuplexStreamingServerMethod<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse>(serviceImpl.OperateBiStreamFlow));
      serviceBinder.AddMethod(__Method_OperateServerStream, serviceImpl == null ? null : new grpc::ServerStreamingServerMethod<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse>(serviceImpl.OperateServerStream));
      serviceBinder.AddMethod(__Method_OperateClientStream, serviceImpl == null ? null : new grpc::ClientStreamingServerMethod<global::com.epl.protobuf.v1.GeometryRequest, global::com.epl.protobuf.v1.GeometryResponse>(serviceImpl.OperateClientStream));
      serviceBinder.AddMethod(__Method_FileOperateBiStreamFlow, serviceImpl == null ? null : new grpc::DuplexStreamingServerMethod<global::com.epl.protobuf.v1.FileRequestChunk, global::com.epl.protobuf.v1.GeometryResponse>(serviceImpl.FileOperateBiStreamFlow));
    }

  }
}
#endregion
