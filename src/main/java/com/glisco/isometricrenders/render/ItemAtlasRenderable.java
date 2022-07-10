package com.glisco.isometricrenders.render;

import com.glisco.isometricrenders.property.DefaultPropertyBundle;
import com.glisco.isometricrenders.property.IntProperty;
import com.glisco.isometricrenders.property.Property;
import com.glisco.isometricrenders.util.ExportPathSpec;
import com.glisco.isometricrenders.widget.WidgetColumnBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3f;

import java.util.List;

public class ItemAtlasRenderable extends DefaultRenderable<ItemAtlasRenderable.ItemAtlasPropertyBundle> {

    private final MinecraftClient client = MinecraftClient.getInstance();
    private final List<ItemStack> items;
    private final String atlasSource;

    public ItemAtlasRenderable(String atlasSource, List<ItemStack> items) {
        this.atlasSource = atlasSource;
        this.items = items;
    }

    @Override
    public void emitVertices(MatrixStack matrices, VertexConsumerProvider vertexConsumers, float tickDelta) {
        final int columns = this.properties().columns.get();
        final int rows = MathHelper.ceilDiv(this.items.size(), columns);
        final int spacing = this.properties().spacing.get();

        matrices.scale(.25f, .25f, .25f);
        matrices.translate(-((columns / 2f) + 0.5f), (rows / 2f) + 0.5f, 0);

        for (int row = 0; row < rows; row++) {
            matrices.translate(0, -spacing, 0);
            matrices.push();
            for (int column = 0; column < columns; column++) {
                matrices.translate(spacing, 0, 0);
                final var index = row * columns + column;
                if (index >= this.items.size()) continue;

                client.getItemRenderer().renderItem(
                        this.items.get(index),
                        ModelTransformation.Mode.GUI,
                        LightmapTextureManager.MAX_LIGHT_COORDINATE,
                        OverlayTexture.DEFAULT_UV,
                        matrices,
                        vertexConsumers,
                        0
                );
            }
            matrices.pop();
        }
    }

    @Override
    public ItemAtlasPropertyBundle properties() {
        return ItemAtlasPropertyBundle.INSTANCE;
    }

    @Override
    public ExportPathSpec exportPath() {
        return ExportPathSpec.of("atlases", this.atlasSource);
    }

    public static class ItemAtlasPropertyBundle extends DefaultPropertyBundle {

        private static final ItemAtlasPropertyBundle INSTANCE = new ItemAtlasPropertyBundle();

        private final IntProperty columns = IntProperty.of(20, 1, 500);
        private IntProperty spacing = IntProperty.of(0, 1, 2);

        @Override
        public void buildGuiControls(Renderable<?> renderable, WidgetColumnBuilder builder) {
            builder.label("transform_options");

            this.appendIntControls(builder, this.scale, "scale", 10);
            this.appendIntControls(builder, this.columns, "columns", 1);
            this.appendIntControls(builder, this.spacing, "spacing", 1);
        }

        @Override
        public void applyToViewMatrix(MatrixStack modelViewStack) {
            super.applyToViewMatrix(modelViewStack);
            modelViewStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(-this.rotation.get()));
            modelViewStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(-this.slant.get()));
        }

    }
}
