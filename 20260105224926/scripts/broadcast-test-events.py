#!/usr/bin/env python3
"""
Python script to broadcast test events to the SSE demo
Requires: requests library (pip install requests)
"""

import requests
import json
import time
import random
from datetime import datetime

# Configuration
BASE_URL = "http://localhost:8080/sse-demo/api/sse/broadcast"

# Test event templates
EVENT_TEMPLATES = [
    {
        "eventType": "NOTIFICATION",
        "message": "System update available",
        "data": {"version": "2.0.1", "critical": False}
    },
    {
        "eventType": "ALERT",
        "message": "Unusual login pattern detected",
        "data": {"severity": "MEDIUM", "ip": "192.168.1.100"}
    },
    {
        "eventType": "METRICS",
        "message": "Performance snapshot",
        "data": {"cpu": random.randint(20, 80), "memory": random.randint(30, 70)}
    },
    {
        "eventType": "INFO",
        "message": "Background task completed",
        "data": {"taskId": f"task-{random.randint(1000, 9999)}"}
    }
]

def broadcast_event(event):
    """Broadcast a single event to the SSE server"""
    try:
        response = requests.post(BASE_URL, json=event, timeout=5)
        if response.status_code == 200:
            print(f"✓ Success: {event['eventType']} - {event['message']}")
            return True
        else:
            print(f"✗ Failed: Status {response.status_code}")
            return False
    except Exception as e:
        print(f"✗ Error: {e}")
        return False

def main():
    print("=" * 60)
    print("SSE Demo - Test Event Broadcaster")
    print("=" * 60)
    print()

    # Load test data from JSON if exists
    try:
        with open('generate-test-data.json', 'r') as f:
            test_data = json.load(f)
            print(f"Loaded {len(test_data)} test events from JSON file")
            print()
    except FileNotFoundError:
        test_data = []
        print("No test-data.json found, using template events")
        print()

    print("Starting event broadcast sequence...")
    print()

    # Broadcast events from JSON file
    for event in test_data:
        broadcast_event(event)
        time.sleep(2)

    # Broadcast template events
    for i in range(5):
        template = random.choice(EVENT_TEMPLATES)
        event = {
            "eventType": template["eventType"],
            "message": f"{template['message']} - {datetime.now().strftime('%H:%M:%S')}",
            "data": template["data"]
        }
        broadcast_event(event)
        time.sleep(1.5)

    print()
    print("=" * 60)
    print("Broadcast sequence completed!")
    print("=" * 60)

if __name__ == "__main__":
    main()
