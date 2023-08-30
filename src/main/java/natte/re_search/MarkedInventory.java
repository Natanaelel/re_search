package natte.re_search;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class MarkedInventory {
    
    public BlockPos blockPos;
    public List<ItemStack> inventory;
    public Vector4f screenPos;
    public ItemStack container;
    public MarkedInventory(BlockPos blockPos, ItemStack container){
        this.blockPos = blockPos;
        this.inventory = new ArrayList<>();
        this.container = container;
    }
    public void addItem(ItemStack itemStack){
        this.inventory.add(itemStack);
    }
    public boolean isEmpty(){
        return inventory.isEmpty();
    }
}
