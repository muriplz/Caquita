extends Control

@onready var username_label = $Username

func _ready():
	username_label.text = UserStore.get_user().username
