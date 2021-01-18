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

public interface GeoJsonImportFlags {
	public static final int geoJsonImportDefaults = 0;
	@Deprecated static final int geoJsonImportNonTrusted = 2;
	/**
	 * If set, the import will skip CRS.
	 */
	public static final int geoJsonImportSkipCRS = 8;
	/**
	 * If set, and the geojson does not have a spatial reference, the result geometry will not have one too, otherwise
	 * it'll assume WGS84.
	 */
	public static final int geoJsonImportNoWGS84Default = 16;	
}
