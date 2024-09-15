package com.github.alexthe666.iceandfire;

import com.github.alexthe666.iceandfire.util.IafMathHelper;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.*;

@Config(modid = IceAndFire.MODID)
public class IceAndFireConfig {

	@Config.Comment("Config Options for World Gen")
	@Config.Name("World Gen Config")
	public static final WorldGenConfig WORLDGEN = new WorldGenConfig();

	@Config.Comment("Config Options for Entity Spawning")
	@Config.Name("Entity Spawning Config")
	public static final EntitySpawningConfig ENTITY_SPAWNING = new EntitySpawningConfig();

	@Config.Comment("Config Options for Dragon Settings")
	@Config.Name("Dragon Config")
	public static final DragonConfig DRAGON_SETTINGS = new DragonConfig();

	@Config.Comment("Config Options for Other Entities")
	@Config.Name("Entity Config")
	public static final EntityConfig ENTITY_SETTINGS = new EntityConfig();

	@Config.Comment("Config Options for Misc Settings")
	@Config.Name("Misc Config")
	public static final MiscConfig MISC_SETTINGS = new MiscConfig();

	@Config.Comment("Config Options for Clientside")
	@Config.Name("Client Config")
	public static final ClientConfig CLIENT_SETTINGS = new ClientConfig();

	public static class WorldGenConfig {

		@Config.Comment("All InF Chunk Gen Spawning and Generation will be disabled in listed dimensions")
		@Config.Name("InF Chunk Gen Dimension Blacklist")
		public int[] chunkGenBlacklist = new int[] {0};

		@Config.Comment("Changes InF Chunk Gen Dimension Blacklist to a Whitelist")
		@Config.Name("InF Chunk Gen Dimension Use Whitelist")
		public boolean chunkGenWhitelist = true;

		@Config.Comment("Minimum distance from spawn for dangerous world gen to begin generating (Dragons, Cyclops, etc)")
		@Config.Name("Dangerous World Gen Minimum Spawn Distance")
		@Config.RangeInt(min = 0, max = 10000)
		public int dangerousWorldGenDistanceLimit = 200;

		@Config.Comment("Should InF generate Glacier biomes")
		@Config.Name("Generate Glacier Biomes")
		public boolean spawnGlaciers = true;

		@Config.Comment("Spawn weight of Glacier biomes, larger number is more common")
		@Config.Name("Glacier Biome Spawn Weight")
		@Config.RangeInt(min = 1, max = 10000)
		public int glacierSpawnChance = 4;

		@Config.Comment("Should InF generate copper ore")
		@Config.Name("Generate Copper Ore")
		public boolean generateCopperOre = true;

		@Config.Comment("Should InF generate silver ore")
		@Config.Name("Generate Silver Ore")
		public boolean generateSilverOre = true;

		@Config.Comment("Should InF generate amethyst ore")
		@Config.Name("Generate Amethyst Ore")
		public boolean generateAmethystOre = true;

		@Config.Comment("Should InF generate sapphire ore")
		@Config.Name("Generate Sapphire Ore")
		public boolean generateSapphireOre = true;

		@Config.Comment("Randomly generate already dead dragon skeletons in the world")
		@Config.Name("Generate Dragon Skeletons")
		public boolean generateDragonSkeletons = true;

