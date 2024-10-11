Changes:

* Split into API and backend modules.
    * Currently there are Fabric, NeoForge, and BadPackets backends.
* Add BadPackets support.
* Changed channel registration API so that it does not rely on explicit use of backends.
    * Now there is a `KNet.Loaded` event that is fired once all backends have been loaded. This provides access to the
      default backend.
