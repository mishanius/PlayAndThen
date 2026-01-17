#!/bin/bash

# Build All Games Script
# Builds all isolated games in the games directory

echo "🎮 Building All PlayAndThen Games"
echo "=================================="
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Track success/failure
SUCCESS_COUNT=0
FAIL_COUNT=0

# Explicitly list games to build
GAMES=("numbers" "match-words")

echo "Found ${#GAMES[@]} games to build"
echo ""

# Build each game
for game in "${GAMES[@]}"; do
  if [ ! -d "$game" ]; then
    echo "${RED}❌ Directory $game not found${NC}"
    ((FAIL_COUNT++))
    continue
  fi
  
  if [ ! -f "$game/package.json" ]; then
    echo "${RED}❌ $game/package.json not found${NC}"
    ((FAIL_COUNT++))
    continue
  fi
  
  echo "${BLUE}Building $game...${NC}"
  
  if (cd "$game" && npm install --silent && npm run build); then
    echo "${GREEN}✅ $game built successfully${NC}"
    ((SUCCESS_COUNT++))
  else
    echo "${RED}❌ $game build failed${NC}"
    ((FAIL_COUNT++))
  fi
  
  echo ""
done

# Summary
echo "=================================="
echo "Build Summary:"
echo "${GREEN}✅ Success: $SUCCESS_COUNT${NC}"
if [ $FAIL_COUNT -gt 0 ]; then
  echo "${RED}❌ Failed: $FAIL_COUNT${NC}"
fi
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
  echo "${GREEN}🎉 All games built successfully!${NC}"
  exit 0
else
  echo "${RED}⚠️  Some games failed to build${NC}"
  exit 1
fi
