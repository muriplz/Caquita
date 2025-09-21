extends Node

class Setting:
	var id: String
	var value: String
	func _init(_id: String = "", _value: String = ""):
		id = _id
		value = _value

var all_settings: Array[Setting] = []
const path := "user://settings.dat"

func _ready() -> void:
	all_settings = load_settings()

func add_setting(id: String, value: String):
	var setting = Setting.new(id, value)
	all_settings.append(setting)
	save_settings()

func get_setting(id: String) -> Setting:
	for setting in all_settings:
		if setting.id == id:
			return setting
	return null

func save_settings() -> void:
	if all_settings.is_empty():
		return
	var f = FileAccess.open(path, FileAccess.WRITE)
	f.store_var(all_settings)
	f.close()

func load_settings() -> Array[Setting]:
	if not FileAccess.file_exists(path):
		return []
	var f = FileAccess.open(path, FileAccess.READ)
	var settings: Array[Setting] = f.get_var()
	f.close()
	return settings

func has_settings() -> bool:
	return not all_settings.is_empty()
