// Level.js
class Level {
    constructor(data) {
        this.level = data.level;
        this.experience = data.experience;
        this.levelProgress = data["level-progress"];
        this.levelTotal = data["level-total"];
        this.nextLevelTotal = data["next-level-total"];
    }

    // Calculate percentage of level completion
    getProgressPercentage() {
        return Math.min(100, Math.floor((this.levelProgress / this.levelTotal) * 100));
    }

    // Calculate remaining XP needed for next level
    getRemainingXp() {
        return this.levelTotal - this.levelProgress;
    }
}

export default Level;