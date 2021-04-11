package com.mndk.kvm2m.mod.event;

import java.util.ArrayList;
import java.util.List;

import com.mndk.kvm2m.mod.KVectorMap2MinecraftMod;
import com.mndk.kvm2m.mod.task.VMapGeneratorTask;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@Mod.EventBusSubscriber()
public class ServerTickRepeater {

	private static final List<VMapGeneratorTask> tasks = new ArrayList<>();
	private static int size = 0, current = 0, lastPercent = -1;
	
	private static boolean alreadySentDoneMessage = true;
	
	public static void addTask(VMapGeneratorTask task) {
		synchronized(tasks) {
			tasks.add(task);
			size += task.getSize();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onServerTick(ServerTickEvent event) {
		synchronized(tasks) {
			if(!tasks.isEmpty()) {
				alreadySentDoneMessage = false;
				VMapGeneratorTask task = tasks.get(0);
				String msg = task.getBroadcastMessage();
				if(msg != null) KVectorMap2MinecraftMod.broadcastMessage(msg);
				task.doTask();
				current += task.getSize();
				tasks.remove(0);
				
				int currentPercent = (int) Math.floor(current / (double) size * 100);
				if(lastPercent != currentPercent) {
					KVectorMap2MinecraftMod.broadcastMessage("§d" + currentPercent + "% Done.");
					lastPercent = currentPercent;
				}
			}
			else {
				if(!alreadySentDoneMessage) {
					KVectorMap2MinecraftMod.broadcastMessage("§dDone!");
					System.gc();
					tasks.clear();
					alreadySentDoneMessage = true;
					size = 0; current = 0;
				}
			}
		}
	}
	
}
