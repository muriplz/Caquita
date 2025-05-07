class Level {
    level: number;
    experience: number;
    progress: number;
    total: number;

    constructor(data: any) {
        this.level = data.level;
        this.experience = data.experience;
        this.progress = data.progress;
        this.total = data.total;
    }
}

export default Level;