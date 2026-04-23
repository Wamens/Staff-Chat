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

import github.scarsz.discordsrv.dependencies.jda.api.entities.Message;
import github.scarsz.discordsrv.dependencies.jda.api.entities.TextChannel;
import github.scarsz.discordsrv.dependencies.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.stream.Stream;

public interface StaffChatAPI {
	StaffChatData data();
	
	boolean isDiscordSrvHookEnabled();
	
	@NullOr TextChannel getDiscordChannelOrNull(ChatChannel channel);
	
	void submitMessageFromConsole(String message, ChatChannel channel);
	
	void submitMessageFromPlayer(Player author, String message, ChatChannel channel);
	
	void submitMessageFromDiscord(User author, Message message, ChatChannel channel);
	
	default Stream<? extends Player> onlineChatParticipants(ChatChannel channel) {
		return Bukkit.getOnlinePlayers().stream()
			.filter(channel.permission()::allows)
			.filter(player -> data().isReceivingChatMessages(player, channel));
	}
	
	default Stream<? extends Player> onlineStaffChatParticipants() {
		return onlineChatParticipants(ChatChannel.STAFF);
	}
}
