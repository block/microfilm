# Microfilm

Uncompressed PNG images bloat your app's binary. The Android Asset Packaging Tool (AAPT) applies some compression by default, but the compression it applies is suboptimal. You can get better results by compressing the images yourself, but there are a couple of problems with that approach. Each time you add or update an image resource, you have to:
1. Apply compression manually. If you're new to a project, you might not know to do this. Even if you do know, it's easy to forget.
2. Choose compression settings. It's not obvious what settings were used to compress other images in the repo, so it's hard to stay consistent.

Microfilm solves both of these problems by automatically compressing resource images from PNG to WebP (Google's [recommended image format](https://developer.android.com/topic/performance/reduce-apk-size#use-webp)) using explicitly configured compression settings. If Microfilm detects an uncompressed PNG that it doesn't expect, it fails the build and suggests a fix.

## Compression

### Step 1: Add the Microfilm Plugin

In the `build.gradle.kts` file of an Android application or library module, add:

```kotlin
plugins {
  id("xyz.block.microfilm") version "0.1.0"
}
```

### Step 2: Configure the Microfilm Plugin

In the same `build.gradle.kts` file, specify the images that you want to compress, and the compression settings that you want to use.

To use lossless compression for all images, add:
```kotlin
microfilm {
  compress {
    lossless = true
  }
}
```

To use lossless compression for some images, lossy compression for others, and to exclude still others from compression entirely, add:
```kotlin
microfilm {
  compress {
    lossless = true
  }

  compress("**/*_lossy.png") {
    lossless = false
    compressionFactor = 90
  }

  exclude("**/*_excluded.png")
}
```

### Step 3: Add a PNG Image Resource

Add a PNG image to any of the Drawable folders within your module's resources folder (e.g. `res/drawable`, `res/drawable-hdpi`).

### Step 4: Compress the PNG Image Resource

Run the generic compression task with:

```shell
./gradlew my-module:compressMicrofilm
```

Or the compression task for individual source sets with:

```shell
./gradlew my-module:compressMicrofilmDebug
./gradlew my-module:compressMicrofilmRelease
```

The compression task does three things:
1. Moves uncompressed PNG images from the `res` directory to the `microfilm` directory.
2. Generates compressed WebP images in the `res` directory.
3. Generates a manifest file in the `microfilm` directory that tracks the image hashes and the compression settings.

The resulting manifest file will look something like this:
```json
{
  "entries": [
    {
      "sourcePath": "drawable/my_image.png",
      "sourceSha256": "bf4efed5cf7bf4bd245c7a02c2a7a2753621040a729eb2d4eb4a4950fc56324e",
      "compressedPath": "drawable/my_image.webp",
      "compressedSha256": "c8493453b732d871601cfe03a8c1fc9d652817991e8e2aec8ca6a56df9dbde4d",
      "compressor": {
        "name": "cwebp",
        "version": "1.6.0",
        "lossless": true,
        "compressionFactor": null
      }
    }
  ]
}
```

Check this manifest into source control, along with the uncompressed PNG images and the compressed WebP images.

## Verification

Run the generic verification task with:

```shell
./gradlew my-module:verifyMicrofilm
```

Or the verification task for individual source sets with:

```shell
./gradlew my-module:verifyMicrofilmDebug
./gradlew my-module:verifyMicrofilmRelease
```

Verification is wired into the standard `check` task, so you can also simply run:

```shell
./gradlew my-module:check
```

The verification task ensures that the images and the compression settings in the manifest file match what's actually in the module. If a PNG image has been added/changed/removed, or if the compression settings in the Gradle module have changed, then the task will report a failure.

## Modules

* [cwebp](cwebp): The cwebp executable, packaged into platform-specific dependencies (currently Mac and Linux).
* [plugin](plugin): The Gradle plugin, with logic to compress and verify resource images.
* [sample](sample): A sample Android project that demonstrates usage of the plugin.