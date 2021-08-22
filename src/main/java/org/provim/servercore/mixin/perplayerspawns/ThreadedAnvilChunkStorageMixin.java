package org.provim.servercore.mixin.perplayerspawns;

import com.mojang.datafixers.DataFixer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import org.provim.servercore.interfaces.ServerPlayerEntityInterface;
import org.provim.servercore.interfaces.TACSInterface;
import org.provim.servercore.utils.patches.PlayerMobDistanceMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin implements TACSInterface {
    PlayerMobDistanceMap playerMobDistanceMap = null;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void init(ServerWorld world, LevelStorage.Session session, DataFixer dataFixer, StructureManager structureManager, Executor executor, ThreadExecutor<Runnable> mainThreadExecutor, ChunkProvider chunkProvider, ChunkGenerator chunkGenerator, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier<PersistentStateManager> persistentStateManagerFactory, int viewDistance, boolean dsync, CallbackInfo ci) {
        this.playerMobDistanceMap = new PlayerMobDistanceMap();
    }

    @Override
    public PlayerMobDistanceMap getPlayerMobDistanceMap() {
        return playerMobDistanceMap;
    }

    /**
     * Adds entity to the per-player mobcap of nearby players when not persistent.
     * Reduced iterations by not adding misc mobs, since they don't have a mobcap.
     *
     * @param entity: The entity to add
     */

    @Override
    public void updateMobCounts(Entity entity) {
        if (entity.getType().getSpawnGroup() != SpawnGroup.MISC && entity instanceof MobEntity mob && !(mob.isPersistent() || mob.cannotDespawn())) {
            int chunkX = (int) Math.floor(entity.getPos().getX()) >> 4;
            int chunkZ = (int) Math.floor(entity.getPos().getZ()) >> 4;
            int index = entity.getType().getSpawnGroup().ordinal();

            for (ServerPlayerEntity player : this.playerMobDistanceMap.getPlayersInRange(chunkX, chunkZ)) {
                ++((ServerPlayerEntityInterface) player).getMobCounts()[index];
            }
        }
    }

    @Override
    public int getMobCountNear(ServerPlayerEntity player, SpawnGroup group) {
        return ((ServerPlayerEntityInterface) player).getMobCounts()[group.ordinal()];
    }
}