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

class OperatorBufferCursor extends GeometryCursor {
	Bufferer m_bufferer = new Bufferer();
	private SpatialReferenceImpl m_Spatial_reference;
	private ProgressTracker m_progress_tracker;
	private double[] m_distances;
	private Envelope2D m_currentUnionEnvelope2D;
	private boolean m_bUnion;
	double m_max_deviation;
	int m_max_vertices_in_full_circle;
	private int m_dindex;

	OperatorBufferCursor(GeometryCursor inputGeoms,
	                     SpatialReference sr,
	                     double[] distances,
	                     double max_deviation,
	                     int max_vertices,
	                     boolean b_union,
	                     ProgressTracker progress_tracker) {
		m_inputGeoms = inputGeoms;
		m_max_deviation = max_deviation;
		m_max_vertices_in_full_circle = max_vertices;
		m_Spatial_reference = (SpatialReferenceImpl) (sr);
		m_distances = distances;
		m_bUnion = b_union;
		m_currentUnionEnvelope2D = new Envelope2D();
		m_currentUnionEnvelope2D.setEmpty();
		m_dindex = -1;
		m_progress_tracker = progress_tracker;
	}

	@Override
	public Geometry next() {
		if (m_bUnion) {
			OperatorBufferCursor bufferCursor = new OperatorBufferCursor(m_inputGeoms,
					m_Spatial_reference, m_distances, m_max_deviation, m_max_vertices_in_full_circle, false, m_progress_tracker);
			return ((OperatorUnion) OperatorFactoryLocal.getInstance().getOperator(Operator.Type.Union)).execute(bufferCursor, m_Spatial_reference, m_progress_tracker).next();
		} else {
			if (hasNext()) {
				if (m_dindex + 1 < m_distances.length)
					m_dindex++;

				return buffer(m_inputGeoms.next(), m_distances[m_dindex]);
			}
			return null;
		}
	}

	Geometry buffer(Geometry geom, double distance) {
		return m_bufferer.buffer(geom, distance, m_Spatial_reference, m_max_deviation, m_max_vertices_in_full_circle, m_progress_tracker);
	}
}
