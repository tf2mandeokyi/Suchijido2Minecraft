package com.mndk.kvm2m.mod.event;

import java.util.ArrayList;
import java.util.List;

import com.mndk.kvm2m.mod.KVectorMap2MinecraftMod;
import com.mndk.kvm2m.mod.task.MapGeneratorTask;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@Mod.EventBusSubscriber()
public class ServerTickRepeater {

	private static final List<MapGeneratorTask> tasks = new ArrayList<>();
	
	public static void addTask(MapGeneratorTask task) {
		synchronized(tasks) {
			tasks.add(task);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onServerTick(ServerTickEvent event) {
		synchronized(tasks) {
			if(!tasks.isEmpty()) {
				MapGeneratorTask task = tasks.get(0);
				KVectorMap2MinecraftMod.broadcastMessage(task.getBroadcastMessage());
				task.doTask();
				tasks.remove(0);
			}
			else {
				KVectorMap2MinecraftMod.broadcastMessage("§dDone!");
			}
		}
	}
	
}
