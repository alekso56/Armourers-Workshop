package riskyken.armourersWorkshop.api.common.skin.type;

import java.util.ArrayList;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.IIcon;

/**
 * 
 * @author RiskyKen
 *
 */
public interface ISkinType {

    public ArrayList<ISkinPartType> getSkinParts();
    
    /**
     * Gets the name this skin will be registered with.
     * Armourer's Workshop uses the format armourers:skinName.
     * Example armourers:head is the registry name of
     * Armourer's Workshop head armour skin.
     * @return registryName
     */
    public String getRegistryName();
    
    /**
     * This only exists for backwards compatibility with old world saves.
     * Just return getRegistryName().
     * @return name
     */
    public String getName();
    
    
    @SideOnly(Side.CLIENT)
    public void registerIcon(IIconRegister register);
    
    @SideOnly(Side.CLIENT)
    public IIcon getIcon();
    
    @SideOnly(Side.CLIENT)
    public IIcon getEmptySlotIcon();
    
    /**
     * Should the show skin overlay check box be shown in the armourer and mini armourer.
     * @return
     */
    public boolean showSkinOverlayCheckbox();
    
    /**
     * Should the helper check box be shown in the armourer and mini armourer.
     * @return
     */
    public boolean showHelperCheckbox();
    
    /**
     * If this skin is for vanilla armour return the slot id here, otherwise return -1.
     * @return slotId
     */
    public int getVanillaArmourSlotId();
    
    /**
     * Should return id that was given in setId.
     * @return id
     */
    public int getId();
    
    /**
     * Id given to this skin when it is register.
     * @param id
     */
    public void setId(int id);
    
    /**
     * Should this skin be hidden from the user?
     * @return Is hidden?
     */
    public boolean isHidden();
}
