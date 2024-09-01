package com.github.alexthe666.iceandfire.client.render.tile;

import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import com.github.alexthe666.iceandfire.entity.EntityDreadBeast;
import com.github.alexthe666.iceandfire.entity.EntityHippogryph;
import com.github.alexthe666.iceandfire.entity.EntityHydra;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexBase;
import com.github.alexthe666.iceandfire.entity.EntityMyrmexSentinel;
import com.github.alexthe666.iceandfire.entity.EntityPixie;
import com.github.alexthe666.iceandfire.entity.EntitySeaSerpent;
import com.github.alexthe666.iceandfire.entity.tile.SpawnerBaseLogic;
import com.github.alexthe666.iceandfire.entity.tile.TileEntityMonsterSpawner;
import com.github.alexthe666.iceandfire.entity.tile.TileEntitySpawnerBase;
import com.github.alexthe666.iceandfire.entity.util.EntityDreadMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntityMobSpawner;

public class RenderMobSpawner extends TileEntitySpecialRenderer<TileEntitySpawnerBase> {

    public static void renderMob(SpawnerBaseLogic mobSpawnerLogic, double posX, double posY, double posZ, float partialTicks) {
        Entity entity = mobSpawnerLogic.getCachedEntity();

        if (entity != null) {
            float f = 0.53125F;
            float f1 = Math.max(entity.width, entity.height);

            if ((double) f1 > 1.0D) {
                f /= f1;
            }

            if (entity instanceof EntityDragonBase || entity instanceof EntityHydra) {
                f /= 2.15F;
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            } else if (entity instanceof EntityDreadMob) {
                if (entity instanceof EntityDreadBeast) {
                    f /= 2.15F;
                }
                GlStateManager.translate(0.0F, 0.5F, 0.0F);
            } else if (entity instanceof EntityMyrmexBase && !(entity instanceof EntityMyrmexSentinel) || entity instanceof EntityHippogryph) {
                f /= 1.5F;
                if (entity instanceof EntityMyrmexBase) {
                    GlStateManager.translate(0.0F, 0.2F, 0.0F);
                }
            } else if (entity instanceof EntitySeaSerpent) {
                f /= 4F;
                GlStateManager.translate(0.0F, 0.3F, 0.0F);
            } else if (entity instanceof EntityPixie) {
                f *= 1.15F;
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            GlStateManager.translate(0.0F, 0.4F, 0.0F);
            GlStateManager.rotate((float) (mobSpawnerLogic.getPrevMobRotation() + (mobSpawnerLogic.getMobRotation() - mobSpawnerLogic.getPrevMobRotation()) * (double) partialTicks) * 10.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -0.2F, 0.0F);
            GlStateManager.rotate(-30.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.scale(f, f, f);
            entity.setLocationAndAngles(posX, posY, posZ, 0.0F, 0.0F);
            Minecraft.getMinecraft().getRenderManager().renderEntity(entity, 0.0D, 0.0D, 0.0D, 0.0F, partialTicks, false);
        }
    }

    @Override
    public void render(TileEntitySpawnerBase te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x + 0.5F, (float) y, (float) z + 0.5F);
        renderMob(te.getSpawnerBaseLogic(), x, y, z, partialTicks);
        GlStateManager.popMatrix();
    }
}