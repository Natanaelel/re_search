package natte.re_search.render;

import java.util.ArrayList;
import java.util.List;

import natte.re_search.RegexSearch;
import natte.re_search.search.MarkedInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class WorldRendering {

    public static List<MarkedInventory> inventories = new ArrayList<>();
    

    

    public static void register() {
        // AFTER_TRANSLUCENT END
        
        MinecraftClient client = MinecraftClient.getInstance();


        // BEFORE_ENTITIES
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register(context -> {
            rotateBasedOnPosition(context, client);
        });
    }

    private static void rotateBasedOnPosition(WorldRenderContext context, MinecraftClient client) {
        if(getMarkedInventoriesSize() == 0) return;

        Camera camera = context.camera();
        ItemRenderer itemRenderer = client.getItemRenderer();
        VertexConsumerProvider vertexConsumers = context.consumers();

        for(MarkedInventory inventory : getMarkedInventories()){
            
            BlockPos blockPos = inventory.blockPos;
            
            MatrixStack matrixStack = new MatrixStack();

            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            
            Vec3d relativeBlockPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).subtract(camera.getPos());

            int size = inventory.inventory.size();
            int sideLength = (int)Math.ceil(Math.sqrt(size));

            matrixStack.push();
            {
                Vec3d transformedPosition = relativeBlockPosition.add(0.5, 0.5, 0.5);
            
                // move item to block pos
                matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);

                // rotate to face player
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float)MathHelper.atan2(-transformedPosition.x, -transformedPosition.z)));
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y, Math.hypot(transformedPosition.x, transformedPosition.z))));

                float distance = (float)transformedPosition.length();

                // move item to 0.25 blocks from camera
                matrixStack.translate(0, 0, distance-0.25f);
                
                float scale = 0.15f / distance;

                // float smoothing = 100f;
                // smoothing = client.player.headYaw;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(0.02f * smoothing)) / smoothing;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(((scale - 0.02f) * 0.5f + 0.02f) * smoothing)) / smoothing;
                // if(scale > 0.1)
                // largest 0.13
                // medium 0.02
                // no smaller than 0.01 ever please

                matrixStack.scale(scale, scale, scale);
            


                int i = -1;
                for(ItemStack itemStack : inventory.inventory){
                    i += 1;
                    int x = i % sideLength;
                    int y = i / sideLength;
            
                    
                    matrixStack.push();

                    matrixStack.translate(x * 1.125f - (y == (float)size / sideLength ? size % sideLength : sideLength) / 2f + 0.5f - 0.125f*sideLength/2+0.0625, y*1.125f + 1.6f, 0f);
                    
                    itemRenderer.renderItem(itemStack, ModelTransformationMode.GUI, 0xff, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumers, client.world, 0);
                
                    matrixStack.pop();
                }
            }
            matrixStack.pop();

            matrixStack.push();
            {
                Vec3d transformedPosition = relativeBlockPosition.add(0.5, 0.5, 0.5);

                // move item to block pos
                matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);

                // rotate to face player
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float)MathHelper.atan2(-transformedPosition.x, -transformedPosition.z)));
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y, Math.hypot(transformedPosition.x, transformedPosition.z))));

                float distance = (float)transformedPosition.length();
                
                // move item to 0.25 blocks from camera
                matrixStack.translate(0, 0, distance-0.25f);
                
                float scale = 0.15f / distance;
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                matrixStack.scale(scale, scale, scale);

                matrixStack.translate(0f, 0.8f, 0f);

                itemRenderer.renderItem(new ItemStack(RegexSearch.ARROW_ITEM), ModelTransformationMode.GUI, 255, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumers, client.world, 0);
            }
            matrixStack.pop();
            
            matrixStack.push();
            {
                Vec3d transformedPosition = relativeBlockPosition.add(0.5, 0.5, 0.5);
                
                
                // move item to block pos
                matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
                // rotate to face player
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float)MathHelper.atan2(-transformedPosition.x, -transformedPosition.z)));
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y, Math.hypot(transformedPosition.x, transformedPosition.z))));

                float distance = (float)transformedPosition.length();
                
                // move item to 0.25 blocks from camera
                matrixStack.translate(0, 0, distance-0.25f);
                
                float scale = 0.15f / distance;
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                matrixStack.scale(scale, scale, scale);
                
                int i = -1;
                for(ItemStack itemStack : inventory.containers){
                    i += 1;
                    int x = i;

                    matrixStack.push();

                    matrixStack.translate(x * 1.125f - inventory.containers.size() / 2f + 0.5f- 0.125f*inventory.containers.size()/2+0.0625, 0f, 0f);
                    itemRenderer.renderItem(itemStack, ModelTransformationMode.GUI, 0xff, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumers, client.world, 0);
                
                    matrixStack.pop();
                }
            }
            matrixStack.pop();
        }
    }
    
    /*
    private static void drawCubes(BufferBuilder buffer, MatrixStack matrixStack) {
        for (Vec3i pos : blockPositions) {
            // matrixStack.push();
            matrixStack.push();
            Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();

            Vector3f fpos = new Vector3f((float) pos.getX(), (float) pos.getY(), (float) pos.getZ());

            positionMatrix.translate(fpos);


            float v0 = 0.0f;
            float v1 = 1.0f;
            buffer.vertex(positionMatrix, v0, v0, v0).color(0xffffffff).texture(1f, 1f).next();
            buffer.vertex(positionMatrix, v0, v1, v0).color(0xffffffff).texture(1f, 0f).next();
            buffer.vertex(positionMatrix, v1, v1, v0).color(0xffffffff).texture(0f, 0f).next();
            buffer.vertex(positionMatrix, v1, v0, v0).color(0xffffffff).texture(0f, 1f).next();

            buffer.vertex(positionMatrix, v0, v1, v1).color(0xffffffff).texture(0f, 0f).next();
            buffer.vertex(positionMatrix, v0, v0, v1).color(0xffffffff).texture(0f, 1f).next();
            buffer.vertex(positionMatrix, v1, v0, v1).color(0xffffffff).texture(1f, 1f).next();
            buffer.vertex(positionMatrix, v1, v1, v1).color(0xffffffff).texture(1f, 0f).next();

            buffer.vertex(positionMatrix, v0, v0, v0).color(0xffffffff).texture(0f, 0f).next();
            buffer.vertex(positionMatrix, v1, v0, v0).color(0xffffffff).texture(0f, 1f).next();
            buffer.vertex(positionMatrix, v1, v0, v1).color(0xffffffff).texture(1f, 1f).next();
            buffer.vertex(positionMatrix, v0, v0, v1).color(0xffffffff).texture(1f, 0f).next();

            buffer.vertex(positionMatrix, v1, v1, v0).color(0xffffffff).texture(0f, 0f).next();
            buffer.vertex(positionMatrix, v0, v1, v0).color(0xffffffff).texture(0f, 1f).next();
            buffer.vertex(positionMatrix, v0, v1, v1).color(0xffffffff).texture(1f, 1f).next();
            buffer.vertex(positionMatrix, v1, v1, v1).color(0xffffffff).texture(1f, 0f).next();

            buffer.vertex(positionMatrix, v0, v0, v0).color(0xffffffff).texture(0f, 0f).next();
            buffer.vertex(positionMatrix, v0, v0, v1).color(0xffffffff).texture(0f, 1f).next();
            buffer.vertex(positionMatrix, v0, v1, v1).color(0xffffffff).texture(1f, 1f).next();
            buffer.vertex(positionMatrix, v0, v1, v0).color(0xffffffff).texture(1f, 0f).next();

            buffer.vertex(positionMatrix, v1, v0, v1).color(0xffffffff).texture(0f, 0f).next();
            buffer.vertex(positionMatrix, v1, v0, v0).color(0xffffffff).texture(0f, 1f).next();
            buffer.vertex(positionMatrix, v1, v1, v0).color(0xffffffff).texture(1f, 1f).next();
            buffer.vertex(positionMatrix, v1, v1, v1).color(0xffffffff).texture(1f, 0f).next();

            matrixStack.pop();
        }
    }
*/
    public static synchronized void addMarkedInventory(MarkedInventory inventory) {
        inventories.add(inventory);
    }

    public static synchronized void clearMarkedInventories() {
        inventories.clear();
    }

    public static synchronized int getMarkedInventoriesSize(){
        return inventories.size();
    }

    static synchronized List<MarkedInventory> getMarkedInventories(){
        return inventories;
    }
    
    public static synchronized void setMarkedInventories(List<MarkedInventory> _inventories){
        inventories = _inventories;
    }
}
