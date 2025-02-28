import {getIpAddress} from "../static.js";

class Landmarks {
    constructor() {
        this.apiUrl = getIpAddress() + '/api/v1/landmarks';
    }

    async getLandmarks(lat, lon, radius) {
        const body = JSON.stringify({lat: lat, lon: lon, radius: radius});
        const response = await fetch(this.apiUrl, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
            body: body
        });
        return await response.json();
    }


}