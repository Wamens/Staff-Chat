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
package com.rezzedup.discordsrv.staffchat.config;

import com.github.zafarkhaja.semver.Version;
import com.rezzedup.discordsrv.staffchat.ChatChannel;
import com.rezzedup.discordsrv.staffchat.StaffChatPlugin;
import com.rezzedup.discordsrv.staffchat.Updater;
import com.rezzedup.discordsrv.staffchat.util.MappedPlaceholder;
import com.rezzedup.discordsrv.staffchat.util.Strings;
import com.rezzedup.util.constants.Aggregates;
import com.rezzedup.util.constants.annotations.AggregatedResult;
import community.leaf.configvalues.bukkit.DefaultYamlValue;
import community.leaf.configvalues.bukkit.ExampleYamlValue;
import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.data.Load;
import community.leaf.configvalues.bukkit.data.YamlDataFile;
import community.leaf.configvalues.bukkit.util.Sections;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class MessagesConfig extends YamlDataFile {
	public static final YamlValue<Version> VERSION =
		YamlValue.of("meta.config-version", Configs.VERSION).maybe();
	
	public static final DefaultYamlValue<String> STAFF_PREFIX =
		YamlValue.ofString("staff.placeholders.prefix")
			.defaults("&d(&5&l&oStaff&d)");
	
	public static final DefaultYamlValue<String> ADMIN_PREFIX =
		YamlValue.ofString("admin.placeholders.prefix")
			.defaults("&c(&4&l&oAdmin&c)");
	
	public static final ExampleYamlValue<String> EXAMPLE_PLACEHOLDER =
		YamlValue.ofString("placeholders.example")
			.example("Define your own shared placeholders here!");
	
	public static final DefaultYamlValue<String> STAFF_IN_GAME_PLAYER_FORMAT =
		YamlValue.ofString("staff.messages.in-game-formats.player")
			.defaults("%prefix% %name%&7:&f %message%");
	
	public static final DefaultYamlValue<String> ADMIN_IN_GAME_PLAYER_FORMAT =
		YamlValue.ofString("admin.messages.in-game-formats.player")
			.defaults("%prefix% %name%&7:&f %message%");
	
	public static final DefaultYamlValue<String> STAFF_IN_GAME_DISCORD_FORMAT =
		YamlValue.ofString("staff.messages.in-game-formats.discord")
			.defaults("&9&ldiscord &f-> %prefix% %name%&7:&f %message%");
	
	public static final DefaultYamlValue<String> ADMIN_IN_GAME_DISCORD_FORMAT =
		YamlValue.ofString("admin.messages.in-game-formats.discord")
			.defaults("&9&ldiscord &f-> %prefix% %name%&7:&f %message%");
	
	public static final DefaultYamlValue<String> STAFF_IN_GAME_CONSOLE_FORMAT =
		YamlValue.ofString("staff.messages.in-game-formats.console")
			.defaults("%prefix% [CONSOLE]&7:&f %message%");
	
	public static final DefaultYamlValue<String> ADMIN_IN_GAME_CONSOLE_FORMAT =
		YamlValue.ofString("admin.messages.in-game-formats.console")
			.defaults("%prefix% [CONSOLE]&7:&f %message%");
	
	public static final DefaultYamlValue<String> STAFF_DISCORD_CONSOLE_FORMAT =
		YamlValue.ofString("staff.messages.discord-formats.console")
			.defaults("**`CONSOLE:`** %message%");
	
	public static final DefaultYamlValue<String> ADMIN_DISCORD_CONSOLE_FORMAT =
		YamlValue.ofString("admin.messages.discord-formats.console")
			.defaults("**`CONSOLE:`** %message%");
	
	public static final DefaultYamlValue<String> STAFF_AUTO_ENABLED_NOTIFICATION =
		YamlValue.ofString("staff.notifications.automatic-chat.enabled")
			.defaults("%prefix% &2->&a &nEnabled&a automatic staff chat");
	
	public static final DefaultYamlValue<String> ADMIN_AUTO_ENABLED_NOTIFICATION =
		YamlValue.ofString("admin.notifications.automatic-chat.enabled")
			.defaults("%prefix% &2->&a &nEnabled&a automatic admin chat");
	
	public static final DefaultYamlValue<String> STAFF_AUTO_DISABLED_NOTIFICATION =
		YamlValue.ofString("staff.notifications.automatic-chat.disabled")
			.defaults("%prefix% &4->&c &nDisabled&c automatic staff chat");
	
	public static final DefaultYamlValue<String> ADMIN_AUTO_DISABLED_NOTIFICATION =
		YamlValue.ofString("admin.notifications.automatic-chat.disabled")
			.defaults("%prefix% &4->&c &nDisabled&c automatic admin chat");
	
	public static final DefaultYamlValue<String> STAFF_LEFT_CHAT_NOTIFICATION_SELF =
		YamlValue.ofString("staff.notifications.leave.self")
			.defaults(
				"%prefix% &4->&c You &nleft&c the staff chat&r\n" +
					"&8&oYou won't receive any staff chat messages"
			);
	
	public static final DefaultYamlValue<String> ADMIN_LEFT_CHAT_NOTIFICATION_SELF =
		YamlValue.ofString("admin.notifications.leave.self")
			.defaults(
				"%prefix% &4->&c You &nleft&c the admin chat&r\n" +
					"&8&oYou won't receive any admin chat messages"
			);
	
	public static final DefaultYamlValue<String> STAFF_LEFT_CHAT_NOTIFICATION_OTHERS =
		YamlValue.ofString("staff.notifications.leave.others")
			.defaults("%prefix% &4->&c %player% &nleft&c the staff chat");
	
	public static final DefaultYamlValue<String> ADMIN_LEFT_CHAT_NOTIFICATION_OTHERS =
		YamlValue.ofString("admin.notifications.leave.others")
			.defaults("%prefix% &4->&c %player% &nleft&c the admin chat");
	
	public static final DefaultYamlValue<String> STAFF_LEFT_CHAT_NOTIFICATION_REMINDER =
		YamlValue.ofString("staff.notifications.leave.reminder")
			.defaults("&8&o(Reminder: you left the staff chat)");
	
	public static final DefaultYamlValue<String> ADMIN_LEFT_CHAT_NOTIFICATION_REMINDER =
		YamlValue.ofString("admin.notifications.leave.reminder")
			.defaults("&8&o(Reminder: you left the admin chat)");
	
	public static final DefaultYamlValue<String> STAFF_LEFT_CHAT_DISABLED_ERROR =
		YamlValue.ofString("staff.notifications.leave.disabled")
			.defaults(
				"%prefix% &6->&e You cannot leave the staff chat\n" +
					"&8&oLeaving the staff chat is currently disabled"
			);
	
	public static final DefaultYamlValue<String> ADMIN_LEFT_CHAT_DISABLED_ERROR =
		YamlValue.ofString("admin.notifications.leave.disabled")
			.defaults(
				"%prefix% &6->&e You cannot leave the admin chat\n" +
					"&8&oLeaving the admin chat is currently disabled"
			);
	
	public static final DefaultYamlValue<String> STAFF_JOIN_CHAT_NOTIFICATION_SELF =
		YamlValue.ofString("staff.notifications.join.self")
			.defaults(
				"%prefix% &2->&a You &njoined&a the staff chat&r\n" +
					"&8&oYou will now receive staff chat messages again"
			);
	
	public static final DefaultYamlValue<String> ADMIN_JOIN_CHAT_NOTIFICATION_SELF =
		YamlValue.ofString("admin.notifications.join.self")
			.defaults(
				"%prefix% &2->&a You &njoined&a the admin chat&r\n" +
					"&8&oYou will now receive admin chat messages again"
			);
	
	public static final DefaultYamlValue<String> STAFF_JOIN_CHAT_NOTIFICATION_OTHERS =
		YamlValue.ofString("staff.notifications.join.others")
			.defaults("%prefix% &2->&a %player% &njoined&a the staff chat");
	
	public static final DefaultYamlValue<String> ADMIN_JOIN_CHAT_NOTIFICATION_OTHERS =
		YamlValue.ofString("admin.notifications.join.others")
			.defaults("%prefix% &2->&a %player% &njoined&a the admin chat");
	
	public static final DefaultYamlValue<String> STAFF_MUTE_SOUNDS_NOTIFICATION =
		YamlValue.ofString("staff.notifications.sounds.muted")
			.defaults("%prefix% &4->&c You have &nmuted&c staff chat sounds");
	
	public static final DefaultYamlValue<String> ADMIN_MUTE_SOUNDS_NOTIFICATION =
		YamlValue.ofString("admin.notifications.sounds.muted")
			.defaults("%prefix% &4->&c You have &nmuted&c admin chat sounds");
	
	public static final DefaultYamlValue<String> STAFF_UNMUTE_SOUNDS_NOTIFICATION =
		YamlValue.ofString("staff.notifications.sounds.unmuted")
			.defaults("%prefix% &2->&a You have &nunmuted&a staff chat sounds");
	
	public static final DefaultYamlValue<String> ADMIN_UNMUTE_SOUNDS_NOTIFICATION =
		YamlValue.ofString("admin.notifications.sounds.unmuted")
			.defaults("%prefix% &2->&a You have &nunmuted&a admin chat sounds");
	
	@AggregatedResult
	public static final List<YamlValue<?>> VALUES =
		Aggregates.fromThisClass().constantsOfType(YamlValue.type()).toList();
	
	private final StaffChatPlugin plugin;
	
	private @NullOr MappedPlaceholder definitions = null;
	
	public MessagesConfig(StaffChatPlugin plugin) {
		super(plugin.directory(), "messages.config.yml", Load.LATER);
		this.plugin = plugin;
		
		reloadsWith(() ->
		{
			if (isInvalid()) {
				Configs.couldNotLoad(plugin.getLogger(), getFilePath());
				plugin.debug(getClass()).log("Reload", () -> "Couldn't load: " + getInvalidReason());
				
				if (definitions == null) {
					definitions = new MappedPlaceholder();
				}
				
				return;
			}
			
			Version existing = get(VERSION).orElse(Configs.NO_VERSION);
			boolean isOutdated = existing.lessThan(plugin.version());
			
			if (isOutdated) {
				plugin.debug(getClass()).log("Reload", () -> "Updating outdated config: " + existing);
				set(VERSION, plugin.version());
			}
			
			headerFromResource("messages.config.header.txt");
			defaultValues(VALUES);
			
			if (isUpdated()) {
				plugin.debug(getClass()).log("Reload", () -> "Saving updated config and backing up old config: v" + existing);
				backupThenSave(plugin.backups(), "v" + existing);
			}
			
			definitions = null;
			
			Sections.get(data(), "placeholders").ifPresent(section ->
			{
				definitions = new MappedPlaceholder();
				
				for (String key : section.getKeys(false)) {
					if ("prefix".equalsIgnoreCase(key)) {
						continue;
					}
					
					@NullOr String value = section.getString(key);
					if (Strings.isEmptyOrNull(value)) {
						continue;
					}
					definitions.map(key).to(() -> value);
				}
			});
		});
	}
	
	private DefaultYamlValue<String> value(ChatChannel channel, DefaultYamlValue<String> staff, DefaultYamlValue<String> admin) {
		return (channel == ChatChannel.ADMIN) ? admin : staff;
	}
	
	private DefaultYamlValue<String> prefixValue(ChatChannel channel) {
		return value(channel, STAFF_PREFIX, ADMIN_PREFIX);
	}
	
	private DefaultYamlValue<String> inGamePlayerFormat(ChatChannel channel) {
		return value(channel, STAFF_IN_GAME_PLAYER_FORMAT, ADMIN_IN_GAME_PLAYER_FORMAT);
	}
	
	private DefaultYamlValue<String> inGameDiscordFormat(ChatChannel channel) {
		return value(channel, STAFF_IN_GAME_DISCORD_FORMAT, ADMIN_IN_GAME_DISCORD_FORMAT);
	}
	
	private DefaultYamlValue<String> inGameConsoleFormat(ChatChannel channel) {
		return value(channel, STAFF_IN_GAME_CONSOLE_FORMAT, ADMIN_IN_GAME_CONSOLE_FORMAT);
	}
	
	private DefaultYamlValue<String> discordConsoleFormat(ChatChannel channel) {
		return value(channel, STAFF_DISCORD_CONSOLE_FORMAT, ADMIN_DISCORD_CONSOLE_FORMAT);
	}
	
	private DefaultYamlValue<String> autoEnabledNotification(ChatChannel channel) {
		return value(channel, STAFF_AUTO_ENABLED_NOTIFICATION, ADMIN_AUTO_ENABLED_NOTIFICATION);
	}
	
	private DefaultYamlValue<String> autoDisabledNotification(ChatChannel channel) {
		return value(channel, STAFF_AUTO_DISABLED_NOTIFICATION, ADMIN_AUTO_DISABLED_NOTIFICATION);
	}
	
	private DefaultYamlValue<String> leftChatNotificationSelf(ChatChannel channel) {
		return value(channel, STAFF_LEFT_CHAT_NOTIFICATION_SELF, ADMIN_LEFT_CHAT_NOTIFICATION_SELF);
	}
	
	private DefaultYamlValue<String> leftChatNotificationOthers(ChatChannel channel) {
		return value(channel, STAFF_LEFT_CHAT_NOTIFICATION_OTHERS, ADMIN_LEFT_CHAT_NOTIFICATION_OTHERS);
	}
	
	public DefaultYamlValue<String> leftChatReminder(ChatChannel channel) {
		return value(channel, STAFF_LEFT_CHAT_NOTIFICATION_REMINDER, ADMIN_LEFT_CHAT_NOTIFICATION_REMINDER);
	}
	
	private DefaultYamlValue<String> leftChatDisabledError(ChatChannel channel) {
		return value(channel, STAFF_LEFT_CHAT_DISABLED_ERROR, ADMIN_LEFT_CHAT_DISABLED_ERROR);
	}
	
	private DefaultYamlValue<String> joinChatNotificationSelf(ChatChannel channel) {
		return value(channel, STAFF_JOIN_CHAT_NOTIFICATION_SELF, ADMIN_JOIN_CHAT_NOTIFICATION_SELF);
	}
	
	private DefaultYamlValue<String> joinChatNotificationOthers(ChatChannel channel) {
		return value(channel, STAFF_JOIN_CHAT_NOTIFICATION_OTHERS, ADMIN_JOIN_CHAT_NOTIFICATION_OTHERS);
	}
	
	private DefaultYamlValue<String> muteSoundsNotification(ChatChannel channel) {
		return value(channel, STAFF_MUTE_SOUNDS_NOTIFICATION, ADMIN_MUTE_SOUNDS_NOTIFICATION);
	}
	
	private DefaultYamlValue<String> unmuteSoundsNotification(ChatChannel channel) {
		return value(channel, STAFF_UNMUTE_SOUNDS_NOTIFICATION, ADMIN_UNMUTE_SOUNDS_NOTIFICATION);
	}
	
	public String getInGamePlayerFormat(ChatChannel channel) {
		return getOrDefault(inGamePlayerFormat(channel));
	}
	
	public String getInGameDiscordFormat(ChatChannel channel) {
		return getOrDefault(inGameDiscordFormat(channel));
	}
	
	public String getInGameConsoleFormat(ChatChannel channel) {
		return getOrDefault(inGameConsoleFormat(channel));
	}
	
	public String getDiscordConsoleFormat(ChatChannel channel) {
		return getOrDefault(discordConsoleFormat(channel));
	}
	
	public MappedPlaceholder placeholders() {
		MappedPlaceholder placeholders = new MappedPlaceholder();
		if (definitions != null) {
			placeholders.inherit(definitions);
		}
		return placeholders;
	}
	
	public MappedPlaceholder placeholders(ChatChannel channel) {
		MappedPlaceholder placeholders = placeholders();
		placeholders.map("prefix").to(() -> getOrDefault(prefixValue(channel)));
		placeholders.map("chat", "chat_key").to(channel::key);
		placeholders.map("chat_label").to(() -> channel.label().toLowerCase(Locale.ROOT));
		placeholders.map("chat_name").to(channel::displayName);
		placeholders.map("chat_channel").to(channel::discordChannelName);
		return placeholders;
	}
	
	public MappedPlaceholder placeholders(Player player, ChatChannel channel) {
		MappedPlaceholder placeholders = placeholders(channel);
		placeholders.map("user", "name", "username", "player", "sender").to(player::getName);
		placeholders.map("nickname", "displayname").to(player::getDisplayName);
		return placeholders;
	}
	
	private void sendNotification(Player player, ChatChannel channel, String message) {
		player.sendMessage(message);
		plugin.config().playNotificationSound(player, channel);
	}
	
	private void sendNotification(Player player, ChatChannel channel, DefaultYamlValue<String> self, @NullOr DefaultYamlValue<String> others) {
		MappedPlaceholder placeholders = placeholders(player, channel);
		sendNotification(player, channel, Strings.colorful(placeholders.update(getOrDefault(self))));
		
		if (others == null) {
			return;
		}
		
		String notification = Strings.colorful(placeholders.update(getOrDefault(others)));
		plugin.getServer().getConsoleSender().sendMessage(notification);
		
		plugin.onlineChatParticipants(channel)
			.filter(Predicate.not(player::equals))
			.forEach(staff -> sendNotification(staff, channel, notification));
	}
	
	public void notifyAutoChatEnabled(Player enabler, ChatChannel channel) {
		sendNotification(enabler, channel, autoEnabledNotification(channel), null);
	}
	
	public void notifyAutoChatDisabled(Player disabler, ChatChannel channel) {
		sendNotification(disabler, channel, autoDisabledNotification(channel), null);
	}
	
	public void notifyLeaveChat(Player leaver, ChatChannel channel, boolean notifyOthers) {
		@NullOr DefaultYamlValue<String> others = (notifyOthers) ? leftChatNotificationOthers(channel) : null;
		sendNotification(leaver, channel, leftChatNotificationSelf(channel), others);
	}
	
	public void notifyLeavingChatIsDisabled(Player leaver, ChatChannel channel) {
		sendNotification(leaver, channel, leftChatDisabledError(channel), null);
	}
	
	public void notifyJoinChat(Player joiner, ChatChannel channel, boolean notifyOthers) {
		@NullOr DefaultYamlValue<String> others = (notifyOthers) ? joinChatNotificationOthers(channel) : null;
		sendNotification(joiner, channel, joinChatNotificationSelf(channel), others);
	}
	
	public void notifySoundsMuted(Player player, ChatChannel channel) {
		sendNotification(player, channel, muteSoundsNotification(channel), null);
	}
	
	public void notifySoundsUnmuted(Player player, ChatChannel channel) {
		sendNotification(player, channel, unmuteSoundsNotification(channel), null);
	}
	
	public void notifyUpdateAvailable(Player manager, Version version) {
		sendNotification(manager, ChatChannel.STAFF, Strings.colorful(
			"&9DiscordSRV-&lStaff&9-&lChat&6 ->&e Update available: &f" +
				version + " &6&o(" + plugin.version() + ")&r\n" + "&9&o&n" + Updater.RESOURCE_PAGE
		));
	}
}
