extends Node

signal item_kinds_ready

var API_URL: String
var item_kinds: Array[ItemKind] = []
@onready var http_request := HTTPRequest.new()

func _ready():
	API_URL = Static.API_URL + "api/v1/item-kinds"
	add_child(http_request)
	http_request.request_completed.connect(_on_response)
	http_request.request(API_URL)

func _on_response(result: int, code: int, headers: Array, body: PackedByteArray) -> void:
	if code != 200:
		return

	var text = body.get_string_from_utf8()
	var parser = JSON.new()
	var err = parser.parse(text)
	if err != OK:
		push_error("Failed to parse item kinds JSON: %s" % parser.get_error_message())
		return

	var data = parser.get_data()
	if typeof(data) != TYPE_ARRAY:
		push_error("Expected JSON array but got %s" % typeof(data))
		return

	item_kinds.clear()
	for entry in data:
		# ensure entry is a Dictionary
		if typeof(entry) == TYPE_DICTIONARY:
			item_kinds.append(ItemKind.from_dict(entry))
	emit_signal("item_kinds_ready")

# API
func get_all_item_kinds() -> Array[ItemKind]:
	return item_kinds

func get_item_kind(id: String) -> ItemKind:
	for ik in item_kinds:
		if ik.id == id: return ik
	return null
