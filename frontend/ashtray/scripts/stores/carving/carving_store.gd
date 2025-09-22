extends Node

signal carving_site_ready
signal carving_site_updated
signal carving_site_cleared

var carving_site: CarvingSite = null

func set_carving_site(new_carving_site: Dictionary):
	print(new_carving_site)
	carving_site = CarvingSite.from_dict(new_carving_site)
	emit_signal("carving_site_ready")

func get_carving_site() -> CarvingSite:
	return carving_site

func update_carving_site(updated_carving_site: Dictionary):
	carving_site = CarvingSite.from_dict(updated_carving_site)
	emit_signal("carving_site_updated")

func clear_carving_site():
	carving_site = null
	emit_signal("carving_site_cleared")

func has_carving_site() -> bool:
	return carving_site != null
