/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.api.operators;

import org.apache.flink.annotation.Internal;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.streaming.api.graph.StreamConfig;
import org.apache.flink.streaming.api.graph.StreamGraph;
import org.apache.flink.streaming.runtime.streamrecord.StreamRecord;
import org.apache.flink.streaming.runtime.tasks.StreamTask;

import java.io.Serializable;

/**
 * A factory to create {@link StreamOperator}.
 * 创建一个StreamOperator工厂
 *
 * @param <OUT> The output type of the operator
 *              操作的输出类型
 */
@Internal
public interface StreamOperatorFactory<OUT> extends Serializable {

	/**
	 * Create the operator. Sets access to the context and the output.
	 * 创建算子。设置对上下文和输出的访问权限。
	 */
	<T extends StreamOperator<OUT>> T createStreamOperator(
		StreamTask<?, ?> containingTask, StreamConfig config, Output<StreamRecord<OUT>> output);

	/**
	 * Set the chaining strategy for operator factory.
	 * 设置算子工厂的链接策略
	 */
	void setChainingStrategy(ChainingStrategy strategy);

	/**
	 * Get the chaining strategy of operator factory.
	 * 获取算子工厂的链接策略
	 */
	ChainingStrategy getChainingStrategy();

	/**
	 * Is this factory for {@link StreamSource}.
	 * 这是StreamSource的工厂么
	 */
	default boolean isStreamSource() {
		return false;
	}

	/**
	 * Test whether the operator is selective reading one.
	 * 测试算子是否有选择性地阅读
	 */
	boolean isOperatorSelectiveReading();

	/**
	 * If the stream operator need access to the output type information at {@link StreamGraph}
	 * generation. This can be useful for cases where the output type is specified by the returns
	 * method and, thus, after the stream operator has been created.
	 * 如果流算子需要访问StreamGraph（流执行图）生成的输出信息。对于输出类型由returns方法指定的情况很有用，
	 * 因此在创建流运算符之后也是如此。
	 */
	default boolean isOutputTypeConfigurable() {
		return false;
	}

	/**
	 * Is called by the {@link StreamGraph#addOperator} method when the {@link StreamGraph} is
	 * generated. The method is called with the output {@link TypeInformation} which is also used
	 * for the {@link StreamTask} output serializer.
	 * 生成StreamGraph时，调用StreamGraph.addOperator（）方法。调用该方法TypeInformation（）使用输出，
	 * 该输出也用于StreamTask输出序列化程序
	 *
	 * @param type            Output type information of the {@link StreamTask}
	 * @param executionConfig Execution configuration
	 */
	default void setOutputType(TypeInformation<OUT> type, ExecutionConfig executionConfig) {
	}

	/**
	 * If the stream operator need to be configured with the data type they will operate on.
	 * 如果需要使用数据类型配置流算子，则它们将对其进行操作。
	 */
	default boolean isInputTypeConfigurable() {
		return false;
	}

	/**
	 * Is called by the {@link StreamGraph#addOperator} method when the {@link StreamGraph} is
	 * generated.
	 * 生成StreamGraph时，由StreamGraph.addOperator方法调用
	 *
	 * @param type            The data type of the input.
	 * @param executionConfig The execution config for this parallel execution.
	 */
	default void setInputType(TypeInformation<?> type, ExecutionConfig executionConfig) {
	}
}
