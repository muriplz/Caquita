extends Node

signal item_kinds_ready

var API_URL: String
var item_kinds: Array = []    # array of Dictionaries
@onready var http_request := HTTPRequest.new()

func _ready():
	API_URL = Static.API_URL + "api/v1/item-kinds"
	add_child(http_request)
	http_request.request_completed.connect(_on_response)
	http_request.request(API_URL)

func _on_response(result, code, headers, body):
	if code != 200:
		return

	var arr = JSON.parse_string(body.get_string_from_utf8())
	if typeof(arr) != TYPE_ARRAY:
		return

	item_kinds = arr
	emit_signal("item_kinds_ready")

# API
func get_all_item_kinds() -> Array[ItemKind]:
	return item_kinds

func get_item_kind(id: String) -> Dictionary:
	for dict in item_kinds:
		if dict.get("id", "") == id:
			return dict
	return {}
