import {getIpAddress} from "../Static.js";

class Cans {

    constructor() {
        this.apiUrl = getIpAddress() + '/api/v1/cans';
    }

    async addCan(name, lat, long) {
        const response = await fetch(this.apiUrl, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({name: name, lat: lat, long: long})
        });
        return await response.json();
    }
}