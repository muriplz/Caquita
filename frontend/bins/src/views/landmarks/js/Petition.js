import User from "@/js/auth/User.js";

class Petition {
    constructor(id, userId, lat, lon, landmarkInfo, accepted, creation, edition) {
        this.id = id;
        this.userId = userId;
        this.lat = lat;
        this.lon = lon;
        this.landmarkInfo = landmarkInfo;
        this.accepted = accepted;
        this.creation = creation;
        this.edition = edition;
    }
}

export default Petition