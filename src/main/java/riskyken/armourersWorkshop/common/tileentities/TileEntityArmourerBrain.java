package riskyken.armourersWorkshop.common.tileentities;

import java.util.ArrayList;

import org.apache.logging.log4j.Level;

import com.mojang.authlib.GameProfile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import riskyken.armourersWorkshop.api.common.painting.IPantableBlock;
import riskyken.armourersWorkshop.api.common.skin.type.ISkinType;
import riskyken.armourersWorkshop.common.exception.InvalidCubeTypeException;
import riskyken.armourersWorkshop.common.exception.SkinSaveException;
import riskyken.armourersWorkshop.common.items.ItemEquipmentSkin;
import riskyken.armourersWorkshop.common.lib.LibBlockNames;
import riskyken.armourersWorkshop.common.painting.IBlockPainter;
import riskyken.armourersWorkshop.common.skin.ArmourerWorldHelper;
import riskyken.armourersWorkshop.common.skin.ISkinHolder;
import riskyken.armourersWorkshop.common.skin.SkinDataCache;
import riskyken.armourersWorkshop.common.skin.data.Skin;
import riskyken.armourersWorkshop.common.skin.data.SkinPointer;
import riskyken.armourersWorkshop.common.skin.data.SkinTexture;
import riskyken.armourersWorkshop.common.skin.type.SkinTypeRegistry;
import riskyken.armourersWorkshop.common.undo.UndoManager;
import riskyken.armourersWorkshop.utils.GameProfileUtils;
import riskyken.armourersWorkshop.utils.GameProfileUtils.IGameProfileCallback;
import riskyken.armourersWorkshop.utils.ModLogger;
import riskyken.armourersWorkshop.utils.SkinNBTHelper;
import riskyken.plushieWrapper.common.world.BlockLocation;

public class TileEntityArmourerBrain extends AbstractTileEntityInventory implements IGameProfileCallback {
    
    private static final String TAG_DIRECTION = "direction";
    private static final String TAG_OWNER = "owner";
    private static final String TAG_TYPE = "skinType";
    private static final String TAG_TYPE_OLD = "type";
    private static final String TAG_SHOW_GUIDES = "showGuides";
    private static final String TAG_SHOW_OVERLAY = "showOverlay";
    private static final String TAG_SHOW_HELPER = "showHelper";
    private static final String TAG_CUSTOM_NAME = "customeName";
    private static final String TAG_PAINT_DATA = "paintData";
    private static final int HEIGHT_OFFSET = 1;
    
    private ForgeDirection direction;
    private GameProfile gameProfile = null;
    private GameProfile newProfile = null;
    private ISkinType skinType;
    private boolean showGuides;
    private boolean showOverlay;
    private boolean showHelper;
    private String customName;
    private int[] paintData;
    @SideOnly(Side.CLIENT)
    public SkinTexture skinTexture;
    
    public TileEntityArmourerBrain() {
        this.direction = ForgeDirection.NORTH;
        this.skinType = SkinTypeRegistry.INSTANCE.getSkinTypeFromRegistryName("armourers:head");
        this.items = new ItemStack[2];
        this.showOverlay = true;
        this.showGuides = true;
        this.showHelper = true;
        this.customName = "";
        clearPaintData();
    }
    
    @Override
    public boolean canUpdate() {
        return false;
    }
    
    public int[] getPaintData() {
        return paintData;
    }
    
