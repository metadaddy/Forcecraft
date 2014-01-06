package us.forcecraft;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManagerHell;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;

public class ForcecraftWorldProvider extends WorldProvider {
	@Override
	public String getDimensionName()
	{
		return "Forcecraft";
	}

	public void registerWorldChunkManager()
	{
		this.worldChunkMgr = new WorldChunkManagerHell(BiomeGenBase.plains, 0.5F, 0.1F);
		this.dimensionId = Forcecraft.dimensionId;
	}    

	public IChunkProvider createChunkGenerator()
	{
		return new ForcecraftChunkProvider(worldObj, worldObj.getSeed(), true);
	}

	public ChunkCoordinates getSpawnPoint()
	{
		return new ChunkCoordinates(0, Forcecraft.groundLevel+1, 0);
	}
}
