package dev.piyush.shyamduels.settings;

import java.util.UUID;

public class PlayerSettings {
    
    private final UUID playerUuid;
    private boolean autoGG;
    private boolean deathMessages;
    private boolean killstreakMessages;
    private boolean matchStartSounds;
    
    public PlayerSettings(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.autoGG = true;
        this.deathMessages = true;
        this.killstreakMessages = true;
        this.matchStartSounds = true;
    }
    
    public UUID getPlayerUuid() {
        return playerUuid;
    }
    
    public boolean isAutoGG() {
        return autoGG;
    }
    
    public void setAutoGG(boolean autoGG) {
        this.autoGG = autoGG;
    }
    
    public boolean isDeathMessages() {
        return deathMessages;
    }
    
    public void setDeathMessages(boolean deathMessages) {
        this.deathMessages = deathMessages;
    }
    
    public boolean isKillstreakMessages() {
        return killstreakMessages;
    }
    
    public void setKillstreakMessages(boolean killstreakMessages) {
        this.killstreakMessages = killstreakMessages;
    }
    
    public boolean isMatchStartSounds() {
        return matchStartSounds;
    }
    
    public void setMatchStartSounds(boolean matchStartSounds) {
        this.matchStartSounds = matchStartSounds;
    }
    
    public void toggle(SettingType type) {
        switch (type) {
            case AUTO_GG:
                this.autoGG = !this.autoGG;
                break;
            case DEATH_MESSAGES:
                this.deathMessages = !this.deathMessages;
                break;
            case KILLSTREAK_MESSAGES:
                this.killstreakMessages = !this.killstreakMessages;
                break;
            case MATCH_START_SOUNDS:
                this.matchStartSounds = !this.matchStartSounds;
                break;
        }
    }
    
    public boolean getValue(SettingType type) {
        switch (type) {
            case AUTO_GG:
                return autoGG;
            case DEATH_MESSAGES:
                return deathMessages;
            case KILLSTREAK_MESSAGES:
                return killstreakMessages;
            case MATCH_START_SOUNDS:
                return matchStartSounds;
            default:
                return true;
        }
    }
    
    public enum SettingType {
        AUTO_GG,
        DEATH_MESSAGES,
        KILLSTREAK_MESSAGES,
        MATCH_START_SOUNDS
    }
}
