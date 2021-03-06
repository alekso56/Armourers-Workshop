package riskyken.armourersWorkshop.client.model.bake;

import java.util.ArrayList;
import java.util.BitSet;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import riskyken.plushieWrapper.client.IRenderBuffer;
import riskyken.plushieWrapper.client.RenderBridge;

public class CustomModelRenderer extends ModelRenderer {

    public CustomModelRenderer(ModelBase p_i1174_1_, int p_i1174_2_, int p_i1174_3_) {
        super(p_i1174_1_, p_i1174_2_, p_i1174_3_);
    }
    
    public void render(float scale, BitSet faceFlags, int x, int y, int z, byte[] r, byte[] g, byte[] b, byte a) {
        IRenderBuffer renderBuffer = RenderBridge.INSTANCE;
        int size = this.cubeList.size();
        renderBuffer.startDrawingQuads();
        for (int i = 0; i < size; ++i) {
            ((ModelEquipmentBox)this.cubeList.get(i)).render(renderBuffer, scale, faceFlags, x, y, z , r, g , b, a);
        }
        renderBuffer.draw();
    }
    
    public void buildDisplayListArray(ArrayList<ColouredVertexWithUV> vertexList, float scale, BitSet faceFlags, int x, int y, int z, byte[] r, byte[] g, byte[] b, byte a, byte[] paintType) {
        int size = this.cubeList.size();
        for (int i = 0; i < size; ++i) {
            ((ModelEquipmentBox)this.cubeList.get(i)).buildDisplayListArray(vertexList, scale, faceFlags, x, y, z , r, g , b, a, paintType);
        }
    }
    
    @Override
    public ModelRenderer addBox(float x, float y, float z, int xSize, int ySize, int zSize) {
        this.cubeList.add(new ModelEquipmentBox(this, 0, 0, x, y, z, xSize, ySize, zSize, 0.0F));
        return this;
    }
}
