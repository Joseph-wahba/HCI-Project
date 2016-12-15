

import edu.berkeley.guir.lib.awt.geom.AffineTransformLib;
import edu.berkeley.guir.lib.awt.geom.Polygon2D;
import edu.berkeley.guir.lib.collection.SortedValueMap;
import edu.berkeley.guir.lib.debugging.Debug;
import edu.berkeley.guir.lib.satin.SatinConstants;
import edu.berkeley.guir.lib.satin.command.CommandQueue;
import edu.berkeley.guir.lib.satin.command.CopyCommand;
import edu.berkeley.guir.lib.satin.command.CutCommand;
import edu.berkeley.guir.lib.satin.command.DeleteCommand;
import edu.berkeley.guir.lib.satin.command.PasteCommand;
import edu.berkeley.guir.lib.satin.event.SatinEvent;
import edu.berkeley.guir.lib.satin.event.SingleStrokeEvent;
import edu.berkeley.guir.lib.satin.event.StrokeEvent;
import edu.berkeley.guir.lib.satin.interpreter.GestureCommandInterpreterImpl;
import edu.berkeley.guir.lib.satin.interpreter.InterpreterImpl;
import edu.berkeley.guir.lib.satin.objects.GraphicalObject;
import edu.berkeley.guir.lib.satin.objects.GraphicalObjectCollection;
import edu.berkeley.guir.lib.satin.objects.GraphicalObjectCollectionImpl;
import edu.berkeley.guir.lib.satin.objects.GraphicalObjectGroup;
import edu.berkeley.guir.lib.satin.objects.GraphicalObjectImpl;
import edu.berkeley.guir.lib.satin.objects.GraphicalObjectLib;
import edu.berkeley.guir.lib.satin.recognizer.Classification;
import edu.berkeley.guir.lib.satin.stroke.TimedStroke;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Iterator;
import javax.swing.undo.UndoManager;

