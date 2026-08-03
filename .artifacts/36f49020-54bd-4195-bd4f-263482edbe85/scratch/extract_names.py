import json
import re

with open('F:/BrainBites/app/src/main/res/raw/background.json', 'r') as f:
    content = f.read()
    names = re.findall(r'"nm":"(.*?)"', content)
    unique_names = sorted(list(set(names)))
    for name in unique_names:
        print(name)
