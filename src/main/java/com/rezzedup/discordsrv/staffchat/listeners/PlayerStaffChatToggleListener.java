/*
 * The MIT License
 * Copyright © 2017-2026 RezzedUp and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.rezzedup.discordsrv.staffchat.listeners;

import com.rezzedup.discordsrv.staffchat.ChatChannel;
import com.rezzedup.discordsrv.staffchat.StaffChatPlugin;
import com.rezzedup.discordsrv.staffchat.config.StaffChatConfig;
import com.rezzedup.discordsrv.staffchat.events.AutoStaffChatToggleEvent;
import com.rezzedup.discordsrv.staffchat.events.ReceivingStaffChatToggleEvent;
import community.leaf.eventful.bukkit.CancellationPolicy;
import community.leaf.eventful.bukkit.ListenerOrder;
import community.leaf.eventful.bukkit.annotations.CancelledEvents;
import community.leaf.eventful.bukkit.annotations.EventListener;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import pl.tlinkowski.annotation.basic.NullOr;

@SuppressWarnings("unused")
public class PlayerStaffChatToggleListener implements Listener {
	private final StaffChatPlugin plugin;
	
	public PlayerStaffChatToggleListener(StaffChatPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventListener(ListenerOrder.FIRST)
	public void onAutomaticChatFirst(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		
		for (ChatChannel channel : ChatChannel.values()) {
			if (plugin.data().isAutomaticChatEnabled(player, channel)) {
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventListener(ListenerOrder.MONITOR)
	public void onAutomaticChatMonitor(AsyncPlayerChatEvent event) {
		Player player = event.getPlayer();
		@NullOr ChatChannel channel = null;
		
		for (ChatChannel option : ChatChannel.values()) {
			if (plugin.data().isAutomaticChatEnabled(player, option)) {
				channel = option;
				break;
			}
		}
		
		if (channel == null) {
			return;
		}
		final ChatChannel activeChannel = channel;
		
		event.setCancelled(true);
		
		if (activeChannel.permission().allows(player)) {
			plugin.debug(getClass()).log(event, () ->
				"Player " + player.getName() + " has automatic " + activeChannel.displayName() + " enabled"
			);
			
			plugin.sync().run(() -> plugin.submitMessageFromPlayer(event.getPlayer(), event.getMessage(), activeChannel));
		} else {
			plugin.debug(getClass()).log(event, () ->
				"Player " + player.getName() + " has automatic " + activeChannel.displayName() + " enabled " +
					"but they don't have permission to use that chat"
			);
			
			plugin.sync().run(() -> {
				plugin.data().updateProfile(player);
				player.chat(event.getMessage());
			});
		}
	}
	
	@EventListener(ListenerOrder.LAST)
	@CancelledEvents(CancellationPolicy.REJECT)
	public void onToggleAutoChat(AutoStaffChatToggleEvent event) {
		@NullOr Player player = event.getProfile().toPlayer().orElse(null);
		
		plugin.debug(getClass()).log(event, () -> {
			String name = (player == null) ? "<Offline>" : player.getName();
			String enabled = (event.isEnablingAutomaticChat()) ? "Enabled" : "Disabled";
			return enabled + " automatic " + event.getChannel().displayName() +
				" for player: " + name + " (" + event.getProfile().uuid() + ")";
		});
		
		if (player == null || event.isQuiet()) {
			return;
		}
		
		if (event.isEnablingAutomaticChat()) {
			plugin.messages().notifyAutoChatEnabled(player, event.getChannel());
		} else {
			plugin.messages().notifyAutoChatDisabled(player, event.getChannel());
		}
	}
	
	@EventListener(ListenerOrder.EARLY)
	@CancelledEvents(CancellationPolicy.REJECT)
	public void onLeavingStaffChatIsDisabled(ReceivingStaffChatToggleEvent event) {
		if (event.isJoiningStaffChat()) {
			return;
		}
		if (plugin.config().getOrDefault(StaffChatConfig.LEAVING_STAFFCHAT_ENABLED)) {
			return;
		}
		
		// Leaving is disabled, cancel the event.
		event.setCancelled(true);
		
		@NullOr Player player = event.getProfile().toPlayer().orElse(null);
		
		plugin.debug(getClass()).log(event, () -> {
			String name = (player == null) ? "<Offline>" : player.getName();
			return "Player: " + name + " (" + event.getProfile().uuid() + ") " +
				"tried to leave the " + event.getChannel().displayName() + ", but leaving is disabled in the config";
		});
		
		if (player == null || event.isQuiet()) {
			return;
		}
		
		plugin.messages().notifyLeavingChatIsDisabled(player, event.getChannel());
	}
	
	@EventListener(ListenerOrder.LAST)
	@CancelledEvents(CancellationPolicy.REJECT)
	public void onToggleReceivingMessages(ReceivingStaffChatToggleEvent event) {
		@NullOr Player player = event.getProfile().toPlayer().orElse(null);
		
		plugin.debug(getClass()).log(event, () -> {
			String name = (player == null) ? "<Offline>" : player.getName();
			String left = (event.isLeavingStaffChat()) ? "left" : "joined";
			return "Player: " + name + " (" + event.getProfile().uuid() + ") " + left + " the " + event.getChannel().displayName();
		});
		
		if (player == null || event.isQuiet()) {
			return;
		}
		
		boolean broadcastToEveryone =
			event.getProfile().sinceLeftStaffChat(event.getChannel()).isPresent() != event.isLeavingStaffChat();
		
		if (event.isLeavingStaffChat()) {
			plugin.messages().notifyLeaveChat(player, event.getChannel(), broadcastToEveryone);
		} else {
			plugin.messages().notifyJoinChat(player, event.getChannel(), broadcastToEveryone);
		}
	}
}
