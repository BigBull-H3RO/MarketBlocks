# Commands & Permissions

This overview groups the most important commands and permissions by role.

## Admin / Operator

| Command | Purpose |
| --- | --- |
| `/marketblocks adminmode [true|false]` | Enables/disables global admin/edit mode. |
| `/marketblocks marketplace` | Opens the Marketplace. |
| `/marketblocks marketplace reload` | Reloads Marketplace configuration. |
| `/marketblocks marketplace resetlimits <player>` | Resets daily limits for a player. |

## Players

- Access to purchase flows based on shop/marketplace state
- No administrative marketplace mutations without proper rights

## Permission Nodes (Short Explanation)

The exact permission nodes can vary by server setup/permission mod.

Recommendation:

1. Test permissions first with operator/admin rights.
2. Then separate groups cleanly (for example admin, moderator, player).
3. Grant critical commands (`reload`, `resetlimits`, `adminmode`) only to trusted roles.
