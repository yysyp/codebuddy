#!/bin/bash
# Run script for Transaction Tagging System on Linux/Mac
# Usage: ./run.sh [control-panel|data-panel|all]

set -e

COMPONENT=${1:-all}

echo "============================================"
echo "Transaction Tagging System - Run Script"
echo "============================================"
echo
echo "Component: $COMPONENT"
echo

run_control_panel() {
    echo "Starting Control Panel..."
    echo
    cd control-panel
    java -jar target/control-panel-1.0.0-SNAPSHOT.jar
}

run_data_panel() {
    echo "Starting Data Panel..."
    echo
    cd data-panel
    java -cp "target/data-panel-1.0.0-SNAPSHOT.jar:$FLINK_HOME/lib/*" \
         com.transaction.tagging.datapanel.DataPanelApplication
}

run_all() {
    echo "Starting all components..."
    echo
    echo "Starting Control Panel in background..."
    cd control-panel
    java -jar target/control-panel-1.0.0-SNAPSHOT.jar &
    CP_PID=$!
    cd ..
    
    sleep 10
    
    echo
    echo "Starting Data Panel..."
    cd data-panel
    java -cp "target/data-panel-1.0.0-SNAPSHOT.jar:$FLINK_HOME/lib/*" \
         com.transaction.tagging.datapanel.DataPanelApplication
    
    # Wait for Control Panel
    wait $CP_PID
}

case $COMPONENT in
    control-panel)
        run_control_panel
        ;;
    data-panel)
        run_data_panel
        ;;
    all)
        run_all
        ;;
    *)
        echo "Usage: ./run.sh [control-panel|data-panel|all]"
        echo
        echo "  control-panel - Start only the Control Panel (rule management API)"
        echo "  data-panel    - Start only the Data Panel (Flink processor)"
        echo "  all           - Start both Control Panel and Data Panel"
        exit 1
        ;;
esac
