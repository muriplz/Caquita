import User from "@/js/auth/User.ts";

class Petition {
    constructor(id, description, userId, lat, lon, landmarkInfo, status, creation, edition, image) {
        this.id = id;
        this.description = description;
        this.userId = userId;
        this.lat = lat;
        this.lon = lon;
        this.landmarkInfo = landmarkInfo;
        this.status = status;
        this.creation = creation;
        this.edition = edition;
        this.image = image;
    }
}

export default Petition