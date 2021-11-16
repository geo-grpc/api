/*
 Copyright 1995-2015 Esri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 For additional information, contact:
 Environmental Systems Research Institute, Inc.
 Attn: Contracts Dept
 380 New York Street
 Redlands, California, USA 92373

 email: contracts@esri.com
 */

package com.esri.core.geometry;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleByteBufferCursor extends ByteBufferCursor {
	private ArrayDeque<Integer> m_ids;
	private ArrayDeque<ByteBuffer> m_byteBufferDeque;
	private ArrayDeque<SimpleStateEnum> m_simpleStates;
	private ArrayDeque<String> m_featureIDs;
	private int m_current_id = -1;
	private SimpleStateEnum m_currentSimpleState = SimpleStateEnum.SIMPLE_UNKNOWN;
	private String m_currentFeatureID = "";
	private Envelope2D m_env2D = new Envelope2D();

	@Deprecated
	public SimpleByteBufferCursor(ByteBuffer byteBuffer) {
		m_byteBufferDeque = new ArrayDeque<>();
		m_byteBufferDeque.add(byteBuffer);
	}

	public SimpleByteBufferCursor(ByteBuffer byteBuffer, int id) {
		m_byteBufferDeque = new ArrayDeque<>(1);
		m_byteBufferDeque.add(byteBuffer);
		m_ids = new ArrayDeque<>(1);
		m_ids.push(id);
	}

	public SimpleByteBufferCursor(ByteBuffer byteBuffer, int id, SimpleStateEnum simpleState) {
		m_byteBufferDeque = new ArrayDeque<>(1);
		m_byteBufferDeque.add(byteBuffer);
		m_ids = new ArrayDeque<>(1);
		m_ids.push(id);
		m_simpleStates = new ArrayDeque<>(1);
		m_simpleStates.push(simpleState);
	}

	public SimpleByteBufferCursor(ByteBuffer byteBuffer, int id, SimpleStateEnum simpleState, String featureID) {
		m_byteBufferDeque = new ArrayDeque<>(1);
		m_byteBufferDeque.add(byteBuffer);
		m_ids = new ArrayDeque<>(1);
		m_ids.push(id);
		m_simpleStates = new ArrayDeque<>(1);
		m_simpleStates.push(simpleState);
		m_featureIDs = new ArrayDeque<>(1);
		m_featureIDs.push(featureID);
	}

	@Deprecated
	public SimpleByteBufferCursor(ByteBuffer[] byteBufferArray) {
		m_byteBufferDeque = Arrays.stream(byteBufferArray).collect(Collectors.toCollection(ArrayDeque::new));
	}

	@Deprecated
	public SimpleByteBufferCursor(List<ByteBuffer> byteBufferArray) {
		m_byteBufferDeque = new ArrayDeque<>(byteBufferArray);
	}

	public SimpleByteBufferCursor(ArrayDeque<ByteBuffer> byteBufferArrayDeque, ArrayDeque<Integer> ids) {
		m_byteBufferDeque = byteBufferArrayDeque;
		m_ids = ids;
	}

	public SimpleByteBufferCursor(ArrayDeque<ByteBuffer> arrayDeque,
	                              ArrayDeque<Integer> ids,
	                              ArrayDeque<SimpleStateEnum> simpleStates,
	                              ArrayDeque<String> featureIDs) {
		if ((arrayDeque.size() & ids.size() & simpleStates.size() & featureIDs.size()) != arrayDeque.size()) {
			throw new GeometryException("arrays must be same size");
		}
		m_byteBufferDeque = arrayDeque;
		m_ids = ids;
		m_simpleStates = simpleStates;
		m_featureIDs = featureIDs;
	}

	@Override
	public int getByteBufferID() {
		return m_current_id;
	}

	@Override
	public String getFeatureID() {
		return m_currentFeatureID;
	}

	@Override
	public SimpleStateEnum getSimpleState() {
		return m_currentSimpleState;
	}

	@Override
	public Envelope2D getEnvelope2D() {
		return m_env2D;
	}

	@Override
	public boolean hasNext() {
		return m_byteBufferDeque.size() > 0;
	}

	void _incrementInternals() {
		if (m_ids != null && !m_ids.isEmpty()) {
			m_current_id = m_ids.pop();
		} else {
			m_current_id++;
		}

		if (m_simpleStates != null && !m_simpleStates.isEmpty()) {
			m_currentSimpleState = m_simpleStates.pop();
		}
		if (m_featureIDs != null && !m_featureIDs.isEmpty()) {
			m_currentFeatureID = m_featureIDs.pop();
		}
	}

	@Override
	public ByteBuffer next() {
		if (hasNext()) {
			_incrementInternals();
			return m_byteBufferDeque.pop();
		}

		return null;
	}

}
