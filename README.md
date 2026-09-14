<div align="center">

# DiscordSRV-Staff-Chat

[![](https://img.shields.io/badge/License-MIT-blue)](./LICENSE "Project License: MIT")
[![](https://img.shields.io/badge/Java-11-orange)](# "Java Version: 11")
[![](https://img.shields.io/github/v/release/DiscordSRV/Staff-Chat.svg?label=Release&color=ok)](https://github.com/DiscordSRV/Staff-Chat/releases/latest "Latest Release")
[![](https://img.shields.io/spiget/downloads/44245?color=yellow&label=Spigot%20Downloads)](https://www.spigotmc.org/resources/discordsrv-staff-chat.44245/ "Spigot Resource Page")
[![](https://img.shields.io/modrinth/dt/uD7Bzf5q?color=%2300af5c&label=Modrinth%20Downloads&logo=modrinth)](https://modrinth.com/plugin/uD7Bzf5q "Modrinth Project Page")

</div>

DiscordSRV-Staff-Chat connects private Minecraft staff channels to DiscordSRV channels. Version 1.4.7 is a cleanup, optimization, and stability release focused on safer DiscordSRV lifecycle handling, cached channel resolution, clearer diagnostics, and lower overhead on join/chat paths.

## Installation

1. Install **DiscordSRV** and **DiscordSRV-Staff-Chat** in your server's `plugins/` directory.
2. Restart the server once so configs are generated.
3. Add DiscordSRV channel links for the chats you want to relay.
4. Run `/discord reload` or restart the server again.

## DiscordSRV Channels

Add a `staff-chat` channel to DiscordSRV's `Channels` map. If you use the admin chat, add `admin-chat` too.

```yaml
Channels: {"global": "000000000000000000", "staff-chat": "000000000000000000", "admin-chat": "000000000000000000"}
```

Use Discord channel IDs, not names. IDs are stable and avoid ambiguous channel lookups.

## Commands

`/staffchat`

Permission: `staffchat.access`

Aliases: `/schat`, `/sc`

Usage:

- `/staffchat` toggles automatic staff chat.
- `/staffchat <message>` sends one message to staff chat.

`/adminchat`

Permission: `staffchat.admin`

Aliases: `/achat`, `/ac`, `/a`

Usage:

- `/adminchat` toggles automatic admin chat.
- `/adminchat <message>` sends one message to admin chat.

`/leavestaffchat` and `/joinstaffchat`

Permission: `staffchat.access`

Use these to stop or resume receiving staff chat messages.

`/leaveadminchat` and `/joinadminchat`

Permission: `staffchat.admin`

Use these to stop or resume receiving admin chat messages.

`/togglestaffchatsounds` and `/toggleadminchatsounds`

Permissions: `staffchat.access` and `staffchat.admin`

Mute or unmute notification sounds for the matching chat.

`/managestaffchat`

Permission: `staffchat.manage`

Aliases: `/discordsrv-staff-chat`, `/discordsrvstaffchat`, `/discordstaffchat`, `/discordadminchat`, `/manageadminchat`

Usage:

- `/managestaffchat` shows plugin usage.
- `/managestaffchat reload` reloads config, messages, data, updater state, and cached Discord channels.
- `/managestaffchat debug` toggles debug logging.

## Permissions

- `staffchat.access` lets a player send and receive staff chat.
- `staffchat.admin` lets a player send and receive admin chat.
- `staffchat.manage` lets a player reload and debug the plugin.
- `staffchat.*` grants all plugin permissions.

## Formatting

Messages are configured in `messages.config.yml`. Staff and admin chat have separate sections:

- `staff.placeholders.prefix`
- `staff.messages.*`
- `staff.notifications.*`
- `admin.placeholders.prefix`
- `admin.messages.*`
- `admin.notifications.*`

Legacy color codes and supported hex color formats still work. PlaceholderAPI placeholders are applied when PlaceholderAPI is installed.

## Reloading

Use `/managestaffchat reload` after config changes. The reload refreshes config files, persisted profile data, update-check settings, and cached DiscordSRV channel references.

## Debugging

Use `/managestaffchat debug` to toggle debug logging. Debug file writes are scheduled asynchronously in 1.4.7 so debug mode is less likely to affect join or chat performance.

## Troubleshooting

Staff chat is not reaching Discord:

- Confirm DiscordSRV is installed and enabled.
- Confirm DiscordSRV has a `staff-chat` channel link.
- Use `/managestaffchat reload` after editing DiscordSRV channel links.

Admin chat is not reaching Discord:

- Confirm DiscordSRV has an `admin-chat` channel link.
- Confirm the sender has `staffchat.admin`.

Discord messages are not reaching Minecraft:

- Confirm messages are sent in the linked DiscordSRV channel.
- Bot and webhook messages are ignored to prevent relay loops.
- Confirm recipients have the matching permission and have not left the chat.

Duplicate messages:

- Avoid server `/reload`.
- Use `/managestaffchat reload` for plugin config changes.
- Restart the server if another plugin reload tool duplicated listeners globally.

Configured channel not found:

- Check the DiscordSRV `Channels` map.
- Use numeric Discord channel IDs.
- Run `/discord reload`, then `/managestaffchat reload`.

Permissions not working:

- Staff chat requires `staffchat.access`.
- Admin chat requires `staffchat.admin`.
- Reload or relog after permission changes if your permission plugin caches state.

[![](https://bstats.org/signatures/bukkit/DiscordSRV-Staff-Chat.svg)](https://bstats.org/plugin/bukkit/DiscordSRV-Staff-Chat/11056)
