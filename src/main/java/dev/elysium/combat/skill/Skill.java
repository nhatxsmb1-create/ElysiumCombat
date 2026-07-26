package dev.elysium.combat.skill;

import java.util.Map;

public class Skill {

    private final String      name;
    private final String      description;
    private final int         manaCost;
    private final int         cooldownSeconds;
    private final SkillEffect effect;
    private final Map<String, Object> params;

    public Skill(String name, String description, int manaCost,
                 int cooldownSeconds, SkillEffect effect, Map<String, Object> params) {
        this.name            = name;
        this.description     = description;
        this.manaCost        = manaCost;
        this.cooldownSeconds = cooldownSeconds;
        this.effect          = effect;
        this.params          = params;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        Object val = params.get(key);
        if (val == null) return defaultValue;
        try {
            if (defaultValue instanceof Double) return (T)(Double) ((Number)val).doubleValue();
            if (defaultValue instanceof Integer) return (T)(Integer) ((Number)val).intValue();
            if (defaultValue instanceof Float)  return (T)(Float)   ((Number)val).floatValue();
            return (T) val;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public String      getName()            { return name; }
    public String      getDescription()     { return description; }
    public int         getManaCost()        { return manaCost; }
    public int         getCooldownSeconds() { return cooldownSeconds; }
    public SkillEffect getEffect()          { return effect; }
}
