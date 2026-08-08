package top.gregtao.iconrenderer.utils;

import com.google.gson.JsonObject;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

public class FluidJsonMeta {
    public Fluid fluid;
    public FluidState fluidState;
    public String zhName;
    public String enName;
    public String regName;
    public RenderType type;
    public String smallIcon;
    public String largeIcon;
    public String fluidColor;
    public ImageHelper imageHelper;

    public FluidJsonMeta(Fluid fluid) {
        this.fluid = fluid;
        this.fluidState = fluid.defaultFluidState();
        this.regName = Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)).toString();
        this.type = RenderType.Fluid;
        int color = IClientFluidTypeExtensions.of(fluidState).getTintColor();
        this.fluidColor = String.format("#%08X", color);
        this.imageHelper = new ImageHelper(this);
    }

    public JsonObject toJsonObject() {
        JsonObject result = new JsonObject();
        result.addProperty("name", this.zhName);
        result.addProperty("englishName", this.enName);
        result.addProperty("registerName", this.regName);
        result.addProperty("type", this.type.toString());
        result.addProperty("fluidColor", this.fluidColor);
        result.addProperty("smallIcon", this.smallIcon);
        result.addProperty("largeIcon", this.largeIcon);
        return result;
    }
}