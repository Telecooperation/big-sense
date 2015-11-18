var big = big || {};

big.vis = function() {
  function tabulate(field, data, columns) {
    d3.select("#"+field).html("");
      
    var table = d3.select("#"+field).append("table").attr("width", "100%"),
      thead = table.append("thead"),
      tbody = table.append("tbody");
          
    // append the header row
    thead.append("tr")
      .selectAll("th")
      .data(columns)
      .enter()
      .append("th")
        .html(function(column) { return column; });
      
    // create a row for each object in the data
    var rows = tbody.selectAll("tr")
      .data(data)
      .enter()
      .append("tr");

    // create a cell in each row for each column
    var cells = rows.selectAll("td")
      .data(function(row) {
        return Object.keys(row).map(function(column) {
          return {column: column, value: row[column]};
        });
      })
      .enter()
      .append("td")
        .html(function(d) { return d.value; });
      
    return table;
  }
  
  return {
    tabulate: tabulate
  }
}();