		@Config.Comment("Chance to generate dragon skeletons per chunk, 1 in N chance")
		@Config.Name("Generate Dragon Skeletons Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int generateDragonSkeletonChance = 300;

		@Config.Comment("The maximum life stage that a dragon skeleton can generate as")
		@Config.Name("Generate Dragon Skeletons Max Stage")
		@Config.RangeInt(min = 0, max = 5)
		public int generateDragonSkeletonMaximumStage = 5;

		@Config.Comment("Should InF generate dragon caves")
		@Config.Name("Generate Dragon Caves")
		public boolean generateDragonDens = true;

		@Config.Comment("Chance to generate dragon dens per chunk, 1 in N chance")
		@Config.Name("Generate Dragon Caves Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int generateDragonDenChance = 180;

		@Config.Comment("Chance per block that gold will generate in Dragon Dens (1 in N chance)")
		@Config.Name("Dragon Den Gold Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int dragonDenGoldAmount = 4;

		@Config.Comment("Ratio of stone to ore in dragon dens, large number is less ore")
		@Config.Name("Dragon Den Ore Ratio")
		@Config.RangeInt(min = 1, max = 10000)
		public int oreToStoneRatioForDragonCaves = 45;

		@Config.Comment("Should InF generate dragon roosts")
		@Config.Name("Generate Dragon Roosts")
		public boolean generateDragonRoosts = true;

		@Config.Comment("Chance to generate dragon roosts per chunk, 1 in N chance")
		@Config.Name("Generate Dragon Roosts Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int generateDragonRoostChance = 360;

		@Config.Comment("Dragon Dens and Roosts will not generate in these named biomes (Takes priority over other options)")
		@Config.Name("All Dragon Den and Roost Disabled Biome Names")
		public String[] generateDragonDisabledBiomeNames = {""};

		@Config.Comment("Dragon Dens and Roosts will not generate in these biome types (Lower priority than specific dragon type biome names)")
		@Config.Name("All Dragon Den and Roost Disabled Biome Types")
		public String[] generateDragonDisabledBiomeTypes = {""};

		@Config.Comment("Fire Dragon Dens and Roosts will additionally generate in these named biomes (Takes priority over disabled biome types)")
		@Config.Name("Fire Dragon Den and Roost Enabled Biome Names")
		public String[] generateFireDragonEnabledBiomeNames = {""};

		@Config.Comment("Ice Dragon Dens and Roosts will additionally generate in these named biomes (Takes priority over disabled biome types)")
		@Config.Name("Ice Dragon Den and Roost Enabled Biome Names")
		public String[] generateIceDragonEnabledBiomeNames = {""};

		@Config.Comment("Lightning Dragon Dens and Roosts will additionally generate in these named biomes (Takes priority over disabled biome types)")
		@Config.Name("Lightning Dragon Den and Roost Enabled Biome Names")
		public String[] generateLightningDragonEnabledBiomeNames = {""};

		@Config.Comment("Fire Dragon Dens and Roosts will generate in these biome types")
		@Config.Name("Fire Dragon Den and Roost Enabled Biome Types")
		public String[] generateFireDragonEnabledBiomeTypes = {""};

		@Config.Comment("Ice Dragon Dens and Roosts will generate in these biome types")
		@Config.Name("Ice Dragon Den and Roost Enabled Biome Types")
		public String[] generateIceDragonEnabledBiomeTypes = {""};

		@Config.Comment("Lightning Dragon Dens and Roosts will generate in these biome types")
		@Config.Name("Lightning Dragon Den and Roost Enabled Biome Types")
		public String[] generateLightningDragonEnabledBiomeTypes = {"JUNGLE", "SAVANNA", "MESA"};

		@Config.Comment("Fire Dragon Dens and Roosts will not generate in these biome types")
		@Config.Name("Fire Dragon Den and Roost Disabled Biome Types")
		public String[] generateFireDragonDisabledBiomeTypes = {"COLD", "SNOWY", "WET", "OCEAN", "RIVER"};

		@Config.Comment("Ice Dragon Dens and Roosts will not generate in these biome types")
		@Config.Name("Ice Dragon Den and Roost Disabled Biome Types")
		public String[] generateIceDragonDisabledBiomeTypes = {""};

		@Config.Comment("Lightning Dragon Dens and Roosts will not generate in these biome types")
		@Config.Name("Lightning Dragon Den and Roost Disabled Biome Types")
		public String[] generateLightningDragonDisabledBiomeTypes = {""};

		@Config.Comment("Chance for Dragon Roosts to generate in the named biome, in the format name=chance (Overrides general Dragon Roost Chance, 1 in N chance)")
		@Config.Name("Generate Dragon Roosts Biome Name Chance")
		public String[] generateDragonRoostChanceForBiome = {""};

		@Config.Comment("Chance for Dragon Dens to generate in the named biome, in the format name=chance (Overrides general Dragon Den Chance, 1 in N chance)")
		@Config.Name("Generate Dragon Dens Biome Name Chance")
		public String[] generateDragonDenChanceForBiome = {""};

		@Config.Comment("Dragons and related generation will not spawn in these dimensions")
		@Config.Name("Dragon Dimension Blacklist")
		public int[] dragonDimensionBlacklistedDimensions = new int[] {0};

		@Config.Comment("If true, treat the Dragon Dimension Blacklist as a Whitelist instead")
		@Config.Name("Dragon Dimension Use Whitelist")
		public boolean dragonDimensionWhitelist = true;

		@Config.Comment("Should InF generate snow villages")
		@Config.Name("Generate Snow Villages")
		public boolean generateSnowVillages = true;

		@Config.Comment("Chance to generate snow villages per chunk, 1 in N chance")
		@Config.Name("Generate Snow Villages Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int generateSnowVillageChance = 100;

		@Config.Comment("Snow Villages and related generation will not spawn in these dimensions")
		@Config.Name("Snow Village Dimension Blacklist")
		public int[] snowVillageBlacklistedDimensions = new int[] {0};

		@Config.Comment("If true, treat the Snow Village Dimension Blacklist as a Whitelist instead")
		@Config.Name("Snow Village Dimension Use Whitelist")
		public boolean snowVillageWhitelist = true;

		@Config.Comment("Should InF generate Gorgon Temples and Gorgons")
		@Config.Name("Generate Gorgon Temple")
		public boolean generateGorgonTemple = true;

		@Config.Comment("Chance per chunk for Gorgon temples to generate, 1 in N chance")
		@Config.Name("Generate Gorgon Temple Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int generateGorgonChance = 75;

		@Config.Comment("Should InF generate Pixie Villages and pixies")
		@Config.Name("Generate Pixie Villages")
		public boolean generatePixieVillages = true;

		@Config.Comment("Chance per chunk for Pixie Villages to generate, 1 in N chance")
		@Config.Name("Generate Pixie Villages Chance")
		@Config.RangeInt(min = 1, max  = 10000)
		public int generatePixieChance = 60;

		@Config.Comment("Size of Pixie Villages to generate")
		@Config.Name("Pixie Village Generation Size")
		@Config.RangeInt(min = 1, max = 10000)
		public int pixieVillageSize = 5;

		@Config.Comment("Should InF generate Cyclops Caves and Cyclops")
		@Config.Name("Generate Cyclops Caves")
		public boolean generateCyclopsCaves = true;

		@Config.Comment("Should InF generate Wandering Cyclops")
		@Config.Name("Generate Wandering Cyclops")
		public boolean generateWanderingCyclops = true;

		@Config.Comment("Chance per chunk for Cyclops Caves to generate, 1 in N chance")
		@Config.Name("Generate Cyclops Caves Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int generateCyclopsChance = 170;

		@Config.Comment("Chance per chunk for Wandering Cyclops to generate, 1 in N chance")
		@Config.Name("Generate Wandering Cyclops Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int generateWanderingCyclopsChance = 900;

		@Config.Comment("Should InF generate Siren Islands and Sirens")
		@Config.Name("Generate Siren Islands")
		public boolean generateSirenIslands = true;

		@Config.Comment("Chance per chunk for Siren Islands to generate, 1 in N chance")
		@Config.Name("Generate Siren Islands Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int generateSirenChance = 300;

		@Config.Comment("Should InF generate Myrmex Colonies")
		@Config.Name("Generate Myrmex Colonies")
		public boolean generateMyrmexColonies = true;

		@Config.Comment("Myrmex Colonies will not generate in these named biomes")
		@Config.Name("Myrmex Disabled Biome Names")
		public String[] generateMyrmexDisabledBiomeNames = {""};

		@Config.Comment("Myrmex Colonies will not generate in these biome types")
		@Config.Name("Myrmex Disabled Biome Types")
		public String[] generateMyrmexDisabledBiomeTypes = {""};

		@Config.Comment("Chance per chunk for Myrmex Colonies to generate, 1 in N chance")
		@Config.Name("Generate Myrmex Colony Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int myrmexColonyGenChance = 150;

		@Config.Comment("Size of Myrmex Colonies to generate")
		@Config.Name("Myrmex Colony Generation Size")
		@Config.RangeInt(min = 1, max = 10000)
		public int myrmexColonySize = 80;

		@Config.Comment("Should InF generate Hydra Caves")
		@Config.Name("Generate Hydra Caves")
		public boolean generateHydraCaves = true;

		@Config.Comment("Chance per chunk for Hydra Caves to generate, 1 in N chance")
		@Config.Name("Generate Hydra Caves Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int generateHydrasChance = 200;
	}

	public static class EntitySpawningConfig {

		@Config.Comment("Should InF spawn Hippocampus on generation")
		@Config.Name("Generate Hippocampus")
		public boolean spawnHippocampus = true;

		@Config.Comment("Chance per chunk for Hippocampus to spawn, 1 in N chance")
		@Config.Name("Hippocampus Generate Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int hippocampusSpawnChance = 70;

		@Config.Comment("Should InF spawn Stymphalian Birds on generation")
		@Config.Name("Generate Stymphalian Birds")
		public boolean spawnStymphalianBirds = true;

		@Config.Comment("Chance per chunk for Stymphalian Birds to spawn, 1 in N chance")
		@Config.Name("Stymphalian Bird Generate Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int stymphalianBirdSpawnChance = 100;

		@Config.Comment("Should InF spawn Sea Serpents on generation")
		@Config.Name("Generate Sea Serpent")
		public boolean spawnSeaSerpents = true;

		@Config.Comment("Chance per chunk for Sea Serpents to spawn, 1 in N chance")
		@Config.Name("Sea Serpent Generate Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int seaSerpentSpawnChance = 200;

		@Config.Comment("Should InF spawn Hippogryphs")
		@Config.Name("Spawn Hippogryphs")
		public boolean spawnHippogryphs = true;

		@Config.Comment("Hippogrpyh spawn weight, larger number is more common")
		@Config.Name("Hippogryph Spawn Weight")
		@Config.RangeInt(min = 1, max = 10000)
		public int hippogryphSpawnRate = 2;

		@Config.Comment("Should InF spawn Deathworms")
		@Config.Name("Spawn Deathworms")
		public boolean spawnDeathWorm = true;

		@Config.Comment("Deathworm spawn weight, larger number is more common")
		@Config.Name("Deathworm Spawn Weight")
		@Config.RangeInt(min = 1, max = 10000)
		public int deathWormSpawnRate = 2;

		@Config.Comment("Deathworm spawn check recheck amount, higher number is lower chance to spawn")
		@Config.Name("Deathworm Spawn Check Rechecks")
		@Config.RangeInt(min = 0, max = 10)
		public int deathWormSpawnCheckChance = 1;

		@Config.Comment("Should InF spawn Cockatrices")
		@Config.Name("Spawn Cockatrices")
		public boolean spawnCockatrices= true;

		@Config.Comment("Cockatrice spawn weight, larger number is more common")
		@Config.Name("Cockatrice Spawn Weight")
		@Config.RangeInt(min = 1, max = 10000)
		public int cockatriceSpawnRate = 4;

		@Config.Comment("Cockatrice spawn check recheck amount, higher number is lower chance to spawn")
		@Config.Name("Cockatrice Spawn Check Rechecks")
		@Config.RangeInt(min = 0, max = 10)
		public int cockatriceSpawnCheckChance = 0;

		@Config.Comment("Should InF spawn Trolls")
		@Config.Name("Spawn Trolls")
		public boolean spawnTrolls = true;

		@Config.Comment("Troll spawn weight, larger number is more common")
		@Config.Name("Troll Spawn Weight")
		@Config.RangeInt(min = 1, max = 10000)
		public int trollSpawnRate = 20;

		@Config.Comment("Troll spawn check recheck amount, higher number is lower chance to spawn")
		@Config.Name("Troll Spawn Check Rechecks")
		@Config.RangeInt(min = 0, max = 10)
		public int trollSpawnCheckChance = 1;

		@Config.Comment("Troll spawn check maximum height")
		@Config.Name("Troll Spawn Check Height")
		@Config.RangeInt(min = 0, max = 255)
		public int trollSpawnCheckHeight = 50;

		@Config.Comment("Troll spawn check maximum height for a given biome name, in the format name=height (Overrides general Troll Spawn Check Height")
		@Config.Name("Troll Spawn Check Height For Biome")
		public String[] trollSpawnCheckHeightForBiome = {""};

		@Config.Comment("Troll spawn type for a given biome name, in the format name=type ('mountain', 'frost', or 'forest')")
		@Config.Name("Troll Spawn Type For Biome")
		public String[] trollSpawnTypeForBiome = {""};

		@Config.Comment("Should InF spawn Amphitheres")
		@Config.Name("Spawn Amphitheres")
		public boolean spawnAmphitheres = true;

		@Config.Comment("Amphithere spawn weight, larger number is more common")
		@Config.Name("Amphithere Spawn Weight")
		@Config.RangeInt(min = 1, max = 10000)
		public int amphithereSpawnRate = 10;

		@Config.Comment("True if allow spawn ghost when player death")
		@Config.Name("Player Death Spawns Ghost")
		public boolean ghostSpawnFromPlayerDeaths = false;

		@Config.Comment("Chance graveyard soil spawn a ghost, Higher number = more rare")
		@Config.Name("Ghost Spawn Rate At Graveyard Soid")
		@Config.RangeInt(min = 1, max = 100)
		public int ghostSpawnChanceFromGraveyardSoil = 9;

		@Config.Comment("Should InF spawn Dread Liches")
		@Config.Name("Spawn Dread Liches")
		public boolean spawnLiches = true;

		@Config.Comment("Dread Lich spawn weight, larger number is more common")
		@Config.Name("Dread Lich Spawn Weight")
		@Config.RangeInt(min = 1, max = 10000)
		public int lichSpawnRate = 2;
	}

	public static class DragonConfig {

		@Config.Comment("How long it takes in ticks for a dragon egg to hatch")
		@Config.Name("Dragon Egg Hatch Time")
		@Config.RangeInt(min = 1, max = 120000)
		public int dragonEggTime = 7200;

		@Config.Comment("Griefing Value; 0 is default; 1 is breaking weak blocks, 2 is no griefing")
		@Config.Name("Dragon Griefing Value")
		@Config.RangeInt(min = 0, max = 2)
		public int dragonGriefing = 0;

		@Config.Comment("If true tamed dragons will follow griefing rules")
		@Config.Name("Tamed Dragon Griefing")
		public boolean tamedDragonGriefing = true;

		@Config.Comment("Block to chance list for blocks to drop as items from dragon griefing, in the format name=chance, 0 - 100 chance")
		@Config.Name("Dragon Griefing Drop Chance")
		public String[] dragonGriefingBlockChance = new String[]{
				"minecraft:cobblestone=3",
				"minecraft:dirt=3",
				"minecraft:grass=4",
				"minecraft:sand=3",
				"minecraft:stone=2",
				"iceandfire:ash=2",
				"iceandfire:chared_cobblestone=2",
				"iceandfire:chared_stone=2",
				"iceandfire:chared_grass=2",
				"iceandfire:chared_dirt=2",
				"iceandfire:chared_gravel=2",
				"iceandfire:chared_grass_path=2",
				"iceandfire:frozen_cobblestone=2",
				"iceandfire:frozen_stone=2",
				"iceandfire:frozen_grass=2",
				"iceandfire:frozen_dirt=2",
				"iceandfire:frozen_gravel=2",
				"iceandfire:frozen_grass_path=2",
				"iceandfire:frozen_splinters=2",
				"iceandfire:crackled_cobblestone=2",
				"iceandfire:crackled_stone=2",
				"iceandfire:crackled_grass=2",
				"iceandfire:crackled_dirt=2",
				"iceandfire:crackled_gravel=2",
				"iceandfire:crackled_grass_path=2"
		};

		@Config.Comment("Block to chance list for break effects to render from dragon griefing, in the format name=chance, 0 - 100 chance")
		@Config.Name("Dragon Griefing Block Effect Chance")
		public String[] dragonGriefingEffectChance = new String[]{
				"minecraft:dirt=5",
				"minecraft:stone=5",
				"iceandfire:ash=5",
				"iceandfire:chared_cobblestone=5",
				"iceandfire:chared_stone=5",
				"iceandfire:chared_dirt=5",
				"iceandfire:chared_gravel=5",
				"iceandfire:frozen_cobblestone=5",
				"iceandfire:frozen_stone=5",
				"iceandfire:frozen_dirt=5",
				"iceandfire:frozen_gravel=5",
				"iceandfire:frozen_splinters=5",
				"iceandfire:crackled_cobblestone=5",
				"iceandfire:crackled_stone=5",
				"iceandfire:crackled_dirt=5",
				"iceandfire:crackled_gravel=5"
		};

		@Config.Comment("Distance that you can hear dragon flapping, large number is further away")
		@Config.Name("Dragon Flap Noise Distance")
		@Config.RangeInt(min = 0, max = 100)
		public int dragonFlapNoiseDistance = 4;

		@Config.Comment("How many chunks away is the dragon flute effective")
		@Config.Name("Dragon Flute Distance")
		@Config.RangeInt(min = 0, max = 100)
		public int dragonFluteDistance = 4;

		@Config.Comment("Maximum dragon health, health scales up to this")
		@Config.Name("Max Dragon Health")
		@Config.RangeInt(min = 1, max = 100000)
		public int dragonHealth = 500;

		@Config.Comment("Maximum dragon attack damage, damage scales up to this")
		@Config.Name("Max Dragon Attack Damage")
		@Config.RangeInt(min = 1, max = 10000)
		public int dragonAttackDamage = 17;

		@Config.Comment("Percentage of damage done when a dragon bites a target that the dragon will heal for")
		@Config.Name("Dragon Bite Healing Percentage")
		@Config.RangeDouble(min = 0D, max = 1D)
		public double dragonBiteHeal = 0.5D;

		@Config.Comment("Maximum dragon flight height, in Y height")
		@Config.Name("Max Dragon Flight Height")
		@Config.RangeInt(min = 10, max = 1000)
		public int maxDragonFlight = 128;

		@Config.Comment("How far away dragons will detect gold blocks being destroyed or chests being opened")
		@Config.Name("Dragon Treasure Search Range")
		@Config.RangeInt(min = 0, max = 1000)
		public int dragonGoldSearchLength = 17;

		@Config.Comment("If true dragons can despawn")
		@Config.Name("Can Dragons Despawn")
		public boolean canDragonsDespawn = false;

		@Config.Comment("If true dragons can drop their skull on death")
		@Config.Name("Dragons Drop Skull")
		public boolean dragonDropSkull = true;

		@Config.Comment("If true dragons can drop their heart on death")
		@Config.Name("Dragons Drop Heart")
		public boolean dragonDropHeart = true;

		@Config.Comment("If true dragons can drop their blood on death")
		@Config.Name("Dragons Drop Blood")
		public boolean dragonDropBlood = true;

		@Config.Comment("How many blocks away can a dragon spot potential prey")
		@Config.Name("Dragon Target Search Range")
		@Config.RangeInt(min = 1, max = 528)
		public int dragonTargetSearchLength = 64;

		@Config.Comment("How many blocks away can dragons wander from their home position")
		@Config.Name("Dragon Wander From Home Range")
		@Config.RangeInt(min = 1, max = 10000)
		public int dragonWanderFromHomeDistance = 40;

		@Config.Comment("Tick interval for dragon hunger decreasing")
		@Config.Name("Dragon Hunger Tick Rate")
		@Config.RangeInt(min = 1, max = 10000)
		public int dragonHungerTickRate = 3000;

		@Config.Comment("If true, lightning dragon projectile attacks knockback their target")
		@Config.Name("Lightning Dragon Knockback")
		public boolean lightningDragonKnockback = true;

		@Config.Comment("If true, lightning dragon projectile attacks apply paralysis")
		@Config.Name("Lightning Dragon Paralysis")
		public boolean lightningDragonParalysis = true;

		@Config.Comment("How many ticks to apply paralysis from lightning dragon attacks")
		@Config.Name("Lightning Dragon Paralysis Ticks")
		@Config.RangeInt(min = 1, max = 1000)
		public int lightningDragonParalysisTicks = 10;

		@Config.Comment("If true, simplifies dragon pathfinding which makes them dumber but reduces server load")
		@Config.Name("Experimental Dragon Pathfinder")
		public boolean experimentalPathFinder = false;

		@Config.Comment("If true, villagers will attempt to run away and hide from dragons and other hostile InF mobs (Can cause increased server lag)")
		@Config.Name("Villagers Fear Dragons")
		public boolean villagersFearDragons = true;

		@Config.Comment("If true, animals will attempt to run away and hide from dragons and other hostile InF mobs (Can cause increased server lag)")
		@Config.Name("Animals Fear Dragons")
		public boolean animalsFearDragons = true;

		@Config.Comment("If true, dragon affected blocks will revert to their natural state after a period of time")
		@Config.Name("Dragon Affected Blocks Revert")
		public boolean dragonAffectedBlocksRevert = false;
	}

	public static class EntityConfig {

		@Config.Comment("Entities in this list will be blacklisted from being stoned")
		@Config.Name("Stone Entity Blacklist")
		public String[] stoneEntityBlacklist = {""};

		@Config.Comment("Maximum Gorgon Health")
		@Config.Name("Gorgon Max Health")
		@Config.RangeDouble(min = 1, max = 10000)
		public double gorgonMaxHealth = 100D;

		@Config.Comment("If true, pixies will attempt to steal from player inventories")
		@Config.Name("Pixies Steal Items")
		public boolean pixiesStealItems = true;

		@Config.Comment("Amount of ticks before a Pixie is ready to produce dust again")
		@Config.Name("Pixie Dust Production Cooldown")
		@Config.RangeInt(min = 100)
		public int pixieCooldown = 24000;

		@Config.Comment("Maximum Cyclops Health")
		@Config.Name("Cyclops Max Health")
		@Config.RangeDouble(min = 1, max = 10000)
		public double cyclopsMaxHealth = 150;

		@Config.Comment("Amount of damage Cyclops will deal with their attack")
		@Config.Name("Cyclops Attack Strength")
		@Config.RangeDouble(min = 1, max = 10000)
		public double cyclopsAttackStrength = 15;

		@Config.Comment("Amount of damage Cyclops will deal with their bite")
		@Config.Name("Cyclops Bite Strength")
		@Config.RangeDouble(min = 1, max = 10000)
		public double cyclopsBiteStrength = 40;
		@Config.Comment("Maximum range that a Cyclops can detect sheep from")
		@Config.Name("Cyclops Sheep Search Range")
		@Config.RangeInt(min = 1, max = 1000)
		public int cyclopesSheepSearchLength = 17;

		@Config.Comment("If true, Cyclops can break logs and leaves in their way")
		@Config.Name("Cyclops Griefing")
		public boolean cyclopsGriefing = true;

		@Config.Comment("Maximum Siren Health")
		@Config.Name("Siren Max Health")
		@Config.RangeDouble(min = 1, max = 10000)
		public double sirenMaxHealth = 50D;

		@Config.Comment("How long in ticks a siren can use its sing effect on a player without a cooldown")
		@Config.Name("Siren Max Sing Time")
		@Config.RangeInt(min = 100, max = 24000)
		public int sirenMaxSingTime = 400;

		@Config.Comment("How long in ticks a siren has to wait after failing to lure in a player before singing")
		@Config.Name("Siren Time Between Songs")
		@Config.RangeInt(min = 100, max = 24000)
		public int sirenTimeBetweenSongs = 1200;

		@Config.Comment("How many blocks away can Deathworms spot potential prey")
		@Config.Name("Deathworm Target Search Range")
		@Config.RangeInt(min = 1, max = 1000)
		public int deathWormTargetSearchLength = 64;

		@Config.Comment("Default Deathworm health, scaled to worm size")
		@Config.Name("Deathworm Base Health")
		@Config.RangeDouble(min = 1, max = 10000)
		public double deathWormMaxHealth = 10D;

		@Config.Comment("Default Deathworm attack strength, scaled to worm size")
		@Config.Name("Deathworm Base Attack Strength")
		@Config.RangeDouble(min = 1, max = 10000)
		public double deathWormAttackStrength = 3D;

		@Config.Comment("If true wild Deathworms will target and attack monsters")
		@Config.Name("Deathworms Attack Monsters")
		public boolean deathWormAttackMonsters = true;

		@Config.Comment("How many blocks away can Cockatrice detect chickens")
		@Config.Name("Cockatrice Chicken Search Range")
		@Config.RangeInt(min = 1, max = 10000)
		public int cockatriceChickenSearchLength = 32;

		@Config.Comment("If true, chickens have a chance to lay rotten eggs")
		@Config.Name("Chickens Lay Rotten Eggs")
		public boolean chickensLayRottenEggs = true;

		@Config.Comment("Chance per 6000 ticks for chickens to lay a rotten egg, 1 in N chance")
		@Config.Name("Chicken Rotten Egg Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int chickenEggChance = 30;

		@Config.Comment("How many blocks away can Stymphalian Birds spot potential prey")
		@Config.Name("Stymphalian Bird Target Search Range")
		@Config.RangeInt(min = 1, max = 10000)
		public int stymphalianBirdTargetSearchLength = 64;

		@Config.Comment("If true, Stymphalian Bird feather projectiles have a chance to turn into an item before despawning")
		@Config.Name("Stymphalian Bird Feather Projectile Item")
		public boolean stymphalianBirdFeatherProjectileItem = true;

		@Config.Comment("Chance for Stymphalian Bird feather projectiles to turn into an item before despawning, 1 in N chance")
		@Config.Name("Stymphalian Bird Feather Drop Chance")
		@Config.RangeInt(min = 1, max = 10000)
		public int stymphalianBirdFeatherDropChance = 25;

		@Config.Comment("Stymphalian Bird feather attack strength")
		@Config.Name("Stymphalian Bird Feather Attack Strength")
		@Config.RangeDouble(min = 0, max = 10000)
		public double stymphalianBirdFeatherAttackStength = 1D;

		@Config.Comment("Range from other Stymphalian Birds for them to still be considered in the same flock")
		@Config.Name("Stymphalian Bird Flock Range")
		@Config.RangeInt(min = 1, max = 10000)
		public int stymphalianBirdFlockLength = 40;

		@Config.Comment("Maximum height a Stymphalian Bird can fly, in Y height")
		@Config.Name("Stymphalian Bird Flight Height")
		@Config.RangeInt(min = 10, max = 1000)
		public int stymphalianBirdFlightHeight = 80;

		@Config.Comment("If true, Stymphalian birds will drop items registered in the oreDictionaries ingotBronze, nuggetBronze")
		@Config.Name("Stymphalian Birds Drop OreDict Items")
		public boolean stymphalianBirdsOreDictDrops = true;

		@Config.Comment("If true, Stymphalian birds are allowed to target and attack animals")
		@Config.Name("Stymphalian Birds Target Animals")
		public boolean stympahlianBirdAttackAnimals = false;

		@Config.Comment("If true, trolls can drop their weapon on death")
		@Config.Name("Trolls Drop Weapon")
		public boolean trollsDropWeapon = true;

		@Config.Comment("Maximum Troll Health")
		@Config.Name("Troll Max Health")
		@Config.RangeDouble(min = 1, max = 10000)
		public double trollMaxHealth = 50;

		@Config.Comment("Maximum Troll Attack Strength")
		@Config.Name("Troll Attack Strength")
		@Config.RangeDouble(min = 1, max = 10000)
		public double trollAttackStrength = 10;

		@Config.Comment("How many ticks it takes for a Myrmex Queen to produce an egg")
		@Config.Name("Myrmex Gestation Length")
		@Config.RangeInt(min = 1, max = 10000)
		public int myrmexPregnantTicks = 2500;

		@Config.Comment("How many ticks it takes for a Myrmex Egg to hatch")
		@Config.Name("Myrmex Hatch Length")
		@Config.RangeInt(min = 1, max = 10000)
		public int myrmexEggTicks = 3000;

		@Config.Comment("How many ticks it takes for a Myrmex to move from larvae to pupae, and pupae to adult")
		@Config.Name("Myrmex Life Stage Length")
		@Config.RangeInt(min = 1, max = 100000)
		public int myrmexLarvaTicks = 35000;

		@Config.Comment("Range that Amphitheres can detect villagers being hurt")
		@Config.Name("Amphithere Villager Hurt Range")
		@Config.RangeDouble(min = 1, max = 10000)
		public double amphithereVillagerSearchLength = 64;

		@Config.Comment("How many ticks it takes while riding an untamed Amphithere to tame it")
		@Config.Name("Amphithere Tame Time")
		@Config.RangeInt(min = 1, max = 10000)
		public int amphithereTameTime = 400;

		@Config.Comment("Amount of damage an Amphithere attacks the player for each bite while atttempting to tame them")
		@Config.Name("Amphithere Taming Bite Damage")
		@Config.RangeDouble(min = 1, max = 10000)
		public double amphithereTameDamage = 1D;

		@Config.Comment("How fast Amphitheres fly")
		@Config.Name("Amphithere Flight Speed")
		@Config.RangeDouble(min = 1.0D, max = 3.0D)
		public double amphithereFlightSpeed = 1.75D;

		@Config.Comment("Maximum Amphithere Health")
		@Config.Name("Amphithere Max Health")
		@Config.RangeDouble(min = 1, max = 10000)
		public double amphithereMaxHealth = 50D;

		@Config.Comment("Amphithere Attack Strength")
		@Config.Name("Amphithere Attack Strength")
		@Config.RangeDouble(min = 1, max = 10000)
		public double amphithereAttackStrength = 7D;

		@Config.Comment("Maximum amphithere flight height, in Y height")
		@Config.Name("Max Amphithere Flight Height")
		@Config.RangeInt(min = 10, max = 1000)
		public int maxAmphithereFlight = 128;

		@Config.Comment("If true, Sea Serpents can break weak blocks in their way")
		@Config.Name("Sea Serpent Griefing")
		public boolean seaSerpentGriefing = true;

		@Config.Comment("Default Sea Serpent health, this is scaled to Sea Serpent's size")
		@Config.Name("Sea Serpent Base Health")
		@Config.RangeDouble(min = 1, max = 10000)
		public double seaSerpentBaseHealth = 20D;

		@Config.Comment("Default Sea Serpent Attack Strength, this is scaled to Sea Serpent's size")
		@Config.Name("Sea Serpent Base Attack Strength")
		@Config.RangeDouble(min = 1, max = 10000)
		public double seaSerpentAttackStrength = 4D;

		@Config.Comment("Base Hydra health, health starts at this")
		@Config.Name("Base Hydra Health")
		@Config.RangeInt(min = 1, max = 100000)
		public int hydraBaseHealth = 200;

		@Config.Comment("Maximum hydra health, health scales up to this")
		@Config.Name("Max Hydra Health")
		@Config.RangeInt(min = 1, max = 100000)
		public int hydraMaxHealth = 2500;

		@Config.Comment("Hydra Bite Attack Strength")
		@Config.Name("Hydra Attack Strength")
		@Config.RangeDouble(min = 1, max = 1000)
		public double hydraBiteAttackStrength = 3D;

		@Config.Comment("Hydra Breath Attack Damage")
		@Config.Name("Hydra Breath Attack Damage")
		@Config.RangeDouble(min = 1, max = 1000)
		public float hydraBreathAttackDamage = 1F;


		@Config.Comment("Maximum ghost health.")
		@Config.Name("Ghost Max Health")
		@Config.RangeDouble(min = 1F, max = 10000F)
		public float ghostMaxHealth = 30f;

		@Config.Comment("Maximum ghost attack strength.")
		@Config.Name("Ghost Attack Strength")
		@Config.RangeDouble(min = 0F, max = 10000F)
		public float ghostAttackStrength = 3f;
	}

	public static class MiscConfig {

		@Config.Comment("If true, chain lightning bypasses armor")
		@Config.Name("Chain Lightning Bypasses Armor")
		public boolean chainLightningBypassesArmor = true;

		@Config.Comment("Base damage dealth by chain lightning, decreasing proportionally on each hop")
		@Config.Name("Chain Lightning Base Damage")
		@Config.RangeDouble(min = 1, max = 1000)
		public float[] chainLightningDamagePerHop = {5.0f, 4.0f, 3.0f, 2.0f, 1.0f};

		@Config.Comment("Default range for chain lightning, maximum range for each hop")
		@Config.Name("Chain Lightning Range")
		@Config.RangeInt(min = 5, max = 20)
		public int chainLightningRange = 8;

		@Config.Comment("If true, chain lightning causes paralysis")
		@Config.Name("Chain Lightning Paralysis")
		public boolean chainLightningParalysis = false;

		@Config.Comment("Length in ticks of paralysis applied on each hop by chain lightning")
		@Config.Name("Chain Lightning Paralysis Ticks")
		public int[] chainLightningParalysisTicksPerHop = new int[] {10, 8, 6, 4, 2};

		@Config.Comment("Should a trade be added to Craftsman snow villagers to trade snow for sapphires?")
		@Config.Name("Snow Villager Allow Craftsman Snow Trade")
		public boolean allowSnowForSapphireTrade = true;

		@Config.Comment("If true, hydra hearts provide healing while in the player's hotkey bar")
		@Config.Name("Hydra Heart Passive Healing")
		public boolean hydraHeartPassiveHealing = false;

		@Config.Comment("Base damage for the Tide Trident")
		@Config.Name("Tide Trident Base Damage")
		@Config.RangeDouble(min = 1, max = 1000)
		public float tideTridentBaseDamage = 13.0f;

		@Config.Comment("Damage multiplier for the Tide Trident - applied when the trident is underwater")
		@Config.Name("Tide Trident Underwater Damage Multiplier")
		@Config.RangeDouble(min = 1, max = 1000)
		public float tideTridentUnderwaterDamageMultiplier = 2.0f;
	}

	public static class ClientConfig {

		@Config.Comment("Use custom images in the main menu panorama")
		@Config.Name("Custom Main Menu")
		public boolean customMainMenu = true;

		@Config.Comment("Should the Bestiary use the Vanilla Font or custom Font")
		@Config.Name("Bestiary Vanilla Font")
		public boolean useVanillaFont = false;

		@Config.Comment("If true, uses a custom shader when players are charmed by sirens")
		@Config.Name("Use Siren Shader")
		public boolean sirenShader = true;

		@Config.Comment("Render stoned entities using layered rendering")
		@Config.Name("Layered Stoned Entity Texture")
		public boolean customStoneTexture = false;

		@Config.Comment("Enables armor rendering fixes - to resolve overlapping armor model issues")
		@Config.Name("Enable Armor Rendering Fixes")
		public boolean fixArmorRenderingBugs = true;
	}

	@Mod.EventBusSubscriber(modid = IceAndFire.MODID)
	private static class EventHandler{
		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(IceAndFire.MODID)) {
				stoneBlacklist = null;
				dragonDisabledNames = null;
				dragonDisabledTypes = null;
				fireDragonEnabledNames = null;
				iceDragonEnabledNames = null;
				lightningDragonEnabledNames = null;
				dragonRoostChance = null;
				dragonDenChance = null;
				dragonGriefingBlockChance = null;
				dragonGriefingEffectChance = null;
				ConfigManager.sync(IceAndFire.MODID, Config.Type.INSTANCE);
			}
		}
	}

	//Caching garbage

	private static HashSet<ResourceLocation> stoneBlacklist = null;
	private static HashSet<String> myrmexDisabledNames = null;
	private static HashMap<String, Integer> trollSpawnCheckHeight = null;
	private static HashMap<String, String> trollSpawnCheckType = null;
	private static HashSet<BiomeDictionary.Type> myrmexDisabledTypes = null;
	private static HashSet<String> dragonDisabledNames = null;
	private static HashSet<BiomeDictionary.Type> dragonDisabledTypes = null;
	private static HashSet<String> fireDragonEnabledNames = null;
	private static HashSet<String> iceDragonEnabledNames = null;
	private static HashSet<String> lightningDragonEnabledNames = null;
	private static HashSet<BiomeDictionary.Type> fireDragonEnabledTypes = null;
	private static HashSet<BiomeDictionary.Type> iceDragonEnabledTypes = null;
	private static HashSet<BiomeDictionary.Type> lightningDragonEnabledTypes = null;
	private static HashSet<BiomeDictionary.Type> fireDragonDisabledTypes = null;
	private static HashSet<BiomeDictionary.Type> iceDragonDisabledTypes = null;
	private static HashSet<BiomeDictionary.Type> lightningDragonDisabledTypes = null;
	private static HashMap<String, Integer> dragonRoostChance = null;
	private static HashMap<String, Integer> dragonDenChance = null;
	private static HashMap<Block, Integer> dragonGriefingBlockChance = null;
	private static HashMap<Block, Integer> dragonGriefingEffectChance = null;

	public static HashSet<ResourceLocation> getStoneEntityBlacklist() {
		if(stoneBlacklist != null) return stoneBlacklist;
		HashSet<ResourceLocation> set = new HashSet<>();
		for(String string : ENTITY_SETTINGS.stoneEntityBlacklist) set.add(new ResourceLocation(string));
		stoneBlacklist = set;
		return stoneBlacklist;
	}

	public static HashSet<String> getMyrmexDisabledNames() {
		if(myrmexDisabledNames != null) return myrmexDisabledNames;
		myrmexDisabledNames = new HashSet<>(Arrays.asList(WORLDGEN.generateMyrmexDisabledBiomeNames));
		return myrmexDisabledNames;
	}

	public static HashSet<BiomeDictionary.Type> getMyrmexDisabledTypes() {
		if(myrmexDisabledTypes != null) return myrmexDisabledTypes;
		HashSet<BiomeDictionary.Type> set = new HashSet<>();
		for(String string : WORLDGEN.generateMyrmexDisabledBiomeTypes) set.add(BiomeDictionary.Type.getType(string));
		myrmexDisabledTypes = set;
		return myrmexDisabledTypes;
	}

	public static HashMap<String, Integer> getTrollSpawnHeight() {
		if(trollSpawnCheckHeight != null) return trollSpawnCheckHeight;
		trollSpawnCheckHeight = mapNameInteger(ENTITY_SPAWNING.trollSpawnCheckHeightForBiome);
		return trollSpawnCheckHeight;
	}

	public static HashMap<String, String> getTrollSpawnType() {
		if(trollSpawnCheckType != null) return trollSpawnCheckType;
		trollSpawnCheckType = mapNameString(ENTITY_SPAWNING.trollSpawnTypeForBiome);
		return trollSpawnCheckType;
	}

	public static HashSet<String> getDragonDisabledNames() {
		if(dragonDisabledNames != null) return dragonDisabledNames;
		dragonDisabledNames = new HashSet<>(Arrays.asList(WORLDGEN.generateDragonDisabledBiomeNames));
		return dragonDisabledNames;
	}

	public static HashSet<BiomeDictionary.Type> getDragonDisabledTypes() {
		if(dragonDisabledTypes != null) return dragonDisabledTypes;
		HashSet<BiomeDictionary.Type> set = new HashSet<>();
		for(String string : WORLDGEN.generateDragonDisabledBiomeTypes) set.add(BiomeDictionary.Type.getType(string));
		dragonDisabledTypes = set;
		return dragonDisabledTypes;
	}

	public static HashSet<String> getFireDragonEnabledNames() {
		if(fireDragonEnabledNames != null) return fireDragonEnabledNames;
		fireDragonEnabledNames = new HashSet<>(Arrays.asList(WORLDGEN.generateFireDragonEnabledBiomeNames));
		return fireDragonEnabledNames;
	}

	public static HashSet<String> getIceDragonEnabledNames() {
		if(iceDragonEnabledNames != null) return iceDragonEnabledNames;
		iceDragonEnabledNames = new HashSet<>(Arrays.asList(WORLDGEN.generateIceDragonEnabledBiomeNames));
		return iceDragonEnabledNames;
	}

	public static HashSet<String> getLightningDragonEnabledNames() {
		if(lightningDragonEnabledNames != null) return lightningDragonEnabledNames;
		lightningDragonEnabledNames = new HashSet<>(Arrays.asList(WORLDGEN.generateLightningDragonEnabledBiomeNames));
		return lightningDragonEnabledNames;
	}

	public static HashSet<BiomeDictionary.Type> getFireDragonEnabledTypes() {
		if(fireDragonEnabledTypes != null) return fireDragonEnabledTypes;
		HashSet<BiomeDictionary.Type> set = new HashSet<>();
		for (String string : WORLDGEN.generateFireDragonEnabledBiomeTypes) set.add(BiomeDictionary.Type.getType(string));
		fireDragonEnabledTypes = set;
		return fireDragonEnabledTypes;
	}

	public static HashSet<BiomeDictionary.Type> getIceDragonEnabledTypes() {
		if(iceDragonEnabledTypes != null) return iceDragonEnabledTypes;
		HashSet<BiomeDictionary.Type> set = new HashSet<>();
		for (String string : WORLDGEN.generateIceDragonEnabledBiomeTypes) set.add(BiomeDictionary.Type.getType(string));
		iceDragonEnabledTypes = set;
		return iceDragonEnabledTypes;
	}

	public static HashSet<BiomeDictionary.Type> getLightningDragonEnabledTypes() {
		if(lightningDragonEnabledTypes != null) return lightningDragonEnabledTypes;
		HashSet<BiomeDictionary.Type> set = new HashSet<>();
		for (String string : WORLDGEN.generateLightningDragonEnabledBiomeTypes) set.add(BiomeDictionary.Type.getType(string));
		lightningDragonEnabledTypes = set;
		return lightningDragonEnabledTypes;
	}

	public static HashSet<BiomeDictionary.Type> getFireDragonDisabledTypes() {
		if(fireDragonDisabledTypes != null) return fireDragonDisabledTypes;
		HashSet<BiomeDictionary.Type> set = new HashSet<>();
		for (String string : WORLDGEN.generateFireDragonDisabledBiomeTypes) set.add(BiomeDictionary.Type.getType(string));
		fireDragonDisabledTypes = set;
		return fireDragonDisabledTypes;
	}

	public static HashSet<BiomeDictionary.Type> getIceDragonDisabledTypes() {
		if(iceDragonDisabledTypes != null) return iceDragonDisabledTypes;
		HashSet<BiomeDictionary.Type> set = new HashSet<>();
		for (String string : WORLDGEN.generateIceDragonDisabledBiomeTypes) set.add(BiomeDictionary.Type.getType(string));
		iceDragonDisabledTypes = set;
		return iceDragonDisabledTypes;
	}

	public static HashSet<BiomeDictionary.Type> getLightningDragonDisabledTypes() {
		if(lightningDragonDisabledTypes != null) return lightningDragonDisabledTypes;
		HashSet<BiomeDictionary.Type> set = new HashSet<>();
		for (String string : WORLDGEN.generateLightningDragonDisabledBiomeTypes) set.add(BiomeDictionary.Type.getType(string));
		lightningDragonDisabledTypes = set;
		return lightningDragonDisabledTypes;
	}

	public static HashMap<String, Integer> getDragonRoostChance() {
		if(dragonRoostChance != null) return dragonRoostChance;
		dragonRoostChance = mapNameInteger(WORLDGEN.generateDragonRoostChanceForBiome);
		return dragonRoostChance;
	}

	public static HashMap<String, Integer> getDragonDenChance() {
		if(dragonDenChance != null) return dragonDenChance;
		dragonDenChance = mapNameInteger(WORLDGEN.generateDragonDenChanceForBiome);
		return dragonDenChance;
	}

	public static HashMap<Block, Integer> getDragonGriefingBlockChance() {
		if(dragonGriefingBlockChance != null) return dragonGriefingBlockChance;
		dragonGriefingBlockChance = loadBlockChanceMapping(DRAGON_SETTINGS.dragonGriefingBlockChance);
		return dragonGriefingBlockChance;
	}

	public static HashMap<Block, Integer> getDragonGriefingEffectChance() {
		if(dragonGriefingEffectChance != null) return dragonGriefingEffectChance;
		dragonGriefingEffectChance = loadBlockChanceMapping(DRAGON_SETTINGS.dragonGriefingEffectChance);
		return dragonGriefingEffectChance;
	}

	private static HashMap<String, Integer> mapNameInteger(String[] mappings) {
		HashMap<String, Integer> map = new HashMap<>();
		for(String biomeNameMapping : mappings) {
			if(StringUtils.isNullOrEmpty(biomeNameMapping)) continue;
			String[] split = biomeNameMapping.split("=");
			if(split.length != 2 || split[0].isEmpty() || split[1].isEmpty()) {
				IceAndFire.logger.error("Failed to parse biome name mapping: " + biomeNameMapping);
				continue;
			}
			try {
				map.put(split[0], Integer.parseInt(split[1]));
			} catch (NumberFormatException e) {
				IceAndFire.logger.error("Failed to parse biome name mapping, invalid integer: " + biomeNameMapping);
			}
		}
		return map;
	}

	private static HashMap<String, String> mapNameString(String[] mappings) {
		HashMap<String, String> map = new HashMap<>();
		for(String biomeNameMapping : mappings) {
			if(StringUtils.isNullOrEmpty(biomeNameMapping)) continue;
			String[] split = biomeNameMapping.split("=");
			if(split.length != 2 || split[0].isEmpty() || split[1].isEmpty()) {
				IceAndFire.logger.error("Failed to parse biome name mapping: " + biomeNameMapping);
				continue;
			}
			map.put(split[0], split[1]);
		}
		return map;
	}

	private static HashMap<Block, Integer> loadBlockChanceMapping(String[] mappings) {
		HashMap<Block, Integer> map = new HashMap<>();
		for(String blockNameMapping : mappings) {
			if(StringUtils.isNullOrEmpty(blockNameMapping)) continue;
			String[] split = blockNameMapping.split("=");
			if(split.length != 2 || split[0].isEmpty() || split[1].isEmpty()) {
				IceAndFire.logger.error("Failed to parse block name mapping: " + blockNameMapping);
				continue;
			}
			Block block = Block.getBlockFromName(split[0]);
			if(block == null || block == Blocks.AIR) {
				IceAndFire.logger.error("Failed to parse block name mapping, invalid block or air: " + blockNameMapping);
				continue;
			}
			try {
				map.put(block, IafMathHelper.clamp(Integer.parseInt(split[1]), 0, 100));
			} catch (NumberFormatException e) {
				IceAndFire.logger.error("Failed to parse block name mapping, invalid chance: " + blockNameMapping);
			}
		}
		return map;
	}
}