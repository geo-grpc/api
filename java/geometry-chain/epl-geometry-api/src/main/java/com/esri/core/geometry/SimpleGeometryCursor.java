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

import java.util.*;
import java.util.stream.Collectors;

/**
 * A simple GeometryCursor implementation that wraps a single Geometry or
 * an array of Geometry classes
 */
public class SimpleGeometryCursor extends GeometryCursor {

	private int m_index = -1;
	private String m_currentFeatureId = "";
	private SimpleStateEnum m_simpleState = SimpleStateEnum.SIMPLE_UNKNOWN;
	private MapGeometryCursor m_mapGeometryCursor = null;
	private ArrayDeque<Geometry> m_geometryDeque = null;

	private int m_current_id = -1;

	public SimpleGeometryCursor(Geometry geom) {
		m_geometryDeque = new ArrayDeque<>(1);
		m_geometryDeque.add(geom);
	}

	public SimpleGeometryCursor(Geometry[] geoms) {
		m_geometryDeque = Arrays.stream(geoms).collect(Collectors.toCollection(ArrayDeque::new));
	}

	@Deprecated
	public SimpleGeometryCursor(List<Geometry> geoms) {
		m_geometryDeque = new ArrayDeque<>(geoms);
	}

	public SimpleGeometryCursor(ArrayDeque<Geometry> geoms) {
		m_geometryDeque = geoms;
	}

	public SimpleGeometryCursor(MapGeometryCursor mapGeometryCursor) {
		m_mapGeometryCursor = mapGeometryCursor;
	}

	@Override
	public int getGeometryID() {
		return m_current_id;
	}

	@Override
	public String getFeatureID() {
		return m_currentFeatureId;
	}

	@Override
	public SimpleStateEnum getSimpleState() {
		return m_simpleState;
	}

	@Override
	public boolean hasNext() {
		return (m_geometryDeque != null && m_geometryDeque.size() > 0) || (m_mapGeometryCursor != null && m_mapGeometryCursor.hasNext());
	}

	@Override
	public Geometry next() {
		m_index++;
		Geometry geometry = null;
		if (m_geometryDeque != null && !m_geometryDeque.isEmpty()) {
			geometry = m_geometryDeque.pop();

			// TODO get id off of geometry if exists
			m_current_id = m_index;
			m_simpleState = geometry.getSimpleState();
		} else if (m_mapGeometryCursor != null && m_mapGeometryCursor.hasNext()) {
			geometry = m_mapGeometryCursor.next().m_geometry;

			// TODO get id off of geometry if exists
			m_current_id = m_mapGeometryCursor.getGeometryID();
			m_simpleState = m_mapGeometryCursor.getSimpleState();
		}

		return geometry;
	}
}
