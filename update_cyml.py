import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

cyml = '''# ElysiumCombat Config
prefix: "&c[Combat] &r"

skill-item:
  material: NETHER_STAR
  slot-1: 6
  slot-2: 7
  slot-3: 8

mana:
  display-interval: 20
  regen-interval: 40

cooldown-update-interval: 20

class-change:
  enabled: true
  cost: 50000.0
'''
write_file('src/main/resources/config.yml', cyml)
print("Updated config.yml")