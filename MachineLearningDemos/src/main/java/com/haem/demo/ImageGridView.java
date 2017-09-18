package com.haem.demo;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ImageGridView extends JFrame implements ComponentListener {
  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private GridBagLayout gridBagLayout;

  public ImageGridView() throws HeadlessException {
    super();
    gridBagLayout = new GridBagLayout();
    setLayout(gridBagLayout);
  }

  public void setImage(Image img, int x, int y) {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = x;
    constraints.gridy = y;
    constraints.weightx = 1;
    constraints.weighty = 1;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(2, 2, 2, 2);

    ImageIcon imageIcon = new ImageIcon(img);

    for (int i = 0; i < getContentPane().getComponentCount(); i++) {
      Component current = getContentPane().getComponent(i);
      GridBagConstraints other = gridBagLayout.getConstraints(current);
      if (other.gridx == constraints.gridx && other.gridy == constraints.gridy) {
        ((JLabel) current).setIcon(imageIcon);
        scaleImage(((JLabel) current));
        return;
      }
    }
    JLabel label = new JLabel();
    label.setSize(img.getWidth(this), img.getHeight(this));
    label.setIcon(imageIcon);

    label.addComponentListener(this);
    getContentPane().add(label, constraints);
    getContentPane().repaint();
  }

  @Override
  public void componentHidden(ComponentEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void componentMoved(ComponentEvent arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void componentResized(ComponentEvent arg0) {
    if (arg0.getComponent() instanceof JLabel) {
      JLabel component = (JLabel) arg0.getComponent();
      scaleImage(component);
    }
  }

  public void scaleImage(JLabel component) {
    ImageIcon icon = (ImageIcon) component.getIcon();
    Image img = icon.getImage();
    int dimension = Math.min(component.getHeight(), component.getWidth());
    if (dimension > 0) {
      Image scaledImage = img.getScaledInstance(dimension, dimension, Image.SCALE_SMOOTH);
      icon.setImage(scaledImage);
      component.setIcon(icon);
    }
  }

  @Override
  public void componentShown(ComponentEvent arg0) {
    // TODO Auto-generated method stub

  }
}
