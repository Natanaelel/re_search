package natte.re_search.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Quaternionf;

import natte.re_search.RegexSearch;
import natte.re_search.search.MarkedInventory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
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



        WorldRenderEvents.BEFORE_ENTITIES.register(context -> {
            rotateBasedOnPosition(context, client);
            // rotateBasedOnCamera(context, client);
            // rotateBasedOnSomethingElse(context, client);

            return;
           
        });
    }

    private static void rotateBasedOnPosition(WorldRenderContext context, MinecraftClient client) {
        if(getMarkedInventoriesSize() == 0) return;

        Camera camera = context.camera();
        
        for(MarkedInventory inventory : getMarkedInventories()){
            
            BlockPos blockPos = inventory.blockPos;
            
            MatrixStack matrixStack = new MatrixStack();
    
            int size = inventory.inventory.size();
            int sideLength = (int)Math.ceil(Math.sqrt(size));
            // int height = (int)Math.ceil((double)size / sideLength);

            int i = -1;
            for(ItemStack itemStack : inventory.inventory){
                i += 1;
                int x = i % sideLength;
                int y = i / sideLength;
        
                Vec3d targetPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.5);
                Vec3d transformedPosition = targetPosition.subtract(camera.getPos());
            
                matrixStack.push();
                
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                
                float d = 0*-0.5f;
                matrixStack.translate(transformedPosition.x, transformedPosition.y+d, transformedPosition.z); // move item to block pos
                
                float rotation = (float)MathHelper.atan2(transformedPosition.x, transformedPosition.z);
                Quaternionf q = RotationAxis.POSITIVE_Y.rotation(rotation+(float)Math.PI);
                matrixStack.multiply(q);
                q = RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y+d, Math.hypot(transformedPosition.x, transformedPosition.z)));
                matrixStack.multiply(q);


                float distance = (float)transformedPosition.length();

                matrixStack.translate(0, 0, distance-0.25f);  // move item to 0.25 distance from camera
                float scale = 1f / distance;
                scale *= 0.15f;
                // if(scale < 0.05) scale = (scale-0.05f)*0.9f+0.05f;//(scale-0.05f)*2+0.05f;
                // scale = Math.max(scale, 0.02f);
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                // float smoothing = 100f;
                // smoothing = client.player.headYaw;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(0.02f * smoothing)) / smoothing;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(((scale - 0.02f) * 0.5f + 0.02f) * smoothing)) / smoothing;
                // if(scale > 0.1)
                // largest 0.13
                // medium 0.02
                // no smaller than 0.01 ever please

                matrixStack.scale(scale, scale, scale);


                // matrixStack.translate(x * 1.1f - (y == size / sideLength ? size % sideLength : sideLength) / 2f + 0.5f, y*1.1f + 1f, 0f);
                matrixStack.translate(x * 1.125f - (y == (float)size / sideLength ? size % sideLength : sideLength) / 2f + 0.5f- 0.125f*sideLength/2+0.0625, y*1.125f + 1f+0.6f, 0f);
                

                ItemRenderer itemRenderer = client.getItemRenderer();
                itemRenderer.renderItem(itemStack, ModelTransformationMode.GUI, 0xff, OverlayTexture.DEFAULT_UV, matrixStack, context.consumers(), client.world, 0);
            
                matrixStack.pop();
            }


            {
                
                matrixStack.push();

                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                
                
                Vec3d targetPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.5);
                Vec3d transformedPosition = targetPosition.subtract(camera.getPos());


                
                Quaternionf q;
                float rotation = (float)MathHelper.atan2(transformedPosition.x + 0.5*0, transformedPosition.z + 0.5*0);
                q = RotationAxis.POSITIVE_Y.rotation(rotation+(float)Math.PI);
                matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
                
                matrixStack.multiply(q);
                q = RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y, Math.hypot(transformedPosition.x, transformedPosition.z)));
                matrixStack.multiply(q.scale(1));
                float distance = (float)transformedPosition.length();
                matrixStack.translate(0, 0, distance-0.25f);
                float scale = 1f / (float)transformedPosition.length();
                scale *= 0.15f;
                // if(scale < 0.05) scale = (scale-0.05f)*0.9f+0.05f;//(scale-0.05f)*2+0.05f;
                // scale = Math.max(scale, 0.02f);
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                // float smoothing = 100f;
                // smoothing = client.player.headYaw;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(0.02f * smoothing)) / smoothing;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(((scale - 0.02f) * 0.5f + 0.02f) * smoothing)) / smoothing;
                // largest 0.13
                // medium 0.02
                // no smaller than 0.01 ever please
                matrixStack.scale(scale, scale, scale);

                matrixStack.translate(0f, 0.8f, 0f);

                ItemRenderer itemRenderer = client.getItemRenderer();
            
                itemRenderer.renderItem(new ItemStack(RegexSearch.ARROW_ITEM), ModelTransformationMode.GUI, 255, OverlayTexture.DEFAULT_UV, matrixStack, context.consumers(), client.world, 0);
                
                matrixStack.pop();
            }


            // targetPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.5);
            // transformedPosition = targetPosition.subtract(camera.getPos());

            // matrixStack.push();

            
            // rotation = (float)MathHelper.atan2(transformedPosition.x + 0.5*0, transformedPosition.z + 0.5*0);
            // q = RotationAxis.POSITIVE_Y.rotation(rotation+(float)Math.PI);
            // matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
            
            // matrixStack.multiply(q);
            // q = RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y, Math.hypot(transformedPosition.x, transformedPosition.z)));
            // matrixStack.multiply(q.scale(1));
            // distance = (float)transformedPosition.length();
            // matrixStack.translate(0, 0, distance-0.25f);
            // scale = 1f / (float)transformedPosition.length();
            // scale *= 0.15f;
            // // if(scale < 0.05) scale = (scale-0.05f)*0.9f+0.05f;//(scale-0.05f)*2+0.05f;
            // // scale = Math.max(scale, 0.02f);
            // scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

            // // float smoothing = 100f;
            // // smoothing = client.player.headYaw;
            // // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(0.02f * smoothing)) / smoothing;
            // // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(((scale - 0.02f) * 0.5f + 0.02f) * smoothing)) / smoothing;
            // // largest 0.13
            // // medium 0.02
            // // no smaller than 0.01 ever please
            // matrixStack.scale(scale, scale, scale);


            
            // itemRenderer.renderItem(inventory.containers.get(0), ModelTransformationMode.GUI, 255, OverlayTexture.DEFAULT_UV, matrixStack, context.consumers(), client.world, 0);
            
            // matrixStack.pop();
            i = -1;
            for(ItemStack itemStack : inventory.containers){
                i += 1;
                int x = i;
        
                Vec3d targetPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.5);
                Vec3d transformedPosition = targetPosition.subtract(camera.getPos());
            
                matrixStack.push();
                
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                
                float d = 0*-0.5f;
                matrixStack.translate(transformedPosition.x, transformedPosition.y+d, transformedPosition.z); // move item to block pos
                
                float rotation = (float)MathHelper.atan2(transformedPosition.x, transformedPosition.z);
                Quaternionf q = RotationAxis.POSITIVE_Y.rotation(rotation+(float)Math.PI);
                matrixStack.multiply(q);
                q = RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y+d, Math.hypot(transformedPosition.x, transformedPosition.z)));
                matrixStack.multiply(q);


                float distance = (float)transformedPosition.length();

                matrixStack.translate(0, 0, distance-0.25f);  // move item to 0.25 distance from camera
                float scale = 1f / distance;
                scale *= 0.15f;
                // if(scale < 0.05) scale = (scale-0.05f)*0.9f+0.05f;//(scale-0.05f)*2+0.05f;
                // scale = Math.max(scale, 0.02f);
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                // float smoothing = 100f;
                // smoothing = client.player.headYaw;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(0.02f * smoothing)) / smoothing;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(((scale - 0.02f) * 0.5f + 0.02f) * smoothing)) / smoothing;
                // if(scale > 0.1)
                // largest 0.13
                // medium 0.02
                // no smaller than 0.01 ever please

                matrixStack.scale(scale, scale, scale);


                // matrixStack.translate(x * 1.1f - (y == size / sideLength ? size % sideLength : sideLength) / 2f + 0.5f, y*1.1f + 1f, 0f);
                matrixStack.translate(x * 1.125f - inventory.containers.size() / 2f + 0.5f- 0.125f*inventory.containers.size()/2+0.0625, 0f, 0f);
                

                ItemRenderer itemRenderer = client.getItemRenderer();
                itemRenderer.renderItem(itemStack, ModelTransformationMode.GUI, 0xff, OverlayTexture.DEFAULT_UV, matrixStack, context.consumers(), client.world, 0);
            
                matrixStack.pop();
            }
        }
    }
    
  /*  private static void rotateBasedOnCamera(WorldRenderContext context, MinecraftClient client){
        if(getMarkedInventoriesSize() == 0) return;

        Camera camera = context.camera();
               
        
        for(MarkedInventory inventory : getMarkedInventories()){
            
            BlockPos blockPos = inventory.blockPos;
            
            MatrixStack matrixStack = new MatrixStack();
    
            int size = inventory.inventory.size();
            int sideLength = (int)Math.ceil(Math.sqrt(size));
            int height = (int)Math.ceil((double)size / sideLength);

            int i = -1;
            for(ItemStack itemStack : inventory.inventory){
                i += 1;
                int x = i % sideLength;
                int y = i / sideLength;
        
                Vec3d targetPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 1, 0.5);
                Vec3d transformedPosition = targetPosition.subtract(camera.getPos());
            
                matrixStack.push();
                
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                
                float d = 0*-0.5f;
                matrixStack.translate(transformedPosition.x, transformedPosition.y+d, transformedPosition.z); // move item to block pos
                
                float rotation = (float)MathHelper.atan2(transformedPosition.x, transformedPosition.z);
                Quaternionf q = RotationAxis.POSITIVE_Y.rotation(rotation+(float)Math.PI);
                // matrixStack.multiply(q);
                q = RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y+d, Math.hypot(transformedPosition.x, transformedPosition.z)));
                // matrixStack.multiply(q);
                // camera.getRotation()
                // q = RotationAxis.of(transformedPosition.toVector3f()).rotationDegrees(45f);
                matrixStack.multiply(camera.getRotation().rotateY((float)Math.PI));
                // matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f));
                // matrixStack.multiply(, x, y, i);
                float distance = (float)transformedPosition.length();
                // matrixStack.translate(-transformedPosition.x, -transformedPosition.y, 1);
                new Vec3d(0, 0, 1);
                var r = camera.getRotation().rotateY((float)Math.PI);
                var vv = transformedPosition;
                                // matrixStack.translate(transformedPosition.x, transformedPosition.y+d, transformedPosition.z); // move item to block pos

                // vv = transformedPosition.normalize();
                // vv.subtract(vv.normalize().multiply(1));
                // vv=vv.multiply(0.5);

                Vector3f dirVec = transformedPosition.toVector3f();
                r.transform(dirVec);
                //transformedPosition.toVector3f();
                Quaternionf direction = new Quaternionf().lookAlong(transformedPosition.toVector3f(), dirVec);
                
                direction.difference(r);
                Vector3f v = new Vector3f(0, 0, 1);
                direction.invert().transform(v);
                // direction.transform(dirVec);
                // dirVec.mul((float)transformedPosition.length() * 0.5f);
                // Vector3f v = r.transformInverse(vv.toVector3f());
                // v.sub(new Vec3d(v.x, v.y, v.z).multiply(0.1f).toVector3f());
                // v=vv.toVector3f();
                // v=new Vector3f(0, 0, distance-0.25f);
                // matrixStack.peek().getPositionMatrix().rotate
                // v = transformedPosition.toVector3f().mul(-1f);
                // matrixStack.translate(transformedPosition.toVector3f().mul(camera.getRotation().t), y, size);
                // matrixStack.translate(v.x, v.y, 0);
                // matrixStack.translate(dirVec.x, dirVec.y, dirVec.z);
                // matrixStack.translate(0, 0, (float)transformedPosition.length() - 1f);
                // matrixStack.multiply(direction.invert(), 0, 0, 0);
                // if(client.world.random.nextFloat() < 0.0001) System.out.println(matrixStack.peek());
                // matrixStack.
                // matrixStack.multiply(new Quaternionf().identity());
                // matrixStack.multiplyPositionMatrix(new Matrix4f().scale(0.1f));
                // matrixStack.multiplyPositionMatrix(new Matrix4f().scale(10f));
                // matrixStack.translate(0, 0, 1);
                
                // matrixStack.translate(0, 0, distance-0.25f);  // move item to 0.25 distance from camera
                Matrix4f m=new Matrix4f();
                camera.getRotation().get(m);
                // matrixStack.multiplyPositionMatrix(m);
                // matrixStack.translate(0, 0, distance-0.25f);  // move item to 0.25 distance from camera
                // matrixStack.multiplyPositionMatrix(m.invert());

                float scale = 1f / distance;
                scale *= 0.15f;
                // if(scale < 0.05) scale = (scale-0.05f)*0.9f+0.05f;//(scale-0.05f)*2+0.05f;
                // scale = Math.max(scale, 0.02f);
                // scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                float smoothing = 100f;
                // smoothing = client.player.headYaw;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(0.02f * smoothing)) / smoothing;
                // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(((scale - 0.02f) * 0.5f + 0.02f) * smoothing)) / smoothing;
                // if(scale > 0.1)
                // largest 0.13
                // medium 0.02
                // no smaller than 0.01 ever please

                // matrixStack.scale(scale, scale, scale);


                matrixStack.translate(x * 1.125f - (y == (float)size / sideLength ? size % sideLength : sideLength) / 2f + 0.5f- 0.125f*sideLength/2+0.0625, y*1.125f + 1f, 0f);
                
                // matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(45), 0, 0, 0);

                ItemRenderer itemRenderer = client.getItemRenderer();
                itemRenderer.renderItem(itemStack, ModelTransformationMode.GUI, 0xff, OverlayTexture.DEFAULT_UV, matrixStack, context.consumers(), client.world, 0);
            
                matrixStack.pop();
            }



            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
            

            Vec3d targetPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 1.0, 0.5);
            Vec3d transformedPosition = targetPosition.subtract(camera.getPos());

            matrixStack.push();

            
            Quaternionf q;
            float rotation = (float)MathHelper.atan2(transformedPosition.x + 0.5*0, transformedPosition.z + 0.5*0);
            q = RotationAxis.POSITIVE_Y.rotation(rotation+(float)Math.PI);
            matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
            
            matrixStack.multiply(q);
            q = RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y, Math.hypot(transformedPosition.x, transformedPosition.z)));
            matrixStack.multiply(q.scale(1));
            float distance = (float)transformedPosition.length();
            matrixStack.translate(0, 0, distance-0.25f);
            float scale = 1f / (float)transformedPosition.length();
            scale *= 0.15f;
            // if(scale < 0.05) scale = (scale-0.05f)*0.9f+0.05f;//(scale-0.05f)*2+0.05f;
            // scale = Math.max(scale, 0.02f);
            scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

            float smoothing = 100f;
            // smoothing = client.player.headYaw;
            // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(0.02f * smoothing)) / smoothing;
            // scale = (float)Math.log(Math.exp(scale * smoothing)+Math.exp(((scale - 0.02f) * 0.5f + 0.02f) * smoothing)) / smoothing;
            // largest 0.13
            // medium 0.02
            // no smaller than 0.01 ever please
            matrixStack.scale(scale, scale, scale);

          
            ItemRenderer itemRenderer = client.getItemRenderer();
        
            itemRenderer.renderItem(new ItemStack(RegexSearch.ARROW_ITEM), ModelTransformationMode.GUI, 255, OverlayTexture.DEFAULT_UV, matrixStack, context.consumers(), client.world, 0);
          
            matrixStack.pop();
        }
    }

    private static void rotateBasedOnSomethingElse(WorldRenderContext context, MinecraftClient client){
        if(getMarkedInventoriesSize() == 0) return;

        Camera camera = context.camera();
        
        for(MarkedInventory inventory : getMarkedInventories()){
            
            BlockPos blockPos = inventory.blockPos;
            
            MatrixStack matrixStack = new MatrixStack();
    
            int size = inventory.inventory.size();
            int sideLength = (int)Math.ceil(Math.sqrt(size));
            // int height = (int)Math.ceil((double)size / sideLength);

            int i = -1;
            for(ItemStack itemStack : inventory.inventory){
                i += 1;
                int x = i % sideLength;
                int y = i / sideLength;
        
                Vec3d targetPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()).add(0.5, 0.5, 0.5);
                Vec3d transformedPosition = targetPosition.subtract(camera.getPos());
                // Vec3d transformedPosition = targetPosition.subtract(camera.getPos().subtract(0, 1.62f - 0.5f, 0));
                // System.out.println(client.player.getEyeY());
                matrixStack.push();
                
                matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
                matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));
                
                float d = 1.62f-0.5f;
                d = client.player.getInventory().selectedSlot;
                // System.out.println(d);
                matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z); // move item to block pos
                
                float rotation = (float)MathHelper.atan2(transformedPosition.x, transformedPosition.z);
                Quaternionf q = RotationAxis.POSITIVE_Y.rotation(rotation+(float)Math.PI);
                matrixStack.multiply(q);
                q = RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y, Math.hypot(transformedPosition.x, transformedPosition.z)));
                matrixStack.translate(0, d, 0);
                matrixStack.multiply(q);
                matrixStack.translate(0, -d, 0);


                float distance = (float)transformedPosition.subtract(0, 0, 0).length();

                float scale = 1f / distance;
                scale *= 0.15f;
                // if(scale < 0.05) scale = (scale-0.05f)*0.9f+0.05f;//(scale-0.05f)*2+0.05f;
                // scale = Math.max(scale, 0.02f);
                // scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);
                
                // matrixStack.translate(0, 0, distance-0.25f);  // move item to 0.25 distance from camera
                // matrixStack.scale(scale, scale, scale);
                // matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(rotation+(float)Math.PI).invert());

                // matrixStack.translate(x * 1.1f - (y == size / sideLength ? size % sideLength : sideLength) / 2f + 0.5f, y*1.1f + 1f, 0f);
                matrixStack.translate(x * 1.125f - (y == (float)size / sideLength ? size % sideLength : sideLength) / 2f + 0.5f- 0.125f*sideLength/2+0.0625, y*1.125f + 1f+0.6f, 0f);
                

                ItemRenderer itemRenderer = client.getItemRenderer();
                itemRenderer.renderItem(itemStack, ModelTransformationMode.GUI, 0xff, OverlayTexture.DEFAULT_UV, matrixStack, context.consumers(), client.world, 0);
            
                matrixStack.pop();
            }
        }
    }
*/
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
