package dev.elysium.combat.clazz;

public class ClassData {

    private final String id;
    private final String displayName;
    private final String description;
    private final String item;

    // Stats
    private final double bonusHp;
    private final double bonusDamage;
    private final int    defense;         // % giam sat thuong
    private final double speedModifier;
    private final int    manaRegen;
    private final int    maxManaBonus;

    public ClassData(String id, String displayName, String description, String item,
                     double bonusHp, double bonusDamage, int defense,
                     double speedModifier, int manaRegen, int maxManaBonus) {
        this.id            = id;
        this.displayName   = displayName;
        this.description   = description;
        this.item          = item;
        this.bonusHp       = bonusHp;
        this.bonusDamage   = bonusDamage;
        this.defense       = defense;
        this.speedModifier = speedModifier;
        this.manaRegen     = manaRegen;
        this.maxManaBonus  = maxManaBonus;
    }

    public String getId()           { return id; }
    public String getDisplayName()  { return displayName; }
    public String getDescription()  { return description; }
    public String getItem()         { return item; }
    public double getBonusHp()      { return bonusHp; }
    public double getBonusDamage()  { return bonusDamage; }
    public int    getDefense()      { return defense; }
    public double getSpeedModifier(){ return speedModifier; }
    public int    getManaRegen()    { return manaRegen; }
    public int    getMaxManaBonus() { return maxManaBonus; }
}
