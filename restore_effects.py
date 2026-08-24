import re

with open(r'src/main/java/dev/elysium/combat/skill/SkillManager.java', 'r', encoding='utf-8') as f:
    code = f.read()

old_activate = re.search(r'    public void activate\(Player player, int slot\) \{.*?(?=    // ── Skill item ──)', code, re.DOTALL)
if not old_activate:
    print("Could not find activate()")
else:
    new_activate = '''    public void activate(Player player, int slot) {
        PlayerClass pc = plugin.getClassManager().getPlayerClass(player.getUniqueId());
        if (pc == null || pc == PlayerClass.NONE) return;

        Skill skill = getSkill(pc, slot);
        if (skill == null) return;

        String cdKey = pc + "_skill" + slot;
        long rem = CoreAPI.getCore().getCooldownManager().remainingSeconds(player.getUniqueId(), cdKey);
        if (rem > 0) {
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                ColorUtil.color("&cChiêu thức chưa sẵn sàng! Thử lại sau " + rem + "s.")));
            return;
        }

        dev.elysium.core.player.ElysiumPlayer ep = CoreAPI.getPlayer(player);
        if (ep == null) return;
        if (ep.getMana() < skill.getManaCost()) {
            player.sendActionBar(net.kyori.adventure.text.Component.text(
                ColorUtil.color("&cKhông đủ Mana!")));
            return;
        }

        ep.setMana(ep.getMana() - skill.getManaCost());
        CoreAPI.getCore().getCooldownManager().set(player.getUniqueId(), cdKey, skill.getCooldownSeconds() * 1000L);

        playSkillEffects(player, skill);
        if (skill.getEffect() != null) {
            executeEffect(player, skill);
        }

        refreshHotbarSkills(player, pc);
    }

    private void executeEffect(Player player, Skill skill) {
        switch (skill.getEffect()) {
            case BUFF_POTION  -> buffPotion(player, skill);
            case RESTORE_MANA -> restoreMana(player, skill);
            case HEAL         -> heal(player, skill);
            case DASH         -> dash(player, skill);
        }
    }

    private void buffPotion(Player player, Skill skill) {
        applyPotion(player, skill.get("potion-type","SPEED"),
            skill.get("potion-amplifier",0), skill.get("duration",100));
        String extra = skill.get("extra-potion-type","");
        if (extra != null && !extra.isBlank()) applyPotion(player, extra,
            skill.get("extra-potion-amplifier",0), skill.get("extra-duration",100));
    }

    private void applyPotion(Player player, String name, int amp, int dur) {
        try {
            org.bukkit.potion.PotionEffectType type = org.bukkit.potion.PotionEffectType.getByName(name);
            if (type == null) return;
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(type, dur, amp, false, true, true));
        } catch (Exception ignored) {}
    }

    private void restoreMana(Player player, Skill skill) {
        int amount = skill.get("mana-restore", 50);
        dev.elysium.core.player.ElysiumPlayer ep = CoreAPI.getPlayer(player);
        if (ep != null) {
            ep.setMana(Math.min(ep.getMaxMana(), ep.getMana() + amount));
            player.getWorld().spawnParticle(org.bukkit.Particle.ENCHANT,
                player.getLocation().add(0,1,0), 20, 0.5,0.5,0.5, 0.2);
        }
    }

    private void heal(Player player, Skill skill) {
        double amt   = skill.get("heal-amount", 6.0);
        int regenDur = skill.get("regen-duration", 60);
        int regenAmp = skill.get("regen-amplifier", 0);
        player.setHealth(Math.min(player.getHealth() + amt, player.getMaxHealth()));
        player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.REGENERATION, regenDur, regenAmp));
        player.getWorld().spawnParticle(org.bukkit.Particle.HEART, player.getLocation().add(0,1,0), 8, 0.5,0.5,0.5,0);
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.8f);
    }

    private void dash(Player player, Skill skill) {
        double dist = skill.get("dash-distance", 6.0);
        player.setVelocity(player.getLocation().getDirection().normalize().multiply(dist).setY(0.3));
        player.getWorld().spawnParticle(org.bukkit.Particle.CLOUD, player.getLocation(), 15,0.3,0.2,0.3,0.05);
        player.getWorld().playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.5f);
    }

'''
    code = code[:old_activate.start()] + new_activate + code[old_activate.end():]
    with open(r'src/main/java/dev/elysium/combat/skill/SkillManager.java', 'w', encoding='utf-8') as f:
        f.write(code)
    print("Successfully restored effects!")