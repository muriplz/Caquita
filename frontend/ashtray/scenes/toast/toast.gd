extends Control

signal finished

@export var duration: float = 5.0

var label
var message_text: String = ""

func _ready():
	label = $PanelContainer/MarginContainer/Label
	
	label.text = message_text
	
	# Start timer for auto-dismiss
	var timer = get_tree().create_timer(duration)
	timer.timeout.connect(func(): 
		_dismiss()
	)

func set_message(text: String):
	message_text = text
	
	if label:
		label.text = text

func _gui_input(event):
	if event is InputEventMouseButton and event.pressed and event.button_index == MOUSE_BUTTON_LEFT:
		_dismiss()

func _dismiss():
	emit_signal("finished")
	queue_free()
