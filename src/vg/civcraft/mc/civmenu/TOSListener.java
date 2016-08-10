package vg.civcraft.mc.civmenu;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import vg.civcraft.mc.civmenu.database.TOSManager;
import vg.civcraft.mc.civmodcore.Config;
import vg.civcraft.mc.civmodcore.annotations.CivConfig;
import vg.civcraft.mc.civmodcore.annotations.CivConfigType;
import vg.civcraft.mc.civmodcore.annotations.CivConfigs;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;

public class TOSListener implements Listener {

	private CivMenu plugin = CivMenu.getInstance();
	private Map<UUID, Location> locations;
	private Config config = plugin.GetConfig();
	
	public TOSListener() {
		locations = new ConcurrentHashMap<UUID, Location>();
	}
	
	@CivConfigs({
		@CivConfig(name = "terms.kickDelay", def = "1200", type = CivConfigType.Int),
		@CivConfig(name = "terms.kickMessage", def = "You must accept the terms using /sign in order to play.", type = CivConfigType.String)
	})
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
						p.kickPlayer(ChatColor.DARK_RED+plugin.GetConfig().get("terms.kickMessage").getString());
					}
					
				}
			}.runTaskLater(this.plugin, plugin.GetConfig().get("terms.kickDelay").getInt());
		}
	}
	
	@EventHandler
	public void playerRespawnEvent(PlayerRespawnEvent e) {
		Player p = e.getPlayer();
		if (!TOSManager.isTermPlayer(p, "CivMenu Agreement")) {
			locations.put(p.getUniqueId(), e.getRespawnLocation());
		}
	}
	
	@CivConfig(name = "terms.MovementRange", def = "15", type = CivConfigType.Int)
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
			if(event.getTo().distance(from) > plugin.GetConfig().get("terms.MovementRange").getInt()){
				p.sendMessage(ChatColor.RED + "You must accept the terms in order to play.");
				sendTOS(p);
				event.setTo(from);
			}
		}
	}
	
	@CivConfigs({
		@CivConfig(name = "terms.title.title" , def = "Welcome to Civcraft!", type = CivConfigType.String),
		@CivConfig(name = "terms.subtitle" , def = "You need to agree to the TOS in chat", type = CivConfigType.String),
		@CivConfig(name = "terms.message", def = "Civcraft is an experiment for communities, political ideologies, debate and discussion. Before you can start playing and join a civilization you must first read and agree to the terms of service", type = CivConfigType.String),
		@CivConfig(name = "terms.link", def = "http://www.google.com", type = CivConfigType.String),
		@CivConfig(name = "terms.linkMessage" , def = "You can click this message to open up the terms of service.", type = CivConfigType.String),
		@CivConfig(name = "terms.confirm", def = "Once you've read it, you can click this message to agree to the terms", type = CivConfigType.String),
		@CivConfig(name = "terms.title.fadeIn" , def = "20", type = CivConfigType.Int),
		@CivConfig(name = "terms.title.stay" , def = "200", type = CivConfigType.Int),
		@CivConfig(name = "terms.title.fadeOut" , def = "20", type = CivConfigType.Int)
	})
	public void sendTOS(Player p) {

		Menu menu = new Menu();

		TextComponent welcome = new TextComponent(config.get("terms.title.title").getString());
		welcome.setColor(ChatColor.YELLOW);
		menu.setTitle(welcome);

		TextComponent message = new TextComponent(plugin.GetConfig().get("terms.message").getString());
		message.setColor(ChatColor.AQUA);
		menu.setSubTitle(message);

		TextComponent link = new TextComponent(plugin.GetConfig().get("terms.linkMessage").getString());
		link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, plugin.GetConfig().get("terms.link").getString()));
		link.setItalic(true);
		menu.addPart(link);
		
		TextComponent confirm = new TextComponent(plugin.GetConfig().get("terms.confirm").getString());
		confirm.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,"/sign"));
		confirm.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("/sign").create()));
		confirm.setItalic(true);
		menu.addPart(confirm);
		
		menu.sendPlayer(p);
		
		Title title = new Title(config.get("terms.title.title").getString(), config.get("terms.subtitle").getString(),
				config.get("terms.title.fadeIn").getInt(), config.get("terms.title.stay").getInt(),
				config.get("terms.title.fadeOut").getInt());
		title.sendTitle(p);
	}
}