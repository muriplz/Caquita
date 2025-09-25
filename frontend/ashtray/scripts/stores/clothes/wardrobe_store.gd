extends Node

signal wardrobe_ready
signal wardrobe_updated

var API_URL: String
var wardrobe: Wardrobe = null

func _ready():
	API_URL = Static.API_URL + "api/v1/wardrobe"
	var http_request = HTTPRequest.new()
	add_child(http_request)
	http_request.request_completed.connect(_on_response)
	var headers = UserStore.get_auth_header()
	http_request.request(API_URL, headers)

func _on_response(_result, code, _headers, body):
	if code != 200:
		return
	var data = JSON.parse_string(body.get_string_from_utf8())
	wardrobe = Wardrobe.from_dict(data)
	emit_signal("wardrobe_ready")

func get_wardrobe() -> Wardrobe:
	return wardrobe

func wardrobe_inventory(response_data):
	wardrobe = Wardrobe.from_dict(response_data)
	emit_signal("wardrobe_updated")
