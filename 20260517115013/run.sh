#!/bin/bash
# Run script for CodeBuddy Agent (Unix/Linux/macOS)

echo "========================================"
echo "Running CodeBuddy Dev Assistant Agent"
echo "========================================"

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Set Python path
export PYTHONPATH="${PYTHONPATH}:${PWD}"

# Run the agent
python -m .codebuddy.agent.core
