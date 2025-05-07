extends Node
signal user_changed(user)
signal user_logged_out

var _token_store = TokenStore.new()
var _current_user: User = null

func _ready() -> void:
	add_child(_token_store)
	attempt_restore_session()

func store_user(user_data: Dictionary) -> void:
	_current_user = User.new(user_data)
	
	if "token" in user_data and not user_data["token"].is_empty():
		_token_store.save_token(user_data["token"])
	
	save_user_data(user_data)
	user_changed.emit(_current_user)

func get_user() -> User:
	return _current_user

func is_logged_in() -> bool:
	return _current_user != null and _token_store.has_token()

func get_token() -> String:
	return _token_store.get_token()

func clear_session() -> void:
	_current_user = null
	_token_store.clear_token()
	
	if FileAccess.file_exists("user://user_data.json"):
		DirAccess.open("user://").remove("user_data.json")
	
	user_logged_out.emit()

func save_user_data(user_data: Dictionary) -> void:
	var file = FileAccess.open("user://user_data.json", FileAccess.WRITE)
	var json_string = JSON.stringify(user_data)
	file.store_string(json_string)

func attempt_restore_session() -> bool:
	if not _token_store.has_token() or not FileAccess.file_exists("user://user_data.json"):
		return false
	
	var file = FileAccess.open("user://user_data.json", FileAccess.READ)
	var json_string = file.get_as_text()
	var data = JSON.parse_string(json_string)
	
	if data != null:
		_current_user = User.new(data)
		user_changed.emit(_current_user)
		return true
	
	return false

func get_auth_header() -> Dictionary:
	var token = get_token()
	if token.is_empty():
		return {}
	return {"Authorization": "Bearer " + token}
