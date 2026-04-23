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

import com.rezzedup.discordsrv.staffchat.config.MessagesConfig;
import com.rezzedup.discordsrv.staffchat.events.ConsoleStaffChatMessageEvent;
import com.rezzedup.discordsrv.staffchat.events.DiscordStaffChatMessageEvent;
import com.rezzedup.discordsrv.staffchat.events.PlayerStaffChatMessageEvent;
import com.rezzedup.discordsrv.staffchat.util.MappedPlaceholder;
import com.rezzedup.discordsrv.staffchat.util.Strings;
import github.scarsz.discordsrv.DiscordSRV;
import github.scarsz.discordsrv.dependencies.emoji.EmojiParser;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Member;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.Role;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import github.scarsz.discordsrv.util.DiscordUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

public class MessageProcessor {
	private final StaffChatPlugin plugin;
	
	MessageProcessor(StaffChatPlugin plugin) {
		this.plugin = plugin;
	}
	
	private void sendFormattedChatMessage(
		@NullOr Object author,
		ChatChannel channel,
		String format,
		MappedPlaceholder placeholders
	) {
		if (Strings.isEmptyOrNull(placeholders.get("message"))) {
			return;
		}
		
		String formatted = format;
		
		if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
			@NullOr Player player = (author instanceof Player) ? (Player) author : null;
			formatted = PlaceholderAPI.setPlaceholders(player, formatted);
		}
		
		String content = Strings.colorful(placeholders.update(formatted));
		
		if (author instanceof Player) {
			Player player = (Player) author;
			StaffChatProfile profile = plugin.data().getOrCreateProfile(player);
			
			if (!profile.receivesStaffChatMessages(channel)) {
				String reminder = Strings.colorful(placeholders.update(
					plugin.messages().getOrDefault(plugin.messages().leftChatReminder(channel)))
				);
				
				player.sendMessage(content);
				player.sendMessage(reminder);
				plugin.config().playNotificationSound(player, channel);
			}
		}
		
		plugin.onlineChatParticipants(channel).forEach(staff -> {
			staff.sendMessage(content);
			plugin.config().playMessageSound(staff, channel);
		});
		
