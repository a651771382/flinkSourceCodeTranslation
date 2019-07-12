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

package org.apache.flink.runtime.jobgraph;

/**
 * The ScheduleMode decides how tasks of an execution graph are started.
 * scheduleMode决定如何启动执行图的任务。
 */
public enum ScheduleMode {

	/**
	 * Schedule tasks lazily from the sources. Downstream tasks are started once their input data are ready
	 * 在数据源上使用懒加载。 一旦输入数据准备就绪，就开始下游任务
	 */
	LAZY_FROM_SOURCES,

	/**
	 * Schedules all tasks immediately.
	 * 立即安排所有任务。
	 */
	EAGER;

	/**
	 * Returns whether we are allowed to deploy consumers lazily.
	 * 消费者是否使用懒加载
	 */
	public boolean allowLazyDeployment() {
		return this == LAZY_FROM_SOURCES;
	}

}
