library(shiny)

user_creation_ui <- function(id) {
  ns <- NS(id)
  fluidPage(
    titlePanel("User Registrations per Day"),
    sidebarLayout(
      sidebarPanel(
        dateRangeInput(ns("date_range"), "Select Date Range:", 
                       start = Sys.Date() - 30, 
                       end = Sys.Date())
      ),
      mainPanel(
        plotlyOutput(ns("user_creation_output"))
      )
    )
  )
}