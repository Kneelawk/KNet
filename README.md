# Kneelawk's Networking Library

KNet is a cross-platform Minecraft networking abstraction and utility library.

KNet is inspired by [LibNetworkStack], but fundamentally different in some key ways:

 * KNet is much more just intended to be an abstraction layer rather than a complete networking overhaul like LNS is.
 * KNet uses Minecraft's [CustomPayload]s instead of passing [PacketByteBuf]s to users directly. This is more in-line with the way Minecraft expects you to do things and will likely work better with future Minecraft code changes.

[LibNetworkStack]: https://github.com/AlexIIL/LibNetworkStack
[CustomPayload]: https://maven.fabricmc.net/docs/yarn-1.20.4+build.3/net/minecraft/network/packet/CustomPayload.html
[PacketByteBuf]: https://maven.fabricmc.net/docs/yarn-1.20.4+build.3/net/minecraft/network/PacketByteBuf.html

## Example Usage

TODO.
