package riskyken.armourersWorkshop.api.client.render.entity;

import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import riskyken.armourersWorkshop.api.common.skin.IEntityEquipment;

public interface ISkinnableEntityRenderer {

    public void render(EntityLivingBase entity, RenderLivingBase renderLivingBase, double x, double y, double z, IEntityEquipment entityEquipment);
}
