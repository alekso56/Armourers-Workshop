package riskyken.armourersWorkshop.common.painting.tool;

import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;

public class ToolOptionIntensity extends AbstractToolOption {

    private static final String TAG_INTENSITY = "intensity";
    
    public ToolOptionIntensity() {
        super(TAG_INTENSITY);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getDisplayHeight() {
        return 20;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public GuiButton getGuiControl(int id, int x, int y, NBTTagCompound compound) {
        GuiSlider sliderControl = new GuiSlider(id, x, y, getLocalisedLabel() + " ", 1, 64, (Integer) readFromNBT(compound), null);
        sliderControl.showDecimal = false;
        return sliderControl;
    }

    @Override
    public Object readFromNBT(NBTTagCompound compound) {
        int intensityValue = 16;
        if (compound != null && compound.hasKey(TAG_INTENSITY)) {
            intensityValue = compound.getInteger(TAG_INTENSITY);
        }
        return intensityValue;
    }

    @Override
    public void writeToNBT(NBTTagCompound compound, GuiButton control) {
        GuiSlider sliderControl = (GuiSlider) control;
        writeToNBT(compound, sliderControl.getValueInt());
    }

    @Override
    public void writeToNBT(NBTTagCompound compound, Object value) {
        compound.setInteger(TAG_INTENSITY, (Integer) value);
    }
}
