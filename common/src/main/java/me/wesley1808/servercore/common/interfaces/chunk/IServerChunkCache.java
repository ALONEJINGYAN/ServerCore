package me.wesley1808.servercore.common.interfaces.chunk;

import net.minecraft.server.level.ChunkHolder;

public interface IServerChunkCache {
    void requiresBroadcast(ChunkHolder holder);
}
