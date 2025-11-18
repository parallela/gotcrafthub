# GotCraftHub Plugin - Implementation Summary

## Overview
A comprehensive hub management plugin for GotCraft servers with full protection, hub items, cosmetics, movement features, and PlaceholderAPI support.

## Features Implemented

### ✅ 1. Server Protection
All protection features are configurable in `config.yml`:
- **Block Protection**: Prevent breaking and placing blocks
- **Interaction Protection**: Prevent interaction with containers, doors, buttons, etc.
- **Combat Protection**: Disable PvP and all damage types
- **Item Protection**: Prevent item drops and pickups, item durability damage
- **Entity Protection**: Disable mob spawns, armor stand/item frame damage
- **Player Stats**: Lock health at 20 and hunger at 20 (configurable)

### ✅ 2. Hub Items System
Fully configurable in `hub-items.yml`:
- **Custom Items**: Define items with custom names, lore, materials, and slots
- **Custom Head Textures**: Support for base64 encoded skull textures
- **Commands**: Execute player or console commands on right-click
- **Inventory Protection**: Hub items cannot be dropped or moved
- **Auto-Restore**: Items restored on join and when build mode is disabled
- **Configurable**: Can disable hub items entirely with `hub-items.enabled: false`
- **Inventory Management**: Option to clear inventory or keep existing items

### ✅ 3. Movement Features

#### Pressure Plate Boost
- Step on weighted pressure plates (gold/iron) to boost forward and upward
- Configurable power and cooldown
- Smooth velocity application

#### Double Jump
- Press space twice to perform a double jump
- Configurable power and cooldown
- Works only in hub world
- Automatically enabled when player lands

### ✅ 4. Trails System
- Configurable particle trails that follow players
- Performance-optimized with tick rate control
- Customizable particle type and amount
- Can be disabled in config

### ✅ 5. Player Visibility Toggle
- `/ghub hide` command to toggle player visibility
- Individual preference per player
- Persists during session
- Updates automatically for joining players
- **No admin permission required**

### ✅ 6. Build Mode
- `/ghub build` command for admin players
- Bypasses all hub protections
- Items are restored when build mode is disabled
- Permission: `ghub.admin`

### ✅ 7. Spawn Teleport
- **Teleport to spawn on join**: Enabled by default
- Configurable with `teleport-to-spawn-on-join: true/false`
- Teleports players to world spawn location when joining the hub

### ✅ 8. Join/Leave Messages
- Custom join messages when players enter the hub
- Custom leave messages when players leave the hub
- Supports PlaceholderAPI placeholders
- Can be disabled with `join-leave-messages.enabled: false`
- Default format:
  - Join: `[+] PlayerName joined the hub`
  - Leave: `[-] PlayerName left the hub`

### ✅ 9. Chat Format
- Custom chat format for hub world
- Supports PlaceholderAPI placeholders
- Can be disabled with `chat-format.enabled: false`
- Default format: `PlayerName » message`
- Fully customizable with any PlaceholderAPI placeholders (e.g., rank, prefix, etc.)

### ✅ 10. PlaceholderAPI Support
Registered placeholders (prefix: `%gothub_`):
- `%gothub_is_hidden%` - true/false if player has hidden others
- `%gothub_trail_enabled%` - true/false if trails are enabled
- `%gothub_buildmode%` - true/false if player is in build mode
- `%gothub_world%` - Current world name
- `%gothub_players_visible%` - Number of visible players
- `%gothub_in_hub%` - true/false if player is in hub world

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/ghub hide` | Toggle player visibility | None |
| `/ghub build` | Toggle build mode | `ghub.admin` |
| `/ghub setspawn` | Set hub spawn location | `ghub.admin` |
| `/ghub reload` | Reload configuration | `ghub.admin` |

## Configuration Files

### config.yml
```yaml
hub-world: "hub"                    # Hub world name
teleport-to-spawn-on-join: true     # Teleport to spawn on join

hub-items:
  enabled: true                      # Enable/disable hub items
  clear-inventory: true              # Clear inventory before giving items

double-jump:
  enabled: true
  power: 1.2
  cooldown: 1.0

pressure-plate-boost:
  enabled: true
  horizontal: 1.5
  vertical: 0.6
  cooldown: 0.5

protection:
  block-break: true
  block-place: true
  interaction: true

join-leave-messages:
  enabled: true
  join: "<gray>[<green>+</green>]</gray> <white>%player_name%</white> <green>joined the hub</green>"
  leave: "<gray>[<red>-</red>]</gray> <white>%player_name%</white> <red>left the hub</red>"

chat-format:
  enabled: true
  format: "<gray>%player_name%</gray> <dark_gray>»</dark_gray> <white>%message%</white>"
  # MiniMessage examples:
  # - Gradient: "<gradient:red:blue>%player_name%</gradient> <dark_gray>»</dark_gray> <white>%message%</white>"
  # - Rainbow: "<rainbow>%player_name%</rainbow> <dark_gray>»</dark_gray> <white>%message%</white>"
  # - With PlaceholderAPI: "<gray>[%vault_rank%]</gray> %player_name% <dark_gray>»</dark_gray> <white>%message%</white>"
  # Legacy & codes also supported: "&7%player_name% &8» &f%message%"
```
  hunger: false
  health-lock: true
  pvp: true
  item-drop: true
  item-pickup: true
  mob-spawns: true

trails:
  enabled: true
  particle: "END_ROD"
  amount: 2
  spawn-rate: 5

messages:
  # Customizable messages with color codes
```

### hub-items.yml
```yaml
items:
  server-menu:
    slot: 0
    material: PLAYER_HEAD
    name: "&e&lServer Menu"
    lore:
      - "&7Right-click to open"
    texture: "base64_texture_here"
    command: "console:say Server menu clicked"
  
  player-visibility:
    slot: 8
    material: CLOCK
    name: "&e&lPlayer Visibility"
    lore:
      - "&7Right-click to toggle"
    command: "ghub hide"
```

## Technical Details

### Architecture
- **Managers**: ConfigManager, PlayerDataManager, HubItemManager
- **Listeners**: ProtectionListener, PlayerListener, HubItemListener, MovementListener
- **Tasks**: TrailTask, HealthHungerTask, DoubleJumpTask
- **Commands**: GHubCommand with tab completion
- **PlaceholderAPI**: GotCraftHubPlaceholder expansion

### Performance Optimizations
- Cooldown system for movement features
- Tick-based task scheduling
- Efficient player data storage with UUIDs
- Minimal event processing overhead

### Compatibility
- Paper/Spigot 1.21+
- Java 21
- PlaceholderAPI (soft dependency)

## Build Information
- Built with Maven
- Shaded JAR output
- Plugin file: `target/GotCraftHub-1.0-SNAPSHOT.jar`

## Usage Notes

1. **First Setup**: 
   - Set the correct `hub-world` name in config.yml
   - Configure hub items in hub-items.yml
   - Set world spawn with `/setworldspawn` in your hub world

2. **Disabling Hub Items**:
   - Set `hub-items.enabled: false` to not touch player inventory
   - Set `hub-items.clear-inventory: false` to keep player items

3. **Customization**:
   - All messages support color codes with `&`
   - Particle types: See Bukkit Particle enum
   - Head textures: Use base64 from minecraft-heads.com

4. **Permissions**:
   - `ghub.admin` - Build mode, reload command

## Build Command
```bash
mvn clean package
```

The compiled plugin JAR will be in `target/GotCraftHub-1.0-SNAPSHOT.jar`

