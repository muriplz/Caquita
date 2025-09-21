extends Node
signal user_changed(user)
signal user_logged_out

var _token_store = TokenStore.new()
var _current_user: User = null

func _ready() -> void:
	add_child(_token_store)
	attempt_restore_session()

func store_user(user_data: Dictionary) -> void:
	_current_user = User.from_dict(user_data)
	
	if "token" in user_data and not user_data["token"].is_empty():
		_token_store.save_token(user_data["token"])
	
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
	user_logged_out.emit()

func attempt_restore_session() -> bool:
	return _token_store.has_token()

func get_auth_header():
	return ["Cookie: auth=" + get_token()]
