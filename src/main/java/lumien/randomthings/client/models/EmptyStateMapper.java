package lumien.randomthings.client.models;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.IStateMapper;

public class EmptyStateMapper implements IStateMapper
{

	@Override
	public Map putStateModelLocations(Block blockIn)
	{
		return Maps.newLinkedHashMap();
	}

}
