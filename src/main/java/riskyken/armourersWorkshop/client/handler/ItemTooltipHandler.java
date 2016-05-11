package riskyken.armourersWorkshop.client.handler;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import riskyken.armourersWorkshop.common.items.ItemSkin;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemTooltipHandler {

    public ItemTooltipHandler() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onItemTooltipEvent (ItemTooltipEvent event) {
        if (event.itemStack == null) {
            return;
        }
        ItemSkin.addTooltipToSkinItem(event.itemStack, event.entityPlayer, event.toolTip, event.showAdvancedItemTooltips);
    }
}
