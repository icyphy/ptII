from java.applet import Applet

class HelloWorld(Applet):
    def paint(self, g):
        g.drawString("Hello from Jython!", 20, 30)