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

import com.rezzedup.discordsrv.staffchat.config.StaffChatConfig;
import com.rezzedup.discordsrv.staffchat.events.AutoStaffChatToggleEvent;
import com.rezzedup.discordsrv.staffchat.events.ReceivingStaffChatToggleEvent;
import community.leaf.configvalues.bukkit.YamlValue;
import community.leaf.configvalues.bukkit.data.YamlDataFile;
import community.leaf.configvalues.bukkit.util.Sections;
import community.leaf.tasks.TaskContext;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import pl.tlinkowski.annotation.basic.NullOr;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class Data extends YamlDataFile implements StaffChatData {
	private static final String PROFILES_PATH = "staff-chat.profiles";
	
	private final Map<UUID, Profile> profilesByUuid = new HashMap<>();
	
	private final StaffChatPlugin plugin;
	
	private @NullOr TaskContext<BukkitTask> task = null;
	
	Data(StaffChatPlugin plugin) {
		super(plugin.directory().resolve("data"), "staff-chat.data.yml");
		this.plugin = plugin;
		
		if (plugin.config().getOrDefault(StaffChatConfig.PERSIST_TOGGLES)) {
			Sections.get(data(), PROFILES_PATH).ifPresent(section ->
			{
				for (String key : section.getKeys(false)) {
					try {
						getOrCreateProfile(UUID.fromString(key));
					} catch (IllegalArgumentException ignored) {
					}
				}
			});
		}
		
		task = plugin.async().every(2).minutes().run(() -> {
			if (isUpdated()) {
				save();
			}
		});
		
		reloadsWith(() -> plugin.getServer().getOnlinePlayers().forEach(this::updateProfile));
	}
	
	protected void end() {
		if (task != null) {
			task.cancel();
		}
		if (isUpdated()) {
			save();
		}
	}
	
	@Override
	public StaffChatProfile getOrCreateProfile(UUID uuid) {
		return profilesByUuid.computeIfAbsent(uuid, k -> new Profile(plugin, this, k));
	}
	
	@Override
	public Optional<StaffChatProfile> getProfile(UUID uuid) {
		return Optional.ofNullable(profilesByUuid.get(uuid));
	}
	
	public void updateProfile(Player player) {
		@NullOr Profile profile = profilesByUuid.get(player.getUniqueId());
		boolean isStaff = Permissions.ACCESS.allows(player);
		boolean isAdmin = Permissions.ADMIN.allows(player);
		
		if (isStaff || isAdmin) {
			if (profile == null) {
				profile = (Profile) getOrCreateProfile(player);
			}
			
			if (!plugin.config().getOrDefault(StaffChatConfig.LEAVING_STAFFCHAT_ENABLED)) {
				for (ChatChannel channel : ChatChannel.values()) {
					if (channel.permission().allows(player) && profile.left(channel) != null) {
						profile.receivesStaffChatMessages(channel, true);
					}
				}
			}
			
			if (!isStaff) {
				profile.automaticStaffChat(ChatChannel.STAFF, false);
				profile.receivesStaffChatMessages(ChatChannel.STAFF, true);
			}
			
			if (!isAdmin) {
				profile.automaticStaffChat(ChatChannel.ADMIN, false);
				profile.receivesStaffChatMessages(ChatChannel.ADMIN, true);
			}
		} else if (profile != null) {
			for (ChatChannel channel : ChatChannel.values()) {
				if (profile.automaticStaffChat(channel)) {
					profile.automaticStaffChat(channel, false);
				}
			}
			
			profile.clearStoredProfileData();
			profilesByUuid.remove(player.getUniqueId());
		}
	}
	
	static class Profile implements StaffChatProfile {
		private final StaffChatPlugin plugin;
		private final YamlDataFile yaml;
		private final UUID uuid;
		
		private @NullOr Instant staffAuto;
		private @NullOr Instant staffLeft;
		private boolean staffMutedSounds = false;
		private @NullOr Instant adminAuto;
		private @NullOr Instant adminLeft;
		private boolean adminMutedSounds = false;
		
		Profile(StaffChatPlugin plugin, YamlDataFile yaml, UUID uuid) {
			this.plugin = plugin;
			this.yaml = yaml;
			this.uuid = uuid;
			
			if (plugin.config().getOrDefault(StaffChatConfig.PERSIST_TOGGLES)) {
				Sections.get(yaml.data(), path()).ifPresent(section ->
				{
					staffAuto = autoValue(ChatChannel.STAFF).get(section).orElse(null);
					staffLeft = leftValue(ChatChannel.STAFF).get(section).orElse(null);
					staffMutedSounds = mutedSoundsValue(ChatChannel.STAFF).get(section).orElse(false);
					adminAuto = autoValue(ChatChannel.ADMIN).get(section).orElse(null);
					adminLeft = leftValue(ChatChannel.ADMIN).get(section).orElse(null);
					adminMutedSounds = mutedSoundsValue(ChatChannel.ADMIN).get(section).orElse(false);
				});
			}
		}
		
		private static YamlValue<Instant> autoValue(ChatChannel channel) {
			return YamlValue.ofInstant("toggles." + channel.key() + ".auto").maybe();
		}
		
		private static YamlValue<Instant> leftValue(ChatChannel channel) {
			return YamlValue.ofInstant("toggles." + channel.key() + ".left").maybe();
		}
		
		private static YamlValue<Boolean> mutedSoundsValue(ChatChannel channel) {
			return YamlValue.ofBoolean("toggles." + channel.key() + ".muted-sounds").maybe();
		}
		
		String path() {
			return PROFILES_PATH + "." + uuid;
		}
		
		private @NullOr Instant auto(ChatChannel channel) {
			return (channel == ChatChannel.ADMIN) ? adminAuto : staffAuto;
		}
		
		private void auto(ChatChannel channel, @NullOr Instant value) {
			if (channel == ChatChannel.ADMIN) {
				adminAuto = value;
			} else {
				staffAuto = value;
			}
		}
		
		private @NullOr Instant left(ChatChannel channel) {
			return (channel == ChatChannel.ADMIN) ? adminLeft : staffLeft;
		}
		
		private void left(ChatChannel channel, @NullOr Instant value) {
			if (channel == ChatChannel.ADMIN) {
				adminLeft = value;
			} else {
				staffLeft = value;
			}
		}
		
		private boolean mutedSounds(ChatChannel channel) {
			return (channel == ChatChannel.ADMIN) ? adminMutedSounds : staffMutedSounds;
		}
		
		private void mutedSounds(ChatChannel channel, boolean value) {
			if (channel == ChatChannel.ADMIN) {
				adminMutedSounds = value;
			} else {
				staffMutedSounds = value;
			}
		}
		
		@Override
		public UUID uuid() {
			return uuid;
		}
		
		@Override
		public Optional<Instant> sinceEnabledAutoChat(ChatChannel channel) {
			return Optional.ofNullable(auto(channel));
		}
		
		@Override
		public boolean automaticStaffChat(ChatChannel channel) {
			return auto(channel) != null;
		}
		
		@Override
		public void automaticStaffChat(ChatChannel channel, boolean enabled) {
			if (plugin.events().call(new AutoStaffChatToggleEvent(this, channel, enabled)).isCancelled()) {
				return;
			}
			
			auto(channel, (enabled) ? Instant.now() : null);
			if (enabled) {
				for (ChatChannel other : ChatChannel.values()) {
					if (other != channel) {
						auto(other, null);
					}
				}
			}
			
			updateStoredProfileData();
		}
		
		@Override
		public Optional<Instant> sinceLeftStaffChat(ChatChannel channel) {
			return Optional.ofNullable(left(channel));
		}
		
		@Override
		public boolean receivesStaffChatMessages(ChatChannel channel) {
			return left(channel) == null || !plugin.config().getOrDefault(StaffChatConfig.LEAVING_STAFFCHAT_ENABLED);
		}
		
		@Override
		public void receivesStaffChatMessages(ChatChannel channel, boolean enabled) {
			if (plugin.events().call(new ReceivingStaffChatToggleEvent(this, channel, enabled)).isCancelled()) {
				return;
			}
			
			left(channel, (enabled) ? null : Instant.now());
			updateStoredProfileData();
		}
		
		@Override
		public boolean receivesStaffChatSounds(ChatChannel channel) {
			return !mutedSounds(channel);
		}
		
		@Override
		public void receivesStaffChatSounds(ChatChannel channel, boolean enabled) {
			mutedSounds(channel, !enabled);
			updateStoredProfileData();
		}
		
		boolean hasDefaultSettings() {
			return staffAuto == null && staffLeft == null && !staffMutedSounds
				&& adminAuto == null && adminLeft == null && !adminMutedSounds;
		}
		
		void clearStoredProfileData() {
			if (!plugin.config().getOrDefault(StaffChatConfig.PERSIST_TOGGLES)) {
				return;
			}
			
			yaml.data().set(path(), null);
			yaml.updated(true);
		}
		
		void updateStoredProfileData() {
			if (!plugin.config().getOrDefault(StaffChatConfig.PERSIST_TOGGLES)) {
				return;
			}
			
			if (hasDefaultSettings()) {
				clearStoredProfileData();
				return;
			}
			
			ConfigurationSection section = Sections.getOrCreate(yaml.data(), path());
			autoValue(ChatChannel.STAFF).set(section, staffAuto);
			leftValue(ChatChannel.STAFF).set(section, staffLeft);
			mutedSoundsValue(ChatChannel.STAFF).set(section, staffMutedSounds);
			autoValue(ChatChannel.ADMIN).set(section, adminAuto);
			leftValue(ChatChannel.ADMIN).set(section, adminLeft);
			mutedSoundsValue(ChatChannel.ADMIN).set(section, adminMutedSounds);
			yaml.updated(true);
		}
	}
}
