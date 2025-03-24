library(DBI)
library(RPostgres)

connect_to_postgres <- function() {
  con <- tryCatch(
    {
      message("Attempting to connect to PostgreSQL...")
      host <- Sys.getenv("POSTGRES_HOST")
      port <- as.numeric(Sys.getenv("POSTGRES_PORT"))
      dbname <- Sys.getenv("POSTGRES_DBNAME")
      user <- Sys.getenv("POSTGRES_USER")
      password <- Sys.getenv("POSTGRES_PASSWORD")
      dbConnect(
        RPostgres::Postgres(),
        host = host,
        port = port,
        dbname = dbname,
        user = user,
        password = password
      )
    },
    error = function(e) {
      message("Failed to connect to PostgreSQL.")
      message("Error details: ", e$message)
      stop("Failed to connect to PostgreSQL: ", e$message)
    }
  )
  
  message("Successfully connected to PostgreSQL.")
  return(con)
}