    public void updatePaintData(int x, int y, int colour) {
        paintData[x + (y * SkinTexture.TEXTURE_WIDTH)] = colour;
        this.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    public int getPaintData(int x, int y) {
        return paintData[x + (y * SkinTexture.TEXTURE_WIDTH)];
    }
    
    /**
     * Get blocks in the world and saved them onto an items NBT data.
     * @param player The player that pressed the save button.
     * @param name Custom name for the item.
     */
    public void saveArmourItem(EntityPlayerMP player, String name, String tags) {
        if (this.worldObj.isRemote) {
            return;
        }
        ItemStack stackInput = getStackInSlot(0);
        ItemStack stackOutput = getStackInSlot(1);
        
        if (stackInput == null) {
            return;
        }
        if (stackOutput != null) {
            return;
        }
        if (!(stackInput.getItem() instanceof ISkinHolder)) {
            return;
        }
        ISkinHolder inputItem = (ISkinHolder)stackInput.getItem();
        
        String authorName = player.getCommandSenderName();
        //authorName = "";
        String customName = name;
        
        Skin armourItemData = null;
        
        try {
            armourItemData = ArmourerWorldHelper.saveSkinFromWorld(worldObj, player, skinType, authorName, customName,
                    tags, paintData, xCoord, yCoord + HEIGHT_OFFSET, zCoord, direction);
        } catch (InvalidCubeTypeException e) {
            ModLogger.log(Level.ERROR, "Unable to save skin. Unknown cube types found.");
            e.printStackTrace();
        } catch (SkinSaveException e) {
            switch (e.getType()) {
            case NO_DATA:
                player.addChatMessage(new ChatComponentText(e.getMessage()));
                break;
            case MARKER_ERROR:
                player.addChatMessage(new ChatComponentText(e.getMessage()));
                break;
            case MISSING_PARTS:
                player.addChatMessage(new ChatComponentText(e.getMessage()));
                break;
            }
        }
        
        if (armourItemData == null) {
            return;
        }
        
        SkinDataCache.INSTANCE.addEquipmentDataToCache(armourItemData);
        
        stackOutput = inputItem.makeStackForEquipment(armourItemData);
        if (stackOutput == null) {
            return;
        }
        
        this.decrStackSize(0, 1);
        setInventorySlotContents(1, stackOutput);
        
    }

    /**
     * Reads the NBT data from an item and places blocks in the world.
     * @param player The player that pressed the load button.
     */
    public void loadArmourItem(EntityPlayerMP player) {
        if (this.worldObj.isRemote) {
            return;
        }
        ItemStack stackInput = this.getStackInSlot(0);
        ItemStack stackOuput = this.getStackInSlot(1);
        
        if (stackInput == null) {
            return;
        }
        if (stackOuput != null) {
            return;
        }
        if (!(stackInput.getItem() instanceof ItemEquipmentSkin)) {
            return;
        }
        
        if (!SkinNBTHelper.stackHasSkinData(stackInput)) {
            return;
        }
        
        SkinPointer skinData = SkinNBTHelper.getSkinPointerFromStack(stackInput);

        if (skinType == null) {
            return;
        }
        if (skinType != skinData.skinType) {
            if (!(skinType == SkinTypeRegistry.skinLegs && skinData.skinType == SkinTypeRegistry.skinSkirt)) {
                return;
            }
        }
        
        int equipmentId = SkinNBTHelper.getSkinIdFromStack(stackInput);
        Skin equipmentData = SkinDataCache.INSTANCE.getEquipmentData(equipmentId);
        setCustomName(equipmentData.getCustomName());
        
        ArmourerWorldHelper.loadSkinIntoWorld(worldObj, xCoord, yCoord + HEIGHT_OFFSET, zCoord, equipmentData, direction);
        if (equipmentData.hasPaintData()) {
            this.paintData = equipmentData.getPaintData();
        } else {
            clearPaintData();
        }
        this.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        
        this.setInventorySlotContents(0, null);
        this.setInventorySlotContents(1, stackInput);
    }
    
    private void clearPaintData() {
        this.paintData = new int[SkinTexture.TEXTURE_SIZE];
        for (int i = 0; i < SkinTexture.TEXTURE_SIZE; i++) {
            this.paintData[i] = 0x00FFFFFF;
        }
    }
    
    public void toolUsedOnArmourer(IBlockPainter tool, World world, ItemStack stack, EntityPlayer player) {
        UndoManager.begin(player);
        applyToolToBlocks(tool, world, stack, player);
        UndoManager.end(player);
    }
    
    private void applyToolToBlocks(IBlockPainter tool, World world, ItemStack stack, EntityPlayer player) {
        if (skinType != null) {
            ArrayList<BlockLocation> paintableCubes = ArmourerWorldHelper.getListOfPaintableCubes(worldObj, xCoord, yCoord + getHeightOffset(), zCoord, skinType);
            for (int i = 0; i < paintableCubes.size(); i++) {
                BlockLocation bl = paintableCubes.get(i);
                IPantableBlock pBlock = (IPantableBlock) worldObj.getBlock(bl.x, bl.y, bl.z);
                Block block = world.getBlock(bl.x, bl.y, bl.z);
                if (block instanceof IPantableBlock) {
                    for (int side = 0; side < 6; side++) {
                        tool.usedOnBlockSide(stack, player, world, bl, block, side);
                    }
                }
            }
        }
    }
    
    public void onPlaced() {
        createBoundingBoxes();
    }
    
    public void preRemove() {
        removeBoundingBoxes();
    }
    
    public int getHeightOffset() {
        return HEIGHT_OFFSET;
    }

    public void clearArmourCubes() {
        if (skinType != null) {
            ArmourerWorldHelper.clearEquipmentCubes(worldObj, xCoord, yCoord + getHeightOffset(), zCoord, skinType);
        }
    }
    
    protected void removeBoundingBoxes() {
        if (skinType != null) {
            ArmourerWorldHelper.removeBoundingBoxes(worldObj, xCoord, yCoord + getHeightOffset(), zCoord, skinType);
        }
    }
    
    protected void createBoundingBoxes() {
        if (skinType != null) {
            ArmourerWorldHelper.createBoundingBoxes(worldObj, xCoord, yCoord + getHeightOffset(), zCoord, xCoord, yCoord, zCoord, skinType);
        }
    }
    
    public void setDirection(ForgeDirection direction) {
        this.direction = direction;
        this.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    public ForgeDirection getDirection() {
        return direction;
    }
    
    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        AxisAlignedBB bb = super.getRenderBoundingBox();
        
        bb = AxisAlignedBB.getBoundingBox(xCoord - 10, yCoord - 10, zCoord - 18,
                xCoord + 20, yCoord + 40 + 20, zCoord + 20);
        
        return bb;
    }

    public ISkinType getSkinType() {
        return skinType;
    }
    
    public boolean isShowGuides() {
        return showGuides;
    }
    
    public boolean isShowOverlay() {
        return showOverlay;
    }
    
    public boolean isShowHelper() {
        return showHelper;
    }
    
    public GameProfile getGameProfile() {
        return gameProfile;
    }
    
    public void setSkinType(ISkinType skinType) {
        if (this.skinType == skinType) {
            return;
        }
        removeBoundingBoxes();
        this.skinType = skinType;
        createBoundingBoxes(); 
        this.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    public void setGameProfile(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
        updateProfileData();
        this.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    public void toggleGuides() {
        this.showGuides = !this.showGuides;
        this.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    public void toggleOverlay() {
        this.showOverlay = !this.showOverlay;
        this.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    public void toggleHelper() {
        this.showHelper = !this.showHelper;
        this.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    public String getCustomName() {
        return customName;
    }
    
    public void setCustomName(String customName) {
        this.customName = customName;
        this.markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    private void updateProfileData() {
        GameProfileUtils.updateProfileData(gameProfile, this);
    }
    
    @Override
    public String getInventoryName() {
        return LibBlockNames.ARMOURER_BRAIN;
    }
    
    @Override
    public double getMaxRenderDistanceSquared() {
        return super.getMaxRenderDistanceSquared() * 10;
    }
    
    public Packet getDescriptionPacket() {
        NBTTagCompound compound = new NBTTagCompound();
        writeBaseToNBT(compound);
        writeCommonToNBT(compound);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 5, compound);
    }
    
    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet) {
        NBTTagCompound compound = packet.func_148857_g();
        readBaseFromNBT(compound);
        readCommonFromNBT(compound);
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        readCommonFromNBT(compound);
    }
    
    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        writeCommonToNBT(compound);
    }
    
    @Override
    public void readCommonFromNBT(NBTTagCompound compound) {
        super.readCommonFromNBT(compound);
        direction = ForgeDirection.getOrientation(compound.getByte(TAG_DIRECTION));
        skinType = SkinTypeRegistry.INSTANCE.getSkinTypeFromRegistryName(compound.getString(TAG_TYPE));
        //Update code for old saves
        if (skinType == null && compound.hasKey(TAG_TYPE_OLD)) {
            skinType = SkinTypeRegistry.INSTANCE.getSkinTypeFromLegacyId(compound.getInteger(TAG_TYPE_OLD) - 1);
        }
        showGuides = compound.getBoolean(TAG_SHOW_GUIDES);
        showOverlay = compound.getBoolean(TAG_SHOW_OVERLAY);
        if (compound.hasKey(TAG_SHOW_HELPER)) {
            showHelper = compound.getBoolean(TAG_SHOW_HELPER);
        }
        customName = compound.getString(TAG_CUSTOM_NAME);
        if (compound.hasKey(TAG_OWNER, 10)) {
            this.gameProfile = NBTUtil.func_152459_a(compound.getCompoundTag(TAG_OWNER));
        }
        if (compound.hasKey(TAG_PAINT_DATA)) {
            paintData = compound.getIntArray(TAG_PAINT_DATA);
        }
    }
    
    @Override
    public void writeCommonToNBT(NBTTagCompound compound) {
        super.writeCommonToNBT(compound);
        compound.setByte(TAG_DIRECTION, (byte) direction.ordinal());
        if (skinType != null) {
            compound.setString(TAG_TYPE, skinType.getRegistryName());
        }
        compound.setBoolean(TAG_SHOW_GUIDES, showGuides);
        compound.setBoolean(TAG_SHOW_OVERLAY, showOverlay);
        compound.setBoolean(TAG_SHOW_HELPER, showHelper);
        compound.setString(TAG_CUSTOM_NAME, customName);
        if (this.newProfile != null) {
            this.gameProfile = newProfile;
            this.newProfile = null;
        }
        if (this.gameProfile != null) {
            NBTTagCompound profileTag = new NBTTagCompound();
            NBTUtil.func_152460_a(profileTag, this.gameProfile);
            compound.setTag(TAG_OWNER, profileTag);
        }
        compound.setIntArray(TAG_PAINT_DATA, this.paintData);
    }

    @Override
    public void profileUpdated(GameProfile gameProfile) {
        newProfile = gameProfile;
        markDirty();
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
}
