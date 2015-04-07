import sys

try:
  from lxml import etree
  print("running with lxml.etree")
except ImportError:
  try:
    # Python 2.5
    import xml.etree.cElementTree as etree
    print("running with cElementTree on Python 2.5+")
  except ImportError:
    try:
      # Python 2.5
      import xml.etree.ElementTree as etree
      print("running with ElementTree on Python 2.5+")
    except ImportError:
      try:
        # normal cElementTree install
        import cElementTree as etree
        print("running with cElementTree")
      except ImportError:
        try:
          # normal ElementTree install
          import elementtree.ElementTree as etree
          print("running with ElementTree")
        except ImportError:
          print("Failed to import ElementTree from any known place")


tree = etree.parse(sys.argv[1])
root = tree.getroot()
sub_finder = etree.XPath("//entity[@class='org.hlacerti.lib.HlaSubscriber']")
subs = sub_finder(root)
for s in subs:

    find = False
    goodValue = True
    
    for prop in s.iterchildren("*"):
        if prop.get('name') == 'parameterName' :
            find = True
            if prop.get('value') == 'parameterName':
                goodValue = False
            break
    print '----'    
    if not find :
        print 'Not found for '+ s.get('name')
        node = etree.fromstring('<property name="parameterName" class="ptolemy.data.expr.Parameter" value="&quot;'+ s.get('name') +'&quot;"> \n  <display name="Name of the parameter to receive"/> </property> \n\n')
        s.insert(0,node)
    elif find and not goodValue:
        print 'found but wrong for '+ s.get('name')
        prop.set('value','&quot;'+s.get('name')+'&quot;')
    else:
        print 'Ok for '+ s.get('name')
et = etree.ElementTree(root)
et.write(sys.argv[2], pretty_print=True)        


