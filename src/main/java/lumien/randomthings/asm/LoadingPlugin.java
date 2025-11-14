package lumien.randomthings.asm;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

@IFMLLoadingPlugin.SortingIndex(1001)
public class LoadingPlugin implements IFMLLoadingPlugin
{
	public static boolean IN_MCP = false;

	public LoadingPlugin() {
		MixinBootstrap.init();
		Mixins.addConfiguration("quantumthings.mixins.json");
	}

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[] { ClassTransformer.class.getName() };
	}

	@Override
	public String getModContainerClass()
	{
		return null;
	}

	@Override
	public String getSetupClass()
	{
		return null;
	}

	@Override
	public void injectData(Map<String, Object> data)
	{
		IN_MCP = !(Boolean) data.get("runtimeDeobfuscationEnabled");
	}

	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}

}
