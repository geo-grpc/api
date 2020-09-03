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

import com.esri.core.geometry.*;

public class GeometryEngineEx extends GeometryEngine {
	private static OperatorFactoryLocalEx factoryEx = OperatorFactoryLocalEx.getInstance();

	public static boolean isSimple(Geometry geometry, SpatialReferenceEx spatialReference) {
		return GeometryEngine.isSimple(geometry, spatialReference.toSpatialReference());
	}

	/**
	 * Constructs a new geometry by union an array of geometries. All inputs
	 * must be of the same type of geometries and share one spatial reference.
	 * <p>
	 * See OperatorUnion.
	 *
	 * @param geometries       The geometries to union.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return The geometry object representing the resultant union.
	 */
	public static Geometry union(Geometry[] geometries, SpatialReferenceEx spatialReference) {
		return GeometryEngine.union(geometries, spatialReference.toSpatialReference());
	}

	/**
	 * Creates the difference of two geometries. The dimension of geometry2 has
	 * to be equal to or greater than that of geometry1.
	 * <p>
	 * See OperatorDifference.
	 *
	 * @param geometry1        The geometry being subtracted.
	 * @param substractor      The geometry object to subtract from.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return The geometry of the differences.
	 */
	public static Geometry difference(Geometry geometry1, Geometry substractor, SpatialReferenceEx spatialReference) {
		return GeometryEngine.difference(geometry1, substractor, spatialReference.toSpatialReference());
	}

	/**
	 * Creates the symmetric difference of two geometries.
	 * <p>
	 * See OperatorSymmetricDifference.
	 *
	 * @param leftGeometry     is one of the Geometry instances in the XOR operation.
	 * @param rightGeometry    is one of the Geometry instances in the XOR operation.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return Returns the result of the symmetric difference.
	 */
	public static Geometry symmetricDifference(Geometry leftGeometry, Geometry rightGeometry, SpatialReferenceEx spatialReference) {
		return GeometryEngine.symmetricDifference(leftGeometry, rightGeometry, spatialReference.toSpatialReference());
	}

	/**
	 * Indicates if two geometries are equal.
	 * <p>
	 * See OperatorEquals.
	 *
	 * @param geometry1        Geometry.
	 * @param geometry2        Geometry.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return TRUE if both geometry objects are equal.
	 */
	public static boolean equals(Geometry geometry1, Geometry geometry2,
	                             SpatialReferenceEx spatialReference) {
		return GeometryEngine.equals(geometry1, geometry2, spatialReference.toSpatialReference());
	}

	/**
	 * See OperatorDisjoint.
	 */
	public static boolean disjoint(Geometry geometry1, Geometry geometry2,
	                               SpatialReferenceEx spatialReference) {
		return GeometryEngine.disjoint(geometry1, geometry2, spatialReference.toSpatialReference());
	}

	/**
	 * Creates a geometry through intersection between two geometries.
	 * <p>
	 * See OperatorIntersection.
	 *
	 * @param geometry1        The first geometry.
	 * @param intersector      The geometry to intersect the first geometry.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return The geometry created through intersection.
	 */
	public static Geometry intersect(Geometry geometry1, Geometry intersector,
	                                 SpatialReferenceEx spatialReference) {
		return GeometryEngine.intersect(geometry1, intersector, spatialReference.toSpatialReference());
	}

	/**
	 * Indicates if one geometry is within another geometry.
	 * <p>
	 * See OperatorWithin.
	 *
	 * @param geometry1        The base geometry that is tested for within relationship to
	 *                         the other geometry.
	 * @param geometry2        The comparison geometry that is tested for the contains
	 *                         relationship to the other geometry.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return TRUE if the first geometry is within the other geometry.
	 */
	public static boolean within(Geometry geometry1, Geometry geometry2,
	                             SpatialReferenceEx spatialReference) {
		return GeometryEngine.within(geometry1, geometry2, spatialReference.toSpatialReference());
	}

	/**
	 * Indicates if one geometry contains another geometry.
	 * <p>
	 * See OperatorContains.
	 *
	 * @param geometry1        The geometry that is tested for the contains relationship to
	 *                         the other geometry..
	 * @param geometry2        The geometry that is tested for within relationship to the
	 *                         other geometry.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return TRUE if geometry1 contains geometry2.
	 */
	public static boolean contains(Geometry geometry1, Geometry geometry2,
	                               SpatialReferenceEx spatialReference) {
		return GeometryEngine.contains(geometry1, geometry2, spatialReference.toSpatialReference());
	}

	/**
	 * Indicates if one geometry crosses another geometry.
	 * <p>
	 * See OperatorCrosses.
	 *
	 * @param geometry1        The geometry to cross.
	 * @param geometry2        The geometry being crossed.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return TRUE if geometry1 crosses geometry2.
	 */
	public static boolean crosses(Geometry geometry1, Geometry geometry2,
	                              SpatialReferenceEx spatialReference) {
		return GeometryEngine.crosses(geometry1, geometry2, spatialReference.toSpatialReference());
	}

	/**
	 * Indicates if one geometry touches another geometry.
	 * <p>
	 * See OperatorTouches.
	 *
	 * @param geometry1        The geometry to touch.
	 * @param geometry2        The geometry to be touched.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return TRUE if geometry1 touches geometry2.
	 */
	public static boolean touches(Geometry geometry1, Geometry geometry2,
	                              SpatialReferenceEx spatialReference) {
		return GeometryEngine.touches(geometry1,geometry2,spatialReference.toSpatialReference());
	}

