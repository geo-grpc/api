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


/**
 * An abstract class that represent the basic OperatorFactory interface.
 */
public abstract class OperatorFactoryEx {
	/**
	 * Returns True if the given operator exists. The type is one of the Operator::Type values or a user defined value.
	 */
	public abstract boolean isOperatorSupported(OperatorEx.Type type);

	/**
	 * Returns an operator of the given type. Throws an exception if the operator is not supported.
	 */
	public abstract OperatorEx getOperator(OperatorEx.Type type);
}
