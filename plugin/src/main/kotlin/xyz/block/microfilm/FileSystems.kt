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

import okio.FileSystem
import okio.Path
import okio.buffer

/**
 * Returns the results of [listRecursively] if the given directory exists, or an empty sequence if
 * it does not.
 */
internal fun FileSystem.listRecursivelyOrEmpty(dir: Path): Sequence<Path> =
  if (metadataOrNull(path = dir)?.isDirectory == true) {
    listRecursively(dir = dir)
  } else {
    emptySequence()
  }

/** Returns the SHA256 hash of the file at the given path. */
internal fun FileSystem.sha256(file: Path) =
  source(file = file).buffer().use { source -> source.readByteString().sha256().hex() }
