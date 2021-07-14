package com.mndk.kvm2m.mod.event;

import lombok.RequiredArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

import java.util.LinkedList;

@Mod.EventBusSubscriber()
public class ServerTickRepeater {

	private static final LinkedList<BlockTask> blockTasks = new LinkedList<>();

	@RequiredArgsConstructor
	public static class BlockTask {
		public final World world;
		public final BlockPos pos;
		public final IBlockState blockState;
	}

	public static void addTask(BlockTask task) {
		synchronized(blockTasks) {
			blockTasks.addLast(task);
		}
	}
	
	@SubscribeEvent
	public static void onServerTick(ServerTickEvent event) {

		for (int i = 0; i < 2000; ++i) {
			BlockTask task;
			synchronized (blockTasks) {
				if (blockTasks.isEmpty()) break;
				task = blockTasks.getFirst();
				blockTasks.removeFirst();
			}
			task.world.setBlockState(task.pos, task.blockState);
		}
	}
	
}
