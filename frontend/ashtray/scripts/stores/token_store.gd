class_name TokenStore
extends Node

func save_token(token: String) -> void:
	if token.is_empty():
		return
	
	var file = FileAccess.open("user://auth_token.dat", FileAccess.WRITE)
	file.store_string(token)

func get_token() -> String:
	if not FileAccess.file_exists("user://auth_token.dat"):
		return ""
	
	var file = FileAccess.open("user://auth_token.dat", FileAccess.READ)
	var token = file.get_as_text()
	return token

func has_token() -> bool:
	return FileAccess.file_exists("user://auth_token.dat") and not get_token().is_empty()

func clear_token() -> void:
	if FileAccess.file_exists("user://auth_token.dat"):
		var dir = DirAccess.open("user://")
		if dir:
			dir.remove("auth_token.dat")
