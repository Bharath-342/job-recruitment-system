import urllib.request
import json
import sys

api_key = 'rnd_GnM1aWloRAqTGTmH4ZsG3UZNKkuN'
headers = {
    'Authorization': f'Bearer {api_key}',
    'Accept': 'application/json',
    'User-Agent': 'Python'
}

def get(path):
    req = urllib.request.Request(f'https://api.render.com/v1/{path}', headers=headers)
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode())

owners = get('owners')
print('=== OWNERS ===')
print(json.dumps(owners, indent=2))

services = get('services')
print('=== SERVICES ===')
print(json.dumps(services, indent=2))

dbs = get('postgres')
print('=== DATABASES ===')
print(json.dumps(dbs, indent=2))
