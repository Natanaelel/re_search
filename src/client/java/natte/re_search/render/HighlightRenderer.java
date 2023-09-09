package natte.re_search.render;

import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import natte.re_search.RegexSearch;
import natte.re_search.config.Config;
import natte.re_search.search.MarkedInventory;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import static natte.re_search.RegexSearchClient.Game;

public class HighlightRenderer {

    public static List<RenderedItem> renderedItems = new ArrayList<>();

    public static void register() {
        HudRenderCallback.EVENT.register((context, tickDelta) -> {
            if(!Config.isOldHighlighter) onRenderGUI(context, tickDelta);
        });
    }

    public static void onRenderGUI(DrawContext ctx, float tickDelta) {
        if (Game.player == null) {
            return;
        }

        var m = ctx.getMatrices();

        for (RenderedItem renderedItem : getRenderedItems()) {

            var uiScale = (float) Game.getWindow().getScaleFactor();

            if (renderedItem.screenPosition == null) {
                continue;
            }

            var pos = renderedItem.screenPosition;
            var cameraPosVec = Game.player.getCameraPosVec(tickDelta);
            var distance = (float) cameraPosVec.distanceTo(renderedItem.position);

            // var white = ColorHelper.Argb.getArgb(255, 255, 255, 255);
            // var shadowBlack = ColorHelper.Argb.getArgb(64, 0, 0, 0);

            m.push();
            // m.translate((pos.x / uiScale), (pos.y / uiScale), 0);
            // m.scale(pingScale, pingScale, 1f);
            // float sc = 10f;
            // m.translate(renderedItem.x * sc, renderedItem.y * sc, 0);

            // var text = String.format("%.1fm", distanceToPing);
            // var textMetrics = new Vec2f(
            // Game.textRenderer.getWidth(text),
            // Game.textRenderer.fontHeight
            // );
            // var textOffset = textMetrics.multiply(-0.5f).add(new Vec2f(0f, textMetrics.y
            // * -1.5f));

            // m.push();
            // m.translate(textOffset.x, textOffset.y, 0);
            // ctx.fill(-2, -2, (int)textMetrics.x + 1, (int)textMetrics.y, shadowBlack);
            // ctx.drawText(Game.textRenderer, text, 0, 0, white, false);
            // m.pop();
            if (renderedItem.isArrow) {

                m.push();
                float scale = 0.15f / distance;
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                
                float c = 1000f;
                m.translate(pos.x / uiScale, pos.y / uiScale, -100);
                m.translate(-renderedItem.x * scale * c, -renderedItem.y * scale * c, - distance * distance);
                m.scale(scale, scale, scale);
                m.scale(1.f, -1.f, 1.f);
                float sc2 = 1000f;
                m.scale(sc2, sc2, sc2);

                Matrix4f positionMatrix = m.peek().getPositionMatrix();
                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder buffer = tessellator.getBuffer();

                buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
                buffer.vertex(positionMatrix, -0.5f, 0.5f, 0).texture(0f, 0f).next();
                buffer.vertex(positionMatrix, -0.5f, -0.5f, 0).texture(0f, 1f).next();
                buffer.vertex(positionMatrix, 0.5f, -0.5f, 0).texture(1f, 1f).next();
                buffer.vertex(positionMatrix, 0.5f, 0.5f, 0).texture(1f, 0f).next();

                RenderSystem.setShader(GameRenderer::getPositionTexProgram);
                RenderSystem.setShaderTexture(0, new Identifier(RegexSearch.MOD_ID, "textures/arrow.png"));
                RenderSystem.disableCull();

                tessellator.draw();

                RenderSystem.enableCull();

                m.pop();

            } else {
                var model = Game.getItemRenderer().getModel(renderedItem.itemStack, null, null, 0);

                float scale = 0.15f / distance;
                scale = Math.max(scale, (scale - 0.02f) * 0.7f + 0.02f);

                renderGuiItemModel(
                        renderedItem.itemStack,
                        (pos.x / uiScale),
                        (pos.y / uiScale),
                        model,
                        scale, renderedItem);
            }
            m.pop();
        }
    }

    public static void onRenderWorld(MatrixStack matrixStack, Matrix4f projectionMatrix, Camera camera,
            float tickDelta) {
        processItems(matrixStack, projectionMatrix, camera, tickDelta);
    }

    private static void processItems(MatrixStack matrixStack, Matrix4f projectionMatrix, Camera camera,
            float tickDelta) {

        var modelViewMatrix = matrixStack.peek().getPositionMatrix();

        for (RenderedItem renderedItem : getRenderedItems()) {
            renderedItem.screenPosition = project3Dto2D(renderedItem, modelViewMatrix, projectionMatrix, camera);
        }
    }

    public static synchronized List<RenderedItem> getRenderedItems() {
        return renderedItems;
    }

