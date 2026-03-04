import os
import re

# Match emojis, symbols, and pictographs
emoji_pattern = re.compile("[\U00010000-\U0010ffff\u2600-\u27BF]")

def check_dir(d):
    for root, dirs, files in os.walk(d):
        if 'node_modules' in root or '.git' in root or 'target' in root or 'dist' in root:
            continue
        for f in files:
            if f.endswith(('.vue', '.js', '.java', '.html')):
                path = os.path.join(root, f)
                try:
                    with open(path, 'r', encoding='utf-8') as file:
                        for i, line in enumerate(file):
                            if emoji_pattern.search(line):
                                print(f"{path}:{i+1}: {line.strip()}")
                except Exception as e:
                    pass

print("Iniciando busca no FRONEND-SPACE...")
check_dir(r'C:\Users\T.I\Documents\GitHub\Space-Nails\FRONEND-SPACE')
print("Iniciando busca no BACKEND-SPACE...")
check_dir(r'C:\Users\T.I\Documents\GitHub\Space-Nails\BACKEND-SPACE')
print("Busca completa.")
