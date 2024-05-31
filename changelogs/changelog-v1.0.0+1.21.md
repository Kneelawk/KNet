Changes:

* Updated to Minecraft 1.21.
* Renamed existing channels to play-channels, because they are used for sending messages during the 'play' phase.
* Made `PayloadHandlingException` unchecked.
* Added family of `NetBuf` buffer types similar to the `NetByteBuf`.
    * When one type of `NetBuf` wraps another one, implementations try to keep the wrapped buffers' partial bytes
      updated.
* Added `config`-phase channels.
* Added dependency on Common Events.
* Added event for enqueueing configuration tasks.
* Added more utility methods to `NetByteBuf` family of buffers.
* Switched sources to Mojmap.
* Optionally allowed contexts to specify a prefix that gets added to contextual channel ids.
