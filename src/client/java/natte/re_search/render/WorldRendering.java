package natte.re_search.render;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

@Environment(EnvType.CLIENT)
public class WorldRendering {

    private static List<MarkedInventory> inventories = new ArrayList<>();
    

    

    public static void register() {
        // AFTER_TRANSLUCENT END
        
        MinecraftClient client = MinecraftClient.getInstance();

        

        WorldRenderEvents.BEFORE_ENTITIES.register(context -> {
            
            if(getMarkedInventoriesSize() == 0) return;

            Camera camera = context.camera();
            

            MarkedInventory inventory = getMarkedInventories().get(0);
            BlockPos blockPos = inventory.blockPos;
            Vec3d targetPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            double x=targetPosition.x,y=targetPosition.y,z=targetPosition.z;
            Vec3d transformedPosition = targetPosition.subtract(camera.getPos());

            MatrixStack matrixStack = new MatrixStack();
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

            Quaternionf q;
            float rotation = (float)MathHelper.atan2(transformedPosition.x + 0.5*0, transformedPosition.z + 0.5*0);
            q = RotationAxis.POSITIVE_Y.rotation(rotation+(float)Math.PI);
            matrixStack.translate(transformedPosition.x, transformedPosition.y, transformedPosition.z);
            matrixStack.multiply(q);
            q = RotationAxis.POSITIVE_X.rotation((float)MathHelper.atan2(transformedPosition.y, Math.hypot(transformedPosition.x, transformedPosition.z)));
            matrixStack.multiply(q.scale(0.5f));

    

            
            RenderSystem.setShader(GameRenderer::getPositionColorTexProgram);
            // RenderSystem.setShaderTexture(0, new Identifier("re_search", "mark2.png"));
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.disableCull();





            // drawCubes(buffer, matrixStack);

            var stack = new ItemStack(Items.COOKED_BEEF);
            stack = client.player.getMainHandStack();
            stack = inventory.inventory.get(0);
            ItemRenderer itemRenderer = client.getItemRenderer();
            
            itemRenderer.renderItem(stack, ModelTransformationMode.GUI, 255, OverlayTexture.DEFAULT_UV, matrixStack, context.consumers(), client.world, 0);
        });
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
}
