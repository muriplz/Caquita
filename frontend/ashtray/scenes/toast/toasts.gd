extends Node

var toast_scene = preload("res://scenes/toast/toast.tscn")
var active_toasts = []
var toast_queue = []
var max_visible_toasts = 20
var toast_spacing = 5
var toast_height = 40

func show(message: String, duration: float = 5.0):
	toast_queue.append({"message": message, "duration": duration})
	_process_queue()

func _process_queue():
	if active_toasts.size() < max_visible_toasts and toast_queue.size() > 0:
		var toast_data = toast_queue.pop_front()
		_create_toast(toast_data.message, toast_data.duration)

func _create_toast(message: String, duration: float):
	var toast = toast_scene.instantiate()
	
	toast.name = "Toast_" + str(Time.get_ticks_msec())
	
	toast.set_message(message)
	toast.duration = duration

	var viewport_size = get_viewport().get_visible_rect().size
	var toast_width = min(viewport_size.x * 0.8, 500)
	
	# Add to scene tree first
	add_child(toast)
	
	toast.size.x = toast_width
	
	# Calculate position
	var pos_x = (viewport_size.x - toast_width) / 2
	var pos_y = toast_spacing
	
	# Stack toasts
	for i in range(active_toasts.size()):
		pos_y += toast_height + toast_spacing
	
	toast.position = Vector2(pos_x, pos_y)

	# Register the toast
	active_toasts.append(toast)
	toast.connect("finished", _on_toast_finished.bind(toast))

func _on_toast_finished(toast):
	active_toasts.erase(toast)
	_reposition_toasts()
	_process_queue()

func _reposition_toasts():
	var viewport_size = get_viewport().get_visible_rect().size
	var toast_width = min(viewport_size.x * 0.8, 500)
	var pos_x = (viewport_size.x - toast_width) / 2
	var pos_y = 20
	
	for i in range(active_toasts.size()):
		active_toasts[i].position = Vector2(pos_x, pos_y)
		pos_y += toast_height + toast_spacing
