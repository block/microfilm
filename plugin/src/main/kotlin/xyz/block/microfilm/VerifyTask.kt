package xyz.block.microfilm

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class VerifyTask : DefaultTask() {
  @TaskAction
  fun verify() {
    // TODO
  }
}
