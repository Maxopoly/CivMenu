package vg.civcraft.mc.civmenu;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.civmenu.database.TOSManager;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;

public class TOSListener implements Listener {

	private CivMenu plugin = CivMenu.getInstance();
	private Map<UUID, Location> locations;
	
	public TOSListener() {
		locations = new ConcurrentHashMap<UUID, Location>();
	}
	
	@EventHandler
	public void playerJoinEvent(PlayerJoinEvent event) {
		final Player p = event.getPlayer();
		if (!TOSManager.isTermPlayer(p, "CivMenu Agreement")) {
			sendTOS(p);
			locations.put(p.getUniqueId(), p.getLocation());
			new BukkitRunnable() {
				
				@Override
				public void run() {
					locations.remove(p.getUniqueId());
					if(!TOSManager.isTermPlayer(p, "CivMenu Agreement")){
						p.kickPlayer(ChatColor.DARK_RED+plugin.getConfig().getString("terms.kickMessage", "You must accept the terms using /sign in order to play."));
					}
					
				}
			}.runTaskLater(this.plugin, plugin.getConfig().getInt("terms.kickDelay", 1200));
		}
	}
	
	@EventHandler
	public void playerRespawnEvent(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		if (!TOSManager.isTermPlayer(p, "CivMenu Agreement")) {
			locations.put(p.getUniqueId(), e.getRespawnLocation());
		}
	}
	
	@EventHandler
	public void playerMoveEvent(PlayerMoveEvent event) {
		if (event.getFrom().getBlockX() == event.getTo().getBlockX()
				&& event.getFrom().getBlockY() == event.getTo().getBlockY()
				&& event.getFrom().getBlockZ() == event.getTo().getBlockZ()
				&& event.getFrom().getWorld().equals(event.getTo().getWorld())) {
			// Player didn't move by at least one block.
			return;
		}
		
		Player p = event.getPlayer();
		if (!TOSManager.isTermPlayer(p, "CivMenu Agreement")) {
			Location from = locations.get(p.getUniqueId());
			if (from == null){
				from = event.getFrom();
				locations.put(p.getUniqueId(), from);
			}
			if(event.getTo().distance(from) > plugin.getConfig().getInt("terms.MovementRange", 15)){
				p.sendMessage(ChatColor.RED + "You must accept the terms in order to play.");
				sendTOS(p);
				event.setTo(from);
			}
		}
	}
	
	public void sendTOS(Player p) {
		FileConfiguration config = CivMenu.getInstance().getConfig();
		Menu menu = new Menu();
		TextComponent welcome = new TextComponent(config.getString("terms.title.title", "Welcome to Civcraft!"));
		welcome.setColor(ChatColor.YELLOW);
		menu.setTitle(welcome);

		TextComponent message = new TextComponent(config.getString("terms.message", "You need to agree to the TOS in chat"));
		message.setColor(ChatColor.AQUA);
		menu.setSubTitle(message);

		TextComponent link = new TextComponent(config.getString("terms.linkMessage"));
		link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, config.getString("terms.link")));
		link.setItalic(true);
		menu.addPart(link);
		
		TextComponent confirm = new TextComponent(config.getString("terms.confirm"));
		confirm.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/sign"));
		confirm.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/sign").create()));
		confirm.setItalic(true);
		menu.addPart(confirm);
		
		menu.sendPlayer(p);
		
		Title title = new Title(config.getString("terms.title.title"), config.getString("terms.subtitle"),
				config.getInt("terms.title.fadeIn", 20), config.getInt("terms.title.stay", 20),
				config.getInt("terms.title.fadeOut", 20));
		title.sendTitle(p);
	}
}