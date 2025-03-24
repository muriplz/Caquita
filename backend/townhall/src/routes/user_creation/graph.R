library(ggplot2)
library(plotly)

user_creation_graph <- function(data) {
  if (nrow(data) == 0 || !all(c("day", "count") %in% colnames(data))) {
    stop("Data is empty or does not contain required columns.")
  }
  
  data$tooltip <- sprintf("Date: %s<br>New Users: %d", format(data$day, "%b %d, %Y"), as.integer(data$count))
  
  p <- ggplot(data, aes(x = day, y = count)) +
    geom_line(color = "#1982C4", linewidth = 1.5) +
    geom_point(color = "#1982C4", size = 3) +
    labs(title = "Daily User Registrations", x = "Date", y = "Number of New Users") +
    theme_minimal() +
    theme(plot.title = element_text(hjust = 0.5, size = 16, face = "bold"),
          axis.text = element_text(size = 12),
          axis.text.x = element_text(angle = 45, hjust = 1))
  
  ggplotly(p, tooltip = "text") %>%
    layout(hovermode = "closest", 
           hoverlabel = list(bgcolor = "white", font = list(color = "black")))
}