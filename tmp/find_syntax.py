import re, sys

with open('/tmp/script.js') as f:
    text = f.read()

# Let's see if acorn is available or install it
import subprocess
res = subprocess.run(["npx", "acorn", "/tmp/script.js"], capture_output=True, text=True)
print("Acorn returncode:", res.returncode)
print("Acorn stdout:", res.stdout[:500])
print("Acorn stderr:", res.stderr[:500])
