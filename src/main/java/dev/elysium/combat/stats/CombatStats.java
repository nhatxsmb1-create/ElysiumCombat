package dev.elysium.combat.stats;

/** Cache thong so combat cua tung player (doc tu ClassData). */
public class CombatStats {

    private double defense;      // % giam sat thuong nhan vao
    private int    manaRegen;    // mana hoi phuc moi 2 giay
    private int    maxManaBonus; // them/bot max mana

    public CombatStats(double defense, int manaRegen, int maxManaBonus) {
        this.defense      = defense;
        this.manaRegen    = manaRegen;
        this.maxManaBonus = maxManaBonus;
    }

    /** Tinh sat thuong sau khi giam theo defense % */
    public double applyDefense(double rawDamage) {
        double reduction = Math.max(0, Math.min(80, defense)); // cap 80%
        return rawDamage * (1.0 - reduction / 100.0);
    }

    public double getDefense()      { return defense; }
    public void   setDefense(double d) { this.defense = d; }
    public int    getManaRegen()    { return manaRegen; }
    public void   setManaRegen(int r)  { this.manaRegen = r; }
    public int    getMaxManaBonus() { return maxManaBonus; }
    public void   setMaxManaBonus(int b) { this.maxManaBonus = b; }
}
