package natte.re_search.render;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class MarkedInventory {
    
    public BlockPos blockPos;
    public List<ItemStack> inventory;

    public MarkedInventory(BlockPos blockPos){
        this.blockPos = blockPos;
        this.inventory = new ArrayList<>();
    }
    public void addItem(ItemStack itemStack){
        this.inventory.add(itemStack);
    }
    public boolean isEmpty(){
        return inventory.isEmpty();
    }
}
