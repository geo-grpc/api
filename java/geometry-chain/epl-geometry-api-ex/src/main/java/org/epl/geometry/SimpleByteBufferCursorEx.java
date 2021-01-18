package org.epl.geometry;

import com.esri.core.geometry.SimpleByteBufferCursor;
import com.esri.core.geometry.SimpleStateEnum;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.List;

public class SimpleByteBufferCursorEx extends SimpleByteBufferCursor {
	public SimpleByteBufferCursorEx(ByteBuffer byteBuffer) {
		super(byteBuffer);
	}

	public SimpleByteBufferCursorEx(ByteBuffer byteBuffer, int id) {
		super(byteBuffer, id);
	}

	public SimpleByteBufferCursorEx(ByteBuffer byteBuffer, int id, SimpleStateEnum simpleState) {
		super(byteBuffer, id, simpleState);
	}

	public SimpleByteBufferCursorEx(ByteBuffer byteBuffer, int id, SimpleStateEnum simpleState, String featureID) {
		super(byteBuffer, id, simpleState, featureID);
	}

	public SimpleByteBufferCursorEx(ByteBuffer[] byteBufferArray) {
		super(byteBufferArray);
	}

	public SimpleByteBufferCursorEx(List<ByteBuffer> byteBufferArray) {
		super(byteBufferArray);
	}

	public SimpleByteBufferCursorEx(ArrayDeque<ByteBuffer> byteBufferArrayDeque, ArrayDeque<Integer> ids) {
		super(byteBufferArrayDeque, ids);
	}

	public SimpleByteBufferCursorEx(ArrayDeque<ByteBuffer> arrayDeque, ArrayDeque<Integer> ids, ArrayDeque<SimpleStateEnum> simpleStates, ArrayDeque<String> featureIDs) {
		super(arrayDeque, ids, simpleStates, featureIDs);
	}

	public void tick(ByteBuffer byteBuffer) {

	}
}
