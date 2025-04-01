import LandmarkTypes from "@/js/landmarks/LandmarkTypes.js";

const LEVEL = [
    "HIGH",
    "MEDIUM",
    "LOW"
];

const TRASH_CAN_FEATURES = [
    "ashtray",
    "inundated",
    "windblown",
    "overwhelmed",
];

const PLASTIC_CONTAINER_FEATURES = [
    "underground",
    "bottlenecked",
    "modern",
    "overwhelmed"
];

const LANDMARK_FEATURES = {
    [LandmarkTypes.LANDMARK_TYPES.TRASH_CAN]: TRASH_CAN_FEATURES,
    [LandmarkTypes.LANDMARK_TYPES.PLASTIC_CONTAINER]: PLASTIC_CONTAINER_FEATURES
};

export { LANDMARK_FEATURES, LEVEL };