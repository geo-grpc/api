/*
 Copyright 1995-2017 Esri

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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.stream.Collectors;

public class SimpleJsonReaderCursor extends JsonReaderCursor {
	private ArrayDeque<JsonReader> m_jsonDeque;
	private int m_index = -1;
	private String currentFeatureID = "";
	private SimpleStateEnum simpleState = SimpleStateEnum.SIMPLE_UNKNOWN;

	public SimpleJsonReaderCursor(JsonReader jsonString) {
		m_jsonDeque = new ArrayDeque<>(1);
		m_jsonDeque.add(jsonString);
	}

	public SimpleJsonReaderCursor(JsonReader[] jsonStringArray) {
		m_jsonDeque = Arrays.stream(jsonStringArray).collect(Collectors.toCollection(ArrayDeque::new));
	}

	@Override
	public JsonReader next() {
		if (!m_jsonDeque.isEmpty()) {
			m_index++;
			return m_jsonDeque.pop();
		}

		return null;
	}

	@Override
	public int getID() {
		return m_index;
	}

	@Override
	public String getFeatureID() {
		return currentFeatureID;
	}

	@Override
	public SimpleStateEnum getSimpleState() {
		return simpleState;
	}

	@Override
	public boolean hasNext() {
		return m_jsonDeque.size() > 0;
	}
}
