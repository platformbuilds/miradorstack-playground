import requests
import random
import string
import time
import signal
import sys

# Configuration
BASE_URL = "http://localhost:8081/api"  # Change to your server's IP if not localhost
MAX_KEYS = 10  # Maximum number of keys to keep before forcing deletions

# Global variables
keys = set()
running = True

def signal_handler(sig, frame):
    global running
    print("\nStopping the script...")
    running = False

def generate_random_string(length=8):
    return ''.join(random.choices(string.ascii_letters + string.digits, k=length))

def create_key():
    key = generate_random_string()
    value = generate_random_string(16)
    try:
        response = requests.post(f"{BASE_URL}/create", params={"key": key, "value": value})
        if response.status_code == 200:
            keys.add(key)
            print(f"Created key: {key}")
        else:
            print(f"Failed to create key: {key}, Status: {response.status_code}")
    except Exception as e:
        print(f"Error creating key: {e}")

def read_key():
    if keys:
        key = random.choice(list(keys))
        try:
            response = requests.get(f"{BASE_URL}/read/{key}")
            if response.status_code == 200:
                print(f"Read key: {key}, Data: {response.json()}")
            else:
                print(f"Failed to read key: {key}, Status: {response.status_code}")
        except Exception as e:
            print(f"Error reading key: {e}")
    else:
        print("No keys available to read")

def modify_key():
    if keys:
        key = random.choice(list(keys))
        value = generate_random_string(16)
        try:
            response = requests.put(f"{BASE_URL}/modify/{key}", params={"value": value})
            if response.status_code == 200:
                print(f"Modified key: {key}")
            else:
                print(f"Failed to modify key: {key}, Status: {response.status_code}")
        except Exception as e:
            print(f"Error modifying key: {e}")
    else:
        print("No keys available to modify")

def delete_key():
    if keys:
        key = random.choice(list(keys))
        try:
            response = requests.delete(f"{BASE_URL}/delete/{key}")
            if response.status_code == 200:
                keys.remove(key)
                print(f"Deleted key: {key}")
            else:
                print(f"Failed to delete key: {key}, Status: {response.status_code}")
        except Exception as e:
            print(f"Error deleting key: {e}")
    else:
        print("No keys available to delete")

def main():
    signal.signal(signal.SIGINT, signal_handler)
    operations = [create_key, read_key, modify_key, delete_key]

    print("Starting API invocation script. Press Ctrl+C to stop.")
    print(f"Base URL: {BASE_URL}")
    print(f"Max keys: {MAX_KEYS}")

    while running:
        # Force deletion if too many keys
        if len(keys) >= MAX_KEYS:
            delete_key()
        else:
            # Random operation
            op = random.choice(operations)
            op()

        # Wait before next operation
        time.sleep(random.randint(1, 5))

    print("Script stopped.")

if __name__ == "__main__":
    main()