# [Townhall](https://townhall.caquita.app) Statistics portal

[R 4.4.3](https://cran.r-project.org) + [Shiny](https://shiny.posit.co)

R libraries:
- [ShinyJs](https://deanattali.com/shinyjs/)
- [GGPlot2](https://ggplot2.tidyverse.org)
- [Plotly](https://plotly.com/r/)
- [DBI](https://dbi.r-dbi.org)
- [RPostgres](https://cran.r-project.org/web/packages/RPostgres/index.html)

*Recommended editor: [RStudio](https://posit.co/download/rstudio-desktop/)*
## Running

Create a `.Renviron`:
```yaml
POSTGRES_HOST=caquita.app
POSTGRES_PORT=YOUR_PORT
POSTGRES_DBNAME=YOUR_DB_NAME
POSTGRES_USER=postgres
POSTGRES_PASSWORD=YOUR_PASSWORD
```

```bash
shiny::runApp()
```
