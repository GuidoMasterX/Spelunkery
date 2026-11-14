package com.ordana.spelunkery.fabric;

import com.ordana.spelunkery.Spelunkery;
import com.ordana.spelunkery.reg.ModTags;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

public final class FabricBiomeModifications {

    private static final List<String> M_ORES = List.of(
            "spelunkery:m_ore_coal_lower",
            "spelunkery:m_ore_coal_upper",
            "spelunkery:m_ore_copper",
            "spelunkery:m_ore_copper_large",
            "spelunkery:m_ore_diamond",
            "spelunkery:m_ore_diamond_buried",
            "spelunkery:m_ore_diamond_large",
            "spelunkery:m_ore_diamond_medium",
            "spelunkery:m_ore_gold",
            "spelunkery:m_ore_gold_lower",
            "spelunkery:m_ore_iron_middle",
            "spelunkery:m_ore_iron_small",
            "spelunkery:m_ore_iron_upper",
            "spelunkery:m_ore_lapis",
            "spelunkery:m_ore_lapis_buried",
            "spelunkery:m_ore_redstone",
            "spelunkery:m_ore_redstone_lower"
    );

    private static final List<String> VANILLA_ORES = List.of(
            "minecraft:ore_coal_lower",
            "minecraft:ore_coal_upper",
            "minecraft:ore_copper",
            "minecraft:ore_copper_large",
            "minecraft:ore_diamond",
            "minecraft:ore_diamond_buried",
            "minecraft:ore_diamond_large",
            "minecraft:ore_diamond_medium",
            "minecraft:ore_emerald",
            "minecraft:ore_gold",
            "minecraft:ore_gold_extra",
            "minecraft:ore_gold_lower",
            "minecraft:ore_iron_middle",
            "minecraft:ore_iron_small",
            "minecraft:ore_iron_upper",
            "minecraft:ore_lapis",
            "minecraft:ore_lapis_buried",
            "minecraft:ore_redstone",
            "minecraft:ore_redstone_lower"
    );

    private static final List<String> STONE_PATCHES = List.of(
            "minecraft:ore_andesite_upper",
            "minecraft:ore_andesite_lower",
            "minecraft:ore_granite_upper",
            "minecraft:ore_granite_lower",
            "minecraft:ore_diorite_upper",
            "minecraft:ore_diorite_lower",
            "minecraft:ore_tuff",
            "minecraft:ore_dirt",
            "minecraft:ore_gravel",
            "minecraft:ore_clay"
    );

    private FabricBiomeModifications() {
    }

    public static void register() {
        BiomeModifications.create(Spelunkery.res("worldgen_compat"))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.tag(ModTags.HAS_MORES),
                        context -> addAll(context, M_ORES))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.tag(ModTags.HAS_SALT_NOISE),
                        context -> add(context, "spelunkery:m_ore_emerald"))
                .add(ModificationPhase.ADDITIONS, BiomeSelectors.tag(ModTags.HAS_BADLANDS),
                        context -> add(context, "spelunkery:m_ore_gold_extra"))
                .add(ModificationPhase.REMOVALS, BiomeSelectors.tag(ModTags.HAS_MORES),
                        context -> removeAll(context, VANILLA_ORES))
                .add(ModificationPhase.REMOVALS, BiomeSelectors.tag(ModTags.HAS_STONE_NOISE),
                        context -> removeAll(context, STONE_PATCHES));
    }

    private static void addAll(BiomeModificationContext context, List<String> ids) {
        ids.forEach(id -> add(context, id));
    }

    private static void removeAll(BiomeModificationContext context, List<String> ids) {
        ids.forEach(id -> context.getGenerationSettings().removeFeature(
                GenerationStep.Decoration.UNDERGROUND_ORES, key(id)));
    }

    private static void add(BiomeModificationContext context, String id) {
        context.getGenerationSettings().addFeature(
                GenerationStep.Decoration.UNDERGROUND_ORES, key(id));
    }

    private static ResourceKey<PlacedFeature> key(String id) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.parse(id));
    }
}
