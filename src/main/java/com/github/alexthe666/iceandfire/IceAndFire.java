package com.github.alexthe666.iceandfire;

import com.github.alexthe666.iceandfire.api.IEntityEffectCapability;
import com.github.alexthe666.iceandfire.capability.CapabilityHandler;
import com.github.alexthe666.iceandfire.capability.entityeffect.EntityEffectCapability;
import com.github.alexthe666.iceandfire.capability.entityeffect.EntityEffectStorage;
import com.github.alexthe666.iceandfire.core.ModEntities;
import com.github.alexthe666.iceandfire.core.ModRecipes;
import com.github.alexthe666.iceandfire.core.ModVillagers;
import com.github.alexthe666.iceandfire.event.EventLiving;
import com.github.alexthe666.iceandfire.event.StructureGenerator;
import com.github.alexthe666.iceandfire.integration.CompatLoadUtil;
import com.github.alexthe666.iceandfire.integration.RLCombatCompat;
import com.github.alexthe666.iceandfire.integration.ThaumcraftCompatBridge;
import com.github.alexthe666.iceandfire.loot.CustomizeToDragon;
import com.github.alexthe666.iceandfire.loot.CustomizeToSeaSerpent;
import com.github.alexthe666.iceandfire.message.*;
import com.github.alexthe666.iceandfire.misc.CreativeTab;
import com.github.alexthe666.iceandfire.world.village.ComponentAnimalFarm;
import com.github.alexthe666.iceandfire.world.village.MapGenSnowVillage;
import com.github.alexthe666.iceandfire.world.village.VillageAnimalFarmCreator;
import net.ilexiconn.llibrary.server.network.NetworkWrapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

@Mod(modid = IceAndFire.MODID, dependencies = "required-after:llibrary@[" + IceAndFire.LLIBRARY_VERSION + ",)", version = IceAndFire.VERSION, name = IceAndFire.NAME)
public class IceAndFire {

