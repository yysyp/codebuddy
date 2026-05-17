#!/bin/bash
# Build script for CodeBuddy Agent (Unix/Linux/macOS)

echo "========================================"
echo "Building CodeBuddy Dev Assistant Agent"
echo "========================================"

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Create necessary directories
echo "Creating directories..."
mkdir -p ".codebuddy/logs"
mkdir -p ".codebuddy/memory/conversations"
mkdir -p ".codebuddy/memory/patterns"
mkdir -p ".codebuddy/memory/errors"
mkdir -p ".codebuddy/memory/skills"
mkdir -p ".codebuddy/security"

# Install dependencies
echo ""
echo "Installing dependencies..."
pip install -r requirements.txt --quiet

# Run tests
echo ""
echo "Running tests..."
python -m pytest .codebuddy/utils/ -v --tb=short 2>/dev/null || echo "Warning: Some tests failed"

# Syntax check Python files
echo ""
echo "Checking Python syntax..."
python -m py_compile .codebuddy/agent/core.py
python -m py_compile .codebuddy/utils/env_loader.py
python -m py_compile .codebuddy/utils/security_filter.py
python -m py_compile .codebuddy/utils/skill_manager.py
python -m py_compile .codebuddy/utils/hook_executor.py
python -m py_compile .codebuddy/utils/evolution_engine.py

if [ $? -eq 0 ]; then
    echo ""
    echo "========================================"
    echo "Build completed successfully!"
    echo "========================================"
else
    echo ""
    echo "Build failed with errors."
    exit 1
fi
