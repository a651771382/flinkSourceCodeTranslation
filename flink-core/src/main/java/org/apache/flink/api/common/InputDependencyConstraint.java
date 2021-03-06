/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.api.common;

import org.apache.flink.annotation.PublicEvolving;

/**
 * This constraint indicates when a task should be scheduled considering its inputs status.
 * 此约束指示何时应考虑其输入状态来安排任务。
 */
@PublicEvolving
public enum InputDependencyConstraint {

	/**
	 * Schedule the task if any input is consumable.
	 * 如果任何输入是消耗品，则安排任务。
	 */
	ANY,

	/**
	 * Schedule the task if all the inputs are consumable.
	 * 如果所有输入都是消耗品，则安排任务。
	 */
	ALL
}
