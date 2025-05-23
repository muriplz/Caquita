const IP_ADDRESSES = {
    production: 'https://caquita.app',
    development: 'http://localhost:6996'
};

function isProduction() {
    return false;
}

function getIpAddress() {
    if (isProduction()) {
        return IP_ADDRESSES.production;
    } else {
        return IP_ADDRESSES.development;
    }
}

export { IP_ADDRESSES, getIpAddress, isProduction };