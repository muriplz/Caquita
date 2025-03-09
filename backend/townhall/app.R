library(shiny)
library(shinyjs)
library(plotly)
library(ggplot2)

options(shiny.port = 6998)

source("ui/user_creation.R")
source("server/user_creation.R")
source("graph/user_creation.R")

source("db/postgres.R")

home_ui <- function() {
  fluidPage(
    h1("Townhall! (Developed by Muri)")
  )
}

ui <- fluidPage(
  useShinyjs(),
  uiOutput("page")
)

server <- function(input, output, session) {
  current_path <- reactiveVal("")
  
  observe({
    runjs('
      function getHash() {
        var hash = window.location.hash;
        return hash;
      }
      
      // Send the initial hash to Shiny
      Shiny.setInputValue("current_path", getHash());
      
      // Monitor hash changes
      window.addEventListener("hashchange", function() {
        Shiny.setInputValue("current_path", getHash());
      });
    ')
  })
  
  observeEvent(input$current_path, {
    path <- input$current_path
    current_path(path)
  })
  
  # Router
  output$page <- renderUI({
    path <- current_path()
    if (path == "" || path == "#/") {
      home_ui()
    } else if (path == "#/user_creation") {
      user_creation_ui("user_creation_module")
    } else {
      fluidPage(
        h1("404 Not Found")
      )
    }
  })
  
  observeEvent(current_path(), {
    if (current_path() == "#/user_creation") {
      callModule(user_creation_server, "user_creation_module")
    }
  })
}

shinyApp(ui = ui, server = server)