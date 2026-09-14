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
package com.rezzedup.discordsrv.staffchat;

import com.rezzedup.discordsrv.staffchat.listeners.DiscordStaffChatListener;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePreProcessEvent;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

public class DiscordBridge {
	private static final long CHANNEL_WARNING_INTERVAL_MS = 60_000L;
	
	private final StaffChatPlugin plugin;
	private final Map<ChatChannel, TextChannel> channels = new EnumMap<>(ChatChannel.class);
	private final Map<ChatChannel, Long> lastChannelWarnings = new EnumMap<>(ChatChannel.class);
	
	private @NullOr DiscordStaffChatListener listener;
	private DiscordHookState state = DiscordHookState.UNAVAILABLE;
	
	DiscordBridge(StaffChatPlugin plugin) {
		this.plugin = plugin;
	}
	
	public DiscordHookState state() {
		return state;
	}
	
	public boolean isReady() {
		return state == DiscordHookState.READY || state == DiscordHookState.DEGRADED;
	}
	
	public void subscribe(Plugin discordSrvPlugin) {
		state = DiscordHookState.INITIALIZING;
		
		if (!StaffChatPlugin.DISCORDSRV.equals(discordSrvPlugin.getName()) || !(discordSrvPlugin instanceof DiscordSRV)) {
			state = DiscordHookState.FAILED;
			throw plugin.debug(getClass()).failure("Subscribe", new IllegalArgumentException("Not DiscordSRV: " + discordSrvPlugin));
		}
		
		if (listener != null) {
			plugin.debug(getClass()).log("Subscribe", () -> "DiscordSRV listener is already subscribed");
			refreshChannels();
			return;
		}
		
		listener = new DiscordStaffChatListener(plugin);
		DiscordSRV.api.subscribe(listener);
		refreshChannels();
		plugin.getLogger().info("Subscribed to DiscordSRV: messages will be sent to Discord");
	}
	
	public void unsubscribe() {
		if (listener == null) {
			state = DiscordHookState.UNAVAILABLE;
			clearChannels();
			return;
		}
		
		try {
			DiscordSRV.api.unsubscribe(listener);
		} catch (Throwable exception) {
			plugin.debug(getClass()).logException("Unsubscribe", exception);
		} finally {
			listener = null;
			state = DiscordHookState.UNAVAILABLE;
			clearChannels();
		}
	}
	
	public void refreshChannels() {
		if (listener == null) {
			state = DiscordHookState.UNAVAILABLE;
			clearChannels();
			return;
		}
		
		clearChannels();
		
		for (ChatChannel channel : ChatChannel.values()) {
			resolve(channel).ifPresent(resolved -> channels.put(channel, resolved));
		}
		
		state = (channels.size() == ChatChannel.values().length)
			? DiscordHookState.READY
			: DiscordHookState.DEGRADED;
	}
	
	public @NullOr TextChannel getChannelOrNull(ChatChannel channel) {
		return channels.get(channel);
	}
	
	public boolean handleDiscordMessage(DiscordGuildMessagePreProcessEvent event) {
		if (!isReady()) {
			return false;
		}
		
		Message message = event.getMessage();
		User author = event.getAuthor();
		
		if (author.isBot() || message.isWebhookMessage()) {
			return false;
		}
		
		for (Map.Entry<ChatChannel, TextChannel> entry : channels.entrySet()) {
			if (event.getChannel().equals(entry.getValue())) {
				event.setCancelled(true);
				plugin.sync().run(() -> plugin.submitMessageFromDiscord(author, message, entry.getKey()));
				return true;
			}
		}
		
		return false;
	}
	
	public void sendConsoleMessage(ChatChannel chatChannel, String message) {
		sendMessage(chatChannel, channel ->
			DiscordUtil.queueMessage(channel, sanitizeDiscordMentions(message), true)
		);
	}
	
	public void sendPlayerMessage(Player author, ChatChannel chatChannel, String message) {
		sendMessage(chatChannel, channel -> plugin.async().run(() -> {
			if (!plugin.isEnabled()) {
				return;
			}
			
			try {
				DiscordSRV.getPlugin().processChatMessage(
					author,
					sanitizeDiscordMentions(message),
					chatChannel.discordChannelName(),
					false
				);
			} catch (Throwable exception) {
				state = DiscordHookState.DEGRADED;
				plugin.debug(getClass()).logException("Discord Relay", exception);
				plugin.getLogger().warning("Unable to relay " + chatChannel.displayName() + " to Discord: " + exception.getMessage());
			}
		}));
	}
	
	private void sendMessage(ChatChannel chatChannel, ChannelSender sender) {
		@NullOr TextChannel channel = getChannelOrNull(chatChannel);
		
		if (channel == null) {
			channel = resolve(chatChannel).orElse(null);
			
			if (channel == null) {
				warnMissingChannel(chatChannel);
				return;
			}
			
			channels.put(chatChannel, channel);
		}
		TextChannel resolvedChannel = channel;
		
		plugin.debug(getClass()).log(ChatService.MINECRAFT, "Message", () ->
			"Sending message to discord channel: " + chatChannel.discordChannelName() + " => " + resolvedChannel
		);
		
		try {
			sender.send(resolvedChannel);
		} catch (Throwable exception) {
			state = DiscordHookState.DEGRADED;
			plugin.debug(getClass()).logException("Discord Relay", exception);
			plugin.getLogger().warning("Unable to relay " + chatChannel.displayName() + " to Discord: " + exception.getMessage());
		}
	}
	
	private Optional<TextChannel> resolve(ChatChannel channel) {
		try {
			DiscordSRV discordSrv = DiscordSRV.getPlugin();
			@NullOr TextChannel resolved = discordSrv.getDestinationTextChannelForGameChannelName(channel.discordChannelName());
			
			if (resolved != null) {
				return Optional.of(resolved);
			}
			
			@NullOr String channelId = discordSrv.getChannels().get(channel.discordChannelName());
			if (channelId == null || channelId.isBlank()) {
				return Optional.empty();
			}
			
			return Optional.ofNullable(discordSrv.getJda().getTextChannelById(channelId));
		} catch (Throwable exception) {
			state = DiscordHookState.DEGRADED;
			plugin.debug(getClass()).logException("Channel Resolve", exception);
			return Optional.empty();
		}
	}
	
	private void warnMissingChannel(ChatChannel channel) {
		long now = System.currentTimeMillis();
		long last = lastChannelWarnings.getOrDefault(channel, 0L);
		
		if (now - last < CHANNEL_WARNING_INTERVAL_MS) {
			return;
		}
		
		lastChannelWarnings.put(channel, now);
		plugin.getLogger().warning(
			channel.label() + " DiscordSRV channel mapping '" + channel.discordChannelName() +
				"' could not be resolved. Add it to DiscordSRV's Channels config, then run /discord reload and /managestaffchat reload."
		);
	}
	
	private void clearChannels() {
		channels.clear();
		lastChannelWarnings.clear();
	}
	
	private String sanitizeDiscordMentions(String message) {
		return message
			.replace("@everyone", "@\u200beveryone")
			.replace("@here", "@\u200bhere");
	}
	
	@FunctionalInterface
	private interface ChannelSender {
		void send(TextChannel channel);
	}
}
