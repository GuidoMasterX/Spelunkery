package com.ordana.spelunkery.worldgen.features;

import com.mojang.serialization.Codec;
import com.ordana.spelunkery.worldgen.feature_configs.BlockStripeFeatureConfig;
import com.ordana.spelunkery.worldgen.feature_configs.util.FastNoiseLite;
import com.ordana.spelunkery.worldgen.feature_configs.util.StoneEntry;
import com.ordana.spelunkery.worldgen.feature_configs.util.StonePattern;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

import java.util.List;
import java.util.Random;

public class BlockStripeFeature extends Feature<BlockStripeFeatureConfig> {

    private final FastNoiseLite cellNoise;
    private final FastNoiseLite cellBufferNoise;
    private final FastNoiseLite noise;

    public BlockStripeFeature(Codec<BlockStripeFeatureConfig> codec) {
        super(codec);
        this.cellNoise = createCellNoise(FastNoiseLite.CellularReturnType.CellValue);
        this.cellBufferNoise = createCellNoise(FastNoiseLite.CellularReturnType.Distance2Div);
        this.noise = new FastNoiseLite();
        this.noise.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
        this.noise.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        this.noise.SetFrequency(0.03F);
        this.noise.SetDomainWarpAmp(1.5F);
        this.noise.SetFractalOctaves(1);
    }

    private static FastNoiseLite createCellNoise(FastNoiseLite.CellularReturnType returnType) {
        FastNoiseLite noise = new FastNoiseLite();
        noise.SetNoiseType(FastNoiseLite.NoiseType.Cellular);
        noise.SetCellularReturnType(returnType);
        noise.SetDomainWarpType(FastNoiseLite.DomainWarpType.OpenSimplex2);
        noise.SetRotationType3D(FastNoiseLite.RotationType3D.ImproveXZPlanes);
        noise.SetFrequency(0.01F);
        noise.SetFractalOctaves(1);
        return noise;
    }

    @Override
    public boolean place(FeaturePlaceContext<BlockStripeFeatureConfig> context) {
        BlockStripeFeatureConfig config = context.config();
        BlockPos originPos = context.origin();
        WorldGenLevel worldGenLevel = context.level();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        ChunkAccess cachedChunk = worldGenLevel.getChunk(originPos);
        FastNoiseLite.Vector3 domainWarpedVector = new FastNoiseLite.Vector3(0, 0, 0);
        boolean placed = false;
        var getX = (originPos.getX() & ~15);
        var getZ = (originPos.getZ() & ~15);

        for (int x = getX; x < getX + 16; x++) {
            for (int z = getZ; z < getZ + 16; z++) {
                var heightmap = worldGenLevel.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
                for (int y = chunkGenerator.getMinY(); y < heightmap - config.surfaceOffset; y++) {
                    BlockPos currentPos = new BlockPos(x, y, z);

                    if (config.biomes != null && config.useBiomeFilter) {
                        if (!config.biomes.contains(cachedChunk.getNoiseBiome(QuartPos.fromBlock(currentPos.getX()), QuartPos.fromBlock(currentPos.getY()), QuartPos.fromBlock(currentPos.getZ())))) {
                            continue;
                        }
                    }

                    BlockState currentState = cachedChunk.getBlockState(currentPos);
                    boolean isTarget1 = currentState.is(config.firstTarget);
                    boolean isTarget2 = false;

                    if (config.secondTarget != null && config.secondTargetPlacer != null)
                        isTarget2 = config.useSecondTarget && currentState.is(config.secondTarget);
                    if (!isTarget1 && !isTarget2) continue;

                    domainWarpedVector.x = x;
                    domainWarpedVector.y = y;
                    domainWarpedVector.z = z;
                    noise.DomainWarp(domainWarpedVector);
                    float cellValue = cellNoise.GetNoise(domainWarpedVector.x, domainWarpedVector.y, domainWarpedVector.z);
                    cellValue = (cellValue * 0.5F + 0.5F);

                    float seed = cellValue * 1000000;
                    Random patchRandom = new Random((long) seed);
                    boolean isBlankPatch = (patchRandom.nextFloat() < config.blankPatchChance) || cellBufferNoise.GetNoise(domainWarpedVector.x, domainWarpedVector.y, domainWarpedVector.z) > -0.1;

                    /*
                    if (CommonConfigs.CROSS_SECTION.get()) {
                        if (z < 0 && y < 128)
                            cachedChunk.setBlockState(currentPos, Blocks.BARRIER.defaultBlockState(), false);
                    }
                     */
                    if (!isBlankPatch) {
                        if (!config.useHeightFilter || (y > (cachedChunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - config.bottomOffset))) {

                            List<StoneEntry> patternList = null;
                            if (isTarget1) patternList = config.firstTargetPlacer;
                            else if (isTarget2) patternList = config.secondTargetPlacer;


                            if (patternList != null && !patternList.isEmpty()) {
                                int stoneIndex = Math.min(Mth.floor(cellValue * patternList.size()), patternList.size() - 1);
                                StoneEntry stoneEntry = patternList.get(stoneIndex);
                                StonePattern stonePattern = stoneEntry.getStonePattern();
                                float patternValue = stonePattern.getValue(currentPos);

                                if (stonePattern.shouldPlacePrimaryStone(patternValue)) {
                                    worldGenLevel.setBlock(currentPos, stoneEntry.getPrimaryStoneState(worldGenLevel, currentPos), Block.UPDATE_CLIENTS);
                                    placed = true;
                                } else if (stonePattern.shouldPlaceSecondaryStone(patternValue)) {
                                    worldGenLevel.setBlock(currentPos, stoneEntry.getSecondaryStoneState(worldGenLevel, currentPos), Block.UPDATE_CLIENTS);
                                    placed = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return placed;
    }
}