public class StandardGestureInterpreter
  extends GestureCommandInterpreterImpl
{
  static final long serialVersionUID = 1118908262546911989L;
  
  class StandardRubineInterpreter
    extends RubineInterpreter
  {
    public StandardRubineInterpreter(String paramString)
    {
      super(paramString,true);
    }
    
    protected void handleSingleStroke(SingleStrokeEvent paramSingleStrokeEvent, Classification paramClassification)
    {
      String str = (String)paramClassification.getFirstKey();
      TimedStroke localTimedStroke = paramSingleStrokeEvent.getStroke();
      str = str.trim();
      if (localTimedStroke.getLength2D(12) < 20.0D) {
        return;
      }
      System.out.println(str);
      if (str.startsWith("Copy"))
      {
        StandardGestureInterpreter.this.handleCopy(localTimedStroke);
        paramSingleStrokeEvent.setConsumed("Copy");
      }
      if (str.startsWith("Cut"))
      {
        StandardGestureInterpreter.this.handleCut(localTimedStroke);
        paramSingleStrokeEvent.setConsumed("Cut");
      }
      if (str.startsWith("Paste"))
      {
        StandardGestureInterpreter.this.handlePaste(localTimedStroke);
        paramSingleStrokeEvent.setConsumed("Paste");
      }
      if (str.startsWith("Delete"))
      {
        StandardGestureInterpreter.this.handleDelete(localTimedStroke);
        paramSingleStrokeEvent.setConsumed("Delete");
      }
      if (str.startsWith("Undo"))
      {
        StandardGestureInterpreter.this.handleUndo();
        paramSingleStrokeEvent.setConsumed("Undo");
      }
      if (str.startsWith("Redo"))
      {
        StandardGestureInterpreter.this.handleRedo();
        paramSingleStrokeEvent.setConsumed("Redo");
      }
    }
    
    public Object clone()
    {
      return new StandardRubineInterpreter(this.getFileName());
    }
  }
  
  private static final Debug debug = new Debug();
  protected boolean flagShallow = true;
  protected RubineInterpreter intrp;
  
  public StandardGestureInterpreter()
  {
    this.intrp = new StandardRubineInterpreter("Gestures.gsa");
    commonInitializations();
  }
  
  private void commonInitializations()
  {
    setName("Abdo");
  }
  
  public void setShallow()
  {
    this.flagShallow = true;
  }
  
  public void setDeep()
  {
    this.flagShallow = false;
  }
  
  public void handleSingleStroke(SingleStrokeEvent paramSingleStrokeEvent)
  {
    this.intrp.handleSingleStroke(paramSingleStrokeEvent);
  }
  
  protected void handleUndo()
  {
    try
    {
      SatinConstants.cmdqueue.undo();
      getAttachedGraphicalObject().damage(21);
    }
    catch (Exception localException)
    {
      debug.println(localException);
    }
  }
  
  protected void handleRedo()
  {
    try
    {
      SatinConstants.cmdqueue.redo();
      getAttachedGraphicalObject().damage(21);
    }
    catch (Exception localException)
    {
      debug.println(localException);
    }
  }
  
  protected void handleCut(TimedStroke paramTimedStroke)
  {
    CutCommand localCutCommand = new CutCommand();
    
    GraphicalObjectCollection localGraphicalObjectCollection = getGraphicalObjectsTouching(paramTimedStroke, 2.0D);
    Iterator localIterator = localGraphicalObjectCollection.getForwardIterator();
    GraphicalObject localGraphicalObject1 = getAttachedGraphicalObject();
    while (localIterator.hasNext())
    {
      GraphicalObject localGraphicalObject2 = (GraphicalObject)localIterator.next();
      if (localGraphicalObject2 != localGraphicalObject1) {
        localCutCommand.addGraphicalObject(localGraphicalObject2);
      }
    }
    SatinConstants.cmdqueue.doCommand(localCutCommand);
  }
  
  protected void handleCopy(TimedStroke paramTimedStroke)
  {
    GraphicalObject localGraphicalObject = getGraphicalObjectCenteredAt(paramTimedStroke);
    if ((localGraphicalObject != null) && (localGraphicalObject != getAttachedGraphicalObject())) {
      SatinConstants.cmdqueue.doCommand(new CopyCommand(localGraphicalObject));
    }
  }
  
  protected void handlePaste(TimedStroke paramTimedStroke)
  {
    GraphicalObject localGraphicalObject = getAttachedGraphicalObject();
    Rectangle2D localRectangle2D = paramTimedStroke.getBounds2D(12);
    Point2D.Float localFloat = new Point2D.Float((float)(localRectangle2D.getX() + localRectangle2D.getWidth() / 2.0D), (float)(localRectangle2D.getY() + localRectangle2D.getHeight()));
    
    GraphicalObjectLib.absoluteToLocal(localGraphicalObject, localFloat, localFloat);
    if ((localGraphicalObject instanceof GraphicalObjectGroup)) {
      SatinConstants.cmdqueue.doCommand(new PasteCommand((GraphicalObjectGroup)localGraphicalObject, localFloat.getX(), localFloat.getY()));
    }
  }
  
  protected void handleDelete(TimedStroke paramTimedStroke)
  {
    GraphicalObject localGraphicalObject = getGraphicalObjectCenteredAt(paramTimedStroke);
    if ((localGraphicalObject != null) && (localGraphicalObject != getAttachedGraphicalObject())) {
      SatinConstants.cmdqueue.doCommand(new DeleteCommand(localGraphicalObject));
    }
  }
  
  protected void handleCenter(TimedStroke paramTimedStroke)
  {
    GraphicalObject localGraphicalObject = getAttachedGraphicalObject();
    Rectangle2D localRectangle2D1 = localGraphicalObject.getBounds2D();
    Rectangle2D localRectangle2D2 = paramTimedStroke.getBounds2D();
    double d1 = localRectangle2D1.getX() + localRectangle2D1.getWidth() / 2.0D;
    double d2 = localRectangle2D1.getY() + localRectangle2D1.getHeight() / 2.0D;
    double d3 = localRectangle2D2.getX() + localRectangle2D2.getWidth() / 2.0D;
    double d4 = localRectangle2D2.getY() + localRectangle2D2.getHeight() / 2.0D;
    
    AffineTransform[] arrayOfAffineTransform = AffineTransformLib.animateSlowInSlowOut(AffineTransform.getTranslateInstance(d1 - d3, d2 - d4), 10);
    
    GraphicalObjectLib.animate(localGraphicalObject, arrayOfAffineTransform);
  }
  
  protected void handleViewPort(String paramString, TimedStroke paramTimedStroke)
  {
    AffineTransform localAffineTransform = new AffineTransform();
    GraphicalObject localGraphicalObject = getAttachedGraphicalObject();
    double d = paramTimedStroke.getLength2D(12);
    if (paramString.equalsIgnoreCase("ViewPortLeft")) {
      localAffineTransform.translate(d, 0.0D);
    }
    if (paramString.equalsIgnoreCase("ViewPortRight")) {
      localAffineTransform.translate(-d, 0.0D);
    }
    if (paramString.equalsIgnoreCase("ViewPortUp")) {
      localAffineTransform.translate(0.0D, d);
    }
    if (paramString.equalsIgnoreCase("ViewPortDown")) {
      localAffineTransform.translate(0.0D, -d);
    }
    if (paramString.equalsIgnoreCase("ViewPortDiagUpRight")) {
      localAffineTransform.translate(-d / 2.0D, d / 2.0D);
    }
    if (paramString.equalsIgnoreCase("ViewPortDiagUpLeft")) {
      localAffineTransform.translate(d / 2.0D, d / 2.0D);
    }
    if (paramString.equalsIgnoreCase("ViewPortDiagDownRight")) {
      localAffineTransform.translate(-d / 2.0D, -d / 2.0D);
    }
    if (paramString.equalsIgnoreCase("ViewPortDiagDownLeft")) {
      localAffineTransform.translate(d / 2.0D, -d / 2.0D);
    }
    AffineTransform[] arrayOfAffineTransform = AffineTransformLib.animateSlowInSlowOut(localAffineTransform, 8);
    GraphicalObjectLib.animate(localGraphicalObject, arrayOfAffineTransform);
  }
  
  protected GraphicalObjectCollection getGraphicalObjectsTouching(TimedStroke paramTimedStroke, double paramDouble)
  {
    if (!(getAttachedGraphicalObject() instanceof GraphicalObjectGroup)) {
      return null;
    }
    GraphicalObject localGraphicalObject = getAttachedGraphicalObject();
    Object localObject1 = null;
    Polygon2D localPolygon2D = paramTimedStroke.getBoundingPoints2D(12);
    GraphicalObjectGroup localGraphicalObjectGroup = (GraphicalObjectGroup)localGraphicalObject;
    Object localObject2 = new GraphicalObjectCollectionImpl();
    int i;
    if (this.flagShallow == true) {
      i = 40;
    } else {
      i = 41;
    }
    localObject2 = localGraphicalObjectGroup.getGraphicalObjects(12, localPolygon2D, 30, i, 50, paramDouble, (GraphicalObjectCollection)localObject2);
    
    return (GraphicalObjectCollection)localObject2;
  }
  
  protected GraphicalObject getGraphicalObjectCenteredAt(TimedStroke paramTimedStroke)
  {
    return getGraphicalObjectCenteredAt(paramTimedStroke, 5.0D);
  }
  
  protected GraphicalObject getGraphicalObjectCenteredAt(TimedStroke paramTimedStroke, double paramDouble)
  {
    GraphicalObject localGraphicalObject = null;
    
    GraphicalObjectCollection localGraphicalObjectCollection = getGraphicalObjectsTouching(paramTimedStroke, paramDouble);
    if (localGraphicalObjectCollection.numElements() > 0) {
      localGraphicalObject = GraphicalObjectLib.getTopmostGraphicalObject(localGraphicalObjectCollection);
    }
    return localGraphicalObject;
  }
  
  public Object clone()
  {
    return new StandardGestureInterpreter();
  }
}
