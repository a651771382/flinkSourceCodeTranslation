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

import org.apache.flink.annotation.Public;

/**
 * The result of submitting a job to a JobManager.
 * 向JobManager提交job的结果
 */
@Public
public class JobSubmissionResult {

	private final JobID jobID;

	public JobSubmissionResult(JobID jobID) {
		this.jobID = jobID;
	}

	/**
	 * Returns the JobID assigned to the job by the Flink runtime.
	 * 返回由Flink运行时分配给作业的jobid
	 *
	 * @return jobID, or null if the job has been executed on a runtime without JobIDs or if the execution failed.
	 * jobid，如果作业在没有jobid的运行时上执行，或者如果执行失败，则为空。
	 */
	public JobID getJobID() {
		return jobID;
	}

	/**
	 * Checks if this JobSubmissionResult is also a JobExecutionResult.
	 * 检查此JobSubmissionResult是否也是JobExecutionResult。
	 * See {@code getJobExecutionResult} to retrieve the JobExecutionResult.
	 * 请参见@code getjobexecutionresult以检索jobexecutionresult。
	 *
	 * @return True if this is a JobExecutionResult, false otherwise
	 * 如果这是JobExecutionResult，则为true；否则为false
	 */
	public boolean isJobExecutionResult() {
		return this instanceof JobExecutionResult;
	}

	/**
	 * Returns the JobExecutionResult if available.
	 * 如果JobExecutionResult可用就返回
	 *
	 * @return The JobExecutionResult
	 * @throws ClassCastException if this is not a JobExecutionResult
	 */
	public JobExecutionResult getJobExecutionResult() {
		if (isJobExecutionResult()) {
			return (JobExecutionResult) this;
		} else {
			throw new ClassCastException("This JobSubmissionResult is not a JobExecutionResult.");
		}
	}
}
