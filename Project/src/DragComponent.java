import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.*;
import javax.swing.text.*;

import java.lang.*;
import java.util.*;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;
import java.io.*;
import java.util.Iterator;
import java.util.List;

import edu.berkeley.guir.lib.awt.geom.Polygon2D;
import edu.berkeley.guir.lib.satin.*;
import edu.berkeley.guir.lib.satin.event.NewStrokeEvent;
import edu.berkeley.guir.lib.satin.event.SingleStrokeEvent;
import edu.berkeley.guir.lib.satin.event.UpdateStrokeEvent;
import edu.berkeley.guir.lib.satin.graphics.SatinGraphics;
import edu.berkeley.guir.lib.satin.interpreter.*;
import edu.berkeley.guir.lib.satin.interpreter.commands.*;
import edu.berkeley.guir.lib.satin.objects.GObImage;
import edu.berkeley.guir.lib.satin.objects.GObJComponentWrapper;
import edu.berkeley.guir.lib.satin.objects.GObText;
import edu.berkeley.guir.lib.satin.objects.GraphicalObject;
import edu.berkeley.guir.lib.satin.objects.GraphicalObjectGroup;
import edu.berkeley.guir.lib.satin.objects.Style;
import edu.berkeley.guir.lib.satin.view.View;
import edu.berkeley.guir.lib.satin.watch.Watcher;

public class DragComponent extends JPanel
{
	
    public static DataFlavor COMPONENT_FLAVOR;
    public static Sheet  s = new Sheet();
    public static JLabel bg ;

    public DragComponent()
    {
//        try
//        {
//            COMPONENT_FLAVOR = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + Component[].class.getName() + "\"");
//        }
//        catch(Exception e)
//        {
//            System.out.println(e);
//        }

        setLayout(null);
        setTransferHandler( new PanelHandler() );

        MouseListener listener = new MouseAdapter()
        {
            @Override
            public void mousePressed(MouseEvent e)
            {
                JComponent c = (JComponent) e.getSource();
                TransferHandler handler = c.getTransferHandler();
                handler.exportAsDrag(c, e, TransferHandler.MOVE);
            }
        };

        TransferHandler handler = new ComponentHandler();

        for (int i = 0; i < 10; i++)
        {
            //JLabel label = new JLabel("Label " + i);
            GObJComponentWrapper test = new GObJComponentWrapper(new JLabel("Icon"+ i));
            test.getComponent().setSize( test.getComponent().getPreferredSize() );
            test.getComponent().setLocation(30 * (i+1), 30 * (i+1));
            test.getComponent().addMouseListener( listener );
            test.getComponent().setTransferHandler( handler );
            test.getComponent().setBackground(Color.white);
            test.getComponent().setOpaque(true);
            test.setVisible(true);
            s.add(test);
            
        }
    }
    
