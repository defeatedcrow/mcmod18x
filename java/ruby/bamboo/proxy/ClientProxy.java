package ruby.bamboo.proxy;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.FMLLog;
import ruby.bamboo.core.DataManager;
import ruby.bamboo.core.init.BambooData.BambooBlock.StateIgnore;

/**
 * クライアントプロクシ
 * 
 * @author Ruby
 * 
 */
public class ClientProxy extends CommonProxy {

	@Override
	public void preInit() {
		super.preInit();
		this.registJson();
	}

	@Override
	public void init() {
		super.init();
	}

	/**
	 * json登録の自動化 1IDに対して複数タイプは名前の後ろに0(連番)付与
	 */
	private void registJson() {
		List<ItemStack> isList = new ArrayList<ItemStack>();
		List<String> tmpNameList = new ArrayList<String>();
		for (String name : DataManager.getRegstedNameArray()) {
			Item item = Item.getByNameOrId(name);
			isList.clear();
			item.getSubItems(item, item.getCreativeTab(), isList);
			this.setIgnoreState(DataManager.getBlock(DataManager.getClass(name)));

			if (isList.size() == 1) {
				ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(name, "inventory"));
			} else {
				// TODO:複数IDパターン要チェック
				for (int i = 0; i < isList.size(); i++) {
					ModelBakery.addVariantName(item, name + i);
					ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation(name + i, "inventory"));
				}
			}
		}
	}

	/**
	 * stateをmodel参照時無視する
	 * 
	 * @param <T>
	 */
	private <T> void setIgnoreState(T obj) {
		if (obj == null) {
			return;
		}
		Method method = null;
		for (Method e : obj.getClass().getDeclaredMethods()) {
			if (e.getAnnotation(StateIgnore.class) != null) {
				method = e;
				break;
			}
		}
		if (method != null) {
			try {
				IProperty[] prop = (IProperty[]) method.invoke(obj);
				if (prop != null) {
					ModelLoader.setCustomStateMapper((Block) obj, (new StateMap.Builder()).addPropertiesToIgnore(prop).build());
				}
			} catch (Exception e) {
				FMLLog.warning(obj.getClass().getName() + "Ignore State Error");
			}
		}
	}
}