    public static final String MODID = "iceandfire";
    public static final String VERSION = "2.0.0";
    public static final String LLIBRARY_VERSION = "1.7.9";
    public static final String NAME = "Ice And Fire";
    public static final Logger logger = LogManager.getLogger(NAME);
    @Instance(value = MODID)
    public static IceAndFire INSTANCE;
    @NetworkWrapper({
            MessageDragonArmor.class, MessageDragonControl.class, MessageHippogryphArmor.class,
            MessageUpdatePixieHouse.class, MessageUpdatePodium.class, MessageUpdatePixieHouseModel.class,
            MessageUpdatePixieJar.class, MessageSirenSong.class, MessageDeathWormHitbox.class,
            MessageMultipartInteract.class, MessageGetMyrmexHive.class, MessageSetMyrmexHiveNull.class,
            MessagePlayerHitMultipart.class, MessageChainLightningFX.class, MessageEntityEffect.class,
            MessageResetEntityEffect.class, MessageParticleFX.class, MessageParticleVanillaFX.class,
            MessageUpdateRidingState.class, MessageSwingArm.class, MessageUpdateSpawner.class
    })
    public static SimpleNetworkWrapper NETWORK_WRAPPER;
    @SidedProxy(clientSide = "com.github.alexthe666.iceandfire.ClientProxy", serverSide = "com.github.alexthe666.iceandfire.CommonProxy")
    public static CommonProxy PROXY;
    public static CreativeTabs TAB;
    public static DamageSource acid;
    public static DamageSource dragon;
    public static DamageSource dragonFire;
    public static DamageSource dragonIce;
    public static DamageSource dragonLightning;
    public static DamageSource gorgon;
    public static Biome GLACIER;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        CapabilityManager.INSTANCE.register(IEntityEffectCapability.class, new EntityEffectStorage(), EntityEffectCapability::new);
        MinecraftForge.EVENT_BUS.register(new EventLiving());
        MinecraftForge.EVENT_BUS.register(new CapabilityHandler());
        if(CompatLoadUtil.isRLCombatLoaded()) MinecraftForge.EVENT_BUS.register(RLCombatCompat.class);
        TAB = new CreativeTab(MODID);
        ModEntities.init();
        MinecraftForge.EVENT_BUS.register(PROXY);
        logger.info("A raven flies from the north to the sea");
        logger.info("A dragon whispers her name in the east");
        ThaumcraftCompatBridge.loadThaumcraftCompat();
        LootFunctionManager.registerFunction(new CustomizeToDragon.Serializer());
        LootFunctionManager.registerFunction(new CustomizeToSeaSerpent.Serializer());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ModVillagers.INSTANCE.init();
        logger.info("The watcher waits on the northern wall");
        logger.info("A daughter picks up a warrior's sword");
        MapGenStructureIO.registerStructure(MapGenSnowVillage.Start.class, "SnowVillageStart");
        MapGenStructureIO.registerStructureComponent(ComponentAnimalFarm.class, "AnimalFarm");
        VillagerRegistry.instance().registerVillageCreationHandler(new VillageAnimalFarmCreator());
        PROXY.render();
        GameRegistry.registerWorldGenerator(new StructureGenerator(), 0);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new com.github.alexthe666.iceandfire.client.GuiHandler());
        dragon = new DamageSource("dragon") {
            @Override
            public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
                String s = "death.attack.dragon";
                String s1 = s + ".player_" + new Random().nextInt(2);
                return new TextComponentString(entityLivingBaseIn.getDisplayName().getFormattedText() + " ").appendSibling(new TextComponentTranslation(s1, new Object[]{entityLivingBaseIn.getDisplayName()}));
            }
        };
        dragonFire = new DamageSource("dragon_fire") {
            @Override
            public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
                String s = "death.attack.dragon_fire";
                String s1 = s + ".player_" + new Random().nextInt(2);
                return new TextComponentString(entityLivingBaseIn.getDisplayName().getFormattedText() + " ").appendSibling(new TextComponentTranslation(s1, new Object[]{entityLivingBaseIn.getDisplayName()}));
            }
        }.setFireDamage();
        dragonIce = new DamageSource("dragon_ice") {
            @Override
            public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
                String s = "death.attack.dragon_ice";
                String s1 = s + ".player_" + new Random().nextInt(2);
                return new TextComponentString(entityLivingBaseIn.getDisplayName().getFormattedText() + " ").appendSibling(new TextComponentTranslation(s1, new Object[]{entityLivingBaseIn.getDisplayName()}));
            }
        };
        dragonLightning = new DamageSource("dragon_lightning") {
            @Override
            public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
                String s = "death.attack.dragon_lightning";
                String s1 = s + ".player_" + new Random().nextInt(2);
                return new TextComponentString(entityLivingBaseIn.getDisplayName().getFormattedText() + " ").appendSibling(new TextComponentTranslation(s1, new Object[]{entityLivingBaseIn.getDisplayName()}));
            }
        };
        acid = new DamageSource("acid") {
            @Override
            public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
                String s = "death.attack.acid";
                String s1 = s + ".player_" + new Random().nextInt(2);
                return new TextComponentString(entityLivingBaseIn.getDisplayName().getFormattedText() + " ").appendSibling(new TextComponentTranslation(s1, new Object[]{entityLivingBaseIn.getDisplayName()}));
            }
        }.setDamageBypassesArmor();
        gorgon = new DamageSource("gorgon") {
            @Override
            public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn) {
                String s = "death.attack.gorgon";
                String s1 = s + ".player_" + new Random().nextInt(2);
                return new TextComponentString(entityLivingBaseIn.getDisplayName().getFormattedText() + " ").appendSibling(new TextComponentTranslation(s1, new Object[]{entityLivingBaseIn.getDisplayName()}));
            }
        }.setDamageBypassesArmor();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        PROXY.postRender();

        logger.info("A brother bound to a love he must hide");
        logger.info("The younger's armor is worn in the mind");

        logger.info("A cold iron throne holds a boy barely grown");
        logger.info("And now it is known");
        logger.info("A claim to the prize, a crown laced in lies");
        logger.info("You win or you die");
    }
}
