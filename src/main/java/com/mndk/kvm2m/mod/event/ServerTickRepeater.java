package com.mndk.kvm2m.mod.event;

import com.mndk.kvm2m.mod.task.VMapGeneratorTask;
import lombok.RequiredArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
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

	@Deprecated
	public static void addTask(VMapGeneratorTask task) {}

	public static void addTask(BlockTask task) {
		synchronized(blockTasks) {
			blockTasks.addLast(task);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onServerTick(ServerTickEvent event) {

		synchronized (blockTasks) {
			for(int i = 0; i < 2000; ++i) {
				if(blockTasks.isEmpty()) return;
				BlockTask task = blockTasks.getFirst();
				task.world.setBlockState(task.pos, task.blockState);
				blockTasks.removeFirst();
			}
		}
	}
	
}
