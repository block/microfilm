/*
 * Copyright (C) 2026 Block, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.block.microfilm

import java.nio.file.FileSystems
import java.nio.file.Path

/** Returns true if this path matches the given glob pattern. */
internal fun String.matchesGlob(pattern: String): Boolean =
  FileSystems.getDefault().getPathMatcher("glob:$pattern").matches(Path.of(this))

/** Returns true if this path matches any of the given glob patterns. */
internal fun String.matchesGlobs(patterns: List<String>): Boolean = patterns.any { pattern ->
  matchesGlob(pattern = pattern)
}

/** Returns true if this path matches the given glob pattern. */
internal fun okio.Path.matchesGlob(pattern: String): Boolean =
  toString().matchesGlob(pattern = pattern)

/** Returns true if this path matches the given glob pattern. */
internal fun okio.Path.matchesGlobs(patterns: List<String>): Boolean = patterns.any { pattern ->
  matchesGlob(pattern = pattern)
}

/** Returns true if this path matches the given glob pattern. */
internal fun List<okio.Path>.matchesGlobs(patterns: List<String>): Boolean = any { path ->
  path.matchesGlobs(patterns = patterns)
}
