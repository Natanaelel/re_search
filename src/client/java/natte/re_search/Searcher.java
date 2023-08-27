package natte.re_search;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import natte.re_search.render.MarkedInventory;
import natte.re_search.render.WorldRendering;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Language;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

@Environment(EnvType.CLIENT)
public class Searcher {
    
    private static Thread thread;
    private static final AtomicBoolean shouldRun = new AtomicBoolean(false);

    private static final int blockLimit = 1000;

    public static void search(String expression, MinecraftClient client){
        shouldRun.set(false);
        if(thread != null)
            while(thread.isAlive());
        shouldRun.set(true);
        thread = new Thread(()->{
            PlayerEntity player = client.player;
            ClientWorld world = client.world;


            Logger LOGGER = LoggerFactory.getLogger("re_search");
            WorldRendering.clearMarkedInventories();
            // Search: 
            // blocks
            // inventories

            // TODO: configurable
            int range = 20;
            // Pattern pattern = Pattern.compile(expression);

            // Language language = Language.getInstance();
            for(BlockPos blockPos : BlockPos.iterateOutwards(player.getBlockPos(), range, range, range)){
                if(!shouldRun.get()) break;

                BlockState blockstate = world.getBlockState(blockPos);
                // null check?
                MarkedInventory markedInventory = new MarkedInventory(blockPos.toImmutable());
                // System.out.println("searching at " + blockPos.toString());
                // String blockName = language.get(blockstate.getBlock().getTranslationKey());
                if(blockstate.hasBlockEntity()){
                    LOGGER.info("found tileentity");
                    BlockEntity tileEntity = world.getBlockEntity(blockPos);
                    if(tileEntity instanceof Inventory inventory){
                        for(int i = 0;  i < inventory.size(); ++i){
                            ItemStack itemStack = inventory.getStack(i);
                            itemStack = new ItemStack(Items.DIAMOND, 3);
                            LOGGER.info(itemStack.getItem().getName().getString());
                            if(itemStack.getItem().getName().getString().contains(expression)){
                                LOGGER.info("adding");
                                markedInventory.addItem(itemStack);
                            }
                        }
                    }
                }
                if(!markedInventory.isEmpty()){
                    WorldRendering.addMarkedInventory(markedInventory);
                }
                
                // if(blockstate.getBlock().getName().toString().contains(expression) && !blockstate.isAir()){
                    // player.sendMessage(Text.of(blockName + "??" + expression));
                    // player.sendMessage(Text.of(pattern.pattern()));

                // if((!blockstate.isAir()) && blockName.contains(expression)){
                //   /*
                //     // player.sendMessage( Text.of(pattern.matcher(blockName).matches() ? "yes" : "no"));
                //     // world.setBlockState(blockPos, Blocks.STONE.getDefaultState());
                //     // ParticleEffect particleEffect = new DustParticleEffect(new Vector3f(1, 1, 1), 1);
                //     // particleEffect = ModParticles.CUSTOM_PARTICLE;
                    
                //     // LOGGER.info("new particle at " + blockPos.toString());
                //     // world.addImportantParticle(particleEffect, false, range, range, range, range, range, range);
                //     // int n = 2;
                //     // for(int i = 0; i < n; i++){
                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX(), blockPos.getY() + (double)i / n, blockPos.getZ(), 0d, 0d, 0d);
                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX() + 1, blockPos.getY() + (double)i / n, blockPos.getZ(), 0d, 0d, 0d);
                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX(), blockPos.getY() + (double)i / n, blockPos.getZ() + 1, 0d, 0d, 0d);
                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX() + 1, blockPos.getY() + (double)i / n, blockPos.getZ() + 1, 0d, 0d, 0d);

                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX() + (double)i / n, blockPos.getY(), blockPos.getZ(), 0d, 0d, 0d);
                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX() + (double)i / n, blockPos.getY() + 1, blockPos.getZ(), 0d, 0d, 0d);
                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX() + (double)i / n, blockPos.getY(), blockPos.getZ() + 1, 0d, 0d, 0d);
                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX() + (double)i / n, blockPos.getY() + 1, blockPos.getZ() + 1, 0d, 0d, 0d);


                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX(), blockPos.getY(), blockPos.getZ() + (double)i / n, 0d, 0d, 0d);
                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX() + 1, blockPos.getY(), blockPos.getZ() + (double)i / n, 0d, 0d, 0d);
                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX(), blockPos.getY() + 1, blockPos.getZ() + (double)i / n, 0d, 0d, 0d);
                //     //     world.addImportantParticle(particleEffect, true, blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + (double)i / n, 0d, 0d, 0d);



                //     // }
                //     */
                    
                //     WorldRendering.addblockPosition(new Vec3i(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                //     if(WorldRendering.getBlockPositionsSize() >= blockLimit) break;
                //     // world.addParticle(particleEffect, true, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 0d, 0d, 0d);
                // };

            };
        });
        thread.start();
    }
}

