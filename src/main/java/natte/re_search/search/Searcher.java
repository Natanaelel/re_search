package natte.re_search.search;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;


import natte.re_search.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class Searcher {
    
    // private static final int blockLimit = 1000;

    public static List<MarkedInventory> search(SearchOptions searchOptions, ServerPlayerEntity playerEntity){

        String expression = searchOptions.expression;
        
        List<MarkedInventory> inventories = new ArrayList<>();

        PlayerEntity player = playerEntity;
        World world = player.getWorld();

        // Search: 
        // blocks
        // inventories

        Pattern pattern = Pattern.compile(expression, searchOptions.isCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE);

        // Language language = Language.getInstance();
        for(BlockPos blockPos : BlockPos.iterateOutwards(player.getBlockPos(), Config.range, Config.range, Config.range)){

            BlockState blockstate = world.getBlockState(blockPos);

            Predicate<ItemStack> predicate = itemStack -> {
                String name = itemStack.getItem().getName().getString();
                return !name.equals("Air") && pattern.matcher(name).find();
            };

            MarkedInventory markedInventory = new MarkedInventory(blockPos.toImmutable());
            if(blockstate.hasBlockEntity()){
                BlockEntity tileEntity = world.getBlockEntity(blockPos);
                if(tileEntity instanceof Inventory inventory){
                    boolean foundAny = false;
                    for(int i = 0;  i < inventory.size(); ++i){
                        ItemStack itemStack = inventory.getStack(i);
                        if(search(itemStack, predicate, markedInventory, -1)) foundAny = true;
                    }
                    if(foundAny) markedInventory.addContainer(blockstate.getBlock().asItem().getDefaultStack());

                }
            }
            if(!markedInventory.isEmpty()){
                inventories.add(markedInventory);
            }

        }

        return inventories;
    }

    private static boolean search(ItemStack itemStack, Predicate<ItemStack> predicate, MarkedInventory inventory, int recursionDepth){
        boolean foundAny = false;
        boolean itemStackMatches = predicate.test(itemStack);
        if(itemStackMatches) {
            inventory.inventory.add(itemStack);
        }

        if(recursionDepth == 0) return false;
        

        // shulkerbox
        if(itemStack.getItem() instanceof BlockItem blockItem){
            if(blockItem.getBlock() instanceof ShulkerBoxBlock){
                if(itemStack.hasNbt()) {
                    NbtCompound nbt = itemStack.getNbt();
                    if(nbt.contains("BlockEntityTag", NbtElement.COMPOUND_TYPE)){
                        NbtCompound nbtInv = nbt.getCompound("BlockEntityTag");
                        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
                        Inventories.readNbt(nbtInv, defaultedList);
                        for(ItemStack item : defaultedList){
                            if(search(item, predicate, inventory, recursionDepth - 1)) foundAny = true;
                        }
                    }
                }
            }
        }
        
        // bundle
        if(itemStack.isOf(Items.BUNDLE)){
            if(itemStack.hasNbt()){
                NbtCompound nbt = itemStack.getNbt();
                DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(64, ItemStack.EMPTY);
                Inventories.readNbt(nbt, defaultedList);
                for(ItemStack item : defaultedList){
                    if(search(item, predicate, inventory, recursionDepth - 1)) foundAny = true;
                }
            }
        }


        if(foundAny) inventory.addContainer(itemStack);
        if(itemStackMatches) foundAny = true;
        
        return foundAny;
    }
}

