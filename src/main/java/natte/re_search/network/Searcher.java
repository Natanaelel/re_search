package natte.re_search.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import natte.re_search.MarkedInventory;
import natte.re_search.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class Searcher {
    
    // private static final int blockLimit = 1000;

    public static List<MarkedInventory> search(String expression, ServerPlayerEntity playerEntity){

        List<MarkedInventory> inventories = new ArrayList<>();

        PlayerEntity player = playerEntity;
        World world = player.getWorld();

        // Search: 
        // blocks
        // inventories

        Pattern pattern = Pattern.compile(expression);

        // Language language = Language.getInstance();
        for(BlockPos blockPos : BlockPos.iterateOutwards(player.getBlockPos(), Config.range, Config.range, Config.range)){

            BlockState blockstate = world.getBlockState(blockPos);

            Predicate<ItemStack> predicate = itemStack -> {
                String name = itemStack.getItem().getName().getString();
                return !name.equals("Air") && pattern.matcher(name).find();
            };

            MarkedInventory markedInventory = new MarkedInventory(blockPos.toImmutable(), blockstate.getBlock().asItem().getDefaultStack());
            if(blockstate.hasBlockEntity()){
                BlockEntity tileEntity = world.getBlockEntity(blockPos);
                if(tileEntity instanceof Inventory inventory){
                    for(int i = 0;  i < inventory.size(); ++i){
                        ItemStack itemStack = inventory.getStack(i);
                        if(predicate.test(itemStack)){
                            markedInventory.addItem(itemStack);
                        }
                    }
                }
            }
            if(!markedInventory.isEmpty()){
                inventories.add(markedInventory);
            }

        }

        return inventories;
    }
}

