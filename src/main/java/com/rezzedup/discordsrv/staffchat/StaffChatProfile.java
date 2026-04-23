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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface StaffChatProfile {
	UUID uuid();
	
	Optional<Instant> sinceEnabledAutoChat(ChatChannel channel);
	
	boolean automaticStaffChat(ChatChannel channel);
	
	void automaticStaffChat(ChatChannel channel, boolean enabled);
	
	Optional<Instant> sinceLeftStaffChat(ChatChannel channel);
	
	boolean receivesStaffChatMessages(ChatChannel channel);
	
	void receivesStaffChatMessages(ChatChannel channel, boolean enabled);
	
	boolean receivesStaffChatSounds(ChatChannel channel);
	
	void receivesStaffChatSounds(ChatChannel channel, boolean enabled);
	
	default Optional<Instant> sinceEnabledAutoChat() {
		return sinceEnabledAutoChat(ChatChannel.STAFF);
	}
	
	default boolean automaticStaffChat() {
		return automaticStaffChat(ChatChannel.STAFF);
	}
	
	default void automaticStaffChat(boolean enabled) {
		automaticStaffChat(ChatChannel.STAFF, enabled);
	}
	
	default Optional<Instant> sinceLeftStaffChat() {
		return sinceLeftStaffChat(ChatChannel.STAFF);
	}
	
	default boolean receivesStaffChatMessages() {
		return receivesStaffChatMessages(ChatChannel.STAFF);
	}
	
	default void receivesStaffChatMessages(boolean enabled) {
		receivesStaffChatMessages(ChatChannel.STAFF, enabled);
	}
	
	default boolean receivesStaffChatSounds() {
		return receivesStaffChatSounds(ChatChannel.STAFF);
	}
	
	default void receivesStaffChatSounds(boolean enabled) {
		receivesStaffChatSounds(ChatChannel.STAFF, enabled);
	}
	
	default void toggleAutomaticStaffChat() {
		automaticStaffChat(!automaticStaffChat());
	}
	
	default void toggleAutomaticChat(ChatChannel channel) {
		automaticStaffChat(channel, !automaticStaffChat(channel));
	}
	
	default Optional<Player> toPlayer() {
		return Optional.ofNullable(Bukkit.getPlayer(uuid()));
	}
}
