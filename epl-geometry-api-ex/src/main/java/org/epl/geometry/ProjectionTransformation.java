/*
Copyright 2017-2020 Echo Park Labs

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

email: davidraleigh@gmail.com
*/

package org.epl.geometry;

import com.esri.core.geometry.Geometry;
import org.proj4.PJ;

//This is a stub
public class ProjectionTransformation {
	SpatialReferenceEx m_fromSpatialReference;
	SpatialReferenceEx m_toSpatialReference;
	// TODO maybe cache the PJ objects?

	public ProjectionTransformation(SpatialReferenceEx fromSpatialReference, SpatialReferenceEx toSpatialReference) {
		m_fromSpatialReference = fromSpatialReference;
		m_toSpatialReference = toSpatialReference;
	}

	public ProjectionTransformation getReverse() {
		return new ProjectionTransformation(m_toSpatialReference, m_fromSpatialReference);
	}

	PJ getFromProj() {
		return ((SpatialReferenceExImpl) m_fromSpatialReference).getPJ();
	}

	public SpatialReferenceEx getFrom() {
		return m_fromSpatialReference;
	}

	public SpatialReferenceEx getTo() {
		return m_toSpatialReference;
	}

	PJ getToProj() {
		return ((SpatialReferenceExImpl) m_toSpatialReference).getPJ();
	}

    public static ProjectionTransformation getEqualArea(Geometry geometry, SpatialReferenceEx spatialReference) {
        return new ProjectionTransformation(spatialReference, SpatialReferenceEx.createEqualArea(geometry, spatialReference));
    }
}
