import {getIpAddress} from "@/js/Static.js";

const API_URL = getIpAddress() + '/api/v1/landmarks';

export default class LandmarksApi {

    static async getLandmark(id) {
        const response = await fetch(API_URL + '/' + id, {
            credentials: 'include'
        });
        return await response.json();
    }


}