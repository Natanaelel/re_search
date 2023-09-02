package natte.re_search.search;

import java.util.ArrayList;
import java.util.List;

import natte.re_search.config.Config;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
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
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class Searcher {


    public static List<MarkedInventory> search(SearchOptions searchOptions, ServerPlayerEntity playerEntity) {


        List<MarkedInventory> inventories = new ArrayList<>();

        PlayerEntity player = playerEntity;
        World world = player.getWorld();

        Filter filter = new Filter(searchOptions, playerEntity);
     
        for (BlockPos blockPos : BlockPos.iterateOutwards(player.getBlockPos(), Config.range, Config.range,
                Config.range)) {

            MarkedInventory markedInventory = new MarkedInventory(blockPos.toImmutable());
            search(blockPos, world, filter, markedInventory, Config.recursionLimit);

            if (!markedInventory.isEmpty()) {
                inventories.add(markedInventory);
            }

        }

        List<Entity> entities = world.getOtherEntities(player,
                new Box(player.getBlockPos().add(Config.range, Config.range, Config.range),
                        player.getBlockPos().add(-Config.range, -Config.range, -Config.range)),
                entity -> true);

        for (Entity entity : entities) {

            MarkedInventory markedInventory = new MarkedInventory(entity.getBlockPos());
            search(entity, filter, markedInventory, Config.recursionLimit);

            if (!markedInventory.isEmpty()) {
                inventories.add(markedInventory);
            }
        }

        return inventories;
    }

    private static boolean search(ItemStack itemStack, Filter predicate, MarkedInventory markedInventory,
            int recursionDepth) {

        if (recursionDepth == 0)
            return false;

        boolean foundAny = false;
        boolean itemStackMatches = predicate.test(itemStack);
        if (itemStackMatches) {
            markedInventory.inventory.add(itemStack);
        }

        // shulkerbox
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            if (blockItem.getBlock() instanceof ShulkerBoxBlock) {
                if (itemStack.hasNbt()) {
                    NbtCompound nbt = itemStack.getNbt();
                    if (nbt.contains("BlockEntityTag", NbtElement.COMPOUND_TYPE)) {
                        NbtCompound nbtInv = nbt.getCompound("BlockEntityTag");
                        DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(27, ItemStack.EMPTY);
                        Inventories.readNbt(nbtInv, defaultedList);
                        for (ItemStack item : defaultedList) {
                            if (search(item, predicate, markedInventory, recursionDepth - 1))
                                foundAny = true;
                        }
                    }
                }
            }
        }

        // bundle
        else if (itemStack.isOf(Items.BUNDLE)) {
            if (itemStack.hasNbt()) {
                NbtCompound nbt = itemStack.getNbt();
                DefaultedList<ItemStack> defaultedList = DefaultedList.ofSize(64, ItemStack.EMPTY);
                Inventories.readNbt(nbt, defaultedList);
                for (ItemStack item : defaultedList) {
                    if (search(item, predicate, markedInventory, recursionDepth - 1))
                        foundAny = true;
                }
            }
        }

        if (foundAny)
            markedInventory.addContainer(itemStack);
        if (itemStackMatches)
            foundAny = true;

        return foundAny;
    }

    private static boolean search(BlockPos blockPos, World world, Filter predicate,
            MarkedInventory markedInventory, int recursionDepth) {
        if (recursionDepth == 0)
            return false;

        boolean foundAny = false;

        BlockState blockState = world.getBlockState(blockPos);

        if (blockState.hasBlockEntity()) {
            BlockEntity tileEntity = world.getBlockEntity(blockPos);

            // blockentity with inventory
            if (tileEntity instanceof Inventory inventory) {
                for (int i = 0; i < inventory.size(); ++i) {
                    ItemStack itemStack = inventory.getStack(i);
                    if (search(itemStack, predicate, markedInventory, -1))
                        foundAny = true;
                }
            }

            // lectern
            else if (tileEntity instanceof LecternBlockEntity lectern) {
                if (search(lectern.getBook(), predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }


        }

        // campfire
        if (blockState.isOf(Blocks.CAMPFIRE) || blockState.isOf(Blocks.SOUL_CAMPFIRE)) {
            CampfireBlockEntity blockEntity = (CampfireBlockEntity) world.getBlockEntity(blockPos);

            for (ItemStack itemStack : blockEntity.getItemsBeingCooked()) {
                if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
        }




        if (foundAny)
            markedInventory.addContainer(blockState.getBlock().asItem().getDefaultStack());

        return foundAny;
    }

    private static boolean search(Entity entity, Filter predicate, MarkedInventory markedInventory,
            int recursionDepth) {
        if (recursionDepth == 0)
            return false;
        boolean foundAny = false;

        // item frame
        if (entity instanceof ItemFrameEntity itemFrame) {
            if (search(itemFrame.getHeldItemStack(), predicate, markedInventory, recursionDepth - 1)) {
                foundAny = true;
                markedInventory.addContainer(Items.ITEM_FRAME.getDefaultStack());
            }
        }

        // armor stand
        else if (entity instanceof ArmorStandEntity armorStand) {
            for (ItemStack itemStack : armorStand.getHandItems()) {
                if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
            for (ItemStack itemStack : armorStand.getArmorItems()) {
                if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
            if (foundAny) {
                markedInventory.addContainer(Items.ARMOR_STAND.getDefaultStack());
            }
        }
        else if (entity instanceof VehicleInventory vehicleInventory){
            for (ItemStack itemStack : vehicleInventory.getInventory()) {
                if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
            if(foundAny)
                markedInventory.addContainer(entity.getPickBlockStack());
        }
        return foundAny;

    }
}
