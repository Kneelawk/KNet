Changes:

* Updated to Minecraft 1.20.5.
* Renamed existing channels to play-channels, because they are used for sending messages during the 'play' phase.
* Made `PayloadHandlingException` unchecked.
* Added family of `NetBuf` buffer types similar to the `NetByteBuf`.
* Added `config`-phase channels.
* Added dependency on Common Events.
* Added event for enqueueing configuration tasks.
* Added more utility methods to `NetByteBuf` family of buffers.
