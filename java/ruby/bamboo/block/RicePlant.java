package ruby.bamboo.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import ruby.bamboo.core.DataManager;
import ruby.bamboo.core.init.BambooData.BambooBlock;
import ruby.bamboo.core.init.EnumCreateTab;
import ruby.bamboo.item.RiceSeed;
import ruby.bamboo.item.Straw;

@BambooBlock(createiveTabs = EnumCreateTab.NONE)
public class RicePlant extends GrowableBase {

    @Override
    public Item getSeed() {
        return DataManager.getItem(RiceSeed.class);
    }

    @Override
    public Item getProduct() {
        return DataManager.getItem(Straw.class);
    }

    @Override
    public int getMaxGrowthStage() {
        return 4;
    }

    @Override
    public boolean canPlaceBlockOn(Block block) {
        return block == Blocks.FARMLAND;
    }

    @Override
    public float getGrowRate(Block block, World world, BlockPos pos) {
        return super.getGrowRate(block, world, pos);
    }

    @Override
    public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
        return EnumPlantType.Crop;
    }

    @Override
    List<ItemStack> extraDrop(IBlockAccess world, BlockPos pos, IBlockState state, int fortune, List<ItemStack> ret, int age) {
        if (age >= this.getMaxGrowthStage()) {
            // 最大成長の場合、1個は保証される
            ret.add(new ItemStack(this.getSeed(), 1, 0));
        }
        return ret;
    }
}
