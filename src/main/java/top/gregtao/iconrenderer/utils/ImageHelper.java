package top.gregtao.iconrenderer.utils;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import top.gregtao.iconrenderer.IconRenderer;

import java.io.IOException;
import java.util.Base64;

public class ImageHelper {
    public FrameHelper frameHelper1, frameHelper2;
    public JsonMeta jsonMeta;
    public EntityJsonMeta entityJsonMeta;

    public ImageHelper(JsonMeta jsonMeta) {
        this.jsonMeta = jsonMeta;
        this.frameHelper1 = new FrameHelper(128, jsonMeta.itemStack);
        this.frameHelper2 = new FrameHelper(32, jsonMeta.itemStack);

        try (NativeImage image = fromFrame(this.frameHelper1.framebuffer, true)) {
            this.jsonMeta.largeIcon = Base64.getEncoder().encodeToString(image.asByteArray());
        } catch (IOException e) {
            IconRenderer.LOGGER.warn(String.format("Failed to export large icon for %s", this.jsonMeta.regName), e);
        }

        try (NativeImage image = fromFrame(this.frameHelper2.framebuffer, true)) {
            this.jsonMeta.smallIcon = Base64.getEncoder().encodeToString(image.asByteArray());
        } catch (IOException e) {
            IconRenderer.LOGGER.warn(String.format("Failed to export small icon for %s", this.jsonMeta.regName), e);
        }
    }

    public ImageHelper(EntityJsonMeta jsonMeta) {
        this.entityJsonMeta = jsonMeta;
        this.frameHelper1 = new FrameHelper(128, jsonMeta.entity);
        try (NativeImage image = fromFrame(this.frameHelper1.framebuffer, false)) {
            this.entityJsonMeta.icon = Base64.getEncoder().encodeToString(image.asByteArray());
        } catch (IOException e) {
            IconRenderer.LOGGER.warn(String.format("Failed to export icon for %s", this.entityJsonMeta.regName), e);
        }
    }

    public ImageHelper(FluidJsonMeta jsonMeta) {
        this.frameHelper1 = new FrameHelper(128, jsonMeta.fluid);
        this.frameHelper2 = new FrameHelper(32, jsonMeta.fluid);

        try (NativeImage image = fromFrame(this.frameHelper1.framebuffer, true)) {
            jsonMeta.largeIcon = Base64.getEncoder().encodeToString(image.asByteArray());
        } catch (IOException e) {
            IconRenderer.LOGGER.warn(String.format("Failed to export large fluid icon for %s", jsonMeta.regName), e);
        }

        try (NativeImage image = fromFrame(this.frameHelper2.framebuffer, true)) {
            jsonMeta.smallIcon = Base64.getEncoder().encodeToString(image.asByteArray());
        } catch (IOException e) {
            IconRenderer.LOGGER.warn(String.format("Failed to export small fluid icon for %s", jsonMeta.regName), e);
        }
    }

    public static NativeImage fromFrame(RenderTarget frame, boolean flipY) {
        NativeImage img = new NativeImage(frame.width, frame.height, false);
        RenderSystem.bindTexture(frame.getColorTextureId());
        img.downloadTexture(0, false);
        if (flipY) img.flipY();
        return img;
    }

}
