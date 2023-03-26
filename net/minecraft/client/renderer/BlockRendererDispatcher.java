package net.minecraft.client.renderer;

import com.google.common.collect.Sets;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.WeightedBakedModel;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;

import java.util.Set;

public class BlockRendererDispatcher implements IResourceManagerReloadListener {
    private final BlockModelShapes blockModelShapes;
    private final GameSettings gameSettings;
    private final BlockModelRenderer blockModelRenderer = new BlockModelRenderer();
    private final ChestRenderer chestRenderer = new ChestRenderer();
    private final BlockFluidRenderer fluidRenderer = new BlockFluidRenderer();

    private static final Set<Block> FoliageBlocks = Sets.newHashSet(
            Blocks.tallgrass,
            Blocks.double_plant,
            Blocks.red_flower,
            Blocks.yellow_flower
    );

    public BlockRendererDispatcher(BlockModelShapes blockModelShapesIn, GameSettings gameSettingsIn) {
        blockModelShapes = blockModelShapesIn;
        gameSettings = gameSettingsIn;
    }

    public BlockModelShapes getBlockModelShapes() {
        return blockModelShapes;
    }

    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
        Block block = state.getBlock();
        int i = block.getRenderType();

        if (i == 3) {
            state = block.getActualState(state, blockAccess, pos);
            IBakedModel ibakedmodel = blockModelShapes.getModelForState(state);
            IBakedModel ibakedmodel1 = (new SimpleBakedModel.Builder(ibakedmodel, texture)).makeBakedModel();
            blockModelRenderer.renderModel(blockAccess, ibakedmodel1, state, pos, Tessellator.getInstance().getWorldRenderer());
        }
    }

    public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, WorldRenderer worldRendererIn) {
        try {
            int i = state.getBlock().getRenderType();

            if (FoliageBlocks.contains(state.getBlock())) {
                return false;
            }

            if (i == -1) {
                return false;
            } else {
                switch (i) {
                    case 1 -> {
                        return fluidRenderer.renderFluid(blockAccess, state, pos, worldRendererIn);
                    }
                    case 3 -> {
                        IBakedModel ibakedmodel = getModelFromBlockState(state, blockAccess, pos);
                        return blockModelRenderer.renderModel(blockAccess, ibakedmodel, state, pos, worldRendererIn);
                    }
                    case 2, default -> {
                        return false;
                    }
                }
            }
        } catch (Throwable throwable) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
            CrashReportCategory.addBlockInfo(crashreportcategory, pos, state.getBlock(), state.getBlock().getMetaFromState(state));
            throw new ReportedException(crashreport);
        }
    }

    public BlockModelRenderer getBlockModelRenderer() {
        return blockModelRenderer;
    }

    private IBakedModel getBakedModel(IBlockState state) {
        return blockModelShapes.getModelForState(state);
    }

    public IBakedModel getModelFromBlockState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        Block block = state.getBlock();

        if (worldIn.getWorldType() != WorldType.DEBUG_WORLD) {
            try {
                state = block.getActualState(state, worldIn, pos);
            } catch (Exception ignored) {
            }
        }

        IBakedModel ibakedmodel = blockModelShapes.getModelForState(state);

        if (pos != null && gameSettings.allowBlockAlternatives && ibakedmodel instanceof WeightedBakedModel) {
            ibakedmodel = ((WeightedBakedModel) ibakedmodel).getAlternativeModel(MathHelper.getPositionRandom(pos));
        }

        return ibakedmodel;
    }

    public void renderBlockBrightness(IBlockState state, float brightness) {
        int i = state.getBlock().getRenderType();

        if (i != -1) {
            switch (i) {
                case 1, 2 -> chestRenderer.renderChestBrightness(state.getBlock(), brightness);
                case 3 -> {
                    IBakedModel ibakedmodel = getBakedModel(state);
                    blockModelRenderer.renderModelBrightness(ibakedmodel, state, brightness, true);
                }
            }
        }
    }

    public boolean isRenderTypeChest(Block p_175021_1_) {
        if (p_175021_1_ == null) {
            return false;
        } else {
            int i = p_175021_1_.getRenderType();
            return i == 2;
        }
    }

    public void onResourceManagerReload(IResourceManager resourceManager) {
        fluidRenderer.initAtlasSprites();
    }
}