    @SuppressWarnings("null")
	private static void createAndShowUI()
    {
    	JFrame frame = new JFrame("DragComponent");
    	MultiInterpreter mintrp;
        Interpreter      intrp;
        mintrp = new DefaultMultiInterpreterImpl();
   
        intrp = new TapSelectInterpreter();
        intrp.setAcceptRightButton(false);
        intrp.setAcceptMiddleButton(false);
        mintrp.add(intrp);
   
        intrp = new CircleSelectInterpreter();
        intrp.setAcceptRightButton(false);
        intrp.setAcceptMiddleButton(false);
        mintrp.add(intrp);
   
        intrp = new MoveSelectedInterpreter();
        intrp.setAcceptRightButton(false);
        intrp.setAcceptMiddleButton(false);
        mintrp.add(intrp);
        
        intrp = new StandardGestureInterpreter();
        intrp.setAcceptRightButton(false);
        intrp.setAcceptMiddleButton(false);
        mintrp.add(intrp);
        
        s.setGestureInterpreter(mintrp);
        
        mintrp = new DefaultMultiInterpreterImpl();
        s.setInkInterpreter(mintrp);
        s.setAddRightButtonStrokes(false);
        s.setAddLeftButtonStrokes(false);
        
        bg = new JLabel();
        	ImageIcon im = new ImageIcon("bg.jpg");
        	AlphaIcon al = new AlphaIcon(im, 0.5f);
            bg = new JLabel(al);
            
            frame.add(s);
            //bg.add(s);
            //frame.setContentPane(bg);
            frame.setContentPane(s);
            //s.setOpaque(false);
            s.add(bg);
            s.revalidate();
            s.repaint();
                      
		//s.setLayout(new FlowLayout());
		DragComponent north = new DragComponent();
//        north.setBackground(Color.white);
		north.setOpaque(false);
		north.setPreferredSize( new Dimension(1270, 500) );
		DragComponent south = new DragComponent();
		//north.setBackground(Color.white);
		  south.setOpaque(false);
		  south.setPreferredSize( new Dimension(1270, 500) );
	  	DragComponent east = new DragComponent();
		//    north.setBackground(Color.white);
		//east.setOpaque(false);
		   // east.setPreferredSize( new Dimension(640, 480) );
	    DragComponent west = new DragComponent();
		//  north.setBackground(Color.white);
		//  west.setOpaque(false);
		 // west.setPreferredSize( new Dimension(640, 480) );
		frame.setSize(1680, 1050);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		s.add(north, BorderLayout.NORTH);
		s.add(south, BorderLayout.SOUTH);
        s.add(east, BorderLayout.EAST);
        s.add(west, BorderLayout.WEST);
		frame.pack();
		//frame.setLocationByPlatform( true );
		frame.setVisible( true );
    }

    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                createAndShowUI();
//            	JFrame frame = new JFrame("DragComponent");
//                JLabel label = new JLabel("Label ");
//                MouseGestures mouseGestures = new MouseGestures();
//                mouseGestures.setMouseButton(MouseEvent.BUTTON1_MASK);
//                
//                mouseGestures.addMouseGesturesListener(new MouseGesturesListener() {
//					
//					@Override
//					public void processGesture(String gesture) {
//				        try { 
//				            Thread.sleep(400);
//				        } catch (InterruptedException e) {}
//				        label.setText("test");
//				    } 
//					
//					@Override
//					public void gestureMovementRecognized(String currentGesture) {
//				        if("DRUL".equals(currentGesture)){
//				            label.setText("copy");
//				        }
//				        else if("DLUR".equals(currentGesture)){
//				            label.setText("paste");
//				        } 
//				        else{
//				            label.setText("    "  + currentGesture + " - Wrong gesture! release your mouse and try again");
//				        } 
//				    } 
//				});
//                frame.add(label);
//                frame.pack();
//                //frame.setLocationByPlatform( true );
//                frame.setVisible( true );
//                mouseGestures.start();
            }
            
        });
    }
}

class ComponentHandler extends TransferHandler
{
    @Override
    public int getSourceActions(JComponent c)
    {
        setDragImage( ScreenImage.createImage(c) );

        return MOVE;
    }

    @Override
    public Transferable createTransferable(final JComponent c)
    {
        return new Transferable()
        {
            @Override
            public Object getTransferData(DataFlavor flavor)
            {
                Component[] components = new Component[1];
                components[0] = c;
                return components;
            }

            @Override
            public DataFlavor[] getTransferDataFlavors()
            {
                DataFlavor[] flavors = new DataFlavor[1];
                flavors[0] = DragComponent.COMPONENT_FLAVOR;
                return flavors;
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor)
            {
                return flavor.equals(DragComponent.COMPONENT_FLAVOR);
            }
        };
    }

    @Override
    public void exportDone(JComponent c, Transferable t, int action)
    {
        System.out.println(c.getBounds());
    }
}

class PanelHandler extends TransferHandler
{
    @Override
    public boolean canImport(TransferSupport support)
    {
        if (!support.isDrop())
        {
            return false;
        }

        boolean canImport = support.isDataFlavorSupported(DragComponent.COMPONENT_FLAVOR);
        return canImport;
    }

    @Override
    public boolean importData(TransferSupport support)
    {
        if (!canImport(support))
        {
            return false;
        }

        Component[] components;

        try
        {
            components = (Component[])support.getTransferable().getTransferData(DragComponent.COMPONENT_FLAVOR);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }

        Component component = components[0];
        System.out.println(component.getClass());
        Container container = (Container)support.getComponent();
        container.add(component);
//      container.revalidate();
//      container.repaint();
        container.getParent().revalidate();
        container.getParent().repaint();

//      JLabel label = (JLabel)component;
        DropLocation location = support.getDropLocation();
//      System.out.println(label.getText() + " + " + location.getDropPoint());
        component.setLocation( location.getDropPoint() );

        return true;
    }
}