    public static synchronized void setRenderedItems(List<MarkedInventory> inventories) {

        List<RenderedItem> renderedItems = new ArrayList<>();
        for (MarkedInventory inventory : WorldRendering.getMarkedInventories()) {
            BlockPos blockPos = inventory.blockPos;

            Vec3d blockPosition = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());

            Vec3d transformedPosition = blockPosition.add(0.5, 0.5, 0.5);

            int size = inventory.inventory.size();
            int sideLength = (int) Math.ceil(Math.sqrt(size));

            int i = -1;
            for (ItemStack itemStack : inventory.inventory) {
                i += 1;
                int x = i % sideLength;
                int y = i / sideLength;

                RenderedItem renderedItem = new RenderedItem();
                renderedItem.itemStack = itemStack;

                renderedItem.position = transformedPosition;
                renderedItem.x = x * 1.125f - (y == (float) size / sideLength ? size % sideLength : sideLength) / 2f
                        + 0.5f - 0.125f * sideLength / 2 + 0.0625f;
                renderedItem.y = y * 1.125f + 1.6f;

                renderedItems.add(renderedItem);
            }

            {
                RenderedItem arrow = new RenderedItem();
                arrow.isArrow = true;
                arrow.position = transformedPosition;
                arrow.x = 0;
                arrow.y = 0.8f;
                renderedItems.add(arrow);
            }
            i = -1;
            for (ItemStack itemStack : inventory.containers) {
                i += 1;
                int x = i;

                RenderedItem container = new RenderedItem();
                container.itemStack = itemStack;
                container.position = transformedPosition;
                container.x = x * 1.125f - inventory.containers.size() / 2f + 0.5f
                        - 0.125f * inventory.containers.size() / 2f + 0.0625f;
                container.y = 0;
                renderedItems.add(container);
            }

        }
        HighlightRenderer.renderedItems = renderedItems;
    }

    public static Vector4f project3Dto2D(RenderedItem renderedItem, Matrix4f modelViewMatrix, Matrix4f projectionMatrix,
            Camera camera) {

        Vec3d in3d = renderedItem.position.subtract(camera.getPos());

        var wnd = Game.getWindow();
        var quaternion = new Quaternionf((float) in3d.x, (float) in3d.y, (float) in3d.z, 1.f);
        var product = mqProduct(projectionMatrix, mqProduct(modelViewMatrix, quaternion));

        if (product.w <= 0f) {
            return null;
        }

        var screenPos = qToScreen(product);
        var x = screenPos.x * wnd.getWidth();
        var y = screenPos.y * wnd.getHeight();

        if (Float.isInfinite(x) || Float.isInfinite(y)) {
            return null;
        }

        return new Vector4f(x, wnd.getHeight() - y, screenPos.z, 1f / (screenPos.w * 2f));
    }

    public static void rotateZ(MatrixStack matrixStack, float theta) {
        matrixStack.multiplyPositionMatrix(new Matrix4f().rotateZ(theta));
    }

    private static Quaternionf mqProduct(Matrix4f m, Quaternionf q) {
        return new Quaternionf(
                m.m00() * q.x + m.m10() * q.y + m.m20() * q.z + m.m30() * q.w,
                m.m01() * q.x + m.m11() * q.y + m.m21() * q.z + m.m31() * q.w,
                m.m02() * q.x + m.m12() * q.y + m.m22() * q.z + m.m32() * q.w,
                m.m03() * q.x + m.m13() * q.y + m.m23() * q.z + m.m33() * q.w);
    }

    private static Quaternionf qToScreen(Quaternionf q) {
        var w = 1f / q.w * 0.5f;

        return new Quaternionf(
                q.x * w + 0.5f,
                q.y * w + 0.5f,
                q.z * w + 0.5f,
                w);
    }

    public static void renderGuiItemModel(ItemStack itemStack,
            double x,
            double y,
            BakedModel model,
            float scale, RenderedItem renderedItem) {
        Game.getTextureManager()
                .getTexture(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE)
                .setFilter(false, false);

        RenderSystem.setShaderTexture(0, PlayerScreenHandler.BLOCK_ATLAS_TEXTURE);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.f, 1.f, 1.f, 1.f);

        var matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.translate(x, y, -100);

        float c = 1000f;
        matrixStack.translate(-renderedItem.x * scale * c, -renderedItem.y * scale * c,
                -Game.getCameraEntity().squaredDistanceTo(renderedItem.position));
        matrixStack.scale(scale, scale, scale);
        matrixStack.scale(1.f, -1.f, 1.f);
        float sc = 1000f;
        matrixStack.scale(sc, sc, sc);
        RenderSystem.applyModelViewMatrix();

        var matrixStackDummy = new MatrixStack();
        var immediate = Game.getBufferBuilders().getEntityVertexConsumers();

        var bl = !model.isSideLit();
        if (bl) {
            DiffuseLighting.disableGuiDepthLighting();
        }
        Game.getItemRenderer().renderItem(
                itemStack,
                ModelTransformationMode.GUI,
                false,
                matrixStackDummy,
                immediate,
                15728880,
                OverlayTexture.DEFAULT_UV,
                model);

        immediate.draw();
        RenderSystem.enableDepthTest();
        if (bl) {
            DiffuseLighting.enableGuiDepthLighting();
        }

        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    }
}