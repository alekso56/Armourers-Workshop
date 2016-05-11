package riskyken.armourersWorkshop.client.render.entity;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import riskyken.armourersWorkshop.api.client.render.entity.ISkinnableEntityRenderer;
import riskyken.armourersWorkshop.api.common.skin.entity.ISkinnableEntity;
import riskyken.armourersWorkshop.common.skin.entity.EntitySkinHandler;
import riskyken.armourersWorkshop.common.skin.entity.ExPropsEntityEquipmentData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class EntitySkinRenderHandler {
    
    public static EntitySkinRenderHandler INSTANCE;
    
    public static void init() {
        INSTANCE = new EntitySkinRenderHandler();
    }
    
    private HashMap<Class<? extends EntityLivingBase>, ISkinnableEntityRenderer> entityRenderer;
    
    public EntitySkinRenderHandler() {
        MinecraftForge.EVENT_BUS.register(this);
        entityRenderer = new HashMap<Class<? extends EntityLivingBase>, ISkinnableEntityRenderer>();
    }
    
    public void initRenderer() {
        loadNpcRenderers();
    }
    
    private void loadNpcRenderers() {        
        ArrayList<ISkinnableEntity> skinnableEntities = EntitySkinHandler.INSTANCE.getRegisteredEntities();
        for (int i = 0; i < skinnableEntities.size(); i++) {
            ISkinnableEntity skinnableEntity = skinnableEntities.get(i);
            if (skinnableEntity.getRendererClass() != null) {
                registerRendererForNpc(skinnableEntity.getEntityClass(), skinnableEntity.getRendererClass());
            }
        }
    }
    
    private void registerRendererForNpc(Class<? extends EntityLivingBase> entity, Class<? extends ISkinnableEntityRenderer> renderClass) {
        try {
            entityRenderer.put(entity, renderClass.newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    
    @SubscribeEvent
    public void onRenderLivingEvent(RenderLivingEvent.Post event) {
        EntityLivingBase entity = event.getEntity();
        if (entity instanceof EntityPlayer) {
            return;
        }
        if (entityRenderer.containsKey(entity.getClass())) {
            ISkinnableEntityRenderer renderer = entityRenderer.get(entity.getClass());
            ExPropsEntityEquipmentData props = ExPropsEntityEquipmentData.getExtendedPropsForEntity(entity);
            if (props == null) {
                return;
            }
            renderer.render(entity, event.getRenderer(), event.getX(), event.getY(), event.getZ(), props.getEquipmentData());
        }
    }
}
