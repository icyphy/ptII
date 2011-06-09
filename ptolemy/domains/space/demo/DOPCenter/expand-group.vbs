' List the members of a group in the EECS Windows domain.
' larsrohr@eecs, UCB EECS CUSG, 7/27/09
' $Id$
' To run this under Windows:
'   cscript expand-group.vbs "EDA Users"

On Error Resume Next

Const ADS_SCOPE_SUBTREE = 2
LF = chr(10)
Dim arrUsernames()
Dim arrNames()
intSize = 0


' Unless exactly 1 argument was supplied, display usage info:
If WScript.Arguments.Count <> 1 Then
    WScript.Echo "EXPAND-GROUP.VBS: Lists the members of a given EECS Windows group," & LF _
& "                  sorted by username." & LF & "EXPAND-GROUP.VBS groupname"
    WScript.Quit
End If


' The sole argument should be the group name:
arg1 = WScript.Arguments.Item(0)


' Locate the group in Active Directory:
Set objConnection = CreateObject("ADODB.Connection")
Set objCommand =   CreateObject("ADODB.Command")
objConnection.Provider = "ADsDSOObject"
objConnection.Open "Active Directory Provider"
Set objCommand.ActiveConnection = objConnection

objCommand.Properties("Page Size") = 1000
objCommand.Properties("Searchscope") = ADS_SCOPE_SUBTREE 

objCommand.CommandText = _
    "SELECT ADsPath FROM 'LDAP://DC=EECS,DC=Berkeley,DC=EDU' WHERE objectCategory='group' " & _
        "AND name='" & arg1 & "'"
Set objRecordSet = objCommand.Execute

objRecordSet.MoveFirst
Do Until objRecordSet.EOF
'Wscript.Echo objRecordSet.Fields("ADsPath").Value
    myADsPath = objRecordSet.Fields("ADsPath").Value
    objRecordSet.MoveNext
Loop

Set objGroup = GetObject(myADsPath)


' Create the array of member names, and the corresponding array of usernames:
For Each objUser in objGroup.Members
    ReDim Preserve arrNames(intSize)
    ReDim Preserve arrUsernames(intsize)
    arrNames(intSize) = objUser.DisplayName
    arrUsernames(intSize) = objUser.samAccountName
    intSize = intSize + 1
Next



' Alphabetize the arrays, sorting by username:
For i = (UBound(arrUsernames) - 1) to 0 Step -1
    For j= 0 to i
        If UCase(arrUsernames(j)) > UCase(arrUsernames(j+1)) Then
            strHolder1 = arrUsernames(j+1)
                strHolder2 = arrNames(j+1)
            arrUsernames(j+1) = arrUsernames(j)
                arrNames(j+1) = arrNames(j)
            arrUsernames(j) = strHolder1
                arrNames(j) = strHolder2
        End If
    Next
Next


' Now prepare to display the names:
myList = arg1 & ":" & LF
For i = 0 to (UBound(arrUsernames) - 1) Step 1
    nameLength = Len(arrUsernames(i))
    spacesToAdd = 10 - nameLength
    displayUsername = " (" & arrUsernames(i) & ")" & Space(spacesToAdd)
    myList = myList & displayUsername & arrNames(i) & LF
Next


' Display the output:
Wscript.Echo myList
