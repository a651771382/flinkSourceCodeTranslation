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

package org.apache.flink.api.common.typeinfo;

import org.apache.flink.annotation.Public;
import org.apache.flink.annotation.PublicEvolving;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.functions.InvalidTypesException;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.util.FlinkRuntimeException;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * TypeInformation is the core class of Flink's type system. Flink requires a type information
 * for all types that are used as input or return type of a user function. This type information
 * class acts as the tool to generate serializers and comparators, and to perform semantic checks
 * such as whether the fields that are used as join/grouping keys actually exist.
 * TypeInformation是flink的类型系统的核心类。Flink需要所有用作用户函数的输入或返回类型的类型信息。
 * 此类型信息类充当生成序列化器和比较器的工具，并执行语义检查，例如是否实际存在用作连接/分组键的字段。
 * <p>
 * <p>The type information also bridges between the programming languages object model and a
 * logical flat schema. It maps fields from the types to columns (fields) in a flat schema.
 * Not all fields from a type are mapped to a separate fields in the flat schema and
 * often, entire types are mapped to one field. It is important to notice that the schema must
 * hold for all instances of a type. For that reason, elements in lists and arrays are not
 * assigned to individual fields, but the lists and arrays are considered to be one field in total,
 * to account for different lengths in the arrays.
 * <p>
 * 类型信息还在编程语言对象模型和逻辑平面模式之间架起桥梁。 它将字段中的字段映射到平面模式中的列（字段）。
 * 并非所有类型的字段都映射到平面模式中的单独字段，并且通常将整个类型映射到一个字段。 请注意，架构必须适用于所有类型的实例。
 * 因此，列表和数组中的元素不会分配给单个字段，但列表和数组总共被视为一个字段，以说明数组中的不同长度。
 * <p>
 * <ul>
 * <li>Basic types are indivisible and are considered a single field.</li>
 * <li>Arrays and collections are one field</li>
 * <li>Tuples and case classes represent as many fields as the class has fields</li>
 * </ul>
 * <p>
 * <p>To represent this properly, each type has an <i>arity</i> (the number of fields it contains
 * directly), and a <i>total number of fields</i> (number of fields in the entire schema of this
 * type, including nested types).
 * <p>
 * <p>Consider the example below:
 * <pre>{@code
 * public class InnerType {
 *   public int id;
 *   public String text;
 * }
 *
 * public class OuterType {
 *   public long timestamp;
 *   public InnerType nestedType;
 * }
 * }</pre>
 * <p>
 * <p>The types "id", "text", and "timestamp" are basic types that take up one field. The "InnerType"
 * has an arity of two, and also two fields totally. The "OuterType" has an arity of two fields,
 * and a total number of three fields ( it contains "id", "text", and "timestamp" through recursive flattening).
 *
 * @param <T> The type represented by this type information.
 */
@Public
public abstract class TypeInformation<T> implements Serializable {

	private static final long serialVersionUID = -7742311969684489493L;

	/**
	 * Checks if this type information represents a basic type.
	 * Basic types are defined in {@link BasicTypeInfo} and are primitives, their boxing types,
	 * Strings, Date, Void, ...
	 * 检查此类型信息是否表示基本类型。基本类型在BasicTypeInfo中定义，是基本类型，它们的装箱类型、字符串、日期、void…。
	 *
	 * @return True, if this type information describes a basic type, false otherwise.
	 * 如果此类型信息描述基本类型，则为true，否则为false。
	 */
	@PublicEvolving
	public abstract boolean isBasicType();

	/**
	 * Checks if this type information represents a Tuple type.
	 * 检查此类型信息是否表示元组类型。
	 * Tuple types are subclasses of the Java API tuples.
	 * 元组类型是Java API元组的子类。
	 *
	 * @return True, if this type information describes a tuple type, false otherwise.
	 */
	@PublicEvolving
	public abstract boolean isTupleType();

	/**
	 * Gets the arity of this type - the number of fields without nesting.
	 * 获取此类型的arity-不嵌套的字段数。
	 *
	 * @return Gets the number of fields in this type without nesting.
	 * 获取此类型中没有嵌套的字段数。
	 */
	@PublicEvolving
	public abstract int getArity();

	/**
	 * Gets the number of logical fields in this type. This includes its nested and transitively nested
	 * fields, in the case of composite types. In the example above, the OuterType type has three
	 * fields in total.
	 * 获取此类型中的逻辑字段数。 在复合类型的情况下，这包括嵌套和可传递嵌套的字段。 在上面的示例中，OuterType类型总共有三个字段。
	 * <p>
	 * <p>The total number of fields must be at least 1. 字段总数必须至少为1
	 *
	 * @return The number of fields in this type, including its sub-fields (for composite types)
	 * 此类型中的字段数，包括其子字段（对于复合类型）
	 */
	@PublicEvolving
	public abstract int getTotalFields();