		plugin.getServer().getConsoleSender().sendMessage(content);
	}
	
	private void sendToDiscord(ChatChannel chatChannel, Consumer<TextChannel> sender) {
		@NullOr TextChannel channel = plugin.getDiscordChannelOrNull(chatChannel);
		
		if (channel == null) {
			plugin.debug(getClass()).log(ChatService.MINECRAFT, "Message", () ->
				"Unable to send message to discord: " + chatChannel.discordChannelName() + " => null"
			);
			return;
		}
		
		plugin.debug(getClass()).log(ChatService.MINECRAFT, "Message", () ->
			"Sending message to discord channel: " + chatChannel.discordChannelName() + " => " + channel
		);
		
		sender.accept(channel);
	}
	
	public void processConsoleChat(String message, ChatChannel channel) {
		Objects.requireNonNull(message, "message");
		Objects.requireNonNull(channel, "channel");
		
		plugin.debug(getClass()).logConsoleChatMessage(message);
		
		ConsoleStaffChatMessageEvent event =
			plugin.events().call(new ConsoleStaffChatMessageEvent(channel, message));
		
		if (event.isCancelled() || event.getText().isEmpty()) {
			plugin.debug(getClass()).log(ChatService.MINECRAFT, event, () -> "Cancelled or text is empty");
			return;
		}
		
		MappedPlaceholder placeholders = plugin.messages().placeholders(channel);
		placeholders.map("message", "content", "text").to(event::getText);
		
		sendFormattedChatMessage(null, channel, plugin.messages().getInGameConsoleFormat(channel), placeholders);
		
		if (plugin.isDiscordSrvHookEnabled()) {
			String discordMessage = placeholders.update(
				plugin.messages().getDiscordConsoleFormat(channel)
			);
			
			sendToDiscord(channel, discord -> DiscordUtil.queueMessage(discord, discordMessage, true));
		} else {
			plugin.debug(getClass()).log(ChatService.MINECRAFT, "Message", () ->
				"DiscordSRV hook is not enabled, cannot send to discord"
			);
		}
	}
	
	public void processPlayerChat(Player author, String message, ChatChannel channel) {
		Objects.requireNonNull(author, "author");
		Objects.requireNonNull(message, "message");
		Objects.requireNonNull(channel, "channel");
		
		plugin.debug(getClass()).logPlayerChatMessage(author, message);
		
		PlayerStaffChatMessageEvent event =
			plugin.events().call(new PlayerStaffChatMessageEvent(author, channel, message));
		
		if (event.isCancelled() || event.getText().isEmpty()) {
			plugin.debug(getClass()).log(ChatService.MINECRAFT, event, () -> "Cancelled or text is empty");
			return;
		}
		
		MappedPlaceholder placeholders = plugin.messages().placeholders(author, channel);
		placeholders.map("message", "content", "text").to(event::getText);
		
		sendFormattedChatMessage(author, channel, plugin.messages().getInGamePlayerFormat(channel), placeholders);
		
		if (plugin.isDiscordSrvHookEnabled()) {
			sendToDiscord(channel, discord -> plugin.async().run(() ->
				DiscordSRV.getPlugin().processChatMessage(author, event.getText(), channel.discordChannelName(), false)
			));
		} else {
			plugin.debug(getClass()).log(ChatService.MINECRAFT, "Message", () ->
				"DiscordSRV hook is not enabled, cannot send to discord"
			);
		}
	}
	
	public void processDiscordChat(User author, Message message, ChatChannel channel) {
		Objects.requireNonNull(author, "author");
		Objects.requireNonNull(message, "message");
		Objects.requireNonNull(channel, "channel");
		
		plugin.debug(getClass()).logDiscordChatMessage(author, message);
		
		DiscordStaffChatMessageEvent event =
			plugin.events().call(new DiscordStaffChatMessageEvent(author, message, channel, message.getContentStripped()));
		
		if (event.isCancelled() || event.getText().isEmpty()) {
			plugin.debug(getClass()).log(ChatService.DISCORD, "Message", () -> "Cancelled or text is empty");
			return;
		}
		
		String text = EmojiParser.parseToAliases(event.getText());
		MappedPlaceholder placeholders = plugin.messages().placeholders(channel);
		
		placeholders.map("message", "content", "text").to(() -> text);
		placeholders.map("user", "name", "username", "sender").to(author::getName);
		placeholders.map("discriminator", "discrim").to(author::getDiscriminator);
		
		@NullOr Member member = message.getMember();
		
		if (member != null) {
			placeholders.map("nickname", "displayname").to(member::getEffectiveName);
			
			DiscordSRV discordSrv = DiscordSRV.getPlugin();
			List<Role> selectedRoles = discordSrv.getSelectedRoles(member);
			@NullOr Role topRole = (selectedRoles.isEmpty()) ? null : selectedRoles.get(0);
			
			if (topRole != null) {
				placeholders.map("toprole").to(topRole::getName);
				placeholders.map("toproleinitial").to(() -> topRole.getName().substring(0, 1));
				placeholders.map("toprolealias").to(() ->
					discordSrv.getRoleAliases().getOrDefault(
						topRole.getId(),
						discordSrv.getRoleAliases().getOrDefault(
							topRole.getName().toLowerCase(Locale.ROOT),
							topRole.getName()
						)
					)
				);
				placeholders.map("toprolecolor").to(() -> ChatColor.of(new Color(topRole.getColorRaw())));
				placeholders.map("allroles").to(() -> DiscordUtil.getFormattedRoles(selectedRoles));
			}
		}
		
		sendFormattedChatMessage(author, channel, plugin.messages().getInGameDiscordFormat(channel), placeholders);
	}
}
