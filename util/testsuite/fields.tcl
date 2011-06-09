set className java.awt.event.InputEvent
set c [java::call Class forName $className]
set fieldsArray [$c getDeclaredFields]
set fieldsList [$fieldsArray getrange]
foreach field $fieldsList {
    if [catch {
        puts "[$field toString] [java::field $className [$field getName]]"
    } err] {
        puts "[$field toString]"
    }
}
