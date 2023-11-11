package natte.re_search.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import natte.re_search.config.Config;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.VehicleInventory;
import net.minecraft.inventory.Inventories;
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

    private static int totalItems;

    public static List<MarkedInventory> search(SearchOptions searchOptions, ServerPlayerEntity playerEntity) {

        List<MarkedInventory> inventories = new ArrayList<>();
        totalItems = 0;

        PlayerEntity player = playerEntity;
        World world = player.getWorld();

        Filter filter = new Filter(searchOptions, playerEntity);
        int range = Config.range;
        if (Config.searchBlocks) {
            for (BlockPos blockPos : BlockPos.iterateOutwards(player.getBlockPos(), range, range, range)) {
                if (inventories.size() == Config.maxInventories)
                    break;
                MarkedInventory markedInventory = new MarkedInventory(blockPos.toImmutable());
                search(blockPos, world, filter, markedInventory, Config.recursionLimit);

                if (!markedInventory.isEmpty()) {
                    inventories.add(markedInventory);
                }

            }
        }

        if (Config.searchEntities) {
            List<Entity> entities = world.getOtherEntities(player,
                    Box.of(player.getPos(), range * 2, range * 2, range * 2));

            entities.sort(Comparator.comparing(entity -> entity.squaredDistanceTo(player)));

            for (Entity entity : entities) {
                if (inventories.size() == Config.maxInventories)
                    break;

                MarkedInventory markedInventory = new MarkedInventory(entity.getBlockPos());
                search(entity, filter, markedInventory, Config.recursionLimit);

                if (!markedInventory.isEmpty()) {
                    inventories.add(markedInventory);
                }
            }
        }

        return inventories;
    }

    private static boolean search(ItemStack itemStack, Filter predicate, MarkedInventory markedInventory,
            int recursionDepth) {
        if (markedInventory.inventory.size() == Config.maxSearchResultsPerInventory)
            return false;
        if (totalItems == Config.maxSearchResults)
            return false;

        if (recursionDepth == 0)
            return false;

        boolean foundAny = false;
        boolean itemStackMatches = predicate.test(itemStack);
        if (itemStackMatches) {
            markedInventory.inventory.add(itemStack);
            ++totalItems;
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

        Storage<ItemVariant> storage = ItemStorage.SIDED.find(world, blockPos, null);

        if (storage != null) {
            for (StorageView<ItemVariant> view : storage) {
                if (view.getAmount() == 0)
                    continue;
                if (view.isResourceBlank())
                    continue;
                if (search(view.getResource().toStack(), predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
        } else if (blockState.hasBlockEntity()) {
            BlockEntity tileEntity = world.getBlockEntity(blockPos);

            // lectern
            if (tileEntity instanceof LecternBlockEntity lectern) {
                if (search(lectern.getBook(), predicate, markedInventory, recursionDepth -
                        1))
                    foundAny = true;
            }
        }

        // // campfire
        if (blockState.isOf(Blocks.CAMPFIRE) ||
                blockState.isOf(Blocks.SOUL_CAMPFIRE)) {
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

        // item entity
        if (entity instanceof ItemEntity itemEntity) {

            if (search(itemEntity.getStack(), predicate, markedInventory, recursionDepth - 1)) {
                foundAny = true;
            }
        }

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
        } else if (entity instanceof VehicleInventory vehicleInventory) {
            for (ItemStack itemStack : vehicleInventory.getInventory()) {
                if (search(itemStack, predicate, markedInventory, recursionDepth - 1))
                    foundAny = true;
            }
            if (foundAny)
                markedInventory.addContainer(entity.getPickBlockStack());
        }
        return foundAny;

    }
}
