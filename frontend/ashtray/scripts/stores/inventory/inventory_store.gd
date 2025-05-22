extends Node

signal inventory_ready

var API_URL: String
var inventory

func _ready():
	API_URL = Static.API_URL + "api/v1/inventory"
	
func sync():
	var http_request = HTTPRequest.new()
	
	add_child(http_request)
	http_request.request_completed.connect(_on_response)
	var headers = [
		"Cookie: auth=%s" % UserStore.get_token()
	]
	
	http_request.request(API_URL, headers)

func _on_response(_result, code, _headers, body):
	if code != 200:
		return

	inventory = JSON.parse_string(body.get_string_from_utf8())	
	emit_signal("inventory_ready")

# API
func get_inventory() -> Inventory:
	return Inventory.from_dict(inventory)
