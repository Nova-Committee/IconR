package top.gregtao.iconrenderer.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import org.joml.Matrix4f;

public class FrameHelper {
    public RenderTarget framebuffer;
    private PoseStack modelStack;

    public FrameHelper(int size, ItemStack itemStack) {
        this.framebuffer = new TextureTarget(size, size, true, Minecraft.ON_OSX);
        this.startRecord();
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        this.renderGuiItemIcon(itemStack, 0, 0, renderer);
        this.endRecord();
    }

    public FrameHelper(int size, Entity entity) {
        this.framebuffer = new TextureTarget(size, size, true, Minecraft.ON_OSX);
        this.startRecord();
        this.renderEntity(entity);
        this.endRecord();
    }

    public FrameHelper(int size, Fluid fluid) {
        this.framebuffer = new TextureTarget(size, size, true, Minecraft.ON_OSX);
        this.startRecord();
        this.renderFluidIcon(fluid);
        this.endRecord();
    }

    public void startRecord() {
        this.modelStack = RenderSystem.getModelViewStack();
        this.modelStack.pushPose();
        this.modelStack.setIdentity();

        RenderSystem.backupProjectionMatrix();
        Matrix4f p = new Matrix4f().setOrtho(0, 16, 16, 0, -150, 150);
        RenderSystem.setProjectionMatrix(p, VertexSorting.ORTHOGRAPHIC_Z);

        this.framebuffer.bindWrite(true);
        this.framebuffer.bindRead();
    }

    public void endRecord() {
        RenderSystem.restoreProjectionMatrix();
        this.modelStack.popPose();

        this.framebuffer.unbindWrite();
        this.framebuffer.unbindRead();
    }

    public void renderEntity(Entity spawnEntity) {
        Minecraft client = Minecraft.getInstance();
        MultiBufferSource.BufferSource immediate = client.renderBuffers().bufferSource();

        this.modelStack = RenderSystem.getModelViewStack();
        this.modelStack.pushPose();
        this.modelStack.setIdentity();
        this.modelStack.mulPose(Axis.XP.rotationDegrees(112.5f));
        this.modelStack.scale(2.5f, -2.5f, -2.5f);
        this.modelStack.translate(0.75f, 1f, 1f);
        this.modelStack.mulPose(Axis.ZP.rotationDegrees(45f));
        this.modelStack.translate(-0.75f, 0, 0);
        this.modelStack.mulPose(Axis.YP.rotationDegrees(22.5f));
        this.modelStack.mulPose(Axis.ZN.rotationDegrees(22.5f));
        this.modelStack.translate(0.75f, 0, 0);

        if (!(client.player == null)) {
            spawnEntity.setPosRaw(client.player.getX(), client.player.getY(), client.player.getZ());
        }

        try {
            client.getEntityRenderDispatcher().render(spawnEntity, 0, 0, 0, 0,
                    client.getFrameTime(), this.modelStack, immediate, 15728880);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void renderGuiItemIcon(ItemStack stack, int x, int y, ItemRenderer renderer) {
        this.renderGuiItemModel(stack, x, y, renderer.getModel(stack, null, null, 0), renderer);
    }

    protected void renderGuiItemModel(ItemStack stack, int x, int y, BakedModel model, ItemRenderer renderer) {
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        matrixStack.translate(x, y, 100.0F);
        matrixStack.translate(8.0D, 8.0D, 0.0D);
        matrixStack.scale(1.0F, -1.0F, 1.0F);
        matrixStack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack matrixStack2 = new PoseStack();
        MultiBufferSource.BufferSource immediate = Minecraft.getInstance().renderBuffers().bufferSource();
        boolean bl = !model.usesBlockLight();
        if (bl) {
            Lighting.setupForFlatItems();
        }

        renderer.render(stack, ItemDisplayContext.GUI, false, matrixStack2, immediate, 15728880, OverlayTexture.NO_OVERLAY, model);
        immediate.endBatch();
        RenderSystem.enableDepthTest();
        if (bl) {
            Lighting.setupFor3DItems();
        }

        matrixStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public void renderFluidIcon(Fluid fluid) {
        FluidState state = fluid.defaultFluidState();
        IClientFluidTypeExtensions clientExtensions = IClientFluidTypeExtensions.of(state);
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(clientExtensions.getStillTexture());

        int tintColor = clientExtensions.getTintColor();
        float r = (tintColor >> 16 & 255) / 255.0F;
        float g = (tintColor >> 8 & 255) / 255.0F;
        float b = (tintColor & 255) / 255.0F;
        float a = (tintColor >> 24 & 255) / 255.0F;
        if (a == 0.0F) a = 1.0F;

        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);

        float u0 = sprite.getU0();
        float v0 = sprite.getV0();
        float u1 = sprite.getU1();
        float v1 = sprite.getV1();

        builder.vertex(0, 16, 0).uv(u0, v1).color(r, g, b, a).endVertex();
        builder.vertex(16, 16, 0).uv(u1, v1).color(r, g, b, a).endVertex();
        builder.vertex(16, 0, 0).uv(u1, v0).color(r, g, b, a).endVertex();
        builder.vertex(0, 0, 0).uv(u0, v0).color(r, g, b, a).endVertex();

        BufferUploader.drawWithShader(builder.end());
        RenderSystem.disableBlend();
    }
}
