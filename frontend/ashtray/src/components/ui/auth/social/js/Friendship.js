class Friendship {
    constructor(id, userId, friendId, status, creation, edition) {
        this.id = id;
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
        this.creation = creation;
        this.edition = edition;
    }

    // Status enum equivalent
    static Status = {
        PENDING: 'PENDING',
        ACCEPTED: 'ACCEPTED',
        BLOCKED: 'BLOCKED',
        REJECTED: 'REJECTED'
    };
}

export default Friendship;