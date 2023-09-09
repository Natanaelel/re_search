package natte.re_search.render;

import org.joml.Vector4f;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class RenderedItem {
    public Vec3d position;
    public Vector4f screenPosition;

    public float x;
    public float y;

    public ItemStack itemStack;

    public boolean isArrow = false;
    public RenderedItem(){

    }
}
