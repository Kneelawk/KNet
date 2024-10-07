# Kneelawk's Networking Library

[![Github Release Status]][Github Release] [![Maven Status]][Maven] [![Javadoc Badge]][Javadoc] [![Discord Badge]][Discord] [![Ko-fi Badge]][Ko-fi]

[Github Release Status]: https://img.shields.io/github/v/release/Kneelawk/KNet?include_prereleases&sort=semver&display_name=release&style=flat-square&logo=github

[Github Release]: https://github.com/Kneelawk/KNet/releases/latest

[Maven Status]: https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fkneelawk.com%2Fmaven%2Fcom%2Fkneelawk%2Fknet-xplat-intermediary%2Fmaven-metadata.xml&style=flat-square&logo=apachemaven&logoColor=blue

[Maven]: https://kneelawk.com/maven#com/kneelawk/knet-xplat-intermediary

[Javadoc Badge]: https://img.shields.io/badge/-javadoc-green?style=flat-square

[Javadoc]: https://kneelawk.com/docs#knet

[Discord Badge]: https://img.shields.io/discord/988299232731607110?style=flat-square&logo=discord

[Discord]: https://discord.gg/6vgpHcKmxg

[Ko-fi Badge]: https://img.shields.io/badge/ko--fi-donate-blue?style=flat-square&logo=kofi

[Ko-fi]: https://ko-fi.com/kneelawk

KNet is a cross-platform Minecraft networking abstraction and utility library.

KNet is inspired by [LibNetworkStack], but fundamentally different in some key ways:

* KNet is much more just intended to be an abstraction layer rather than a complete networking overhaul like LNS is.
* KNet uses Minecraft's [CustomPacketPayload]s instead of passing [FriendlyByteBuf]s to users directly. This is more in-line
  with the way Minecraft expects you to do things and will likely work better with future Minecraft code changes.

[LibNetworkStack]: https://github.com/AlexIIL/LibNetworkStack

[CustomPacketPayload]: https://maven.kneelawk.com/javadoc/releases/com/kneelawk/javadoc-mc/javadoc-mc-mojmap-vanilla-loom/1.21.1+parchment.1.21-2024.07.28-build.1/raw/net/minecraft/network/protocol/common/custom/CustomPacketPayload.html

[FriendlyByteBuf]: https://maven.kneelawk.com/javadoc/releases/com/kneelawk/javadoc-mc/javadoc-mc-mojmap-vanilla-loom/1.21.1+parchment.1.21-2024.07.28-build.1/raw/net/minecraft/network/FriendlyByteBuf.html

## Example Usage

TODO.
