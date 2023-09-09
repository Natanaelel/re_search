package natte.re_search.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import natte.re_search.RegexSearch;
import natte.re_search.config.Config;
import natte.re_search.search.MarkedInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
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
            if(Config.isOldHighlighter) rotateBasedOnPosition(context, client);

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




            // /* v combat view bobbing v */
            // float tickDelta = context.tickDelta();
            // //  if (!(this.client.getCameraEntity() instanceof PlayerEntity)) {
            // //     return;
            // // }

            // float s = 0.0f;
            // float scale2 = 1f - s;
            // PlayerEntity playerEntity = (PlayerEntity)client.getCameraEntity();
            // float f = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
            // float g = -(playerEntity.horizontalSpeed + f * tickDelta);
            // float h = MathHelper.lerp((float)tickDelta, (float)playerEntity.prevStrideDistance, (float)playerEntity.strideDistance);
            // // matrixStack.translate(-MathHelper.sin((float)(g * (float)Math.PI)) * h * 0.5f, Math.abs(MathHelper.cos((float)(g * (float)Math.PI)) * h), 0.0f);
            // // matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin((float)(g * (float)Math.PI)) * h * 3.0f).invert().scale(scale2));
            // // matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos((float)(g * (float)Math.PI - 0.2f)) * h) * 5.0f).invert().scale(scale2));
        

            // /* ^ combat view bobbing ^ */
            

            

            Vec3d relativeBlockPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).subtract(camera.getPos());

            int size = inventory.inventory.size();
            int sideLength = (int)Math.ceil(Math.sqrt(size));

            matrixStack.push();
            {

                Vec3d transformedPosition = relativeBlockPosition.add(0.5, 0.5, 0.5);
                float distance = (float)transformedPosition.length();


                
                float scale = 0.15f / distance;
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                // if(client.options.getBobView().getValue()){
                //     /* v combat view bobbing v */
                //     float tickDelta = context.tickDelta();
                //     //  if (!(this.client.getCameraEntity() instanceof PlayerEntity)) {
                //     //     return;
                //     // }


                //     float scale2 = 1f - scale;
                //     // scale2 = 1f;
                //     PlayerEntity playerEntity = (PlayerEntity)client.getCameraEntity();
                //     float f = playerEntity.horizontalSpeed - playerEntity.prevHorizontalSpeed;
                //     float g = -(playerEntity.horizontalSpeed + f * tickDelta);
                //     float h = MathHelper.lerp((float)tickDelta, (float)playerEntity.prevStrideDistance, (float)playerEntity.strideDistance);
                //     // matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(Math.abs(MathHelper.cos((float)(g * (float)Math.PI - 0.2f)) * h) * 5.0f).scale(scale2));
                //     // matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(MathHelper.sin((float)(g * (float)Math.PI)) * h * 3.0f).scale(scale2));
                //     // matrixStack.translate(MathHelper.sin((float)(g * (float)Math.PI)) * h * 0.5f * -scale2, -Math.abs(MathHelper.cos((float)(g * (float)Math.PI)) * h) * -scale2, 0.0f);
                // }

                /* ^ combat view bobbing ^ */
                

                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

            
                // move item to block pos
                matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);

                // rotate to face player
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation((float)MathHelper.atan2(-transformedPosition.x, -transformedPosition.z)));
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y, Math.hypot(transformedPosition.x, transformedPosition.z))));

               
                // float smoothing = 100f;
                // smoothing = client.player.headYaw;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(0.02f * smoothing)) / smoothing;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(((scale - 0.02f) * 0.5f + 0.02f) * smoothing)) / smoothing;
                // if(scale > 0.1)
                // largest 0.13
                // medium 0.02
                // no smaller than 0.01 ever please
                
                // move item to 0.25 blocks from camera
                matrixStack.translate(0, 0, distance-0.25f);
               
                matrixStack.scale(scale, scale, scale);
            


                int i = -1;
                for(ItemStack itemStack : inventory.inventory){
                    i += 1;
                    int x = i % sideLength;
                    int y = i / sideLength;
            
                    
                    matrixStack.push();

                    matrixStack.translate(x * 1.125f - (y == (float)size / sideLength ? size % sideLength : sideLength) / 2f + 0.5f - 0.125f*sideLength/2+0.0625, y*1.125f + 1.6f, 0f);
                    
                    
                    RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                    RenderSystem.setShaderTexture(0, new Identifier(RegexSearch.MOD_ID, "textures/arrow_light.png"));
                    RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                    RenderSystem.disableCull();
                    RenderSystem.depthFunc(GL11.GL_ALWAYS);
                    RenderSystem.disableDepthTest();
                    RenderSystem.depthMask(false);
                    // vertexConsumers.getBuffer(RenderLayer.getDebugQuads());
                    RenderLayers.getItemLayer(itemStack, true);
                    itemRenderer.renderItem(itemStack, ModelTransformationMode.GUI, 0xff, OverlayTexture.DEFAULT_UV, matrixStack, vertexConsumers, client.world, 0);
                    itemRenderer.renderItem(null, itemStack, ModelTransformationMode.GUI, false, matrixStack, vertexConsumers, client.world, 0xff, OverlayTexture.DEFAULT_UV, 0);
                    RenderSystem.depthMask(true);
                    RenderSystem.enableDepthTest();
                    // tessellator.draw();
                
                    RenderSystem.depthFunc(GL11.GL_LEQUAL);
                    RenderSystem.enableCull();
                    
                    matrixStack.pop();
                }
            }
            matrixStack.pop();

            matrixStack.push();
            {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                
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

                Matrix4f positionMatrix = matrixStack.peek().getPositionMatrix();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();
            
                // buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                buffer.vertex(positionMatrix, -0.5f, 0.5f, 0).texture(0f, 0f).next();
                buffer.vertex(positionMatrix, -0.5f, -0.5f, 0).texture(0f, 1f).next();
                buffer.vertex(positionMatrix, 0.5f, -0.5f, 0).texture(1f, 1f).next();
                buffer.vertex(positionMatrix, 0.5f, 0.5f, 0).texture(1f, 0f).next();
            
                // RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderTexture(0, new Identifier(RegexSearch.MOD_ID, "textures/arrow.png"));
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
                RenderSystem.disableCull();
                RenderSystem.depthFunc(GL11.GL_ALWAYS);
            
                tessellator.draw();
            
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                RenderSystem.enableCull();
                        
            }
            matrixStack.pop();
            
            matrixStack.push();
            {
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

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
        HighlightRenderer.setRenderedItems(_inventories);
    }
}
