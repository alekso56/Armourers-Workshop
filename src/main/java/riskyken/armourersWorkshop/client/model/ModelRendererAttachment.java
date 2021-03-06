package riskyken.armourersWorkshop.client.model;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import riskyken.armourersWorkshop.api.common.skin.data.ISkinDye;
import riskyken.armourersWorkshop.api.common.skin.type.ISkinPartType;
import riskyken.armourersWorkshop.api.common.skin.type.ISkinType;
import riskyken.armourersWorkshop.client.model.bake.SkinBaker;
import riskyken.armourersWorkshop.client.render.EquipmentModelRenderer;
import riskyken.armourersWorkshop.client.render.EquipmentPartRenderer;
import riskyken.armourersWorkshop.client.render.MannequinFakePlayer;
import riskyken.armourersWorkshop.common.skin.data.Skin;
import riskyken.armourersWorkshop.common.skin.data.SkinPart;
import riskyken.armourersWorkshop.common.skin.type.SkinTypeRegistry;
import riskyken.armourersWorkshop.proxies.ClientProxy;

@SideOnly(Side.CLIENT)
public class ModelRendererAttachment extends ModelRenderer {

    private final ISkinType skinType;
    private final ISkinPartType skinPart;
    private final Minecraft mc;
    private ModelBiped baseModel;
    
    public ModelRendererAttachment(ModelBiped modelBase, ISkinType skinType, ISkinPartType skinPart) {
        super(modelBase);
        this.baseModel = modelBase;
        mc = Minecraft.getMinecraft();
        this.skinType = skinType;
        this.skinPart = skinPart;
        addBox(0, 0, 0, 0, 0, 0);
    }
    
    @Override
    public void render(float scale) {
        if (!ClientProxy.useAttachedModelRender()) {
            return;
        }
        mc.mcProfiler.startSection("armourers player render");
        EquipmentModelRenderer modelRenderer = EquipmentModelRenderer.INSTANCE;
        EntityPlayer player = modelRenderer.targetPlayer;
        if (player == null) {
            mc.mcProfiler.endSection();
            return;
        }
        if (player instanceof MannequinFakePlayer) {
            mc.mcProfiler.endSection();
            return;
        }
        if (!SkinBaker.withinMaxRenderDistance(player.posX, player.posY, player.posZ)) {
            mc.mcProfiler.endSection();
            return;
        }
        Skin data = modelRenderer.getPlayerCustomArmour(player, skinType);
        if (data == null) {
            mc.mcProfiler.endSection();
            return;
        }
        ISkinDye skinDye = modelRenderer.getPlayerDyeData(player, skinType);
        
        data.onUsed();
        int size = data.getParts().size();
        //ModLogger.log("start");
        for (int i = 0; i < size; i++) {
            SkinPart partData = data.getParts().get(i);
            //ModLogger.log(partData.getPartType());
            if (partData.getPartType() == skinPart) {
                
                GL11.glPushMatrix();
                if (skinType == SkinTypeRegistry.skinLegs && skinPart.getRegistryName().equals("armourers:legs.skirt")) {
                    GL11.glRotated(Math.toDegrees(-baseModel.bipedLeftLeg.rotateAngleX), 1F, 0F, 0F);
                    GL11.glTranslatef(-2 * scale, 0, 0);
                    if (player.isSneaking()) {
                    }
                }
                GL11.glEnable(GL11.GL_CULL_FACE);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                GL11.glEnable(GL11.GL_BLEND);
                EquipmentPartRenderer.INSTANCE.renderPart(partData, scale, skinDye);
                GL11.glDisable(GL11.GL_CULL_FACE);
                GL11.glPopMatrix();
                break;
            }
            
        }
        
        if (ClientProxy.useSafeTextureRender()) {
            if (player instanceof AbstractClientPlayer) {
                AbstractClientPlayer clientPlayer = (AbstractClientPlayer) player;
                Minecraft.getMinecraft().renderEngine.bindTexture(clientPlayer.getLocationSkin());
            }
        }
        mc.mcProfiler.endSection();
    }
}
