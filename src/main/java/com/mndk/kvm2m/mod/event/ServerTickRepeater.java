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
	
	private static boolean alreadySentDoneMessage = true;
	
	public static void addTask(VMapGeneratorTask task) {
		synchronized(tasks) {
			tasks.add(task);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onServerTick(ServerTickEvent event) {
		synchronized(tasks) {
			if(!tasks.isEmpty()) {
				alreadySentDoneMessage = false;
				VMapGeneratorTask task = tasks.get(0);
				KVectorMap2MinecraftMod.broadcastMessage(task.getBroadcastMessage());
				task.doTask();
				tasks.remove(0);
			}
			else {
				if(!alreadySentDoneMessage) {
					KVectorMap2MinecraftMod.broadcastMessage("§dDone!");
					System.gc();
					alreadySentDoneMessage = true;
				}
			}
		}
	}
	
}
