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

package org.apache.flink.optimizer.plan;

import java.io.File;
import java.io.IOException;

import org.apache.flink.api.common.JobID;
import org.apache.flink.runtime.jobgraph.JobGraph;

import javax.annotation.Nullable;

/**
 * Abstract class representing Flink Streaming plans
 * 代表Flink Streaming计划的抽象类
 */
public abstract class StreamingPlan implements FlinkPlan {

	/**
	 * Gets the assembled {@link JobGraph} with a random {@link JobID}.
	 * 获取随机的JobID的JobGraph作业
	 */
	@SuppressWarnings("deprecation")
	public JobGraph getJobGraph() {
		return getJobGraph(null);
	}

	/**
	 * Gets the assembled {@link JobGraph} with a specified {@link JobID}.
	 * 获取指定JobID的JobGraph作业
	 */
	public abstract JobGraph getJobGraph(@Nullable JobID jobID);

	public abstract String getStreamingPlanAsJSON();

	public abstract void dumpStreamingPlanAsJSON(File file) throws IOException;

}
