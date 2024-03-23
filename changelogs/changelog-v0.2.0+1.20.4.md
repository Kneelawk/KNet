Changes:

* Added the `NetByteBuf` from LibNetworkStack.
* Migrated most things that were using `PacketByteBuf`/`FriendlyByteBuf` over to using `NetByteBuf`.
* Added a `Palette<T>` type for managing palettes in packets.
* Under common use, this version is source-compatible with `v0.1.0`.
