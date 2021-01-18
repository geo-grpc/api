//package org.epl.geometry;
//
//import org.epl.protobuf.GeometryResponse;
//import org.epl.protobuf.GeometryRequest;
//
//import java.util.ArrayDeque;
//import java.util.Iterator;
//
//public class ProtobufCursor implements Iterator<GeometryResponse> {
//
//	private ArrayDeque<GeometryRequest> m_geometryRequest;
//
//	public ProtobufCursor() {
//		m_geometryRequest = new ArrayDeque<>();
//	}
//
//	@Override
//	public boolean hasNext() {
//		return m_geometryRequest != null && !m_geometryRequest.isEmpty();
//	}
//
//	@Override
//	public GeometryResponse next() {
//		return GeometryResponse.newBuilder().setGeometry(m_geometryRequest.pop().getGeometry()).build();
//	}
//
//	public void tick(GeometryRequest geometryRequest) {
//		m_geometryRequest.push(geometryRequest);
//	}
//}
