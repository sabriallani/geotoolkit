/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2009, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.display2d.style.renderer;

import java.awt.BasicStroke;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.util.ArrayList;
import java.util.List;

import org.geotools.display2d.canvas.RenderingContext2D;
import org.geotools.display2d.style.j2d.TextStroke;
import org.geotools.display2d.style.renderer.LabelDescriptor;
import org.geotools.display2d.style.renderer.LabelRenderer;
import org.geotools.display2d.style.renderer.LinearLabelDescriptor;
import org.geotools.display2d.style.renderer.PointLabelDescriptor;


/**
 * Default implementation of label renderer.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class DefaultLabelRenderer implements LabelRenderer{

    private final RenderingContext2D context;
    private final List<LabelDescriptor> labels = new ArrayList<LabelDescriptor>();
    
    public DefaultLabelRenderer(RenderingContext2D context) {
        if(context == null) throw new NullPointerException("Rendering context can not be null");
        this.context = context;
    }
    
    /**
     * {@inheritDoc }
     */ 
    @Override
    public RenderingContext2D getRenderingContext() {
        return context;
    }
    
    /**
     * {@inheritDoc }
     */ 
    @Override
    public void append(final LabelDescriptor desc) {
        labels.add(desc);
    }
    
    /**
     * {@inheritDoc }
     */ 
    @Override
    public void portrayLabels(){
        final Graphics2D g2 = context.getGraphics();
        //enable antialiasing for labels
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        for(final LabelDescriptor label : labels){
            if(label instanceof PointLabelDescriptor){
                portray(g2, (PointLabelDescriptor)label);
            }else if(label instanceof LinearLabelDescriptor){
                portray(g2, (LinearLabelDescriptor)label);
            }
        }
    }
    
    private void portray(Graphics2D g2, PointLabelDescriptor label){
        context.switchToDisplayCRS();
        
        final FontMetrics metric = g2.getFontMetrics(label.getTextFont());
        final int textHeight = metric.getHeight();
        final int textWidth = metric.stringWidth(label.getText());
        float refX = label.getX();
        float refY = label.getY();
        
        //adjust displacement---------------------------------------------------
        //displacement is oriented above and to the right
        refX = refX + label.getDisplacementX();
        refY = refY - label.getDisplacementY();
        
        //rotation--------------------------------------------------------------
        final float rotate = (float) Math.toRadians(label.getRotation());
        g2.rotate(rotate, refX, refY);
        
        //adjust anchor---------------------------------------------------------
        refX = refX - (label.getAnchorX()*textWidth);
        //text is draw above reference point so use +
        refY = refY + (label.getAnchorY()*textHeight);
        
        //paint halo------------------------------------------------------------
        final FontRenderContext fontContext = g2.getFontRenderContext();
        final GlyphVector glyph = label.getTextFont().createGlyphVector(fontContext, label.getText());
        final Shape shape = glyph.getOutline(refX,refY);
        g2.setPaint(label.getHaloPaint());
        g2.setStroke(new BasicStroke(label.getHaloWidth()*2,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g2.draw(shape);
                
        //paint text------------------------------------------------------------
        g2.setPaint(label.getTextPaint());
        g2.setFont(label.getTextFont());
        g2.drawString(label.getText(), refX, refY);
        
    }
    
    private void portray(Graphics2D g2, LinearLabelDescriptor label){
        context.switchToDisplayCRS();
        
        final TextStroke stroke = new TextStroke(label.getText(), label.getTextFont(), label.isRepeated(),
                label.getOffSet(), label.getInitialGap(), label.getGap());
        final Shape shape = stroke.createStrokedShape(label.getLineplacement());
        
        //paint halo
        g2.setStroke(new BasicStroke(label.getHaloWidth(),BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND) );
        g2.setPaint(label.getHaloPaint());
        g2.draw(shape);
        
        //paint text
        g2.setStroke(new BasicStroke(0));
        g2.setPaint(label.getTextPaint());
        g2.fill(shape);
    }
    

}