	/**
	 * Gets the class of the type represented by this type information.
	 * 获取此类型信息表示的类型的类。
	 *
	 * @return The class of the type represented by this type information.
	 * 由此类型信息表示的类型的类。
	 */
	@PublicEvolving
	public abstract Class<T> getTypeClass();

	/**
	 * Optional method for giving Flink's type extraction system information about the mapping
	 * of a generic type parameter to the type information of a subtype. This information is necessary
	 * in cases where type information should be deduced from an input type.
	 * 提供Flink类型提取系统有关将泛型类型参数映射到子类型信息的信息的可选方法。在应该从输入类型推导类型信息的情况下，
	 * 此信息是必需的。
	 * <p>
	 * <p>For instance, a method for a {@link Tuple2} would look like this:
	 * <code>
	 * Map m = new HashMap();
	 * m.put("T0", this.getTypeAt(0));
	 * m.put("T1", this.getTypeAt(1));
	 * return m;
	 * </code>
	 *
	 * @return map of inferred subtypes; it does not have to contain all generic parameters as key;
	 * values may be null if type could not be inferred
	 * 推断子类型的映射；它不必包含所有作为键的泛型参数；
	 * 如果无法推断类型，则值可能为空
	 */
	@PublicEvolving
	public Map<String, TypeInformation<?>> getGenericParameters() {
		// return an empty map as the default implementation
		return Collections.emptyMap();
	}

	/**
	 * Checks whether this type can be used as a key. As a bare minimum, types have
	 * to be hashable and comparable to be keys.
	 * 检查此类型是否可以用作键。作为最低限度的要求，类型必须是可哈希的，并且可以与键进行比较。
	 *
	 * @return True, if the type can be used as a key, false otherwise.
	 */
	@PublicEvolving
	public abstract boolean isKeyType();

	/**
	 * Checks whether this type can be used as a key for sorting.
	 * The order produced by sorting this type must be meaningful.
	 * 检查此类型是否可用作排序键。
	 * 通过对此类型进行排序所产生的顺序必须有意义。
	 */
	@PublicEvolving
	public boolean isSortKeyType() {
		return isKeyType();
	}

	/**
	 * Creates a serializer for the type. The serializer may use the ExecutionConfig
	 * for parameterization.
	 * 为该类型创建一个序列化程序。 序列化程序可以使用ExecutionConfig进行参数化。
	 *
	 * @param config The config used to parameterize the serializer.
	 *               用于参数化序列化器的配置。
	 * @return A serializer for this type.此类型的序列化程序。
	 */
	@PublicEvolving
	public abstract TypeSerializer<T> createSerializer(ExecutionConfig config);

	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	/**
	 * Returns true if the given object can be equaled with this object. If not, it returns false.
	 * 如果给定对象可以与此对象相等，则返回true。 如果不是，则返回false。
	 *
	 * @param obj Object which wants to take part in the equality relation
	 *            想要参与平等关系的对象
	 * @return true if obj can be equaled with this, otherwise false
	 */
	public abstract boolean canEqual(Object obj);

	// ------------------------------------------------------------------------

	/**
	 * Creates a TypeInformation for the type described by the given class.
	 * 为给定类描述的类型创建TypeInformation。
	 * <p>
	 * <p>This method only works for non-generic types. For generic types, use the
	 * {@link #of(TypeHint)} method.
	 * 此方法仅适用于非泛型类型。 对于泛型类型，请使用TypeHint（）方法。
	 *
	 * @param typeClass The class of the type.
	 * @param <T>       The generic type.
	 * @return The TypeInformation object for the type described by the hint.
	 */
	public static <T> TypeInformation<T> of(Class<T> typeClass) {
		try {
			return TypeExtractor.createTypeInfo(typeClass);
		} catch (InvalidTypesException e) {
			throw new FlinkRuntimeException(
				"Cannot extract TypeInformation from Class alone, because generic parameters are missing. " +
					"Please use TypeInformation.of(TypeHint) instead, or another equivalent method in the API that " +
					"accepts a TypeHint instead of a Class. " +
					"For example for a Tuple2<Long, String> pass a 'new TypeHint<Tuple2<Long, String>>(){}'.");
		}
	}

	/**
	 * Creates a TypeInformation for a generic type via a utility "type hint".
	 * 通过实用程序“类型提示”为泛型类型创建TypeInformation。
	 * This method can be used as follows:
	 * 该方法可以使用如下：
	 * <pre>
	 * {@code
	 * TypeInformation<Tuple2<String, Long>> info = TypeInformation.of(new TypeHint<Tuple2<String, Long>>(){});
	 * }
	 * </pre>
	 *
	 * @param typeHint The hint for the generic type.
	 * @param <T>      The generic type.
	 * @return The TypeInformation object for the type described by the hint.
	 */
	public static <T> TypeInformation<T> of(TypeHint<T> typeHint) {
		return typeHint.getTypeInfo();
	}
}
