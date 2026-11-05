# mc_core_common

Minecraft-facing code (and resources) that compiles **unchanged** against every supported Minecraft version and
loader. It has no build of its own: each `mc_<version>_common` module adds these folders to its source set, so the
classes are compiled (and checked) once per Minecraft version.

Shared code must not name a type that differs between versions (e.g. `ResourceLocation`/`Identifier`).
