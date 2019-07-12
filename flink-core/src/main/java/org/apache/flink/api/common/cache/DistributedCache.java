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

package org.apache.flink.api.common.cache;

import org.apache.flink.annotation.Public;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;

import java.io.File;
import java.io.Serializable;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * DistributedCache provides static methods to write the registered cache files into job configuration or decode
 * them from job configuration. It also provides user access to the file locally.
 * DistributedCache提供静态方法将已注册的缓存文件写入作业配置或从作业配置解码它们。 它还为用户提供本地文件的访问权限。
 */
@Public
public class DistributedCache {

	/**
	 * Meta info about an entry in {@link DistributedCache}.
	 * 有关{@linkDistributedCache}中条目的元信息。
	 * <p>
	 * <p>Entries have different semantics for local directories depending on where we are in the job-submission process.
	 * After registration through the API {@code filePath} denotes the original directory.
	 * After the upload to the cluster (which includes zipping the directory), {@code filePath} denotes the (server-side) copy of the zip.
	 * 条目对于本地目录具有不同的语义，具体取决于我们在作业提交过程中的位置。通过API {@code filePath}注册后表示原始目录。
	 * 上传到集群（包括压缩目录）后，{@ code filePath}表示zip的（服务器端）副本。
     */
	public static class DistributedCacheEntry implements Serializable {

		//文件路径
		public String filePath;
		//是否可执行
		public Boolean isExecutable;
		//是否已压缩
		public boolean isZipped;

		public byte[] blobKey;

		/**
		 * Client-side constructor used by the API for initial registration.
		 * API用于初始化的客户端构造函数。
		 */
		public DistributedCacheEntry(String filePath, Boolean isExecutable) {
			this(filePath, isExecutable, null);
		}

		/**
		 * Client-side constructor used during job-submission for zipped directory.
		 * 在压缩目录的作业提交期间使用的客户端构造函数。
		 */
		public DistributedCacheEntry(String filePath, boolean isExecutable, boolean isZipped) {
			this(filePath, isExecutable, null, isZipped);
		}

		/**
		 * Server-side constructor used during job-submission for zipped directories.
		 * 在压缩目录的作业提交期间使用的客户端构造函数。
		 */
		public DistributedCacheEntry(String filePath, Boolean isExecutable, byte[] blobKey, boolean isZipped) {
			this.filePath = filePath;
			this.isExecutable = isExecutable;
			this.blobKey = blobKey;
			this.isZipped = isZipped;
		}

		/**
		 * Server-side constructor used during job-submission for files.
		 * 在作业提交期间用于文件的服务器端构造函数。
		 */
		public DistributedCacheEntry(String filePath, Boolean isExecutable, byte[] blobKey) {
			this(filePath, isExecutable, blobKey, false);
		}

		@Override
		public String toString() {
			return "DistributedCacheEntry{" +
				"filePath='" + filePath + '\'' +
				", isExecutable=" + isExecutable +
				", isZipped=" + isZipped +
				", blobKey=" + Arrays.toString(blobKey) +
				'}';
		}
	}

	// ------------------------------------------------------------------------

	//缓存复制任务
	private final Map<String, Future<Path>> cacheCopyTasks;

	public DistributedCache(Map<String, Future<Path>> cacheCopyTasks) {
		this.cacheCopyTasks = cacheCopyTasks;
	}

	// ------------------------------------------------------------------------

	public File getFile(String name) {
		if (name == null) {
			throw new NullPointerException("name must not be null");
		}

		Future<Path> future = cacheCopyTasks.get(name);
		if (future == null) {
			throw new IllegalArgumentException("File with name '" + name + "' is not available." +
				" Did you forget to register the file?");
		}

		try {
			final Path path = future.get();
			URI tmp = path.makeQualified(path.getFileSystem()).toUri();
			return new File(tmp);
		} catch (ExecutionException e) {
			throw new RuntimeException("An error occurred while copying the file.", e.getCause());
		} catch (Exception e) {
			throw new RuntimeException("Error while getting the file registered under '" + name +
				"' from the distributed cache", e);
		}
	}

	// ------------------------------------------------------------------------
	//  Utilities to read/write cache files from/to the configuration
	// ------------------------------------------------------------------------

	public static void writeFileInfoToConfig(String name, DistributedCacheEntry e, Configuration conf) {
		int num = conf.getInteger(CACHE_FILE_NUM, 0) + 1;
		conf.setInteger(CACHE_FILE_NUM, num);
		conf.setString(CACHE_FILE_NAME + num, name);
		conf.setString(CACHE_FILE_PATH + num, e.filePath);
		conf.setBoolean(CACHE_FILE_EXE + num, e.isExecutable || new File(e.filePath).canExecute());
		conf.setBoolean(CACHE_FILE_DIR + num, e.isZipped || new File(e.filePath).isDirectory());
		if (e.blobKey != null) {
			conf.setBytes(CACHE_FILE_BLOB_KEY + num, e.blobKey);
		}
	}

	public static Set<Entry<String, DistributedCacheEntry>> readFileInfoFromConfig(Configuration conf) {
		int num = conf.getInteger(CACHE_FILE_NUM, 0);
		if (num == 0) {
			return Collections.emptySet();
		}

		Map<String, DistributedCacheEntry> cacheFiles = new HashMap<String, DistributedCacheEntry>();
		for (int i = 1; i <= num; i++) {
			String name = conf.getString(CACHE_FILE_NAME + i, null);
			String filePath = conf.getString(CACHE_FILE_PATH + i, null);
			boolean isExecutable = conf.getBoolean(CACHE_FILE_EXE + i, false);
			boolean isDirectory = conf.getBoolean(CACHE_FILE_DIR + i, false);

			byte[] blobKey = conf.getBytes(CACHE_FILE_BLOB_KEY + i, null);
			cacheFiles.put(name, new DistributedCacheEntry(filePath, isExecutable, blobKey, isDirectory));
		}
		return cacheFiles.entrySet();
	}

	//缓存文件编号
	private static final String CACHE_FILE_NUM = "DISTRIBUTED_CACHE_FILE_NUM";

	//缓存文件名
	private static final String CACHE_FILE_NAME = "DISTRIBUTED_CACHE_FILE_NAME_";

	//缓存文件路径
	private static final String CACHE_FILE_PATH = "DISTRIBUTED_CACHE_FILE_PATH_";

	//缓存文件exe
	private static final String CACHE_FILE_EXE = "DISTRIBUTED_CACHE_FILE_EXE_";

	//缓存文件目录
	private static final String CACHE_FILE_DIR = "DISTRIBUTED_CACHE_FILE_DIR_";

	//缓存文件blob key
	private static final String CACHE_FILE_BLOB_KEY = "DISTRIBUTED_CACHE_FILE_BLOB_KEY_";
}
