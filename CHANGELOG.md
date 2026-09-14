# Changelog

## DiscordSRV-Staff-Chat 1.4.7

### Optimization

- Cached DiscordSRV staff/admin channel resolution instead of resolving channels during every relay path.
- Reduced Discord listener work by filtering bot/webhook messages before scheduling Bukkit-thread delivery.
- Moved debug file writes off hot server paths.

### Stability

- Added explicit Discord hook state for unavailable, initializing, ready, degraded, and failed integration states.
- Centralized DiscordSRV subscribe/unsubscribe handling.
- Added rate-limited warnings when a linked Discord channel cannot be resolved.
- Refreshed cached Discord channels during `/managestaffchat reload`.

### Configuration

- Preserved separate staff/admin message sections in `messages.config.yml`.
- Updated project metadata to 1.4.7.

### Diagnostics

- Improved README troubleshooting for missing channels, duplicate messages, permissions, reloads, and Discord relay issues.
- Debug logging now avoids synchronous file writes from join/chat paths.
