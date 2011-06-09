Const xlSurface = 83

Set oArgs=WScript.Arguments

Set objExcel = CreateObject("Excel.Application")
objExcel.Visible = False

Set objWorkbook = objExcel.Workbooks.Open (oArgs(0))

Set sheet = objExcel.ActiveSheet

sheet.Cells.Select
 
objExcel.Charts.Add
objExcel.ActiveChart.ChartType = xlSurface

objExcel.Visible = True
