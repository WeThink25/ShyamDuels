# 🎮 ShyamDuels

> **A high-performance 1v1 and team dueling plugin for Minecraft servers**

![Version](https://img.shields.io/badge/version-1.0-brightgreen)
![Minecraft](https://img.shields.io/badge/Minecraft-1.20.4+-blue)
![Status](https://img.shields.io/badge/status-stable-success)

---

## ✨ Features

### 🗡️ Advanced Duel System
- **Ranked 1v1 Duels** - ELO-based competitive matchmaking
- **Team Battles** - 2v2, 3v3, and 4v4 party duels
- **Best-of-X Rounds** - Configurable round system (Bo1, Bo3, Bo5)
- **Custom Kits** - Unlimited kit creation with full customization
- **Smart Matchmaking** - Automatic queue system with party support

### 🏟️ Arena Management
- **Unlimited Arenas** - Create as many arenas as needed
- **Kit-Arena Linking** - Assign specific kits to arenas
- **Auto-Reset** - Automatic arena restoration after matches
- **Build Mode** - Optional building with material whitelist
- **FFA Support** - Dedicated Free-For-All arena type

### ⚔️ Free For All (FFA)
- **Continuous PvP** - Non-stop action in FFA arenas
- **Auto-Reset** - Configurable arena reset intervals
- **Kit Selection** - Choose your loadout before joining
- **Spawn Protection** - Brief invincibility on spawn

### 📊 Statistics & Ranking System
- **ELO System** - Dynamic rating with performance-based gains/losses
- **30+ Ranks** - Bronze V → Conqueror with unique colors
- **Comprehensive Stats** - Wins, losses, kills, deaths, K/D ratio
- **Rank Multipliers** - Higher ranks have increased risk/reward
- **PlaceholderAPI Support** - Display stats anywhere

### 🎨 Kit Editor
- **Drag & Drop** - Intuitive inventory customization
- **Armor Trims** - VIP players can customize armor appearance
- **Personal Layouts** - Each player saves their own kit arrangement
- **Real-time Preview** - See changes instantly

### 👥 Party System
- **Party Creation** - Team up with friends
- **Party Queue** - Queue together for team matches
- **Party vs Party** - Challenge other parties
- **Party Chat** - Private communication channel
- **Public/Private Modes** - Control who can join
- **Auto-Transfer** - Ownership transfer on leader disconnect

### 🎬 Spectator Mode
- **Live Spectating** - Watch ongoing matches
- **Boundary Enforcement** - Spectators stay within arena
- **Match Info** - View player stats and match details
- **Easy Navigation** - Teleport between players

### 🎯 Performance Optimized
- **Async Operations** - Database operations don't block main thread
- **Connection Pooling** - Efficient database management
- **Cached Data** - Reduced database queries
- **Optimized YAML** - Compact configuration format
- **Thread-Safe** - Proper concurrency handling

---

## 📋 Commands

### Player Commands
| Command | Aliases | Description |
|---------|---------|-------------|
| `/duel <player>` | | Challenge a player to a duel |
| `/queue` | `/play` | Join ranked matchmaking |
| `/ffa` | | Join Free For All |
| `/kiteditor [kit]` | `/editkit` | Customize your kit layout |
| `/leavefight` | `/leave`, `/spawn` | Return to lobby |
| `/spectate [player]` | | Watch ongoing matches |
| `/party` | `/p` | Party management |
| `/partychat` | `/pc` | Toggle party chat |

### Admin Commands
| Command | Description |
|---------|-------------|
| `/arena create <name>` | Create new arena |
| `/arena delete <name>` | Delete arena |
| `/arena corner1/2 <arena>` | Set arena boundaries |
| `/arena spawn1/2 <arena>` | Set spawn points |
| `/arena addkit <arena> <kit>` | Link kit to arena |
| `/arena build <arena> <true/false>` | Toggle build mode |
| `/arena ffa <arena> <true/false>` | Set FFA mode |
| `/kit create <name>` | Create kit from inventory |
| `/kit delete <name>` | Delete kit |
| `/kit setinv <name>` | Update kit inventory |
| `/kit seticon <name>` | Set kit icon |
| `/kit allowblock <name>` | Add block to whitelist |
| `/shyamduels reload` | Reload configuration |

---

## 🔑 Permissions

| Permission | Description |
|------------|-------------|
| `shyamduels.admin` | Full admin access |
| `shyamduels.vip` | VIP features (armor trims, larger parties) |

---

## 📦 Installation

1. **Download** the latest release
2. **Install Dependencies:**
   - FastAsyncWorldEdit (Required)
   - PlaceholderAPI (Optional)
3. **Place** JAR in `plugins/` folder
4. **Restart** server
5. **Configure** in `plugins/ShyamDuels/`
6. **Setup** arenas and kits (see guides below)

---

## 🚀 Quick Start Guide

### 1. Create Your First Kit
```
1. Equip items, armor, and effects you want
2. /kit create NoDebuff
3. /kit seticon NoDebuff (while holding icon item)
```

### 2. Create Your First Arena
```
1. /arena create Arena1
2. Stand at corner 1: /arena corner1 Arena1
3. Stand at corner 2: /arena corner2 Arena1
4. Set spawn 1: /arena spawn1 Arena1
5. Set spawn 2: /arena spawn2 Arena1
6. Link kit: /arena addkit Arena1 NoDebuff
```

### 3. Test It Out
```
/queue - Join matchmaking
/duel <player> - Challenge someone
/ffa - Join FFA (if configured)
```

---

## 🏟️ Arena Setup (Detailed)

### Standard Duel Arena
```bash
/arena create MyArena
/arena corner1 MyArena          # Stand at one corner
/arena corner2 MyArena          # Stand at opposite corner
/arena spawn1 MyArena           # Player 1 spawn
/arena spawn2 MyArena           # Player 2 spawn
/arena center MyArena           # Optional: arena center
/arena addkit MyArena NoDebuff  # Link kit(s)
```

### FFA Arena
```bash
/arena create FFAArena
/arena corner1 FFAArena
/arena corner2 FFAArena
/arena spawn1 FFAArena
/arena spawn2 FFAArena
/arena ffa FFAArena true        # Enable FFA mode
/arena addkit FFAArena NoDebuff
```

### Build Arena (SkyWars/Bridge)
```bash
/arena create BuildArena
# ... set corners and spawns ...
/arena build BuildArena true    # Enable building
/arena addkit BuildArena Bridge
```

---

## ⚔️ Kit Setup (Detailed)

### Basic Kit
```bash
1. Equip your inventory with items
2. Wear armor
3. Apply potion effects (optional)
4. /kit create KitName
```

### Kit with Building
```bash
/kit create Bridge
# Hold each block type and run:
/kit allowblock Bridge  # Repeat for each block
```

### Update Existing Kit
```bash
/kit setinv KitName     # Update inventory
/kit seticon KitName    # Update icon (hold item)
```

---

## 👥 Party System Guide

### Creating & Managing
```bash
/party create                    # Create party
/party invite <player>           # Invite players
/party public                    # Allow anyone to join
/party private                   # Invite-only
/party kick <player>             # Remove member
/party disband                   # Disband party
```

### Party Features
- **Party Queue**: Queue as a team for ranked matches
- **Party Duels**: Challenge other parties
- **Party Chat**: Private communication
- **Auto-Transfer**: Leadership transfers if owner leaves

---

## ⚙️ Configuration

### Main Config (`config.yml`)
- Database settings (SQLite/MySQL)
- Lobby spawn location
- Party settings
- ELO system configuration
- Rank definitions

### Messages (`messages.yml`)
- All plugin messages
- Gradient color support
- MiniMessage format
- Fully customizable

### GUI (`gui.yml`)
- All GUI layouts
- Item configurations
- Compact YAML format

### Scoreboards (`scoreboards.yml`)
- Lobby, queue, duel, spectator boards
- Placeholder support
- Customizable lines

---

## 🎨 PlaceholderAPI Placeholders

```
%shyamduels_wins%
%shyamduels_losses%
%shyamduels_kills%
%shyamduels_deaths%
%shyamduels_kdr%
%shyamduels_elo%
%shyamduels_rank%
%shyamduels_rank_colored%
```

---

## 🔧 Database Support

### SQLite (Default)
- No setup required
- Automatic file creation
- Perfect for small servers

### MySQL (Recommended for large servers)
```yaml
database:
  type: MYSQL
  mysql:
    host: localhost
    port: 3306
    database: shyamduels
    username: root
    password: your_password
```

---

## 📊 ELO System

### How It Works
- **Base Gain**: +25 ELO on win
- **Base Loss**: -20 ELO on loss (multiplied by rank)
- **Kill Bonus**: +5 ELO per kill
- **Rank Multipliers**: Higher ranks lose more on defeat
- **Minimum ELO**: 0 (configurable)

### Rank Progression
```
Bronze V (0-199) → Bronze IV → ... → Bronze I
Silver V (1000-1099) → ... → Silver I
Gold V (1500-1599) → ... → Gold I
Platinum V (2000-2099) → ... → Platinum I
Diamond V (2500-2599) → ... → Diamond I
Crown V (3000-3099) → ... → Crown I
Ace (3500-3999)
Ace Master (4000-4499)
Ace Dominator (4500-4999)
Conqueror (5000+)
```

---

## 🐛 Troubleshooting

### Arena Not Resetting
- Ensure FastAsyncWorldEdit is installed
- Check console for errors
- Verify arena boundaries are set correctly

### Players Can't Join Queue
- Verify kit is linked to arena: `/arena addkit <arena> <kit>`
- Check if arena has both spawn points set
- Ensure arena is not in use

### Database Errors
- Check database credentials in config.yml
- Verify MySQL server is running (if using MySQL)
- Check file permissions for SQLite

### Kit Not Working
- Verify kit exists: `/kit list`
- Check if kit is linked to arena
- Ensure inventory was saved correctly

---

## 🔄 Migration from Other Plugins

### From Practice/Duels Plugins
1. Export your old arenas (if possible)
2. Recreate arenas using `/arena` commands
3. Recreate kits using `/kit` commands
4. Configure ELO system to match old settings

---

## � Performance Tips

1. **Use MySQL** for servers with 50+ players
2. **Limit FFA arenas** to 2-3 active arenas
3. **Optimize arena size** - smaller arenas = better performance
4. **Regular restarts** - restart server daily for best performance
5. **Monitor TPS** - use `/tps` to check server health


## 🤝 Support

- **Issues**: [GitHub Issues](https://github.com/ShyamStudios/ShyamDuels/issues)
- **Documentation**: This README
- **Updates**: Check [Releases](https://github.com/ShyamStudios/ShyamDuels/releases)

---

## 📜 License

**All Rights Reserved** - See [https://github.com/ShyamStudios/ShyamDuels/blob/main/LICENSE.md](https://github.com/ShyamStudios/ShyamDuels/blob/main/LICENSE.md)

- ✅ Personal use on your servers
- ❌ No redistribution
- ❌ No reselling
- ❌ No modification & sharing

---

<div align="center">

**Made with ❤️ for competitive Minecraft PvP**

[Report Bug](https://github.com/ShyamStudios/ShyamDuels/issues) · [Request Feature](https://github.com/ShyamStudios/ShyamDuels/issues)

</div>
