extends Node

signal inventory_ready
signal inventory_updated

var API_URL: String
var inventory: Inventory = null

func _ready():
	API_URL = Static.API_URL + "api/v1/inventory"
	
func sync():
	var http_request = HTTPRequest.new()
	add_child(http_request)
	http_request.request_completed.connect(_on_response)
	var headers = ["Cookie: auth=%s" % UserStore.get_token()]
	http_request.request(API_URL, headers)

func _on_response(_result, code, _headers, body):
	if code != 200:
		return
	var data = JSON.parse_string(body.get_string_from_utf8())
	inventory = Inventory.from_dict(data)
	emit_signal("inventory_ready")

func get_inventory() -> Inventory:
	return inventory

func update_inventory(response_data):
	inventory = Inventory.from_dict(response_data)
	emit_signal("inventory_updated")
