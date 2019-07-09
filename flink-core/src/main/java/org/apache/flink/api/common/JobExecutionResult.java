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
import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.util.OptionalFailure;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 作业执行结果
 * The result of a job execution. Gives access to the execution time of the job,
 * and to all accumulators created by this job.
 * 作业执行的结果。 可以访问作业的执行时间以及此作业创建的所有累加器。
 */
@Public
public class JobExecutionResult extends JobSubmissionResult {

	//网络时间
	private final long netRuntime;

	//累加结果
	private final Map<String, OptionalFailure<Object>> accumulatorResults;

	/**
	 * Creates a new JobExecutionResult.
	 * 创建新的JobExecutionResult。
	 *
	 * @param jobID        The job's ID. 作业的id
	 * @param netRuntime   The net runtime of the job (excluding pre-flight phase like the optimizer) in milliseconds
	 *                     作业的运行时间（不包括优化程序等飞行前阶段），单位为毫秒
	 * @param accumulators A map of all accumulators produced by the job.
	 *                     作业生成的所有累加器的映射
	 */
	public JobExecutionResult(JobID jobID, long netRuntime, Map<String, OptionalFailure<Object>> accumulators) {
		super(jobID);
		this.netRuntime = netRuntime;

		if (accumulators != null) {
			this.accumulatorResults = accumulators;
		} else {
			this.accumulatorResults = Collections.emptyMap();
		}
	}

	/**
	 * Gets the net execution time of the job, i.e., the execution time in the parallel system,
	 * without the pre-flight steps like the optimizer.
	 * 获取作业的净执行时间，即并行系统中的执行时间，而不包括优化程序之类的飞行前步骤。
	 *
	 * @return The net execution time in milliseconds. 以毫秒为单位的净执行时间。
	 */
	public long getNetRuntime() {
		return this.netRuntime;
	}

	/**
	 * Gets the net execution time of the job, i.e., the execution time in the parallel system,
	 * without the pre-flight steps like the optimizer in a desired time unit.
	 * 获取作业的净执行时间，即并行系统中的执行时间，而不需要飞行前步骤（如所需时间单位中的优化器）。
	 *
	 * @param desiredUnit the unit of the <tt>NetRuntime</tt>
	 * @return The net execution time in the desired unit.
	 */
	public long getNetRuntime(TimeUnit desiredUnit) {
		return desiredUnit.convert(getNetRuntime(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Gets the accumulator with the given name. Returns {@code null}, if no accumulator with
	 * that name was produced.
	 * 获取具有给定名称的累加器。 如果没有生成具有该名称的累加器，则返回null
	 *
	 * @param accumulatorName The name of the accumulator. 累加器的名称。
	 * @param <T>             The generic type of the accumulator value.累加器值的泛型类型。
	 * @return The value of the accumulator with the given name. 具有给定名称的累加器的值。
	 */
	@SuppressWarnings("unchecked")
	public <T> T getAccumulatorResult(String accumulatorName) {
		return (T) this.accumulatorResults.get(accumulatorName).getUnchecked();
	}

	/**
	 * Gets all accumulators produced by the job. The map contains the accumulators as
	 * mappings from the accumulator name to the accumulator value.
	 * 获取作业生成的所有累加器。该映射将累加器作为从累加器名称到累加器值的映射来包含。
	 *
	 * @return A map containing all accumulators produced by the job.包含作业生成的所有累加器的映射。
	 */
	public Map<String, Object> getAllAccumulatorResults() {
		return accumulatorResults.entrySet()
			.stream()
			.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getUnchecked()));
	}

	/**
	 * Gets the accumulator with the given name as an integer.
	 * 获取具有给定名称的整数的累加器。
	 *
	 * @param accumulatorName Name of the counter 计数器的累加器名称
	 * @return Result of the counter, or null if the counter does not exist 计数器的结果，如果计数器不存在，则返回null
	 * @throws java.lang.ClassCastException Thrown, if the accumulator was not aggregating a {@link java.lang.Integer}
	 *                                      如果累加器没有聚集{java.lang.Integer} 则抛出java.lang.ClassCastException异常
	 * @deprecated Will be removed in future versions. Use {@link #getAccumulatorResult} instead.
	 * 将在将来的版本中删除。使用getAccumutorresult。
	 */
	@Deprecated
	@PublicEvolving
	public Integer getIntCounterResult(String accumulatorName) {
		Object result = this.accumulatorResults.get(accumulatorName).getUnchecked();
		if (result == null) {
			return null;
		}
		if (!(result instanceof Integer)) {
			throw new ClassCastException("Requested result of the accumulator '" + accumulatorName
				+ "' should be Integer but has type " + result.getClass());
		}
		return (Integer) result;
	}

	/**
	 * Returns a dummy object for wrapping a JobSubmissionResult.
	 * 返回一个用于包装JobSubmissionResult的虚拟对象。
	 *
	 * @param result The SubmissionResult result 提交结果
	 * @return a JobExecutionResult 作业执行结果
	 * @deprecated Will be removed in future versions.
	 * 将在将来的版本中删除。
	 */
	@Deprecated
	public static JobExecutionResult fromJobSubmissionResult(JobSubmissionResult result) {
		return new JobExecutionResult(result.getJobID(), -1, null);
	}
}
