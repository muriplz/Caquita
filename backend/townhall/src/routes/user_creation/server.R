user_creation_server <- function(input, output, session) {
  # Fetch and process data in one reactive
  aggregated_data <- reactive({
    req(input$date_range)
    
    start_date <- format(input$date_range[1], "%Y-%m-%d")
    end_date <- format(input$date_range[2], "%Y-%m-%d")
    
    con <- connect_to_postgres()
    on.exit(dbDisconnect(con))
    
    # Query to get daily user registrations
    query <- sprintf("
      SELECT 
        DATE(creation) as day,
        COUNT(*) as count
      FROM users
      WHERE DATE(creation) BETWEEN '%s' AND '%s'
      GROUP BY day
      ORDER BY day;", start_date, end_date)
    
    result <- dbGetQuery(con, query)
    
    # Convert day to Date class
    result$day <- as.Date(result$day)
    
    result
  })
  
  output$user_creation_output <- renderPlotly({
    user_creation_graph(aggregated_data())
  })
}