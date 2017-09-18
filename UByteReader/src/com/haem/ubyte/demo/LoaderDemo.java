package com.haem.ubyte.demo;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.haem.ubyte.UByteImageStream;

public class LoaderDemo extends JFrame{
  
  private String filename;
  private List<Image> images;
  private int imgRows;
  private int imgColumns;
  
  public LoaderDemo(String filename){
    try {
      FileInputStream in=new FileInputStream(filename);
      UByteImageStream imgStream=new UByteImageStream(in);
      
      this.imgColumns = imgStream.getColumns();
      this.imgRows = imgStream.getRows();
      this.setBounds(15, 15, 4*imgColumns, 4*imgRows+30);
      
      images=new ArrayList<>(16);
      for(int i=0;i<16;i++){
        images.add(imgStream.readImage());
      }
      imgStream.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    setContentPane(new JPanel(){
      private static final long serialVersionUID = 1L;

      @Override
      protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for(int i=0;i<images.size();i++){
          int x=(i/4)*imgColumns;
          int y=(i%4)*imgRows;
          g.drawImage(images.get(i),x,y,this);
        }
      }
    });
    
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent arg0) {
        System.exit(0);
      }
    });
    
  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    String filename=args[0];
    new LoaderDemo(filename).show();
  }

}
