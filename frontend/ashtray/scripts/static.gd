class_name Static
extends Node

const PRODUCTION: bool = false

static var API_URL: String:
	get:
		if PRODUCTION:
			return "https://caquita.app/"
		else:
			return "http://localhost:6996/"
