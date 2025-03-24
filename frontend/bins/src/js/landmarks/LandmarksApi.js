import {getIpAddress} from "@/js/static.js";

const API_URL = getIpAddress() + '/api/v1/landmarks';

export default class LandmarksApi {

    async getLandmarks(id) {
        const response = await fetch(API_URL + '/' + id, {
            credentials: 'include'
        });
        return await response.json();
    }


}