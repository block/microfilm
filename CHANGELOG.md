# Change Log

## [Unreleased]
[Unreleased]: https://github.com/block/microfilm/commits/main

## [0.4.0] - 2023-08-12
[0.4.0]: https://github.com/cashapp/paraphrase/releases/tag/0.4.0

New:

- Add a decompress task to restore the pre-Microfilm state of a module
- Allow `--images` flags passed to all tasks to target single images or subsets of images
- Support for Windows

## [0.3.0] - 2023-07-21
[0.3.0]: https://github.com/cashapp/paraphrase/releases/tag/0.3.0

Changed:

- Skip compressing images that are already up-to-date
- Start reporting errors for resource images not covered by a compress or an exclude rule

## [0.2.1] - 2023-06-23
[0.2.1]: https://github.com/cashapp/paraphrase/releases/tag/0.2.1

Changed:

- Support isolated projects

## [0.2.0] - 2023-06-22
[0.2.0]: https://github.com/cashapp/paraphrase/releases/tag/0.2.0

New:

- Add a parameter for the compression method

Changed:

- Disable explicit nulls for manifest serialization
- Verify compression settings in addition to image locations and hashes
- Make the metadata parameter configurable

## [0.1.0] - 2026-06-09
[0.1.0]: https://github.com/block/microfilm/releases/tag/0.1.0

Initial release.
