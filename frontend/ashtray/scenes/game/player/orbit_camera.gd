extends Camera3D

@onready var player = $".."

func _ready():
	ToastManager.show("Hi")
