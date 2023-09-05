package natte.re_search.network;

import java.util.ArrayList;
import java.util.List;

import natte.re_search.RegexSearch;
import natte.re_search.search.MarkedInventory;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

public class ItemSearchResultPacketS2C {

    public static final Identifier PACKET_ID = new Identifier(RegexSearch.MOD_ID, "item_search_result");

    public static PacketByteBuf createPackedByteBuf(List<MarkedInventory> inventories) {
        PacketByteBuf packet = PacketByteBufs.create();
        
        packet.writeInt(inventories.size());

        for (MarkedInventory inventory : inventories) {
            
            packet.writeBlockPos(inventory.blockPos);

            packet.writeInt(inventory.containers.size());            
            for (ItemStack itemStack : inventory.containers) {
                packet.writeItemStack(itemStack);
            }

            packet.writeInt(inventory.inventory.size());
            for (ItemStack itemStack : inventory.inventory) {
                packet.writeItemStack(itemStack);
            }
        }

        return packet;
    }

    public static List<MarkedInventory> readPackedByteBuf(PacketByteBuf packet){
        List<MarkedInventory> inventories = new ArrayList<>();
        int inventoriesSize = packet.readInt();
        
        for(int i = 0; i < inventoriesSize; ++i){
            
            BlockPos blockPos = packet.readBlockPos();
            MarkedInventory inventory = new MarkedInventory(blockPos);      


            int containersSize = packet.readInt();

            for(int j = 0; j < containersSize; ++j){
                ItemStack itemStack = packet.readItemStack();
                inventory.addContainer(itemStack);
            }

            int inventorySize = packet.readInt();

            for(int j = 0; j < inventorySize; ++j){
                ItemStack itemStack = packet.readItemStack();
                inventory.addItem(itemStack);
            }

            inventories.add(inventory);

        }

        return inventories;
    }
}
