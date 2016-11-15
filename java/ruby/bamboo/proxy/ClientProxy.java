package ruby.bamboo.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLLog;
import ruby.bamboo.api.BambooItems;
import ruby.bamboo.block.ICustomState;
import ruby.bamboo.block.decoration.DecorationClientFactory;
import ruby.bamboo.core.client.KeyBindFactory;
import ruby.bamboo.core.init.BambooData.BambooBlock.StateIgnore;
import ruby.bamboo.core.init.EntityRegister;
import ruby.bamboo.item.itemblock.IEnumTex;
import ruby.bamboo.item.itemblock.ISubTexture;
import ruby.bamboo.texture.TextureHelper;

/**
 * クライアントプロクシ
 *
 * @author Ruby
 *
 */
public class ClientProxy extends CommonProxy {

    Map<ModelResourceLocation, Item> modelMap = Maps.newHashMap();

    @Override
    public void preInit() {
        super.preInit();
        this.registJson();
        new DecorationClientFactory().register();
        // えんてぃてぃれんだー
        new EntityRegister().renderRegist();
        KeyBindFactory.preInit();
    }

    @Override
    public void init() {
        super.init();
        KeyBindFactory.init();
        MinecraftForge.EVENT_BUS.register(BambooItems.BAMBOO_BOW);
        registColors();

        new TextureHelper(modelMap);
    }

    @Override
    public void postInit() {
        super.postInit();
        //多分使わないのでクリア
        registedList = null;
    }

    /**
     * json登録の自動化
     *
     * setCustomModelResourceLocationの登録ファイル名を変更
     *
     * thx PR defeatedcrow
     */
    private void registJson() {
        List<ItemStack> isList = new ArrayList<ItemStack>();
        List<String> tmpNameList = new ArrayList<String>();
        for (String name : registedList) {
            Item item = Item.getByNameOrId(name);
            isList.clear();
            item.getSubItems(item, item.getCreativeTab(), isList);
            Block block = Block.getBlockFromName(name);
            this.setIgnoreState(block);
            this.setCustomState(block);
            if (item instanceof ISubTexture) {

                //                List<ResourceLocation> locList = Lists.newArrayList();
                for (IEnumTex tex : ((ISubTexture) item).getName()) {
                    String jsonName = tex.getJsonName();
                    //ModelBakery.addVariantName(item, jsonName);
                    //                    locList.add(new ResourceLocation(jsonName));
                    ModelResourceLocation mrl = new ModelResourceLocation(jsonName, "inventory");

                    ModelLoader.setCustomModelResourceLocation(item, tex.getId(), mrl);
                    modelMap.put(mrl, item);
                }
                //                ModelBakery.registerItemVariants(item, locList.toArray(new ResourceLocation[0]));
            } else {
                for (int i = 0; i < isList.size(); i++) {
                    ModelResourceLocation mrl = new ModelResourceLocation(name, "inventory");
                    ModelLoader.setCustomModelResourceLocation(item, i, mrl);
                    modelMap.put(mrl, item);
                }
            }

        }
    }

    /**
     * カスタムstate設定
     *
     * @param block
     */
    private <T> void setCustomState(T obj) {
        if (obj instanceof ICustomState) {
            try {
                IStateMapper state = (IStateMapper) ((ICustomState) obj).getCustomState();
                ModelLoader.setCustomStateMapper((Block) obj, state);
            } catch (Exception e) {
                FMLLog.warning(obj.getClass().getName() + ": Custom State Error");
            }
        }
    }

    /**
     * stateをmodel参照時無視する
     *
     * @param <T>
     */
    private <T> void setIgnoreState(T obj) {
        Method method = this.getMethod(obj, StateIgnore.class);

        if (method != null) {
            try {
                IProperty[] prop = (IProperty[]) method.invoke(obj);
                if (prop != null) {
                    ModelLoader.setCustomStateMapper((Block) obj, (new StateMap.Builder()).ignore(prop).build());
                }
            } catch (Exception e) {
                FMLLog.warning(obj.getClass().getName() + "Ignore State Error");
            }
        }
    }

    /**
     * アノテーション付きメソッド探索
     *
     * @param obj
     * @param ano
     * @return
     */
    private <T> Method getMethod(T obj, Class<? extends Annotation> ano) {
        if (obj == null) {
            return null;
        }
        Method method = null;
        for (Method e : obj.getClass().getDeclaredMethods()) {
            if (e.getAnnotation(ano) != null) {
                method = e;
                break;
            }
        }
        return method;
    }

    private void registColors() {
        List<Block> colorBlockList = registedList.stream().map(Block::getBlockFromName).filter(ins -> ins instanceof IBlockColor).collect(Collectors.toList());
        List<Item> colorItemList = registedList.stream().map(Item::getByNameOrId).filter(ins -> ins instanceof IItemColor).collect(Collectors.toList());

        colorBlockList.forEach(colorBlock -> Minecraft.getMinecraft().getBlockColors().registerBlockColorHandler((IBlockColor) colorBlock, colorBlock));
        //colorBlockList.forEach(colorBlock -> Minecraft.getMinecraft().getItemColors().registerItemColorHandler((IItemColor) colorBlock, Item.getItemFromBlock(colorBlock)));
        colorItemList.forEach(colorItem -> Minecraft.getMinecraft().getItemColors().registerItemColorHandler((IItemColor) colorItem, colorItem));

    }
}
