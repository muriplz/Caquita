extends Button

const LOGIN_SCENE_PATH = "res://scenes/auth/login.tscn"

func _ready():
	pressed.connect(self._on_logout_pressed)

func _on_logout_pressed():
	UserStore.clear_session()
	get_tree().change_scene_to_file(LOGIN_SCENE_PATH)
