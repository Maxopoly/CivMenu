package vg.civcraft.mc.civmenu.guides;

import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import vg.civcraft.mc.civmenu.CivMenu;

public class UnloadDismissalsTask implements Runnable {

	private ResponseManager manager;
	private ConcurrentHashMap<UUID, Long> mru;
	private long delay = -1;
	
	public UnloadDismissalsTask(ResponseManager manager) {
		this.manager = manager;
		mru = new ConcurrentHashMap<UUID, Long>();
		delay = getUnloadDelay();
	}
	
	@Override
	public void run() {
		unloadCache();
	}
	
	public long getUnloadDelay() {
		if(delay < 0) {
			delay = CivMenu.getInstance().getConfig().getInt("unload_delay", 18000);
		}
		return delay;
	}
	
	public void unloadCache() {
		for (Entry <UUID, Long> entry : mru.entrySet()) {
			UUID id = entry.getKey();
			if(mru.get(id) < System.currentTimeMillis() - CivMenu.getInstance().getConfig().getInt("unload_delay", 18000)) {
				manager.unloadDismissals(id);
			}
		}
	}
	
	public void updateMRU(UUID id) {
		mru.put(id, System.currentTimeMillis());
	}
	
	public void removePlayer(UUID id) {
		mru.remove(id);
	}
}
