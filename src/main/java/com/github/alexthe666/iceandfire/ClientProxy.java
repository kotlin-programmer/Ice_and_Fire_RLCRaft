package com.github.alexthe666.iceandfire;

import com.github.alexthe666.iceandfire.block.*;
import com.github.alexthe666.iceandfire.client.gui.GuiMyrmexAddRoom;
import com.github.alexthe666.iceandfire.client.gui.GuiMyrmexStaff;
import com.github.alexthe666.iceandfire.client.gui.bestiary.GuiBestiary;
import com.github.alexthe666.iceandfire.client.model.*;
import com.github.alexthe666.iceandfire.client.model.animator.*;
import com.github.alexthe666.iceandfire.client.model.util.EnumDragonAnimations;
import com.github.alexthe666.iceandfire.client.model.util.EnumSeaSerpentAnimations;
import com.github.alexthe666.iceandfire.client.model.util.IceAndFireTabulaModel;
import com.github.alexthe666.iceandfire.client.particle.*;
import com.github.alexthe666.iceandfire.client.particle.lightning.ParticleLightningRenderer;
import com.github.alexthe666.iceandfire.client.particle.lightning.ParticleLightningVector;
import com.github.alexthe666.iceandfire.client.render.entity.RenderAmphithereArrow;
import com.github.alexthe666.iceandfire.client.render.entity.*;
import com.github.alexthe666.iceandfire.client.render.entity.player.RenderModArmor;
import com.github.alexthe666.iceandfire.client.render.entity.player.RenderModCapes;
import com.github.alexthe666.iceandfire.client.render.tile.*;
import com.github.alexthe666.iceandfire.core.ModBlocks;
import com.github.alexthe666.iceandfire.core.ModItems;
import com.github.alexthe666.iceandfire.core.ModKeys;
import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.entity.projectile.*;
import com.github.alexthe666.iceandfire.entity.tile.*;
import com.github.alexthe666.iceandfire.entity.util.MyrmexHive;
import com.github.alexthe666.iceandfire.enums.*;
import com.github.alexthe666.iceandfire.event.EventClient;
import com.github.alexthe666.iceandfire.event.EventNewMenu;
import com.github.alexthe666.iceandfire.item.ICustomRendered;
import net.ilexiconn.llibrary.client.model.tabula.TabulaModelHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ClientProxy extends CommonProxy {

	private static final ModelCopperArmor COPPER_ARMOR_MODEL = new ModelCopperArmor(0.5F);
	private static final ModelCopperArmor COPPER_ARMOR_MODEL_LEGS = new ModelCopperArmor(0.2F);
	private static final ModelSilverArmor SILVER_ARMOR_MODEL = new ModelSilverArmor(0.5F);
	private static final ModelSilverArmor SILVER_ARMOR_MODEL_LEGS = new ModelSilverArmor(0.2F);
	private static final ModelFireDragonArmor FIRE_DRAGON_SCALE_ARMOR_MODEL = new ModelFireDragonArmor(0.5F, false);
	private static final ModelFireDragonArmor FIRE_DRAGON_SCALE_ARMOR_MODEL_LEGS = new ModelFireDragonArmor(0.2F, true);
	private static final ModelIceDragonArmor ICE_DRAGON_SCALE_ARMOR_MODEL = new ModelIceDragonArmor(0.5F, false);
	private static final ModelIceDragonArmor ICE_DRAGON_SCALE_ARMOR_MODEL_LEGS = new ModelIceDragonArmor(0.2F, true);
	private static final ModelLightningDragonArmor LIGHTNING_DRAGON_SCALE_ARMOR_MODEL = new ModelLightningDragonArmor(0.5F, false);
	private static final ModelLightningDragonArmor LIGHTNING_DRAGON_SCALE_ARMOR_MODEL_LEGS = new ModelLightningDragonArmor(0.2F, true);
	private static final ModelDeathWormArmor DEATHWORM_ARMOR_MODEL = new ModelDeathWormArmor(0.5F);
	private static final ModelDeathWormArmor DEATHWORM_ARMOR_MODEL_LEGS = new ModelDeathWormArmor(0.2F);
	private static final ModelTrollArmor TROLL_ARMOR_MODEL = new ModelTrollArmor(0.75F);
	private static final ModelTrollArmor TROLL_ARMOR_MODEL_LEGS = new ModelTrollArmor(0.35F);
	private static final ModelSeaSerpentArmor TIDE_ARMOR_MODEL = new ModelSeaSerpentArmor(0.4F);
	private static final ModelSeaSerpentArmor TIDE_ARMOR_MODEL_LEGS = new ModelSeaSerpentArmor(0.2F);
	private FontRenderer bestiaryFontRenderer;
	@SideOnly(Side.CLIENT)
	private static final IceAndFireTEISR TEISR = new IceAndFireTEISR();
	public static List<UUID> currentDragonRiders = new ArrayList<>();
	private int thirdPersonViewDragon = 0;
	private static MyrmexHive referedClientHive = null;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void registerModels(ModelRegistryEvent event) {
		ModelBakery.registerItemVariants(Item.getItemFromBlock(ModBlocks.podium), new ResourceLocation("iceandfire:podium_oak"), new ResourceLocation("iceandfire:podium_spruce"), new ResourceLocation("iceandfire:podium_birch"), new ResourceLocation("iceandfire:podium_jungle"), new ResourceLocation("iceandfire:podium_acacia"), new ResourceLocation("iceandfire:podium_dark_oak"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.podium), 0, new ModelResourceLocation("iceandfire:podium_oak", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.podium), 1, new ModelResourceLocation("iceandfire:podium_spruce", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.podium), 2, new ModelResourceLocation("iceandfire:podium_birch", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.podium), 3, new ModelResourceLocation("iceandfire:podium_jungle", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.podium), 4, new ModelResourceLocation("iceandfire:podium_acacia", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.podium), 5, new ModelResourceLocation("iceandfire:podium_dark_oak", "inventory"));
		ModelBakery.registerItemVariants(ModItems.dragonbone_bow, new ResourceLocation("iceandfire:dragonbone_bow"), new ResourceLocation("iceandfire:dragonbone_bow_pulling_0"), new ResourceLocation("iceandfire:dragonbone_bow_pulling_1"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragonbone_bow, 0, new ModelResourceLocation("iceandfire:dragonbone_bow", "inventory"));
		ModelBakery.registerItemVariants(ModItems.dragon_skull, new ResourceLocation("iceandfire:dragon_skull_fire"), new ResourceLocation("iceandfire:dragon_skull_ice"), new ResourceLocation("iceandfire:dragon_skull_lightning"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_skull, 0, new ModelResourceLocation("iceandfire:dragon_skull_fire", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_skull, 1, new ModelResourceLocation("iceandfire:dragon_skull_ice", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_skull, 2, new ModelResourceLocation("iceandfire:dragon_skull_lightning", "inventory"));
		ModelBakery.registerItemVariants(ModItems.dragon_armor_iron, new ResourceLocation("iceandfire:dragonarmor_iron_head"), new ResourceLocation("iceandfire:dragonarmor_iron_neck"), new ResourceLocation("iceandfire:dragonarmor_iron_body"), new ResourceLocation("iceandfire:dragonarmor_iron_tail"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_iron, 0, new ModelResourceLocation("iceandfire:dragonarmor_iron_head", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_iron, 1, new ModelResourceLocation("iceandfire:dragonarmor_iron_neck", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_iron, 2, new ModelResourceLocation("iceandfire:dragonarmor_iron_body", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_iron, 3, new ModelResourceLocation("iceandfire:dragonarmor_iron_tail", "inventory"));
		ModelBakery.registerItemVariants(ModItems.dragon_armor_gold, new ResourceLocation("iceandfire:dragonarmor_gold_head"), new ResourceLocation("iceandfire:dragonarmor_gold_neck"), new ResourceLocation("iceandfire:dragonarmor_gold_body"), new ResourceLocation("iceandfire:dragonarmor_gold_tail"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_gold, 0, new ModelResourceLocation("iceandfire:dragonarmor_gold_head", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_gold, 1, new ModelResourceLocation("iceandfire:dragonarmor_gold_neck", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_gold, 2, new ModelResourceLocation("iceandfire:dragonarmor_gold_body", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_gold, 3, new ModelResourceLocation("iceandfire:dragonarmor_gold_tail", "inventory"));
		ModelBakery.registerItemVariants(ModItems.dragon_armor_diamond, new ResourceLocation("iceandfire:dragonarmor_diamond_head"), new ResourceLocation("iceandfire:dragonarmor_diamond_neck"), new ResourceLocation("iceandfire:dragonarmor_diamond_body"), new ResourceLocation("iceandfire:dragonarmor_diamond_tail"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_diamond, 0, new ModelResourceLocation("iceandfire:dragonarmor_diamond_head", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_diamond, 1, new ModelResourceLocation("iceandfire:dragonarmor_diamond_neck", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_diamond, 2, new ModelResourceLocation("iceandfire:dragonarmor_diamond_body", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_diamond, 3, new ModelResourceLocation("iceandfire:dragonarmor_diamond_tail", "inventory"));
		ModelBakery.registerItemVariants(ModItems.dragon_armor_silver, new ResourceLocation("iceandfire:dragonarmor_silver_head"), new ResourceLocation("iceandfire:dragonarmor_silver_neck"), new ResourceLocation("iceandfire:dragonarmor_silver_body"), new ResourceLocation("iceandfire:dragonarmor_silver_tail"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_silver, 0, new ModelResourceLocation("iceandfire:dragonarmor_silver_head", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_silver, 1, new ModelResourceLocation("iceandfire:dragonarmor_silver_neck", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_silver, 2, new ModelResourceLocation("iceandfire:dragonarmor_silver_body", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_silver, 3, new ModelResourceLocation("iceandfire:dragonarmor_silver_tail", "inventory"));
		ModelBakery.registerItemVariants(ModItems.dragon_armor_copper, new ResourceLocation("iceandfire:dragonarmor_copper_head"), new ResourceLocation("iceandfire:dragonarmor_copper_neck"), new ResourceLocation("iceandfire:dragonarmor_copper_body"), new ResourceLocation("iceandfire:dragonarmor_copper_tail"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_copper, 0, new ModelResourceLocation("iceandfire:dragonarmor_copper_head", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_copper, 1, new ModelResourceLocation("iceandfire:dragonarmor_copper_neck", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_copper, 2, new ModelResourceLocation("iceandfire:dragonarmor_copper_body", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.dragon_armor_copper, 3, new ModelResourceLocation("iceandfire:dragonarmor_copper_tail", "inventory"));
		for(int i = 0; i < EnumHippogryphTypes.values().length; i++){
			ModelLoader.setCustomModelResourceLocation(ModItems.hippogryph_egg, i, new ModelResourceLocation("iceandfire:hippogryph_egg", "inventory"));
		}
		ModelLoader.setCustomModelResourceLocation(ModItems.gorgon_head, 0, new ModelResourceLocation("iceandfire:gorgon_head", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.gorgon_head, 1, new ModelResourceLocation("iceandfire:gorgon_head", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.pixieHouse), 0, new ModelResourceLocation("iceandfire:pixie_house", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.pixieHouse), 1, new ModelResourceLocation("iceandfire:pixie_house", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.pixieHouse), 2, new ModelResourceLocation("iceandfire:pixie_house", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.pixieHouse), 3, new ModelResourceLocation("iceandfire:pixie_house", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.pixieHouse), 4, new ModelResourceLocation("iceandfire:pixie_house", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.pixieHouse), 5, new ModelResourceLocation("iceandfire:pixie_house", "inventory"));
		ModelBakery.registerItemVariants(Item.getItemFromBlock(ModBlocks.jar_pixie), new ResourceLocation("iceandfire:jar_0"), new ResourceLocation("iceandfire:jar_1"), new ResourceLocation("iceandfire:jar_2"), new ResourceLocation("iceandfire:jar_3"), new ResourceLocation("iceandfire:jar_4"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.jar_empty), 0, new ModelResourceLocation("iceandfire:jar", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.jar_pixie), 0, new ModelResourceLocation("iceandfire:jar_0", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.jar_pixie), 1, new ModelResourceLocation("iceandfire:jar_1", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.jar_pixie), 2, new ModelResourceLocation("iceandfire:jar_2", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.jar_pixie), 3, new ModelResourceLocation("iceandfire:jar_3", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.jar_pixie), 4, new ModelResourceLocation("iceandfire:jar_4", "inventory"));
		ModelBakery.registerItemVariants(ModItems.deathworm_chitin, new ResourceLocation("iceandfire:deathworm_chitin_yellow"), new ResourceLocation("iceandfire:deathworm_chitin_white"), new ResourceLocation("iceandfire:deathworm_chitin_red"));
		ModelLoader.setCustomModelResourceLocation(ModItems.deathworm_chitin, 0, new ModelResourceLocation("iceandfire:deathworm_chitin_yellow", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.deathworm_chitin, 1, new ModelResourceLocation("iceandfire:deathworm_chitin_white", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.deathworm_chitin, 2, new ModelResourceLocation("iceandfire:deathworm_chitin_red", "inventory"));
		ModelBakery.registerItemVariants(ModItems.deathworm_egg, new ResourceLocation("iceandfire:deathworm_egg"), new ResourceLocation("iceandfire:deathworm_egg_giant"));
		ModelLoader.setCustomModelResourceLocation(ModItems.deathworm_egg, 0, new ModelResourceLocation("iceandfire:deathworm_egg", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.deathworm_egg, 1, new ModelResourceLocation("iceandfire:deathworm_egg_giant", "inventory"));
		for (EnumDragonArmor armor : EnumDragonArmor.values()) {
			renderDragonArmors(armor);
		}
		for (EnumSeaSerpent armor : EnumSeaSerpent.values()) {
			renderSeaSerpentArmors(armor);
		}
		for(EnumTroll.Weapon weapon : EnumTroll.Weapon.values()){
			ModelLoader.setCustomModelResourceLocation(weapon.item, 0, new ModelResourceLocation("iceandfire:troll_weapon", "inventory"));
		}
		for (EnumTroll troll : EnumTroll.values()) {
			ModelLoader.setCustomModelResourceLocation(troll.helmet, 0, new ModelResourceLocation("iceandfire:"  + troll.name().toLowerCase() + "_troll_leather_helmet", "inventory"));
			ModelLoader.setCustomModelResourceLocation(troll.chestplate, 0, new ModelResourceLocation("iceandfire:"  + troll.name().toLowerCase() + "_troll_leather_chestplate", "inventory"));
			ModelLoader.setCustomModelResourceLocation(troll.leggings, 0, new ModelResourceLocation("iceandfire:"  + troll.name().toLowerCase() + "_troll_leather_leggings", "inventory"));
			ModelLoader.setCustomModelResourceLocation(troll.boots, 0, new ModelResourceLocation("iceandfire:"  + troll.name().toLowerCase() + "_troll_leather_boots", "inventory"));
		}
		ModelLoader.setCustomModelResourceLocation(ModItems.silver_helmet, 0, new ModelResourceLocation("iceandfire:armor_silver_metal_helmet", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.silver_chestplate, 0, new ModelResourceLocation("iceandfire:armor_silver_metal_chestplate", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.silver_leggings, 0, new ModelResourceLocation("iceandfire:armor_silver_metal_leggings", "inventory"));
		ModelLoader.setCustomModelResourceLocation(ModItems.silver_boots, 0, new ModelResourceLocation("iceandfire:armor_silver_metal_boots", "inventory"));
		ModelBakery.registerItemVariants(Item.getItemFromBlock(ModBlocks.myrmex_resin), new ResourceLocation("iceandfire:desert_myrmex_resin"), new ResourceLocation("iceandfire:jungle_myrmex_resin"));
		ModelBakery.registerItemVariants(Item.getItemFromBlock(ModBlocks.myrmex_resin_sticky), new ResourceLocation("iceandfire:desert_myrmex_resin_sticky"), new ResourceLocation("iceandfire:jungle_myrmex_resin_sticky"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.myrmex_resin), 0, new ModelResourceLocation("iceandfire:desert_myrmex_resin", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.myrmex_resin), 1, new ModelResourceLocation("iceandfire:jungle_myrmex_resin", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.myrmex_resin_sticky), 0, new ModelResourceLocation("iceandfire:desert_myrmex_resin_sticky", "inventory"));
		ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ModBlocks.myrmex_resin_sticky), 1, new ModelResourceLocation("iceandfire:jungle_myrmex_resin_sticky", "inventory"));
		for(int i = 0; i < 5; i++){
			ModelLoader.setCustomModelResourceLocation(ModItems.myrmex_desert_egg, i, new ModelResourceLocation("iceandfire:myrmex_desert_egg", "inventory"));
			ModelLoader.setCustomModelResourceLocation(ModItems.myrmex_jungle_egg, i, new ModelResourceLocation("iceandfire:myrmex_jungle_egg", "inventory"));
		}
		try {
			for (Field f : ModItems.class.getDeclaredFields()) {
				Object obj = f.get(null);
				if (obj instanceof Item && !(obj instanceof ICustomRendered)) {
					ModelLoader.setCustomModelResourceLocation((Item)obj, 0, new ModelResourceLocation("iceandfire:" + ((Item)obj).getRegistryName().getPath(), "inventory"));
				} else if (obj instanceof Item[]) {
					for (Item item : (Item[]) obj) {
						if (!(item instanceof ICustomRendered)) {
							ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation("iceandfire:" + item.getRegistryName().getPath(), "inventory"));
						}
					}
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
		ModelLoader.setCustomStateMapper(ModBlocks.charedDirt, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.charedGrass, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.charedStone, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.charedCobblestone, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.charedGravel, (new StateMap.Builder()).ignore(BlockFallingReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.charedGrassPath, (new StateMap.Builder()).ignore(BlockPath.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.frozenDirt, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.frozenGrass, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.frozenStone, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.frozenCobblestone, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.frozenGravel, (new StateMap.Builder()).ignore(BlockFallingReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.frozenGrassPath, (new StateMap.Builder()).ignore(BlockPath.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.crackledDirt, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.crackledGrass, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.crackledStone, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.crackledCobblestone, (new StateMap.Builder()).ignore(BlockReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.crackledGravel, (new StateMap.Builder()).ignore(BlockFallingReturningState.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.crackledGrassPath, (new StateMap.Builder()).ignore(BlockPath.REVERTS).build());
		ModelLoader.setCustomStateMapper(ModBlocks.dread_stone, (new StateMap.Builder()).ignore(BlockDreadBase.PLAYER_PLACED).build());
		ModelLoader.setCustomStateMapper(ModBlocks.dread_stone_bricks, (new StateMap.Builder()).ignore(BlockDreadBase.PLAYER_PLACED).build());
		ModelLoader.setCustomStateMapper(ModBlocks.dread_stone_bricks_chiseled, (new StateMap.Builder()).ignore(BlockDreadBase.PLAYER_PLACED).build());
		ModelLoader.setCustomStateMapper(ModBlocks.dread_stone_bricks_cracked, (new StateMap.Builder()).ignore(BlockDreadBase.PLAYER_PLACED).build());
		ModelLoader.setCustomStateMapper(ModBlocks.dread_stone_bricks_mossy, (new StateMap.Builder()).ignore(BlockDreadBase.PLAYER_PLACED).build());
		ModelLoader.setCustomStateMapper(ModBlocks.dread_stone_tile, (new StateMap.Builder()).ignore(BlockDreadBase.PLAYER_PLACED).build());
		ModelLoader.setCustomStateMapper(ModBlocks.dread_stone_face, (new StateMap.Builder()).ignore(BlockDreadStoneFace.PLAYER_PLACED).build());
		try {
			for (Field f : ModBlocks.class.getDeclaredFields()) {
				Object obj = f.get(null);
				if (obj instanceof Block && !(obj instanceof ICustomRendered)) {
					ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock((Block)obj), 0, new ModelResourceLocation("iceandfire:" + ((Block)obj).getRegistryName().getPath(), "inventory"));
				} else if (obj instanceof Block[]) {
					for (Block block : (Block[]) obj) {
						if(!(block instanceof ICustomRendered)){
							ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), 0, new ModelResourceLocation("iceandfire:" + block.getRegistryName().getPath(), "inventory"));

						}
					}
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public static void onRenderWorldLast(RenderWorldLastEvent event) {
		ParticleLightningRenderer.onRenderWorldLast(event);
	}

	@SideOnly(Side.CLIENT)
	public static void renderDragonArmors(EnumDragonArmor armor) {
		ModelLoader.setCustomModelResourceLocation(armor.helmet, 0, new ModelResourceLocation("iceandfire:" + armor.name() + "_helmet", "inventory"));
		ModelLoader.setCustomModelResourceLocation(armor.chestplate, 0, new ModelResourceLocation("iceandfire:" + armor.name() + "_chestplate", "inventory"));
		ModelLoader.setCustomModelResourceLocation(armor.leggings, 0, new ModelResourceLocation("iceandfire:" + armor.name() + "_leggings", "inventory"));
		ModelLoader.setCustomModelResourceLocation(armor.boots, 0, new ModelResourceLocation("iceandfire:" + armor.name() + "_boots", "inventory"));
	}

	@SideOnly(Side.CLIENT)
	public static void renderSeaSerpentArmors(EnumSeaSerpent armor) {
		ModelLoader.setCustomModelResourceLocation(armor.scale, 0, new ModelResourceLocation("iceandfire:sea_serpent_scales_" + armor.resourceName, "inventory"));
		ModelLoader.setCustomModelResourceLocation(armor.helmet, 0, new ModelResourceLocation("iceandfire:tide_" + armor.resourceName + "_helmet", "inventory"));
		ModelLoader.setCustomModelResourceLocation(armor.chestplate, 0, new ModelResourceLocation("iceandfire:tide_" + armor.resourceName + "_chestplate", "inventory"));
		ModelLoader.setCustomModelResourceLocation(armor.leggings, 0, new ModelResourceLocation("iceandfire:tide_" + armor.resourceName + "_leggings", "inventory"));
		ModelLoader.setCustomModelResourceLocation(armor.boots, 0, new ModelResourceLocation("iceandfire:tide_" + armor.resourceName + "_boots", "inventory"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	@SuppressWarnings("deprecation")
	public void render() {
		this.bestiaryFontRenderer = new FontRenderer(Minecraft.getMinecraft().gameSettings, new ResourceLocation("iceandfire:textures/font/bestiary.png"), Minecraft.getMinecraft().renderEngine, false);
		((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(this.bestiaryFontRenderer);
		ModKeys.init();
		MinecraftForge.EVENT_BUS.register(new RenderModArmor());
		MinecraftForge.EVENT_BUS.register(new RenderModCapes());
		MinecraftForge.EVENT_BUS.register(new EventClient());
		MinecraftForge.EVENT_BUS.register(new EventNewMenu());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDummyGorgonHead.class, new RenderGorgonHead(false));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDummyGorgonHeadActive.class, new RenderGorgonHead(true));
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityDreadSpawner.class, new RenderMobSpawner());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMonsterSpawner.class, new RenderMobSpawner());
		ForgeHooksClient.registerTESRItemStack(ModItems.gorgon_head, 0, TileEntityDummyGorgonHead.class);
		ForgeHooksClient.registerTESRItemStack(ModItems.gorgon_head, 1, TileEntityDummyGorgonHeadActive.class);
		renderEntities();
	}

	@SideOnly(Side.CLIENT)
	public void postRender() {
		EventClient.initializeStoneLayer();
		for(EnumTroll.Weapon weapon : EnumTroll.Weapon.values()) {
			weapon.item.setTileEntityItemStackRenderer(TEISR);
		}
	}

	@SuppressWarnings("deprecation")
	@SideOnly(Side.CLIENT)
	private void renderEntities() {
		EnumDragonAnimations.initializeDragonModels();
		EnumSeaSerpentAnimations.initializeSerpentModels();
		ModelBase firedragon_model = null;
		ModelBase icedragon_model = null;
		ModelBase lightningdragon_model = null;
		ModelBase seaserpent_model = null;

		try {
			firedragon_model = new IceAndFireTabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/firedragon/dragonFireGround"), new FireDragonTabulaModelAnimator());
			icedragon_model = new IceAndFireTabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/icedragon/dragonIceGround"), new IceDragonTabulaModelAnimator());
			lightningdragon_model = new IceAndFireTabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/lightningdragon/dragonLightningGround"), new LightningDragonTabulaModelAnimator());
			seaserpent_model = new IceAndFireTabulaModel(TabulaModelHandler.INSTANCE.loadTabulaModel("/assets/iceandfire/models/tabula/seaserpent/seaserpent"), new SeaSerpentTabulaModelAnimator());
		} catch (IOException e) {
			e.printStackTrace();
		}
		RenderingRegistry.registerEntityRenderingHandler(EntityFireDragon.class, new RenderDragonBase(Minecraft.getMinecraft().getRenderManager(), firedragon_model));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceDragon.class, new RenderDragonBase(Minecraft.getMinecraft().getRenderManager(), icedragon_model));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningDragon.class, new RenderDragonBase(Minecraft.getMinecraft().getRenderManager(), lightningdragon_model));
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonEgg.class, new RenderDragonEgg(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonArrow.class, new RenderDragonArrow(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonSkull.class, new RenderDragonSkull(Minecraft.getMinecraft().getRenderManager(), firedragon_model, icedragon_model, lightningdragon_model));
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonFire.class, new RenderNothing(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonIce.class, new RenderNothing(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonLightning.class, new RenderNothing(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonFireCharge.class, new RenderDragonFireCharge(Minecraft.getMinecraft().getRenderManager(), RenderDragonFireCharge.Type.FIRE));
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonIceCharge.class, new RenderDragonFireCharge(Minecraft.getMinecraft().getRenderManager(), RenderDragonFireCharge.Type.ICE));
		RenderingRegistry.registerEntityRenderingHandler(EntityDragonLightningCharge.class, new RenderDragonFireCharge(Minecraft.getMinecraft().getRenderManager(), RenderDragonFireCharge.Type.LIGHTNING));
		RenderingRegistry.registerEntityRenderingHandler(EntitySnowVillager.class, new RenderSnowVillager(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityHippogryphEgg.class, new RenderSnowball<EntityHippogryphEgg>(Minecraft.getMinecraft().getRenderManager(), ModItems.hippogryph_egg, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityHippogryph.class, new RenderHippogryph(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityStoneStatue.class, new RenderStoneStatue(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGorgon.class, new RenderGorgon(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityPixie.class, new RenderPixie(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityCyclops.class, new RenderCyclops(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntitySiren.class, new RenderSiren(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityHippocampus.class, new RenderHippocampus(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDeathWorm.class, new RenderDeathWorm(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDeathWormEgg.class, new RenderDeathWormEgg(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityCockatrice.class, new RenderCockatrice(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityCockatriceEgg.class, new RenderSnowball<EntityCockatriceEgg>(Minecraft.getMinecraft().getRenderManager(), ModItems.rotten_egg, Minecraft.getMinecraft().getRenderItem()));
		RenderingRegistry.registerEntityRenderingHandler(EntityStymphalianBird.class, new RenderStymphalianBird(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityStymphalianFeather.class, new RenderStymphalianFeather(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityStymphalianArrow.class, new RenderStymphalianArrow(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityTroll.class, new RenderTroll(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityMyrmexWorker.class, new RenderMyrmexBase(Minecraft.getMinecraft().getRenderManager(), new ModelMyrmexWorker(), 0.5F));
		RenderingRegistry.registerEntityRenderingHandler(EntityMyrmexSoldier.class, new RenderMyrmexBase(Minecraft.getMinecraft().getRenderManager(), new ModelMyrmexSoldier(), 0.75F));
		RenderingRegistry.registerEntityRenderingHandler(EntityMyrmexQueen.class, new RenderMyrmexBase(Minecraft.getMinecraft().getRenderManager(), new ModelMyrmexQueen(), 1.25F));
		RenderingRegistry.registerEntityRenderingHandler(EntityMyrmexEgg.class, new RenderMyrmexEgg(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityMyrmexSentinel.class, new RenderMyrmexBase(Minecraft.getMinecraft().getRenderManager(), new ModelMyrmexSentinel(), 0.85F));
		RenderingRegistry.registerEntityRenderingHandler(EntityMyrmexRoyal.class, new RenderMyrmexBase(Minecraft.getMinecraft().getRenderManager(), new ModelMyrmexRoyal(), 0.75F));
		RenderingRegistry.registerEntityRenderingHandler(EntityAmphithere.class, new RenderAmphithere(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityAmphithereArrow.class, new RenderAmphithereArrow(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntitySeaSerpent.class, new RenderSeaSerpent(Minecraft.getMinecraft().getRenderManager(), seaserpent_model));
		RenderingRegistry.registerEntityRenderingHandler(EntitySeaSerpentBubbles.class, new RenderNothing(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntitySeaSerpentArrow.class, new RenderSeaSerpentArrow(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityHydraArrow.class, new RenderHydraArrow(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityHydra.class, new RenderHydra(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityHydraBreath.class, new RenderNothing(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityTideTrident.class, new RenderTideTrident(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDreadThrall.class, new RenderDreadThrall(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDreadGhoul.class, new RenderDreadGhoul(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDreadBeast.class, new RenderDreadBeast(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDreadScuttler.class, new RenderDreadScuttler(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDreadLich.class, new RenderDreadLich(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDreadLichSkull.class, new RenderDreadLichSkull(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDreadKnight.class, new RenderDreadKnight(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityDreadHorse.class, new RenderDreadHorse(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGhost.class, new RenderGhost(Minecraft.getMinecraft().getRenderManager()));
		RenderingRegistry.registerEntityRenderingHandler(EntityGhostSword.class, new RenderGhostSword<>(Minecraft.getMinecraft().getRenderManager(), Minecraft.getMinecraft().getRenderItem()));

		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPodium.class, new RenderPodium());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLectern.class, new RenderLectern());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityEggInIce.class, new RenderEggInIce());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPixieHouse.class, new RenderPixieHouse());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityJar.class, new RenderJar());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityGhostChest.class, new RenderGhostChest());
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(ModBlocks.pixieHouse), 0, TileEntityPixieHouse.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(ModBlocks.pixieHouse), 1, TileEntityPixieHouse.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(ModBlocks.pixieHouse), 2, TileEntityPixieHouse.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(ModBlocks.pixieHouse), 3, TileEntityPixieHouse.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(ModBlocks.pixieHouse), 4, TileEntityPixieHouse.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(ModBlocks.pixieHouse), 5, TileEntityPixieHouse.class);
		ForgeHooksClient.registerTESRItemStack(Item.getItemFromBlock(ModBlocks.ghost_chest), 0, TileEntityGhostChest.class);
	}

	@SideOnly(Side.CLIENT)
	public void spawnLightningEffect(World world, ParticleLightningVector sourceVec, ParticleLightningVector targetVec, boolean isProjectile) {
		Particle particle = new ParticleLightning(world, sourceVec, targetVec, isProjectile);
		Minecraft.getMinecraft().effectRenderer.addEffect(particle);
	}

	@SideOnly(Side.CLIENT)
	public void spawnParticle(EnumParticle type, World world, double x, double y, double z, double motX, double motY, double motZ) {
		spawnParticle(type, world, x, y, z, motX, motY, motZ, 1);
	}
	@SideOnly(Side.CLIENT)
	public void spawnParticle(EnumParticle type, World world, double x, double y, double z, double motX, double motY, double motZ, float size) {
		Particle particle;
		switch (type) {
			case DRAGON_FIRE:
				particle = new ParticleDragonFlame(world, x, y, z, motX, motY, motZ);
				break;
			case DRAGON_ICE:
				particle = new ParticleDragonFrost(world, x, y, z, motX, motY, motZ);
				break;
			case FLAME:
				particle = new ParticleFlame.Factory().createParticle(0, world, x, y, z, motX, motY, motZ);
				break;
			case SNOWFLAKE:
				particle = new ParticleSnowflake(world, x, y, z, motX, motY, motZ);
				break;
			case SPARK:
				particle = new ParticleSpark(world, x, y, z, motX, motY, motZ);
				break;
			case SMOKE_NORMAL:
				particle = new ParticleSmokeNormal.Factory().createParticle(0, world, x, y, z, motX, motY, motZ);
				break;
			case SMOKE_LARGE:
				particle = new ParticleSmokeLarge.Factory().createParticle(0, world, x, y, z, motX, motY, motZ);
				break;
			case HYDRA_BREATH:
				particle = new ParticleHydraBreath(world, x, y, z, (float) motX,(float) motY, (float) motZ);
				break;
			case BLOOD:
				particle = new ParticleBlood(world, x, y, z);
				break;
			case PIXIE_DUST:
				particle = new ParticlePixieDust(world, x, y, z, (float) motX, (float) motY, (float) motZ);
				break;
			case SIREN_APPEARANCE:
				particle = new ParticleSirenAppearance(world, x, y, z);
				break;
			case SIREN_MUSIC:
				particle = new ParticleSirenMusic(world, x, y, z, motX, motY, motZ);
				break;
			case SERPENT_BUBBLE:
				particle = new ParticleSerpentBubble(world, x, y, z, motX, motY, motZ);
				break;
			case EXPLOSION:
				particle = new ParticleExplosion.Factory().createParticle(0, world, x, y, z, motX, motY, motZ);
				break;
			case CLOUD:
				particle = new ParticleCloud.Factory().createParticle(0, world, x, y, z, motX, motY, motZ);
				break;
			case DREAD_TORCH:
				particle = new ParticleDreadTorch(world, x, y, z, motX, motY, motZ, size);
				break;
			case GHOST_APPEARANCE:
				particle = new ParticleGhostAppearance(world, x, y, z);
				break;
			case REDSTONE:
				particle = new ParticleRedstone.Factory().createParticle(0, world, x, y, z, motX, motY, motZ);
				break;
			default:
				particle = new ParticleSmokeNormal.Factory().createParticle(0, world, x, y, z, motX, motY, motZ);
		}
		if (particle != null) {
			Minecraft.getMinecraft().effectRenderer.addEffect(particle);
		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void openBestiaryGui(ItemStack book) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiBestiary(book));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void openMyrmexStaffGui(ItemStack staff) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiMyrmexStaff(staff));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void openMyrmexAddRoomGui(ItemStack staff, BlockPos pos, EnumFacing facing) {
		Minecraft.getMinecraft().displayGuiScreen(new GuiMyrmexAddRoom(staff, pos, facing));
	}


	@SideOnly(Side.CLIENT)
	public Object getArmorModel(int armorId) {
		switch (armorId) {
			case 0:
				return COPPER_ARMOR_MODEL;
			case 1:
				return COPPER_ARMOR_MODEL_LEGS;
			case 2:
				return SILVER_ARMOR_MODEL;
			case 3:
				return SILVER_ARMOR_MODEL_LEGS;
			case 4:
				return FIRE_DRAGON_SCALE_ARMOR_MODEL;
			case 5:
				return FIRE_DRAGON_SCALE_ARMOR_MODEL_LEGS;
			case 6:
				return ICE_DRAGON_SCALE_ARMOR_MODEL;
			case 7:
				return ICE_DRAGON_SCALE_ARMOR_MODEL_LEGS;
			case 8:
				return LIGHTNING_DRAGON_SCALE_ARMOR_MODEL;
			case 9:
				return LIGHTNING_DRAGON_SCALE_ARMOR_MODEL_LEGS;
			case 10:
				return DEATHWORM_ARMOR_MODEL;
			case 11:
				return DEATHWORM_ARMOR_MODEL_LEGS;
			case 12:
				return TROLL_ARMOR_MODEL;
			case 13:
				return TROLL_ARMOR_MODEL_LEGS;
			case 14:
				return TIDE_ARMOR_MODEL;
			case 15:
				return TIDE_ARMOR_MODEL_LEGS;
		}
		return null;
	}

	public Object getFontRenderer() {
		return this.bestiaryFontRenderer;
	}

	public int getDragon3rdPersonView() {
		return thirdPersonViewDragon;
	}

	public void setDragon3rdPersonView(int view) {
		thirdPersonViewDragon = view;
	}

	public void updateDragonArmorRender(String clear){
		RenderDragonBase.clearCache(clear);
	}

	public static MyrmexHive getReferedClientHive(){
		return referedClientHive;
	}

	public static void setReferedClientHive(MyrmexHive hive){
		referedClientHive = hive;
	}
}
