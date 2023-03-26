package net.minecraft.client.renderer;

import com.google.common.collect.Maps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.src.Config;
import net.minecraftforge.client.model.ISmartItemModel;
import net.optifine.CustomItems;
import net.optifine.reflect.Reflector;

import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings("ALL")
public class ItemModelMesher {
    private final Map<Integer, ModelResourceLocation> simpleShapes = Maps.newHashMap();
    private final Map<Integer, IBakedModel> simpleShapesCache = Maps.newHashMap();
    private final Map<Item, ItemMeshDefinition> shapers = Maps.newHashMap();
    private final ModelManager modelManager;

    public ItemModelMesher(ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    public TextureAtlasSprite getParticleIcon(Item item) {
        return getParticleIcon(item, 0);
    }

    public TextureAtlasSprite getParticleIcon(Item item, int meta) {
        return getItemModel(new ItemStack(item, 1, meta)).getParticleTexture();
    }

    public IBakedModel getItemModel(ItemStack stack) {
        Item item = stack.getItem();
        IBakedModel ibakedmodel = getItemModel(item, getMetadata(stack));

        if (ibakedmodel == null) {
            ItemMeshDefinition itemmeshdefinition = shapers.get(item);

            if (itemmeshdefinition != null) {
                ibakedmodel = modelManager.getModel(itemmeshdefinition.getModelLocation(stack));
            }
        }

        if (Reflector.ForgeHooksClient.exists() && ibakedmodel instanceof ISmartItemModel) {
            ibakedmodel = ((ISmartItemModel) ibakedmodel).handleItemState(stack);
        }

        if (ibakedmodel == null) {
            ibakedmodel = modelManager.getMissingModel();
        }

        if (Config.isCustomItems()) {
            ibakedmodel = CustomItems.getCustomItemModel(stack, ibakedmodel, null, true);
        }

        return ibakedmodel;
    }

    protected int getMetadata(ItemStack stack) {
        return stack.isItemStackDamageable() ? 0 : stack.getMetadata();
    }

    protected IBakedModel getItemModel(Item item, int meta) {
        return simpleShapesCache.get(getIndex(item, meta));
    }

    private int getIndex(Item item, int meta) {
        return Item.getIdFromItem(item) << 16 | meta;
    }

    public void register(Item item, int meta, ModelResourceLocation location) {
        simpleShapes.put(getIndex(item, meta), location);
        simpleShapesCache.put(getIndex(item, meta), modelManager.getModel(location));
    }

    public void register(Item item, ItemMeshDefinition definition) {
        shapers.put(item, definition);
    }

    public ModelManager getModelManager() {
        return modelManager;
    }

    public void rebuildCache() {
        simpleShapesCache.clear();

        for (Entry<Integer, ModelResourceLocation> entry : simpleShapes.entrySet()) {
            simpleShapesCache.put(entry.getKey(), modelManager.getModel(entry.getValue()));
        }
    }
}