	/**
	 * Indicates if one geometry overlaps another geometry.
	 * <p>
	 * See OperatorOverlaps.
	 *
	 * @param geometry1        The geometry to overlap.
	 * @param geometry2        The geometry to be overlapped.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return TRUE if geometry1 overlaps geometry2.
	 */
	public static boolean overlaps(Geometry geometry1, Geometry geometry2,
	                               SpatialReferenceEx spatialReference) {
		return GeometryEngine.overlaps(geometry1,geometry2,spatialReference.toSpatialReference());
	}

	/**
	 * Indicates if the given relation holds for the two geometries.
	 * <p>
	 * See OperatorRelate.
	 *
	 * @param geometry1        The first geometry for the relation.
	 * @param geometry2        The second geometry for the relation.
	 * @param spatialReference The spatial reference of the geometries.
	 * @param relation         The DE-9IM relation.
	 * @return TRUE if the given relation holds between geometry1 and geometry2.
	 */
	public static boolean relate(Geometry geometry1, Geometry geometry2,
	                             SpatialReferenceEx spatialReference, String relation) {
		return GeometryEngine.relate(geometry1,geometry2,spatialReference.toSpatialReference(), relation);
	}

	/**
	 * Calculates the 2D planar distance between two geometries.
	 * <p>
	 * See OperatorDistance.
	 *
	 * @param geometry1        Geometry.
	 * @param geometry2        Geometry.
	 * @param spatialReference The spatial reference of the geometries. This parameter is not
	 *                         used and can be null.
	 * @return The distance between the two geometries.
	 */
	public static double distance(Geometry geometry1, Geometry geometry2,
	                              SpatialReferenceEx spatialReference) {
		return GeometryEngine.distance(geometry1,geometry2, spatialReference.toSpatialReference());
	}

	/**
	 * Calculates the clipped geometry from a target geometry using an envelope.
	 * <p>
	 * See OperatorClip.
	 *
	 * @param geometry         The geometry to be clipped.
	 * @param envelope         The envelope used to clip.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return The geometry created by clipping.
	 */
	public static Geometry clip(Geometry geometry, Envelope envelope,
	                            SpatialReferenceEx spatialReference) {
		return GeometryEngine.clip(geometry, envelope, spatialReference.toSpatialReference());
	}

	/**
	 * Calculates the cut geometry from a target geometry using a polyline. For
	 * Polylines, all left cuts will be grouped together in the first Geometry,
	 * Right cuts and coincident cuts are grouped in the second Geometry, and
	 * each undefined cut, along with any uncut parts, are output as separate
	 * Polylines. For Polygons, all left cuts are grouped in the first Polygon,
	 * all right cuts are in the second Polygon, and each undefined cut, along
	 * with any left-over parts after cutting, are output as a separate Polygon.
	 * If there were no cuts then the array will be empty. An undefined cut will
	 * only be produced if a left cut or right cut was produced, and there was a
	 * part left over after cutting or a cut is bounded to the left and right of
	 * the cutter.
	 * <p>
	 * See OperatorCut.
	 *
	 * @param cuttee           The geometry to be cut.
	 * @param cutter           The polyline to cut the geometry.
	 * @param spatialReference The spatial reference of the geometries.
	 * @return An array of geometries created from cutting.
	 */
	public static Geometry[] cut(Geometry cuttee, Polyline cutter,
	                             SpatialReferenceEx spatialReference) {
		return GeometryEngine.cut(cuttee,cutter,spatialReference.toSpatialReference());
	}

	/**
	 * Calculates a buffer polygon for each geometry at each of the
	 * corresponding specified distances.  It is assumed that all geometries have
	 * the same spatial reference. There is an option to union the
	 * returned geometries.
	 * <p>
	 * See OperatorBuffer.
	 *
	 * @param geometries       An array of geometries to be buffered.
	 * @param spatialReference The spatial reference of the geometries.
	 * @param distances        The corresponding distances for the input geometries to be buffered.
	 * @param toUnionResults   TRUE if all geometries buffered at a given distance are to be unioned into a single polygon.
	 * @return The buffer of the geometries.
	 */
	public static Polygon[] buffer(Geometry[] geometries,
	                               SpatialReferenceEx spatialReference, double[] distances,
	                               boolean toUnionResults) {
		return GeometryEngine.buffer(geometries, spatialReference.toSpatialReference(), distances, toUnionResults);
	}
	
	public static Geometry project(Geometry geometry, SpatialReferenceEx spatialReferenceInput, SpatialReferenceEx spatialReferenceOutput) {
		OperatorProject operatorProject = (OperatorProject) factoryEx.getOperator(OperatorEx.Type.Project);
		ProjectionTransformation projectionTransformation = new ProjectionTransformation(spatialReferenceInput, spatialReferenceOutput);
		return operatorProject.execute(geometry, projectionTransformation, null);
	}

	/**
	 * Calculates a buffer polygon of the geometry as specified by the
	 * distance input. The buffer is implemented in the xy-plane.
	 * <p>
	 * See OperatorBuffer
	 *
	 * @param geometry         Geometry to be buffered.
	 * @param spatialReference The spatial reference of the geometry.
	 * @param distance         The specified distance for buffer. Same units as the spatial reference.
	 * @return The buffer polygon at the specified distances.
	 */
	public static Polygon buffer(Geometry geometry,
	                             SpatialReferenceEx spatialReference, double distance) {
		return GeometryEngine.buffer(geometry, spatialReference.toSpatialReference(), distance);
	}

	public static Polygon geodesicBuffer(Geometry geometry, SpatialReferenceEx spatialReference, double d) {
		OperatorGeodesicBuffer operatorGeodesicBuffer = (OperatorGeodesicBuffer)factoryEx.getOperator(OperatorEx.Type.GeodesicBuffer);
		return (Polygon) operatorGeodesicBuffer.execute(geometry, spatialReference, 0, d, Double.NaN, false, null);
	}
}
