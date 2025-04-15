class PetitionMessage {
    constructor(id, petitionId, userId, content, creation) {
        this.id = id;
        this.petitionId = petitionId;
        this.userId = userId;
        this.content = content;
        this.creation = creation;
    }
}

export default PetitionMessage