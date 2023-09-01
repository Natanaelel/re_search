package natte.re_search.search;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector4f;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class MarkedInventory {
    
    public BlockPos blockPos;
    public List<ItemStack> inventory;
    public Vector4f screenPos;
    public List<ItemStack> containers;
    public MarkedInventory(BlockPos blockPos){
        this.blockPos = blockPos;
        this.inventory = new ArrayList<>();
        this.containers = new ArrayList<>();
    }
    
    public void addItem(ItemStack itemStack){
        this.inventory.add(itemStack);
    }

    public void addContainer(ItemStack itemStack){
        this.containers.add(itemStack);
    }

    public boolean isEmpty(){
        return inventory.isEmpty();
    }
}
