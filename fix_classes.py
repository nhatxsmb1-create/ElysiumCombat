import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content)

cyml = '''classes:
  WARRIOR:
    display-name: "&c&l⚔ Lõi Cuồng Chiến"
    description: "Hạch tâm chứa sức mạnh của Bậc thầy cận chiến, chống chịu tốt và sinh tồn mạnh mẽ."
    item: NETHERITE_SWORD
    optimized-weapons:
      - "&fĐại Kiếm"
      - "&fHoa Kiếm"
      - "&fĐại Phủ"
    stats:
      bonus-hp: 20.0
      bonus-damage: 2.0
      defense: 25
      speed-modifier: -0.01
      mana-regen: 2
      max-mana-bonus: -20

  MAGE:
    display-name: "&9&l✦ Lõi Pháp Thần"
    description: "Điều khiển năng lượng vô tận, tung đòn phép thuật diện rộng."
    item: END_CRYSTAL
    optimized-weapons:
      - "&fTrượng Phép"
      - "&fOrb Năng Lượng"
      - "&fSách Ma Thuật"
    stats:
      bonus-hp: -6.0
      bonus-damage: 1.0
      defense: 5
      speed-modifier: 0.0
      mana-regen: 15
      max-mana-bonus: 150

  RANGER:
    display-name: "&a&l🏹 Lõi Phong Thần"
    description: "Chuyên gia đánh xa, cơ động và chính xác tuyệt đối."
    item: BOW
    optimized-weapons:
      - "&fCung Dài"
      - "&fNỏ Liên Châu"
      - "&fSong Súng"
    stats:
      bonus-hp: -2.0
      bonus-damage: 3.0
      defense: 10
      speed-modifier: 0.03
      mana-regen: 5
      max-mana-bonus: 20

  ASSASSIN:
    display-name: "&5&l🗡 Lõi Sát Nhân"
    description: "Ẩn mình trong bóng tối, một đòn chí mạng kết liễu kẻ thù."
    item: PHANTOM_MEMBRANE
    optimized-weapons:
      - "&fDao Găm"
      - "&fSong Kiếm"
      - "&fKatana"
    stats:
      bonus-hp: -8.0
      bonus-damage: 5.0
      defense: 0
      speed-modifier: 0.05
      mana-regen: 8
      max-mana-bonus: 0

  SUPPORT:
    display-name: "&e&l🛡 Lõi Hộ Vệ"
    description: "Trái tim của đội hình, cung cấp buff và hiệu ứng khống chế."
    item: TOTEM_OF_UNDYING
    optimized-weapons:
      - "&fKhiên Thánh"
      - "&fCờ Lệnh"
      - "&fNhẫn Hỗ Trợ"
    stats:
      bonus-hp: 10.0
      bonus-damage: -2.0
      defense: 15
      speed-modifier: 0.01
      mana-regen: 10
      max-mana-bonus: 50
'''
write_file('src/main/resources/classes.yml', cyml)
print("Fixed classes.yml")