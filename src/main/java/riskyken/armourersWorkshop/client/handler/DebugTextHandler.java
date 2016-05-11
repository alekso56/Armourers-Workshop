package riskyken.armourersWorkshop.client.handler;

import java.util.Collection;
import java.util.List;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import riskyken.armourersWorkshop.ArmourersWorkshop;
import riskyken.armourersWorkshop.client.model.bake.ModelBakery;
import riskyken.armourersWorkshop.client.render.SkinModelRenderer;
import riskyken.armourersWorkshop.client.skin.ClientSkinCache;
import riskyken.armourersWorkshop.client.skin.ClientSkinPaintCache;
import riskyken.armourersWorkshop.common.config.ConfigHandler;
import riskyken.armourersWorkshop.common.lib.LibModInfo;

@SideOnly(Side.CLIENT)
public class DebugTextHandler {
    
    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onDebugText(RenderGameOverlayEvent.Text event) {
        if (!ConfigHandler.showF3DebugInfo) {
            return;
        }
        if (event.getLeft() != null && event.getLeft().size() > 0) {
            EntityPlayerSP localPlayer = Minecraft.getMinecraft().thePlayer;
            Collection<NetworkPlayerInfo> playerList = localPlayer.sendQueue.getPlayerInfoMap();
            event.getLeft().add("");
            event.getLeft().add(TextFormatting.GOLD + "[" + LibModInfo.NAME + "]");
            String dataLine = "";
            dataLine += "sc:" + ArmourersWorkshop.proxy.getPlayerModelCacheSize() + " ";
            dataLine += "pc:" + ClientSkinCache.INSTANCE.getPartCount() + " ";
            dataLine += "mc:" + ClientSkinCache.INSTANCE.getModelCount() + " ";
            dataLine += "pd:" + SkinModelRenderer.INSTANCE.getSkinDataMapSize() + " ";
            event.getLeft().add(dataLine);
            dataLine = "bq:" + ModelBakery.INSTANCE.getBakingQueueSize() + " ";
            dataLine += "rq:" + ClientSkinCache.INSTANCE.getRequestQueueSize() + " ";
            dataLine += "sr:" + ModClientFMLEventHandler.skinRenderLastTick + " ";
            dataLine += "tc:" + ClientSkinPaintCache.INSTANCE.size() + " ";
            if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
                for (int i = 0; i < playerList.size(); i++) {
                	NetworkPlayerInfo player = Minecraft.getMinecraft().getNetHandler().getPlayerInfo(localPlayer.getUniqueID());
                    if (player.getGameProfile().getName().equals(localPlayer.getName())) {
                        dataLine += " ping:" + player.getResponseTime() + "ms";
                        break;
                    }
                } 
            }
            
            event.getLeft().add(dataLine);
        }
    }
}
