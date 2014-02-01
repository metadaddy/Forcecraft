package us.forcecraft;

import java.util.EnumSet;

import us.forcecraft.ForcecraftGenerator.BlockCollector;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class ForcecraftTickHandler implements ITickHandler {
	EnumSet<TickType> ticks = EnumSet.of(TickType.SERVER);
	private BlockCollector collector;
	
	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (collector != null){
			if (collector.blocks.size() > 0) {
				collector.replayBlocks(20);
			} else {
				collector.replayEntities();
				collector = null;
			}				
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {
	}

	@Override
	public EnumSet<TickType> ticks() {
		return ticks;
	}

	@Override
	public String getLabel() {
		return this.getClass().getName();
	}

	public void setCollector(BlockCollector collector) {
		this.collector = collector;
	}

}
