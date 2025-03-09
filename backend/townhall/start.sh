#!/bin/bash
# Configuration
APP_DIR="/var/html/caquita.app/backend/townhall/"  # Directory containing app.R
LOG_FILE="app.log"  # Log file for app output
PORT=6998  # Port for Shiny app
REQUIRED_PACKAGES=("shiny" "shinyjs" "ggplot2" "plotly" "DBI" "RPostgres")

# Check if R is installed
if ! command -v R &> /dev/null; then
    echo "R is not installed. Please install R first."
    exit 1
fi

# Install system dependencies
echo "Installing system dependencies..."
sudo apt update
sudo apt install -y libcurl4-openssl-dev libssl-dev

# Navigate to the app directory
cd "$APP_DIR" || { echo "Failed to navigate to $APP_DIR"; exit 1; }

# Function to install R packages if they are not already installed
install_r_packages() {
  for pkg in "${REQUIRED_PACKAGES[@]}"; do
    echo "Installing or checking R package: $pkg"
    R -e "if (!requireNamespace('$pkg', quietly = TRUE)) install.packages('$pkg', repos = 'https://cloud.r-project.org/')"
  done
}

# Install required R packages
echo "Checking and installing required R packages..."
install_r_packages

# Check if the app is already running on the specified port
PID=$(lsof -t -i:$PORT)
if [ -n "$PID" ]; then
  echo "A process is already running on port $PORT (PID: $PID)."
  exit 1
fi

# Start the app in the background with specific port
echo "Starting Shiny app on port $PORT..."
nohup R -e "shiny::runApp(appDir='$APP_DIR', port=$PORT, host='0.0.0.0')" > "$LOG_FILE" 2>&1 &

# Get the PID of the app
APP_PID=$!

# Wait for the app to start
sleep 5

# Check if the app is still running
if ps -p "$APP_PID" > /dev/null; then
  echo "Shiny app started successfully!"
  echo "App PID: $APP_PID"
  echo "Log file: $LOG_FILE"
  echo "Access the app at: http://127.0.0.1:$PORT/"
  echo "To stop the app, run: kill $APP_PID"
else
  echo "Failed to start the Shiny app. Check the log file for details: $LOG_FILE"
  cat "$LOG_FILE"
  exit 1
fi