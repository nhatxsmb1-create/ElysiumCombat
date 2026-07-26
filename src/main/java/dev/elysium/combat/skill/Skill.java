package dev.elysium.combat.skill;

import java.util.Map;

public class Skill {
    private final String name, description, icon;
    private final int manaCost, cooldownSeconds;
    private final SkillEffect effect;
    private final Map<String, Object> params;

    public Skill(String name, String description, String icon,
                 int manaCost, int cooldownSeconds,
                 SkillEffect effect, Map<String, Object> params) {
        this.name=name; this.description=description; this.icon=icon;
        this.manaCost=manaCost; this.cooldownSeconds=cooldownSeconds;
        this.effect=effect; this.params=params;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T def) {
        Object val = params.get(key);
        if (val == null) return def;
        try {
            if (def instanceof Double)  return (T)(Double)  ((Number)val).doubleValue();
            if (def instanceof Integer) return (T)(Integer) ((Number)val).intValue();
            return (T) val;
        } catch (ClassCastException e) { return def; }
    }

    public String      getName()            { return name; }
    public String      getDescription()     { return description; }
    public String      getIcon()            { return icon; }
    public int         getManaCost()        { return manaCost; }
    public int         getCooldownSeconds() { return cooldownSeconds; }
    public SkillEffect getEffect()          { return effect; }
}
