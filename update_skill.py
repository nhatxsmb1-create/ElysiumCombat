import re

with open(r'src/main/java/dev/elysium/combat/skill/SkillManager.java', 'r', encoding='utf-8') as f:
    text = f.read()

text = text.replace('CoreAPI.getCore().getCooldownManager().setCooldown(player.getUniqueId(), cdKey, skill.getCooldownSeconds());', 'CoreAPI.getCore().getCooldownManager().set(player.getUniqueId(), cdKey, skill.getCooldownSeconds() * 1000L);')

with open(r'src/main/java/dev/elysium/combat/skill/SkillManager.java', 'w', encoding='utf-8') as f:
    f.write(text)